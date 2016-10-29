package me.urielsalis.urielsalads.extensions;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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

//This file should be copied in the same package for extensions to work
public class ExtensionAPI {
    public ArrayList<Extension> loadedExtensions = new ArrayList<>();
    public ArrayList<Extension> avaliableExtensions = new ArrayList<>();
    public ArrayList<Listener> listeners = new ArrayList<>();

    ConcurrentHashMap<String, MBassador> bus = new ConcurrentHashMap<>();


    public void registerEvent(String eventName) throws EventAlreadyExistsException {
        synchronized (bus) {
            if (bus.containsKey(eventName)) throw new EventAlreadyExistsException();
            bus.put(eventName, new MBassador(new IPublicationErrorHandler.ConsoleLogger()));
        }
    }

    public void registerListener(String eventName, Listener listener) throws EventDoesntExistsException {
        synchronized (bus) {
            if(!bus.containsKey(eventName)) throw new EventDoesntExistsException();
            bus.get(eventName).subscribe(listener);
        }
        synchronized (listeners) {
            listeners.add(listener);
        }
        fire("onListenerRegistered", eventName);
    }

    public void unregisterListener(String eventName, String name) throws EventDoesntExistsException {
        synchronized (bus) {
            if (!bus.containsKey(eventName)) throw new EventDoesntExistsException();
        }
        for(Listener listener: listeners) {
            if(listener.name().equals(name)) {
                synchronized (bus) {
                    bus.get(eventName).unsubscribe(listener);
                }
                synchronized (listeners) {
                    listeners.remove(listener);
                }
                return;
            }
        }
    }

    public IMessagePublication fire(String eventName, Object data) throws EventDoesntExistsException {
        if(data==null) return null;
        synchronized (bus) {
            if(!bus.containsKey(eventName)) throw new EventDoesntExistsException();
            return bus.get(eventName).post(data).asynchronously();
        }
    }

    public Extension getExtention(String name, String version) {
        for(Extension extension: avaliableExtensions) {
            if(extension.name().equals(name)) {
                if (version != null) {
                    if (version.equals(extension.version())) return extension;
                } else return extension;
            }
        }
        return null;
    }

    public void unRegisterEvent(String eventName) throws EventDoesntExistsException {
        if(!bus.containsKey(eventName)) throw new EventDoesntExistsException();
        synchronized (bus) {
            bus.remove(eventName);
        }
    }


    public interface Listener {
        String name();
    }

    public class EventAlreadyExistsException extends Exception {
    }

    public class EventDoesntExistsException extends Throwable {
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = ElementType.METHOD)
    public @interface ExtensionInit {
        String value();
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = ElementType.METHOD)
    public @interface ExtensionUnload {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Extension {
        public String name();
        public String version();
        public String[] dependencies() default {};
        public String id();
    }

    public static String prettyPrint(ArrayList<ExtensionData> extensions) {
        StringBuilder builder = new StringBuilder();
        for(ExtensionData data: extensions) {
            builder.append(", " + data.extension.name() + " " + data.extension.version());
        }
        return builder.substring(2);
    }
    public static String prettyPrint2(ArrayList<Extension> extensions) {
        StringBuilder builder = new StringBuilder();
        for(Extension extension: extensions) {
            builder.append(", " + extension.name() + " " + extension.version());
        }
        return builder.substring(2);
    }

    public static class ExtensionData {
        public ExtensionAPI.Extension extension;
        public Class clazz;

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
}
