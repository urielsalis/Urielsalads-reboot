package me.urielsalis.urielsalads.extensions.intelSearch;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import net.engio.mbassy.listener.Handler;

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
@ExtensionAPI.Extension(name = "intel-search", version = "1.0.0", id = "intel-search/1.0.0")
public class Main {
    private static ExtensionAPI api;

    @ExtensionAPI.ExtensionInit("intel-search/1.0.0")
    public static void intelSearchInit(ExtensionAPI api) {
        Main.api = api;
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    @ExtensionAPI.ExtensionUnload("intel-search/1.0.0")
    public static void unloadintelSearch(ExtensionAPI api) {
        try {
            api.unregisterListener("commandEvent", "intel-search/1.0.0/CommandListener");
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
            return "intel-search/1.0.0/CommandListener";
        }
    }
}
