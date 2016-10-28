package me.urielsalis.urielsalads.extensions.commands;


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

import com.ircclouds.irc.api.Callback;
import com.ircclouds.irc.api.domain.IRCChannel;
import com.sun.deploy.util.StringUtils;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import net.engio.mbassy.listener.Handler;

import java.util.Arrays;

import static me.urielsalis.urielsalads.extensions.irc.Main.api;

@ExtensionAPI.Extension(name="commands", version="1.0.0", id="commands/1.0.0", dependencies = {"irc"})
public class Main {
    //	commands: handles sending messages as bot, quitting bot, reloading modules, loading a new module and listing modules.
    private static ExtensionAPI extapi;
    @ExtensionAPI.ExtensionInit("commands/1.0.0")
    public static void initCommands(ExtensionAPI extapi) {
        Main.extapi = extapi;
        try {
            extapi.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    @ExtensionAPI.ExtensionUnload("commands/1.0.0")
    public static void unload(ExtensionAPI extapi) {
        try {
            extapi.unregisterListener("commandEvent", "commands/1.0.0/CommandListener");
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            //handle command
            String channel = command.getArgs()[0];
            switch(command.getName()) {
                case "sendMessage":
                    String message = StringUtils.join(Arrays.asList(command.getArgs()), " ");
                    api.message(channel, message);
                    break;

                case "part":
                    api.leaveChannel(channel, "Bai");
                    break;

                case "join":
                    api.joinChannel(channel);
                    break;

                case "joinMsg":
                    api.joinChannel(channel, new Callback<IRCChannel>() {
                        @Override
                        public void onSuccess(IRCChannel aObject) {
                            api.message(aObject.getName(), "Im UrielSalads, defender of the vegetables! Beware of my tomato cannons!");
                        }
                        @Override
                        public void onFailure(Exception aExc) {

                        }
                    });
                    break;

                case "listExtesions":
                    api.message(channel, ExtensionAPI.prettyPrint2(extapi.avaliableExtensions));
                    break;

                case "listLoadedExtesions":
                    api.message(channel, ExtensionAPI.prettyPrint2(extapi.loadedExtensions));
                    break;

                case "unloadExtesion": {
                    String name;
                    String version = null;
                    if (channel.contains("/")) {
                        String[] extensionData = channel.split("/");
                        name = extensionData[0];
                        version = extensionData[1];
                    } else {
                        name = channel;
                    }
                    try {
                        extapi.fire("unloadExtension", extapi.getExtention(name, version));
                    } catch (ExtensionAPI.EventDoesntExistsException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "loadExtesion": {
                    String name;
                    String version = null;
                    if (channel.contains("/")) {
                        String[] extensionData = channel.split("/");
                        name = extensionData[0];
                        version = extensionData[1];
                    } else {
                        name = channel;
                    }
                    try {
                        extapi.fire("loadExtension", extapi.getExtention(name, version));
                    } catch (ExtensionAPI.EventDoesntExistsException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "quit":
                    api.disconnect("But I dont want to go to sleep :o(");
                    break;

            }
        }

        @Override
        public String name() {
            return "commands/1.0.0/CommandListener";
        }
    }
}
