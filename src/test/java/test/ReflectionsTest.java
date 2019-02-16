package test;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by boying on 2018/11/20.
 */
public class ReflectionsTest {
    @Test
    public void test1() {


        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .setUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("org.apache.commons.lang"))));


        //Reflections reflections = new Reflections("test");
        //Reflections reflections = new Reflections("util.json");

        Set<Class<? extends Object>> allClasses =
                reflections.getSubTypesOf(Object.class);
        //Set<String> allTypes = reflections.getAllTypes();

        for (Class<?> allClass : allClasses) {
            System.out.println(allClass);
        }

    }

    @Test
    public void test2() {
        System.out.println(System.getProperty("sun.boot.class.path"));

        URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for (int i = 0; i < urls.length; i++) {
            System.out.println(urls[i].toExternalForm());
        }
    }
}
