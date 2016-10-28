package me.urielsalis.urielsalads.extensions.nvidiaDownloader;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.Nvidia;
import net.engio.mbassy.listener.Handler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.urielsalis.urielsalads.extensions.download.DownloadMain.config;
import static me.urielsalis.urielsalads.extensions.download.DownloadMain.writeJSON;

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
@ExtensionAPI.Extension(name = "nvidia-downloader", version = "1.0.0", dependencies = {"download", "irc"}, id = "nvidia-downloader/1.0.0")
public class Main {
    static HashMap<String, Integer> PRODUCT_TYPES = new HashMap<>();

    @ExtensionAPI.ExtensionInit("nvidia-downloader/1.0.0")
    public static void initNvidiaDownloader(ExtensionAPI api) {
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
        if(config.nvidia.newConfig) {
            fullUpdate();
            config.intel.newConfig = false;
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
        public Date startTime;
        public Date endTime;
        public ArrayList<String> errors;
        public int throttle = 5;

        public NvidiaDriverGrabber(String lookupUrl, String processUrl, String locale, int language, int throttle) {
            this.lookupUrl = lookupUrl;
            this.processUrl = processUrl;
            this.locale = locale;
            this.language = language;
            this.throttle = throttle;
            this.startTime = new Date();
            this.endTime = new Date(); //todo change this to actual end date
            this.errors = new ArrayList<>();
            config.nvidia._meta = new Nvidia.Meta("Urielsalads nvidia-download 1.0.0", DateFormat.getDateInstance().format(startTime));
        }

        public InputStream lookupRequest(int step, int value) {
            String args = "?TypeID=" + step + "&ParentID=" + value;
            System.out.println("--> " + this.lookupUrl + args);
            InputStream stream = null;
            try {
                URL url = new URL(this.lookupUrl+args);
                stream = url.openStream();
            } catch (IOException e) {
                System.out.println("Sleeping for 80 seconds, then retrying");
                try {
                    TimeUnit.SECONDS.sleep(80);
                    URL url = new URL(this.lookupUrl+args);
                    stream = url.openStream();
                } catch (InterruptedException | IOException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }
            return stream;
        }

        public String processRequest(String ProductSeriesID, String ProductFamilyID, String RPF, String OperatingSystemID, String LanguageID, String Locale, String CUDAToolkit) {
            String args = "?psid="+ProductSeriesID+"&pfid="+ProductFamilyID+"&rpf="+RPF+"&osid="+OperatingSystemID+"&lid="+LanguageID+"&lang="+Locale+"&ctk="+CUDAToolkit;
            System.out.println("==> " + this.processUrl + args);
            InputStream stream = null;
            try {
                URL url = new URL(this.lookupUrl+args);
                stream = url.openStream();
            } catch (IOException e) {
                System.out.println("Sleeping for 80 seconds, then retrying");
                try {
                    TimeUnit.SECONDS.sleep(80);
                    URL url = new URL(this.processUrl+args);
                    stream = url.openStream();
                } catch (InterruptedException | IOException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }
            if(stream!=null) {
                Scanner s = new Scanner(stream).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";
                s.close();
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
            return "";
        }

        public void parse() {

        }
    }

    private static class ExtremeBotCompatibleNvidiaDriverGrabber { //todo remove this
        public String lookupUrl;
        public String processUrl;
        public String locale;
        public int language;
        public Date startTime;
        public Date endTime;
        public ArrayList<String> errors;
        public int throttle = 5;

        public NvidiaDriverGrabber(String lookupUrl, String processUrl, String locale, int language, int throttle) {
            this.lookupUrl = lookupUrl;
            this.processUrl = processUrl;
            this.locale = locale;
            this.language = language;
            this.throttle = throttle;
            this.startTime = new Date();
            this.endTime = new Date(); //todo change this to actual end date
            this.errors = new ArrayList<>();
            config.nvidia._meta = new Nvidia.Meta("Urielsalads nvidia-download 1.0.0", DateFormat.getDateInstance().format(startTime));
        }

        public InputStream lookupRequest(int step, int value) {
            String args = "?TypeID=" + step + "&ParentID=" + value;
            System.out.println("--> " + this.lookupUrl + args);
            InputStream stream = null;
            try {
                URL url = new URL(this.lookupUrl+args);
                stream = url.openStream();
            } catch (IOException e) {
                System.out.println("Sleeping for 80 seconds, then retrying");
                try {
                    TimeUnit.SECONDS.sleep(80);
                    URL url = new URL(this.lookupUrl+args);
                    stream = url.openStream();
                } catch (InterruptedException | IOException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }
            return stream;
        }

        public String processRequest(String ProductSeriesID, String ProductFamilyID, String RPF, String OperatingSystemID, String LanguageID, String Locale, String CUDAToolkit) {
            String args = "?psid="+ProductSeriesID+"&pfid="+ProductFamilyID+"&rpf="+RPF+"&osid="+OperatingSystemID+"&lid="+LanguageID+"&lang="+Locale+"&ctk="+CUDAToolkit;
            System.out.println("==> " + this.processUrl + args);
            InputStream stream = null;
            try {
                URL url = new URL(this.lookupUrl+args);
                stream = url.openStream();
            } catch (IOException e) {
                System.out.println("Sleeping for 80 seconds, then retrying");
                try {
                    TimeUnit.SECONDS.sleep(80);
                    URL url = new URL(this.processUrl+args);
                    stream = url.openStream();
                } catch (InterruptedException | IOException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }
            if(stream!=null) {
                Scanner s = new Scanner(stream).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";
                s.close();
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
            return "";
        }

        public void step1() {
            config.nvidia.product_types = new HashMap<>();
            for(String key: PRODUCT_TYPES.keySet()) {
                config.nvidia.product_types.put(key, new HashMap<>());
            }
        }
        //get ProductSeriesID
        public void step2() {
            for(Map.Entry<String, Integer> ptype: PRODUCT_TYPES.entrySet()) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    InputStream stream = lookupRequest(2, ptype.getValue());
                    if(stream != null) {
                        Document doc = dBuilder.parse(stream);
                        doc.getDocumentElement().normalize();
                        NodeList nodes = doc.getElementsByTagName("LookupValueSearch").item(0).getFirstChild().getChildNodes();
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Node node = nodes.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                boolean m_RequiresProduct = element.getAttribute("RequiresProduct").equals("True");
                                String m_ParentID = element.getAttribute("ParentID");
                                String m_SeriesName = element.getElementsByTagName("Name").item(0).getTextContent();
                                int m_SeriesID = Integer.parseInt(element.getElementsByTagName("Value").item(0).getTextContent());
                                config.nvidia.product_types.get(ptype.getKey()).put(m_SeriesName, new HashMap<String, Object>());
                                config.nvidia.product_types.get(ptype.getKey()).get(m_SeriesName).put("_meta", new Nvidia.Series(new Nvidia.Series.Meta(ptype.getValue(), m_SeriesID, m_SeriesName, m_RequiresProduct)));
                            } else {
                                System.out.println(node.getBaseURI() + " its not a element wtf");
                            }
                        }
                    }
                    if (stream != null) {
                        stream.close();
                    }
                    TimeUnit.SECONDS.sleep(throttle);
                } catch (ParserConfigurationException | SAXException | IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        //get ProductFamilyID
        public void step3() {
            for(Map.Entry<String, Map<String, Map<String, Object>>> ptype: config.nvidia.product_types.entrySet()) {
                for(Map.Entry<String, Map<String, Object>> Series: ptype.getValue().entrySet()) {
                    Map<String, Object> series = Series.getValue();
                    //if(!series._meta.SubProducts)
                    //    continue;
                    System.out.println(((Nvidia.Series.Meta) series.get("_meta")).ProductSeriesName);
                    try {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        InputStream stream = lookupRequest(3, ((Nvidia.Series.Meta) series.get("_meta")).ProductSeriesID);
                        if (stream != null) {
                            Document doc = dBuilder.parse(stream);
                            doc.getDocumentElement().normalize();
                            NodeList nodes = doc.getElementsByTagName("LookupValueSearch").item(0).getFirstChild().getChildNodes();
                            for (int i = 0; i < nodes.getLength(); i++) {
                                Node node = nodes.item(i);
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element element = (Element) node;
                                    String m_ProductName = element.getElementsByTagName("Name").item(0).getTextContent();
                                    String m_ProductID = element.getElementsByTagName("Value").item(0).getTextContent();
                                    series.put(m_ProductName, new HashMap<String, Object>());
                                    ((HashMap<String, Object>) series.get(m_ProductName)).put("_meta", new Nvidia.Series.Meta.Meta2(m_ProductID, m_ProductName));
                                } else {
                                    System.out.println(node.getBaseURI() + " its not a element wtf");
                                }
                            }
                            TimeUnit.SECONDS.sleep(throttle);

                        }
                    } catch (SAXException | ParserConfigurationException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //getOSID
        public void step4() {
            for(Map.Entry<String, Map<String, Map<String, Object>>> ptype: config.nvidia.product_types.entrySet()) {
                for (Map.Entry<String, Map<String, Object>> Series : ptype.getValue().entrySet()) {
                    if(Series.getKey().equals("_meta")) continue;
                    Map<String, Object> series = Series.getValue();
                    try{
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        InputStream stream = lookupRequest(4, ((Nvidia.Series.Meta) series.get("_meta")).ProductSeriesID);
                        if (stream != null) {
                            Document doc = dBuilder.parse(stream);
                            doc.getDocumentElement().normalize();
                            NodeList nodes = doc.getElementsByTagName("LookupValueSearch").item(0).getFirstChild().getChildNodes();
                            for (int i = 0; i < nodes.getLength(); i++) {
                                Node node = nodes.item(i);
                                if (node.getNodeType() == Node.ELEMENT_NODE) {
                                    Element element = (Element) node;
                                    String m_OSCode = element.getAttribute("Code");
                                    String m_OSName = element.getElementsByTagName("Name").item(0).getTextContent();
                                    String m_OSID = element.getElementsByTagName("Value").item(0).getTextContent();
                                    int ProductSeriesID = ((Nvidia.Series.Meta) series.get("_meta")).ProductSeriesID;
                                    for(Object obj: series.values()) {
                                        Map<String, Object> product = (Map<String, Object>) obj;
                                        String ProductName = ((Nvidia.Series.Meta.Meta2) product.get("_meta")).ProductName;
                                        TimeUnit.SECONDS.sleep(throttle);

                                    }

                                } else {
                                    System.out.println(node.getBaseURI() + " its not a element wtf");
                                }
                            }
                        }
                    } catch (SAXException | ParserConfigurationException | IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                        /*
                        self.output["product_types"][ptype["name"]][series["_meta"]["ProductSeriesName"]][product["_meta"]["ProductName"]][m_OSName] = \
                            {
                                "_meta": {
                                    "OSCode": m_OSCode,
                                    "OSName": m_OSName,
                                    "OSID": m_OSID
                                }
                            };
                         */
                }
            }
        }
    }

}
