package me.urielsalis.urielsalads.extensions.download;

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
public class Config {
    public String version;
    public Date createdOn;
    public Intel intel;
    public AMD amd;
    public Nvidia nvidia;

    public Config(String version, String id) {
        this.version = version;
        createdOn = new Date();
        intel = new Intel(version, id);
        amd = new AMD();
        nvidia = new Nvidia();
    }
}
