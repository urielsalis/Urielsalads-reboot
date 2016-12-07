package me.urielsalis.urielsalads.extensions.irc;

import com.ircclouds.irc.api.IRCApiImpl;
import com.sun.deploy.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Uriel Salischiker on 12/4/2016.
 */
public class IRCAPIClass extends IRCApiImpl{
    public IRCAPIClass(Boolean aSaveIRCState) {
        super(aSaveIRCState);
    }

    @Override
    public void message(String aTarget, String aMessage) {
        String[] strings = aMessage.split("\n");
        for(String str: strings) {
            if(str.length() > 300) {
                List<String> result = splitString(str, 300);
                for(String str2: result) {
                    super.message(aTarget, str2);
                }
            } else{
                super.message(aTarget, str);
            }
        }
    }

    public static List<String> splitString(String msg, int lineSize) {
        String[] words = msg.split("\\s+");
        StringBuilder res = new StringBuilder();
        List<String> result = new ArrayList<>();
        for(String str: words) {
            if(res.length() > lineSize) {
                result.add(res.toString());
                res = new StringBuilder();
            }
            res.append(str).append(" ");
        }
        if(res.length() > 0) {
            result.add(res.toString());
        }
        return result;
    }

}
