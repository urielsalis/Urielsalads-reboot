package me.urielsalis.urielsalads.extensions.intelDownload;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.Download;
import me.urielsalis.urielsalads.extensions.download.DownloadMain;
import me.urielsalis.urielsalads.extensions.download.Intel;
import net.engio.mbassy.listener.Handler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
@ExtensionAPI.Extension(name="intel-download", version = "1.0.0", dependencies = {"download", "irc"}, id = "intel-download/1.0.0")
public class Main {
    private static ExtensionAPI api;

    @ExtensionAPI.ExtensionInit("intel-download/1.0.0")
    public static void initIntelDownload(ExtensionAPI api) {
        //fill config
        Main.api = api;
        if(getConfig().intel.newConfig) {
            fullUpdate();
            config.intel.newConfig = false;
            writeJSON();
        }
        registerEvents();

    }

    @ExtensionAPI.ExtensionUnload("intel-download/1.0.0")
    public static void unload(ExtensionAPI api) {
        //fill config
        try {
            api.unRegisterEvent("intel-download/1.0.0/CommandListener");
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static void registerEvents() {
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static void fullUpdate() {
        try {
            List<Callable<Object>> callables = new ArrayList<>();
            Document document = Jsoup.connect("http://www.intel.com/content/www/us/en/support/graphics-drivers.html").userAgent("UrielsalisBot for auto-dxdiag parsing/github.com/urielsalads-reboot/uriel@urielsalis.me/Jsoup").get();
            Element tableMain = document.getElementById("productSelector-1").getAllElements().first().getElementsByClass("blade-expand-secondary").first();
            Elements blades = tableMain.getElementsByClass("blade-group").first().getElementsByClass("blade");
            for(Element blade: blades) {
                Elements divs = blade.getElementsByClass("container").first().select("div").first().select("div");
                for(Element div: divs) {
                    Elements uls = div.select("ul");
                    for(Element ul: uls) {
                        Element a = ul.select("li").first().select("a").first();
                        final String name = removeSpecialChars(a.text());
                        final String href = "http://www.intel.com/" + a.attr("href");
                        callables.add(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                String[] html = Jsoup.connect(href).userAgent("UrielsalisBot for auto-dxdiag parsing/github.com/urielsalads-reboot/uriel@urielsalis.me/Jsoup").get().html().split("[\\r\\n]+");
                                //var epmid = "81498";
                                int epmID = 0;
                                for(String str: html) {
                                    if(str.trim().startsWith("var epmid = ")) {
                                        epmID = Integer.parseInt(str.substring(str.indexOf("\"")+1, str.lastIndexOf("\"")));
                                        break;
                                    }
                                }
                                if(epmID==0) {
                                    System.out.println("Error processing " + href);
                                }
                                System.out.println("Thread " + Thread.currentThread().getName() + " of " +  Thread.activeCount() + ": " + name + " - " + epmID);
                                Intel.Driver driver = new Intel.Driver(name.replace("Graphics Drivers for ", ""), epmID);
                                Main.addDriver(driver);
                                return null;
                            }
                        });
                    }
                }
            }
            ExecutorService service = Executors.newFixedThreadPool(8);
            try {
                service.invokeAll(callables);
                while(service.awaitTermination(1, TimeUnit.SECONDS)) { //Wait till all threads finished
                    service.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            partialUpdate();
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void partialUpdate() {
        List<Callable<PartialUpdateData>> callables2 = new ArrayList<>();

        for(final Intel.Driver driver: config.intel.driver) {
            callables2.add(new Callable<PartialUpdateData>() {
                @Override
                public PartialUpdateData call() throws Exception {
                    return new PartialUpdateData(driver, fillDownload(driver));
                }
            });
        }

        ExecutorService service = Executors.newFixedThreadPool(8);
        try {
            List<Future<PartialUpdateData>> futures = service.invokeAll(callables2);
            while(service.awaitTermination(1, TimeUnit.SECONDS)) { //Wait till all threads finished
                service.awaitTermination(1, TimeUnit.SECONDS);
            }
            config.intel.driver.clear();
            for(Future<PartialUpdateData> future: futures) {
                if(future.isDone()) {
                    PartialUpdateData data = future.get();
                    data.driver.download.addAll(data.downloads);
                    synchronized (config.intel.driver) {
                        config.intel.driver.add(data.driver);
                    }
                } else {
                    System.err.println("A future didnt finish in time!!!!");
                }
            }
            writeJSON();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    private static ArrayList<Download> fillDownload(Intel.Driver driver) {
        try {
            URL url = new URL("https://downloadcenter.intel.com/json/pageresults?pageNumber=1&&productId=" + driver.epmID);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();
            ArrayList<Download> downloads = new ArrayList<>();
            EPMIdResults results = g.fromJson(new InputStreamReader((InputStream) request.getContent()), EPMIdResults.class);
            for (EPMIdResults.ResultsForDisplayImpl display : results.ResultsForDisplay) {
                downloads.add(new Download(display, driver));
            }
            return downloads;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static synchronized void fillDownload(Intel.Driver driver, EPMIdResults.ResultsForDisplayImpl display) {
        driver.download.add(new Download(display, driver));
    }

    private static synchronized void addDriver(Intel.Driver driver) {
        config.intel.driver.add(driver);
    }

    private static String removeSpecialChars(String text) {
        return text.contains("(") ? text.substring(0, text.indexOf("(") - 1).replace("®", "").trim() : text.replace("®", "").trim();
    }

    public static void writeJSON() {
        DownloadMain.writeJSON();
    }

    /*
    public static void loadToml() {
        Toml toml = new Toml().read(new File("example.toml"));
        Config config = toml.to(Config.class);

    }*/

    public static void main(String[] args) {
        initIntelDownload(null);
    }


    private static class PartialUpdateData {
        public Intel.Driver driver;
        public ArrayList<Download> downloads;
        public PartialUpdateData(Intel.Driver driver, ArrayList<Download> downloads) {
            this.driver = driver;
            this.downloads = downloads;
        }
    }

    private static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            switch (command.getName()) {
                case "partialUpdate":
                    partialUpdate();
                    break;
                case "fullUpdate":
                    fullUpdate();
                    break;
            }
        }

        @Override
        public String name() {
            return "intel-download/1.0.0/CommandListener";
        }
    }
}
