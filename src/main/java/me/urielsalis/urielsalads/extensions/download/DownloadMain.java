package me.urielsalis.urielsalads.extensions.download;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

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

    public static Config getConfig() {
        return config;
    }

    @ExtensionAPI.ExtensionInit("download/1.0.0")
    public static void initDownload() {
        if(new File("data.json").exists()) {
            config = DownloadMain.getConfig();
        } else {
            config = new Config("1.0.0", "urielsalads-downloader");
            writeJSON();
        }
    }

    @ExtensionAPI.ExtensionUnload("download/1.0.0")
    public static void unloadDownload() {
        writeJSON();
    }

    public static void writeJSON() {
        config.createdOn = new Date();
        try (FileWriter writer = new FileWriter("intel.json")) {
            g.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
