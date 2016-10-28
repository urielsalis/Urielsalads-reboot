package me.urielsalis.urielsalads.extensions.download;

import me.urielsalis.urielsalads.extensions.intelDownload.EPMIdResults;

import java.util.ArrayList;
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
public class Download {
    public int epmID;
    public List<OS> os;

    public Download() {

    }

    public Download(int epmID) {
        this.epmID = epmID;
        os = new ArrayList<>();
    }

    public Download(EPMIdResults.ResultsForDisplayImpl display) {
        epmID = display.Id;
        os = new ArrayList<>();
        for(String str: display.OperatingSystemSet) {
            String version = "TooOld";
            int arch = 32;
            if(str.contains("7")) {
                version = "7";
            } else if(str.contains("7")) {
                version = "8";
            } else if(str.contains("7")) {
                version = "8.1";
            } else if(str.contains("7")) {
                version = "10";
            }
            os.add(new OS(version, arch));
        }
    }
}
