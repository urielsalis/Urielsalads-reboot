package me.urielsalis.urielsalads.extensions.search;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ircclouds.irc.api.domain.messages.ChannelPrivMsg;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.Config;
import me.urielsalis.urielsalads.extensions.download.Download;
import me.urielsalis.urielsalads.extensions.download.DownloadMain;
import me.urielsalis.urielsalads.extensions.irc.ChatFormat;
import net.engio.mbassy.listener.Handler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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
@ExtensionAPI.Extension(name = "search", version = "1.0.0", id = "search/1.0.0", dependencies = {"irc", "download", "amd-download", "nvidia-download", "intel-download"})
public class Main {
    private static ExtensionAPI api;
    private static Gson gson = new GsonBuilder().create();

    @ExtensionAPI.ExtensionInit("search/1.0.0")
    public static void intelSearchInit(ExtensionAPI api) {
        Main.api = api;
        try {
            api.registerListener("commandEvent", new CommandListener());
            api.registerListener("onChannelMessage", new CommandListener2());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    @ExtensionAPI.ExtensionUnload("search/1.0.0")
    public static void unloadintelSearch(ExtensionAPI api) {
        try {
            api.unregisterListener("commandEvent", "search/1.0.0/CommandListener");
            api.unregisterListener("onChannelMessage", "search/1.0.0/CommandListener2");
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    public static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public static void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            switch (command.getName()) {
                case "dx":
                    parseDxdiag(command.getArgs()[0], command.getChannel());
                    break;
                case "addGPU":
                    Config.GPU gpu = new Config.GPU(arrayToString(command.getArgs()));
                    DownloadMain.config.manual.add(gpu);
                    DownloadMain.writeJSON();
                    break;
                case "delGPU":
                    Config.GPU gpu2 = new Config.GPU(arrayToString(command.getArgs()));
                    DownloadMain.config.manual.remove(gpu2);
                    DownloadMain.writeJSON();
                    break;
                case "setDownload":
                    String[] arguments = arrayToString(command.getArgs()).split("\\|");
                    if(arguments.length < 3) return;
                    for(Config.GPU gpu3: DownloadMain.config.manual) {
                        if(gpu3.name.equals(arguments[0])) {
                            gpu3.addDownload(arguments[1], Integer.parseInt(arguments[2]), arguments[3]);
                        }
                    }
                    DownloadMain.writeJSON();
                    break;
            }
        }

        private static String arrayToString(String[] args) {
            StringBuilder builder = new StringBuilder();
            for(String string: args) {
                builder.append(string).append(" ");
            }
            return builder.toString().trim();
        }

        @Override
        public String name() {
            return "search/1.0.0/CommandListener";
        }
    }

    public static class CommandListener2 implements ExtensionAPI.Listener {
        @Handler
        public static void handle(ChannelPrivMsg aMsg) {
            if(!aMsg.getText().startsWith(".") && !aMsg.getText().startsWith("!") && aMsg.getText().contains("paste.ubuntu.com")) {
                parseLink(aMsg.getText());
            }
        }

        @Override
        public String name() {
            return "search/1.0.0/CommandListener2";
        }
    }


    public static void parseLink(String text) {
        String[] temp = text.split(" ");
        for(String str: temp) {
            if(str.contains("paste.ubuntu.com")) {
                try {
                    Document document = Jsoup.parse(new URL(str), 10000);
                    Element code = document.select(".code").first();
                    String value = code.select(".paste").first().select("pre").first().text();
                    if(value.contains("Trend Micro HijackThis")) {
                        me.urielsalis.urielsalads.extensions.irc.Main.api.message("#mchelptraining", "!hjt "+str);
                    } else if(value.contains("System Information")) {
                        me.urielsalis.urielsalads.extensions.irc.Main.api.message("#mchelptraining", ".dx "+str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public static void findCPU(String tmp, String minified, boolean is64, String channel) {
        //ark.intel.com
        String[] strs = tmp.split("\\s+");
        String cpu = null;
        for(String str: strs) {
            if(Character.isLetter(str.charAt(0)) && Character.isDigit(str.charAt(1))) {
                if(str.charAt(0)=='Q' || str.charAt(0)=='E' || str.charAt(0)=='T' || str.charAt(0)=='L') continue;
                cpu = str;
                break;
            }
        }
        try {
            if(cpu != null) {
                InputStreamReader reader = new InputStreamReader(new URL("http://odata.intel.com/API/v1_0/Products/Processors()?api_key="+me.urielsalis.urielsalads.Main.apiKey+"&$select=ProductId,CodeNameEPMId,GraphicsModel&$filter=substringof(%27"+cpu+"%27,ProductName)&$format=json").openStream());
                Ark ark = gson.fromJson(reader, Ark.class);
                boolean showMessage = true;
                for(Ark.CPU cpu2: ark.d) {
                    if(cpu2.GraphicsModel != null) {
                        //search in database
                        String message = findDriver(cpu2.GraphicsModel, minified, is64, channel, false);
                        if(showMessage)
                            me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, ChatFormat.GREEN + "Ark: " + ChatFormat.NORMAL + message);
                        showMessage = false;
                        break;
                    }
                }
                if(showMessage)
                    me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, ChatFormat.RED + "Cant find "+ChatFormat.NORMAL+cpu+ChatFormat.RED + " in ark");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseDxdiag(String s, String channel) {
        String minified = "";
        boolean is64 = false;
        for(String str: s.trim().split(" ")) {
            if(str.contains("paste.ubuntu.com")) {
                try {
                    Document document = Jsoup.parse(new URL(str), 10000);
                    Element code = document.select(".code").first();
                    String value = code.select(".paste").first().select("pre").first().text();
                    String[] lines2 = value.split("\n");
                    boolean showedCpu = false;
                    for(String line2: lines2) {
                        if (line2.contains("Operating System")) {
                            if (line2.contains("64")) is64 = true;
                            String[] split = line2.trim().split(" ");
                            minified = split[3];
                        } else if (line2.contains("Card name")) {
                            String card = line2.trim().split(":")[1];
                            findDriver(card, minified, is64, channel, true);
                        } else if (!showedCpu && line2.contains("Processor: ") && !line2.contains("Video")) {
                            findCPU(line2.trim().split(":")[1].trim(), minified, is64, channel);
                            showedCpu = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private static String findDriver(String card, String minified, boolean is64, String channel, boolean showMessage2) {
        boolean showMessage = true;
        String latestFound = "";
        if (!card.contains("Standard VGA") && !card.contains("Microsoft Basic")) {
            card = resolveWrong(card.replace("NVIDIA ", "").replace("(R)", "").replace("AMD ", "").replace("®", "").trim());
            for (Config.GPU gpu : DownloadMain.config.manual) {
                if (contains(gpu.name, card) && showMessage) {
                    String download = gpu.getDownload(minified, is64);
                    if (download.isEmpty()) {
                        latestFound = gpu.getLatest(latestFound, is64);
                        continue;
                    }
                    if (showMessage2)
                        me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, ChatFormat.BLUE + card + ": " + ChatFormat.NORMAL + download);
                    else return ChatFormat.BLUE + card + ": " + ChatFormat.NORMAL + download;
                    showMessage = false;
                    break;
                }
            }
            for (Config.GPU gpu : DownloadMain.config.list) {
                if (contains(gpu.name, card) && showMessage) {
                    String download = gpu.getDownload(minified, is64);
                    if (download.isEmpty()) {
                        latestFound = gpu.getLatest(latestFound, is64);
                        continue;
                    }
                    if (showMessage2)
                        me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, ChatFormat.BLUE + card + ": " + ChatFormat.NORMAL + download);
                    else return ChatFormat.BLUE + card + ": " + ChatFormat.NORMAL + download;
                    showMessage = false;
                    return null;
                }
            }
            if (!showMessage) return null;
            if (showMessage2)
                if (latestFound.isEmpty()) {
                    me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, "Not Found - Perform Manual Check");
                } else {
                    me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, ChatFormat.RED + "Downgrade to Windows " + latestFound + ChatFormat.NORMAL);
                }
            else {
                if(latestFound.isEmpty()) {
                    return "Not Found - Perform Manual Check";
                } else {
                    return "Downgrade to Windows " + latestFound;
                }
            }
        }
        me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, "Not Found - Perform Manual Check");
        return "Not Found - Perform Manual Check";
    }

    private static String resolveWrong(String card) {
        if(card.contains("/")|| card.contains("(")) {
            StringBuilder builder = new StringBuilder();
            String[] words = card.split("\\s+");
            for(String word: words) {
                if(word.contains("(")) break;
                if(word.contains("/")) {
                    builder.append(word.split("/")[0]).append(" ");
                } else {
                    builder.append(word).append(" ");
                }
            }
            return builder.toString().trim();
        } else return card;
    }

    private static boolean contains(String str1, String str2) {
        return str1.toLowerCase().trim().contains(str2.toLowerCase().trim()) || str2.toLowerCase().trim().contains(str1.toLowerCase().trim());
    }

}
