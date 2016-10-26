package me.urielsalis.urielsalads.extensions.irc;

import com.ircclouds.irc.api.Callback;
import com.ircclouds.irc.api.IRCApi;
import com.ircclouds.irc.api.IRCApiImpl;
import com.ircclouds.irc.api.IServerParameters;
import com.ircclouds.irc.api.domain.IRCServer;
import com.ircclouds.irc.api.domain.messages.*;
import com.ircclouds.irc.api.domain.messages.interfaces.IMessage;
import com.ircclouds.irc.api.listeners.VariousMessageListenerAdapter;
import com.ircclouds.irc.api.state.IIRCState;
import com.sun.deploy.util.ArrayUtil;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import org.aeonbits.owner.ConfigFactory;
import java.util.Arrays;
import java.util.List;

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
@ExtensionAPI.Extension(name = "irc", version = "1.0.0", id = "irc/1.0.0")
public class Main {
    public static IRCApi api;
    private static ExtensionAPI extapi;
    private static IRCConfig ircConfig;

    @ExtensionAPI.ExtensionInit("commands/1.0.0")
    public static void init(ExtensionAPI extapi) {
        Main.extapi = extapi;
        Main.api = new IRCApiImpl(true);
        System.out.println("Reading configs");
        ircConfig = ConfigFactory.create(IRCConfig.class);
        System.out.println("Starting IRC");
        api.connect(getServerParams(ircConfig.nick1(), Arrays.asList(ircConfig.nick2(), ircConfig.nick3()), ircConfig.realName(), ircConfig.ident(), ircConfig.server(), true), new Callback<IIRCState>()
        {
            @Override
            public void onSuccess(final IIRCState aIRCState)
            {
                // Connected! continue loading
                Main.continueLoad();
            }

            @Override
            public void onFailure(Exception aErrorMessage)
            {
                throw new RuntimeException(aErrorMessage);
            }
        });
    }

    private static void continueLoad() {
        try {
            extapi.registerEvent("commandEvent");
            extapi.registerEvent("onUserPing");
            extapi.registerEvent("onUserVersion");
            extapi.registerEvent("onServerPing");
            extapi.registerEvent("onMessage");
            extapi.registerEvent("onChannelJoin");
            extapi.registerEvent("onChannelPart");
            extapi.registerEvent("onChannelNotice");
            extapi.registerEvent("onChannelAction");
            extapi.registerEvent("onChannelKick");
            extapi.registerEvent("onMessage");
            extapi.registerEvent("onTopicChange");
            extapi.registerEvent("onUserPrivMessage");
            extapi.registerEvent("onUserNotice");
            extapi.registerEvent("onUserAction");
            extapi.registerEvent("onServerNumericAction");
            extapi.registerEvent("onServerNotice");
            extapi.registerEvent("onNickChange");
            extapi.registerEvent("onUserQuit");
            extapi.registerEvent("onError");
            extapi.registerEvent("onChannelMode");

            api.addListener(new Listeners());
            api.message("NickServ", "identify " + ircConfig.nick1() + " " + ircConfig.password());
            String[] channels = ircConfig.joinChannels().split(" ");
            for(String channel: channels) {
                api.joinChannel(channel);
            }
        } catch (ExtensionAPI.EventAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public static class Listeners extends VariousMessageListenerAdapter
    {
        public void onUserPing(UserPing aMsg) {
            try { extapi.fire("onUserPing", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onUserVersion(UserVersion aMsg) {
            try { extapi.fire("onUserVersion", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onServerPing(ServerPing aMsg) {
            try { extapi.fire("onServerPing", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onMessage(IMessage aMessage) {
            try { extapi.fire("onMessage", aMessage); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }

        }

        public void onChannelMessage(ChannelPrivMsg aMsg) {
            try { extapi.fire("onChannelMessage", aMsg); extapi.fire("commandEvent", parseCommand(aMsg, aMsg.getChannelName())); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelJoin(ChanJoinMessage aMsg) {
            try { extapi.fire("onChannelJoin", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelPart(ChanPartMessage aMsg) {
            try { extapi.fire("onChannelPart", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelNotice(ChannelNotice aMsg) {
            try { extapi.fire("onChannelNotice", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelAction(ChannelActionMsg aMsg) {
            try { extapi.fire("onChannelAction", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelKick(ChannelKick aMsg) {
            try { extapi.fire("onChannelKick", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onTopicChange(TopicMessage aMsg) {
            try { extapi.fire("onTopicChange", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onUserPrivMessage(UserPrivMsg aMsg) {
            try { extapi.fire("onUserPrivMessage", aMsg); extapi.fire("commandEvent", parseCommand(aMsg, null));} catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onUserNotice(UserNotice aMsg) {
            try { extapi.fire("onUserNotice", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onUserAction(UserActionMsg aMsg) {
            try { extapi.fire("onUserAction", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onServerNumericMessage(ServerNumericMessage aMsg) {
            try { extapi.fire("onServerNumericMessage", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onServerNotice(ServerNotice aMsg) {
            try { extapi.fire("onServerNotice", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onNickChange(NickMessage aMsg) {
            try { extapi.fire("onNickChange", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onUserQuit(QuitMessage aMsg) {
            try { extapi.fire("onUserQuit", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onError(ErrorMessage aMsg) {
            try { extapi.fire("onError", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        public void onChannelMode(ChannelModeMessage aMsg) {
            try { extapi.fire("onChannelMode", aMsg); } catch (ExtensionAPI.EventDoesntExistsException e) { e.printStackTrace(); }
        }

        private Command parseCommand(AbstractPrivMsg aMsg, String channel) {
            String text = aMsg.getText();
            String fromUser = aMsg.getSource().getNick();
            if(channel==null) channel = fromUser;
            String[] split = text.split(" ");
            if(split.length==0) {
                return new Command("", "", channel, fromUser);
            } else if(split.length==1) {
                return new Command(text, "", channel, fromUser);
            } else {
                return new Command(split[0], Arrays.copyOfRange(split, 1, split.length), channel, fromUser);
            }
        }
    }


    public static class Command {
        String name;
        String[] args;
        String fromUser;
        String channel;

        public Command(String name, String args, String channel, String fromUser) {
            this.name = name;
            this.args = args.split(" ");
            this.channel = channel;
            this.fromUser = fromUser;
        }

        public Command(String name, String[] args, String channel, String fromUser) {
            this.name = name;
            this.args = args;
            this.channel = channel;
            this.fromUser = fromUser;
        }
    }

    private static IServerParameters getServerParams(final String aNickname, final List<String> aAlternativeNicks, final String aRealname, final String aIdent,
                                                     final String aServerName, final Boolean aIsSSLServer)
    {
        return new IServerParameters()
        {
            @Override
            public IRCServer getServer()
            {
                return new IRCServer(aServerName, aIsSSLServer);
            }

            @Override
            public String getRealname()
            {
                return aRealname;
            }

            @Override
            public String getNickname()
            {
                return aNickname;
            }

            @Override
            public String getIdent()
            {
                return aIdent;
            }

            @Override
            public List<String> getAlternativeNicknames()
            {
                return aAlternativeNicks;
            }
        };
    }
}
