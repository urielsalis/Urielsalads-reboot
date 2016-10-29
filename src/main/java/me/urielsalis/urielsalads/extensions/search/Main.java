package me.urielsalis.urielsalads.extensions.search;

import com.ircclouds.irc.api.domain.messages.ChannelPrivMsg;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.Config;
import me.urielsalis.urielsalads.extensions.download.DownloadMain;
import net.engio.mbassy.listener.Handler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
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
            }
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
                    for(String line2: lines2) {
                        if (line2.contains("Operating System")) {
                            if (line2.contains("64")) is64 = true;
                            String[] split = line2.trim().split(" ");
                            minified = split[3];
                        } else if (line2.contains("Card name")) {
                            String card = line2.trim().split(":")[1];
                            findDriver(card, minified, is64, channel);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private static void findDriver(String card, String minified, boolean is64, String channel) {
        if(card.contains("Standard VGA") || card.contains("Microsoft Basic")) {
            me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, "Cant process those without ark yet. :(");
        } else {
            card = card.replace("NVIDIA ", "").replace("(R)", "").replace("AMD ", "");
            for(Config.GPU gpu: DownloadMain.config.manual)  {
                if(contains(gpu.name, card)) {
                    String download = gpu.getDownload(minified, is64);
                    if(download.isEmpty()) continue;
                    me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, card + ": " + download);
                    return;
                }
            }
            for(Config.GPU gpu: DownloadMain.config.list)  {
                if(contains(gpu.name, card)) {
                    String download = gpu.getDownload(minified, is64);
                    if(download.isEmpty()) continue;
                    me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, card + ": " + download);
                    return;
                }
            }
            me.urielsalis.urielsalads.extensions.irc.Main.api.message(channel, "Not found");
        }
    }

    private static boolean contains(String str1, String str2) {
        return str1.toLowerCase().trim().contains(str2.toLowerCase().trim()) || str2.toLowerCase().trim().contains(str1.toLowerCase().trim());
    }

}
