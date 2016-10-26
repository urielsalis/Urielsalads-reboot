package me.urielsalis.urielsalads.extensions.base;


/*
UrielSalads
Copyright (C) 2016 Uriel Salischiker

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import net.engio.mbassy.listener.Handler;

@ExtensionAPI.Extension(name="commands", version="1.0.0", id="commands/1.0.0", dependencies = {"irc"})
public class Main {
    //	commands: handles sending messages as bot, quitting bot, reloading modules, loading a new module and listing modules.

    @ExtensionAPI.ExtensionInit("commands/1.0.0")
    public static void init(ExtensionAPI api) {
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            //handle command
        }
    }
}
