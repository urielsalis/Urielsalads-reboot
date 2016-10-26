package me.urielsalis.urielsalads.extensions.extensionLoader;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;

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
public class ExtensionData {
    ExtensionAPI.Extension extension;
    Class clazz;

    public ExtensionData(ExtensionAPI.Extension extension, Class clazz) {
        this.extension = extension;
        this.clazz = clazz;
    }

    public ExtensionData() {

    }

    public ExtensionAPI.Extension getExtension() {

        return extension;
    }

    public void setExtension(ExtensionAPI.Extension extension) {
        this.extension = extension;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
