package me.urielsalis.urielsalads;

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

import me.urielsalis.urielsalads.extensions.extensionLoader.ExtensionHandler;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;


public class Main {
    public static String apiKey = "apikey";

    public static void main(String[] args) {
        long nanotime = System.nanoTime();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        ExtensionHandler.loadExtensions();
        long nanos = System.nanoTime()-nanotime;
        System.out.println("Init took " + String.format("%d min, %d sec",
                TimeUnit.NANOSECONDS.toMinutes(nanos),
                TimeUnit.NANOSECONDS.toSeconds(nanos) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(nanos))
        ));

    }
}
