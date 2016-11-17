package me.urielsalis.urielsalads.extensions.nvidiaDownloader;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.Config;
import me.urielsalis.urielsalads.extensions.download.Nvidia;
import net.engio.mbassy.listener.Handler;
import nu.xom.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.urielsalis.urielsalads.extensions.download.DownloadMain.*;

/**
 * UrielSalads
 * Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@ExtensionAPI.Extension(name = "nvidia-download", version = "1.0.0", dependencies = {"download", "irc"}, id = "nvidia-download/1.0.0")
public class Main {
    static HashMap<String, Integer> PRODUCT_TYPES = new HashMap<>();

    @ExtensionAPI.ExtensionInit("nvidia-download/1.0.0")
    public static void initNvidiaDownloader(ExtensionAPI api) {
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
        if(getConfig().nvidia.newConfig) {
            fullUpdate();
            config.nvidia.newConfig = false;
            writeJSON();
        }
    }

    private static void fullUpdate() {
        PRODUCT_TYPES.put("GeForce", 1);
      //PRODUCT_TYPES.put("nForce", 2);
        PRODUCT_TYPES.put("Quadro", 3);
        PRODUCT_TYPES.put("Legacy", 4);
        PRODUCT_TYPES.put("3D Vision", 5);
        PRODUCT_TYPES.put("ION", 6);
        PRODUCT_TYPES.put("Tesla", 7);
        PRODUCT_TYPES.put("NVS", 8);
        PRODUCT_TYPES.put("GRID", 9);
        NvidiaDriverGrabber driverSearch = new NvidiaDriverGrabber("http://www.nvidia.com/Download/API/lookupValueSearch.aspx", "http://www.nvidia.com/Download/processDriver.aspx", "en-us", 1, 5);
        driverSearch.parse();
    }

    @ExtensionAPI.ExtensionUnload("nvidia-download/1.0.0")
    public static void unloadNvidiaDownloader(ExtensionAPI api) {
        try {
            api.unregisterListener("commandEvent", "nvidia-download/1.0.0/CommandListener");
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public static void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            if(command.getName().equals("nvidiaFullUpdate")) {
                fullUpdate();
                writeJSON();
            }
        }

        @Override
        public String name() {
            return "nvidia-download/1.0.0/CommandListener";
        }
    }

    private static class NvidiaDriverGrabber {
        public String lookupUrl;
        public String processUrl;
        public String locale;
        public int language;
        public ArrayList<String> errors;
        public int throttle = 5;
        Builder parser = new Builder();

        public NvidiaDriverGrabber(String lookupUrl, String processUrl, String locale, int language, int throttle) {
            this.lookupUrl = lookupUrl;
            this.processUrl = processUrl;
            this.locale = locale;
            this.language = language;
            this.throttle = throttle;
            this.errors = new ArrayList<>();
        }

        public Document lookupRequest(int step, int value) {
            String args = "?TypeID=" + step + "&ParentID=" + value;
            System.out.println("--> " + this.lookupUrl + args);
            try {
                URL url = new URL(lookupUrl+args);
                InputStream stream = url.openStream();
                return parser.build(stream);
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String processRequest(int ProductSeriesID, int ProductFamilyID, int RPF, int OperatingSystemID, int LanguageID, String Locale, int CUDAToolkit) {
            String args = "?psid="+ProductSeriesID+"&pfid="+ProductFamilyID+"&rpf="+RPF+"&osid="+OperatingSystemID+"&lid="+LanguageID+"&lang="+Locale+"&ctk="+CUDAToolkit;
            System.out.println("  ==> " + this.processUrl + args);
            try {
                URLConnection conn = new URL(processUrl + args).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void parse() {
            try {
                for (Map.Entry<String, Integer> entry : PRODUCT_TYPES.entrySet()) {
                    Nvidia.ProductType productType = new Nvidia.ProductType(entry.getKey()); //start step 1
                    //start step 2
                    {
                        Document documentStep2 = lookupRequest(2, entry.getValue());
                        if (documentStep2 == null) {
                            System.out.println("Sleeping for 80 secs and trying again");
                            TimeUnit.SECONDS.sleep(80);
                            documentStep2 = lookupRequest(2, entry.getValue());
                            if (documentStep2 == null) {
                                System.out.println("Failed");
                                System.exit(1);
                            }
                        }
                        Elements lookupValuesStep2 = documentStep2.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                        for (int i = 0; i < lookupValuesStep2.size(); i++) {
                            Element lookupValue2 = lookupValuesStep2.get(i);

                            Nvidia.Series series = new Nvidia.Series(lookupValue2);
                            //start step 3
                            {
                                Document documentStep3 = lookupRequest(3, series.id);
                                if (documentStep3 == null) {
                                    System.out.println("Sleeping for 80 secs and trying again");
                                    TimeUnit.SECONDS.sleep(80);
                                    documentStep3 = lookupRequest(3, series.id);
                                    if (documentStep3 == null) {
                                        System.out.println("Failed");
                                        System.exit(1);
                                    }
                                }
                                Elements lookupValuesStep3 = documentStep3.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                                for (int j = 0; j < lookupValuesStep3.size(); j++) {
                                    Element lookupValue3 = lookupValuesStep3.get(j);
                                    Nvidia.Series.Product product = new Nvidia.Series.Product(lookupValue3);
                                    {
                                        //start step 4
                                        Document documentStep4= lookupRequest(4, series.id);
                                        if (documentStep4 == null) {
                                            System.out.println("Sleeping for 80 secs and trying again");
                                            TimeUnit.SECONDS.sleep(80);
                                            documentStep4 = lookupRequest(4, entry.getValue());
                                            if (documentStep4 == null) {
                                                System.out.println("Failed");
                                                System.exit(1);
                                            }
                                        }
                                        Elements lookupValuesStep4 = documentStep4.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                                        Config.GPU gpu = new Config.GPU(product.name);

                                        for (int e = 0; e < lookupValuesStep4.size(); e++) {
                                            Element lookupValue4 = lookupValuesStep4.get(e);
                                            Nvidia.Series.Product.OS os = new Nvidia.Series.Product.OS(lookupValue4);
                                            if(os.shouldDownload) {
                                                //start step 5
                                                //String  String RPF, String OperatingSystemID, String LanguageID, String Locale, String CUDAToolkit) {
                                                String downloadLink = processRequest(series.id, product.id, 1, os.id, language, locale, 0);
                                                os.downloadLink = downloadLink;
                                                int arch = os.is64? 64:32;
                                                gpu.addDownload(os.minified,arch, downloadLink);
                                                //end step 5
                                            }
                                            //end step 4
                                            product.os.add(os);

                                        }
                                        config.list.add(gpu);
                                    }
                                    //end step 3
                                    series.products.add(product);
                                }



                            }
                            //end step 2
                            productType.series.add(series);
                        }
                    }
                    config.nvidia.productTypes.add(productType); //end step 1
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
