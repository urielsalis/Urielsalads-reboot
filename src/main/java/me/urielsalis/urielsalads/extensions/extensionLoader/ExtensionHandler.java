package me.urielsalis.urielsalads.extensions.extensionLoader;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Set;

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
    ArrayList<ExtensionData> extensions = new ArrayList<>();
    public static void loadExtensions() {
        loadJars();
        Configuration configuration = new ConfigurationBuilder().addUrls(ClasspathHelper.forJavaClassPath());

        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Extension.class);
    }

    private static void loadJars() {
        File directory = new File("extensions");
        File[] files = directory.listFiles(new FilenameFilter() {public boolean accept(File dir, String name) {return name.endsWith(".jar");}});

        if (files != null) {
            for (File file : files) {
                try {
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

    public static void initModules() {
        /*ClassLoader loader = URLClassLoader.newInstance(
                new URL[] { yourURL },
                getClass().getClassLoader()
        );
        Class<?> clazz = Class.forName("mypackage.MyClass", true, loader);
        Class<? extends Runnable> runClass = clazz.asSubclass(Runnable.class);
        // Avoid Class.newInstance, for it is evil.
        Constructor<? extends Runnable> ctor = runClass.getConstructor();
        Runnable doRun = ctor.newInstance();
        doRun.run();
*/
    }
}
