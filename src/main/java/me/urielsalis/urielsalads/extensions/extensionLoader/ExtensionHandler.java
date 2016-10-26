package me.urielsalis.urielsalads.extensions.extensionLoader;

import com.google.common.collect.Lists;
import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.ExtensionAPI.Extension;
import net.engio.mbassy.listener.Handler;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static me.urielsalis.urielsalads.extensions.ExtensionAPI.prettyPrint;
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
    private static ArrayList<ExtensionAPI.ExtensionData> extensions = new ArrayList<>();
    public static ExtensionAPI api = new ExtensionAPI();
    public static ArrayList<LoadOrder> steps = new ArrayList<>();
    public static ArrayList<ExtensionAPI.ExtensionData> alreadyLoaded = new ArrayList<>();

    public static void loadExtensions() {
        System.out.println("Loading extensions for Urielsalads");
        try {
            api.registerEvent("onListenerRegistered");
            api.registerEvent("onExtensionLoad"); //this can be used to replace a event of another class, wont use it but its good for people that want to do it :)
            api.registerEvent("onExtensionUnload");
        } catch (ExtensionAPI.EventAlreadyExistsException e) {
            e.printStackTrace();
        }
        loadJars();
        Configuration configuration = new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath());

        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Extension.class);
        for(Class clazz: annotated) extensions.add(new ExtensionAPI.ExtensionData((Extension) clazz.getAnnotation(Extension.class), clazz));
        System.out.println("Extensions to load: " + prettyPrint(extensions));
        for(ExtensionAPI.ExtensionData extensionData: extensions) {
            api.avaliableExtensions.add(extensionData.extension);
        }
        sortLoading();
        runExtensions();
        initEvents();
    }

    private static void initEvents() {
        try {
            api.registerEvent("loadExtension");
            api.registerEvent("unloadExtension");
        } catch (ExtensionAPI.EventAlreadyExistsException e) {
            e.printStackTrace();
        }
        try {
            api.registerListener("loadExtension", new LoadExtensionListener());
            api.registerListener("unloadExtension", new UnloadExtensionListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    public static void unloadExtension(ExtensionAPI.ExtensionData data) {
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
        api.loadedExtensions.remove(data.extension);
        try {
            api.fire("onExtensionUnload", data.extension);
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    public static void loadExtension(ExtensionAPI.ExtensionData data) {
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
        api.loadedExtensions.add(data.extension);
        try {
            api.fire("onExtensionLoad", data.extension);
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static void runExtensions() {
        for(LoadOrder order: steps) {
            List<Callable<Object>> callables = new ArrayList<>();
            for(final ExtensionAPI.ExtensionData data: order.extensions) {
                callables.add(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("Loading " + data.extension.id());
                        loadExtension(data);
                        System.out.println(data.extension.id() + " Loaded");
                        return null;
                    }
                });
            }
            ExecutorService service = Executors.newCachedThreadPool();
            try {
                service.invokeAll(callables);
                while(service.awaitTermination(1, TimeUnit.SECONDS)) { //Wait till all threads finished
                    service.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sortLoading() {
        System.out.print("Dependency loading order");
        ArrayList<ExtensionAPI.ExtensionData> toLoad = Lists.newArrayList(extensions);
        LoadOrder order = new LoadOrder();
        int counter = 1;
        System.out.println("Step " + counter);
        for(ExtensionAPI.ExtensionData extensionData: toLoad) {
            String[] dependencies = extensionData.extension.dependencies();
            if(dependencies.length==0) {
                order.extensions.add(extensionData);
                alreadyLoaded.add(extensionData);
                System.out.print("  " + extensionData.extension.id());
            }
        }
        steps.add(order);
        counter++;
        for(ExtensionAPI.ExtensionData loaded: alreadyLoaded) {
            toLoad.remove(loaded);
        }
        while(alreadyLoaded.size() != extensions.size()) {
            System.out.println("Step " + counter);
            LoadOrder step = new LoadOrder();
            for(ExtensionAPI.ExtensionData data: toLoad) {
                if(dependenciesMet(data)) {
                    step.extensions.add(data);
                    alreadyLoaded.add(data);
                    System.out.print("  " + data.extension.id());
                }
            }
            if(step.extensions.isEmpty()) {
                System.out.println("Cant solve dependencies");
                System.out.println("All extensions: " + prettyPrint(extensions));
                System.out.println("Loaded Extensions: " + prettyPrint(alreadyLoaded));
                System.out.println("Left: " + prettyPrint(toLoad));
                System.exit(1);
            }
            counter++;
        }
        counter--;
        System.out.println("Loading possible in " + counter + " steps");
    }

    private static boolean dependenciesMet(ExtensionAPI.ExtensionData data) {
        String[] dependencies = data.extension.dependencies();
        for(String dependency: dependencies) {
            String nameToSearch;
            String versiontoSearch = null;
            if(dependency.contains("/")) {
                String[] temp = dependency.split("/");
                nameToSearch = temp[0];
                versiontoSearch = temp[1];
            } else {
                nameToSearch = dependency;
            }
            if(!alreadyLoaded(nameToSearch, versiontoSearch)) return false;
        }
        return true;
    }

    private static boolean alreadyLoaded(String nameToSearch, String versiontoSearch) {
        for(ExtensionAPI.ExtensionData data: alreadyLoaded) {
            if(data.extension.name().equals(nameToSearch)) {
                if(versiontoSearch==null) return true;
                else if(versiontoSearch.contains("+") && isEqualOrHigher(data.extension.version(), versiontoSearch)) return true;
                else if(versiontoSearch.contains("-") && isEqualOrHigher(versiontoSearch, data.extension.version())) return true;
                else if(versiontoSearch.equals(data.extension.version())) return true;
            }
        }
        return false;
    }

    private static ExtensionAPI.ExtensionData getExtensionData(String nameToSearch, String versiontoSearch) {
        for(ExtensionAPI.ExtensionData data: extensions) {
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

    public static class LoadExtensionListener implements ExtensionAPI.Listener {
        @Handler
        public void loadExtension(Extension extension) {
            ExtensionAPI.ExtensionData data = ExtensionHandler.getExtensionData(extension.name(), extension.version());
            ExtensionHandler.loadExtension(data);
        }

        @Override
        public String name() {
            return "ExtensionHandler/LoadExtensionListener";
        }
    }

    public static class UnloadExtensionListener implements ExtensionAPI.Listener {
        @Handler
        public void unloadExtension(Extension extension) {
            ExtensionAPI.ExtensionData data = ExtensionHandler.getExtensionData(extension.name(), extension.version());
            ExtensionHandler.unloadExtension(data);
        }


        @Override
        public String name() {
            return "ExtensionHandler/UnloadExtensionListener";
        }
    }

    private static class LoadOrder {
        public ArrayList<ExtensionAPI.ExtensionData> extensions;

        public LoadOrder() {
            extensions = new ArrayList<>();
        }
    }
}
