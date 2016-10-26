package me.urielsalis.urielsalads.extensions.extensionLoader;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import me.urielsalis.urielsalads.extensions.ExtensionAPI.Extension;

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
public class ExtensionHandler {
    // Gets module information in array for further processing
    public static ArrayList<ExtensionData> extensions = new ArrayList<>();
    public static ArrayList<ExtensionData> orderToLoad = new ArrayList<>();
    public static ExtensionAPI api = new ExtensionAPI();

    public static void loadExtensions() {
        System.out.println("Loading extensions for Urielsalads");
        loadJars();
        Configuration configuration = new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath());

        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Extension.class);
        for(Class clazz: annotated) extensions.add(new ExtensionData((Extension) clazz.getAnnotation(Extension.class), clazz));
        System.out.println("Extensions to load: " + prettyPrint(extensions));
        sortLoading();
        runExtensions();
    }

    public static void unloadExtension(ExtensionData data) {
        extensions.remove(data);
        orderToLoad.remove(data);
        System.out.println("Unloading " + data.extension.id());
        // Avoid Class.newInstance, for it is evil.
        Class<?> klass = data.clazz;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(ExtensionAPI.ExtensionUnload.class)) {
                    try {
                        System.out.println("Invoking " + method.getName());
                        method.invoke(null, api); //invoker is null as its static
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.out.println("Error while trying to run method");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
    }

    public static void loadExtension(ExtensionData data) {
        System.out.println("Running init " + data.extension.id());
        // Avoid Class.newInstance, for it is evil.
        Class<?> klass = data.clazz;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(ExtensionAPI.ExtensionInit.class)) {
                    try {
                        System.out.println("Invoking " + method.getName());
                        method.invoke(null, api); //invoker is null as its static
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        System.out.println("Error while trying to run method");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
    }

    private static void runExtensions() {
        for(ExtensionData data: orderToLoad) {
            loadExtension(data);
        }
    }

    private static String prettyPrint(ArrayList<ExtensionData> extensions) {
        StringBuilder builder = new StringBuilder();
        for(ExtensionData data: extensions) {
            builder.append(", " + data.extension.name() + " " + data.extension.version());
        }
        return builder.substring(2);
    }

    private static void sortLoading() {
        System.out.print("Dependency loading order");
        for(ExtensionData extensionData: extensions) {
            String[] dependencies = extensionData.extension.dependencies();
            if(dependencies.length==0) orderToLoad.add(extensionData);
            else {
                checkDependencies(extensionData, dependencies, null);
            }
        }
        System.out.println();
        if(extensions.size() != orderToLoad.size()) {
            System.out.println("Not all dependencies could be loaded");
            System.out.println("To load: " + prettyPrint(extensions));
            System.out.println("Loaded: " + prettyPrint(orderToLoad));
            System.exit(1);
        }
    }

    private static void checkDependencies(ExtensionData extensionData, String[] dependencies, String version) {
        if (dependencies.length == 0 && (version == null || version.endsWith("+") && isEqualOrHigher(extensionData.extension.version(), version) || version.endsWith("-") && isEqualOrHigher(version, extensionData.extension.version()) || version.equals(extensionData.extension.version()))) {
            orderToLoad.add(extensionData);
            System.out.print(" -> " + extensionData.extension.name() + " " + extensionData.extension.version());
        }
        for(String string: dependencies) {
            String nameToSearch;
            String versiontoSearch = null;
            if(string.contains("/")) {
                String[] temp = string.split("/");
                nameToSearch = temp[0];
                versiontoSearch = temp[1];
            } else {
                nameToSearch = string;
            }
            if(!alreadyLoaded(nameToSearch, versiontoSearch)) {
                ExtensionData data = getExtensionData(nameToSearch, versiontoSearch);
                checkDependencies(data, data.extension.dependencies(), data.extension.version());
            }
        }
        if(allDependenciesMet(dependencies)) {
            orderToLoad.add(extensionData);
            System.out.print(" -> " + extensionData.extension.name() + " " + extensionData.extension.version());
        } else {
            System.out.println("\nMissing dependencies of " + extensionData.extension.id());
            System.exit(1);
        }
    }

    private static boolean allDependenciesMet(String[] dependencies) {
        for(String string: dependencies) {
            String nameToSearch;
            String versiontoSearch = null;
            if(string.contains("/")) {
                String[] temp = string.split("/");
                nameToSearch = temp[0];
                versiontoSearch = temp[1];
            } else {
                nameToSearch = string;
            }
            if(!alreadyLoaded(nameToSearch, versiontoSearch)) return false;
        }
        return true;
    }

    private static ExtensionData getExtensionData(String nameToSearch, String versiontoSearch) {
        for(ExtensionData data: extensions) {
            if(data.extension.name().equals(nameToSearch)) {
                if(versiontoSearch==null) return data;
                else if(versiontoSearch.contains("+") && isEqualOrHigher(data.extension.version(), versiontoSearch)) return data;
                else if(versiontoSearch.contains("-") && isEqualOrHigher(versiontoSearch, data.extension.version())) return data;
                else if(versiontoSearch.equals(data.extension.version())) return data;
            }
        }
        System.out.println("\nDependency " + nameToSearch + " " + versiontoSearch + " not found. Exiting");
        System.exit(1);
        return null;
    }

    private static boolean alreadyLoaded(String nameToSearch, String versiontoSearch) {
        for(ExtensionData data: orderToLoad) {
            if(data.extension.name().equals(nameToSearch)) {
                if(versiontoSearch==null) return true;
                else if(versiontoSearch.contains("+") && isEqualOrHigher(data.extension.version(), versiontoSearch)) return true;
                else if(versiontoSearch.contains("-") && isEqualOrHigher(versiontoSearch, data.extension.version())) return true;
                else if(versiontoSearch.equals(data.extension.version())) return true;
            }
        }
        return false;
    }

    private static boolean isEqualOrHigher(String version1, String version2) {
        if(version1.equals(version2)) return true;
        String[] version1Data = version1.split(".");
        String[] version2Data = version2.split(".");
        for (int i = 0; i < version1Data.length; i++) {
            if(Integer.parseInt(version1Data[i]) > Integer.parseInt(version2Data[i])) return true;
        }
        return false;
    }

    private static void loadJars() {
        File directory = new File("extensions");
        File[] files = directory.listFiles(new FilenameFilter() {public boolean accept(File dir, String name) {return name.endsWith(".jar");}});

        if (files != null) {
            for (File file : files) {
                try {
                    System.out.println("Loading .jar: " + file.getName());
                    ClassPathHacker.addFile(file);
                } catch (IOException e) {
                    System.out.println("This should never happen, this is bad");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } else {
            System.out.println("No extensions to load");
        }
    }

    /*public static void initModules() {
        ClassLoader loader = URLClassLoader.newInstance(
                new URL[] { yourURL },
                getClass().getClassLoader()
        );
        Class<?> clazz = Class.forName("mypackage.MyClass", true, loader);
        Class<? extends Runnable> runClass = clazz.asSubclass(Runnable.class);
        // Avoid Class.newInstance, for it is evil.
        Constructor<? extends Runnable> ctor = runClass.getConstructor();
        Runnable doRun = ctor.newInstance();
        doRun.run();
    }*/
}
