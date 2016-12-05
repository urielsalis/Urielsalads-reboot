package me.urielsalis.urielsalads.extensions.irc;

import com.ircclouds.irc.api.IRCApiImpl;

/**
 * Created by Uriel Salischiker on 12/4/2016.
 */
public class IRCAPIClass extends IRCApiImpl{
    /**
     * @param aSaveIRCState A flag to allow saving the IRC state that will be obtained by {@link #connect(IServerParameters, Callback)}
     */
    public IRCAPIClass(Boolean aSaveIRCState) {
        super(aSaveIRCState);
    }

    @Override
    public void message(String aTarget, String aMessage) {
        String[] strings = aMessage.split("\n");
        for(String str: strings) {
            super.message(aTarget, str);
        }
    }
}
