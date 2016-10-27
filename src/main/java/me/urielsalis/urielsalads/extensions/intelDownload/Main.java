package me.urielsalis.urielsalads.extensions.intelDownload;

import com.google.gson.Gson;
import com.moandjiezana.toml.TomlWriter;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.intelDownload.config.Config;
import me.urielsalis.urielsalads.extensions.intelDownload.config.Download;
import me.urielsalis.urielsalads.extensions.intelDownload.config.EPMIdResults;
import me.urielsalis.urielsalads.extensions.intelDownload.config.Intel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
@ExtensionAPI.Extension(name="intel-download", version = "1.0.0", dependencies = {"base"}, id = "intel-download/1.0.0")
public class Main {
    private static Config config;

    @ExtensionAPI.ExtensionInit("intel-download/1.0.0")
    public static void init(ExtensionAPI api) {
        //fill config
        config = new Config("1.0.0", "intel-download/1.0.0");
        downloadDrivers();
        writeToml();
    }

    private static void downloadDrivers() {
        try {
            List<Callable<Object>> callables = new ArrayList<>();
            final Gson g = new Gson();
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
                                URL url = new URL("https://downloadcenter.intel.com/json/pageresults?pageNumber=1&&productId="+epmID);
                                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                                request.connect();
                                EPMIdResults results = g.fromJson(new InputStreamReader((InputStream) request.getContent()), EPMIdResults.class);
                                Intel.Driver driver = new Intel.Driver(name, epmID);
                                for(EPMIdResults.ResultsForDisplayImpl display: results.ResultsForDisplay) {
                                    Main.fillDownload(driver, display);
                                }
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

             System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fillDownload(Intel.Driver driver, EPMIdResults.ResultsForDisplayImpl display) {
        driver.download.add(new Download(display));
    }

    private static synchronized void addDriver(Intel.Driver driver) {
        config.intel.driver.add(driver);
    }

    private static String removeSpecialChars(String text) {
        return text.contains("(") ? text.substring(0, text.indexOf("(") - 1).replace("®", "").trim() : text.replace("®", "").trim();
    }

    public static void writeToml() {
        config.createdOn = new Date();
        try {
            new TomlWriter().write(config, new File("test.toml"));
            System.out.println("Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    public static void loadToml() {
        Toml toml = new Toml().read(new File("example.toml"));
        Config config = toml.to(Config.class);

    }*/

    public static void main(String[] args) {
        init(null);
    }

    @ExtensionAPI.ExtensionUnload("intel-download/1.0.0")
    public static void unload(ExtensionAPI api) {

    }


}
