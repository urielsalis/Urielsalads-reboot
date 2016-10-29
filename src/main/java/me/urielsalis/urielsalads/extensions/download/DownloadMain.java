package me.urielsalis.urielsalads.extensions.download;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

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
@ExtensionAPI.Extension(name = "download", version = "1.0.0", id = "download/1.0.0")
public class DownloadMain {
    public static Gson g = new GsonBuilder().setPrettyPrinting().create();

    public static Config config;
    static boolean initialized = false;

    public static Config getConfig() {
        if(!initialized) {
            if(new File("data.json").exists()) {
                Scanner scanner = null;
                try {
                    scanner = new Scanner(new File("data.json"));
                    String json = scanner.useDelimiter("\\A").next();

                    config = new Gson().fromJson(json, Config.class);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (scanner != null) scanner.close();
                }
            } else {
                config = new Config("1.0.0", "urielsalads-downloader");
                writeJSON();
            }
            initialized = true;
        }
        return config;
    }

    @ExtensionAPI.ExtensionInit("download/1.0.0")
    public static void initDownload(ExtensionAPI api) {
        if(new File("data.json").exists()) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File("data.json"));
                String json = scanner.useDelimiter("\\A").next();

                config = new Gson().fromJson(json, Config.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) scanner.close();
            }
        } else {
            config = new Config("1.0.0", "urielsalads-downloader");
            writeJSON();
        }
        initialized = true;
    }

    @ExtensionAPI.ExtensionUnload("download/1.0.0")
    public static void unloadDownload(ExtensionAPI api) {
        writeJSON();
    }

    public static void writeJSON() {
        config.createdOn = new Date();
        Config config2 = new Config(config.version, "urielsalads");
        config2.list = config.list;
        config2.createdOn = config.createdOn;
        config2.manual = config.manual;
        try {
            FileWriter writer = new FileWriter("data.json");
            g.toJson(config, writer);
            writer.close();
            FileWriter writer2 = new FileWriter("compressedData.json");
            g.toJson(config2, writer2);
            writer2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
