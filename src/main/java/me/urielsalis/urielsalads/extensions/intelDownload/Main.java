package me.urielsalis.urielsalads.extensions.intelDownload;

import com.moandjiezana.toml.Toml;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.intelDownload.config.Intel;

import java.io.File;

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

    public static Toml toml;

    @ExtensionAPI.ExtensionInit("intel-download/1.0.0")
    public static void init(ExtensionAPI api) {
        toml = new Toml().read(new File("example.toml"));
        Intel intel = toml.getTable("intel").to(Intel.class);
        System.out.println("test");
    }

    public static void main(String[] args) {
        init(null);
    }

    @ExtensionAPI.ExtensionUnload("intel-download/1.0.0")
    public static void unload(ExtensionAPI api) {

    }


}
