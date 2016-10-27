package me.urielsalis.urielsalads.extensions.intelDownload.config;

import java.util.Date;
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
public class Intel {
    String version;
    Date lastPartialUpdate;
    Date lastFullUpdate;
    String name;
    List<Driver> drivers;
    List<Ark> families;

    class Driver {
        String name;
        int epmID;
        List<Download> downloads;
    }

    class Ark {
        String name;
        List<Download> download;
    }

}
