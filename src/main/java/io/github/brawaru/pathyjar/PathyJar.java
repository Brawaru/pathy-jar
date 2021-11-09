package io.github.brawaru.pathyjar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class PathyJar {
  public static void main(String[] args)
      throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
          IllegalAccessException {
    String currentRead = null;

    String[] classpathItems = null;
    String jarFilename = null;
    String[] jarArgv = null;

    argsLoop:
    for (int i = 0, l = args.length; i < l; i++) {
      String arg = args[i];

      if (currentRead == null) {
        if ("-cp".equals(arg) || "-classpath".equals(arg)) {
          if (classpathItems != null) {
            throw new IllegalArgumentException(
                "`classpath` argument has already been set to: "
                    + String.join(",", classpathItems));
          }

          currentRead = "cp";
        } else if ("-jar".equals(arg)) {
          currentRead = "jar";
        } else {
          throw new IllegalArgumentException("Unknown argument: " + arg);
        }
      } else {
        switch (currentRead) {
          case "cp":
            classpathItems = arg.split(Pattern.quote(";"));
            break;
          case "jar":
            jarFilename = arg;

            int nextIndex = i + 1;
            if (nextIndex != l) {
              jarArgv = Arrays.copyOfRange(args, nextIndex, args.length);
            }

            break argsLoop;
          default:
            throw new IllegalStateException("Unexpected argument: " + arg);
        }

        currentRead = null;
      }
    }

    if (jarFilename == null) {
      throw new IllegalArgumentException("`jar` argument has not been provided.");
    }

    System.out.print("Class path: ");

    if (classpathItems == null) { // NOSONAR
      System.out.println("- not set -");
    } else {
      System.out.println(classpathItems.length + " item(s)");

      for (String item : classpathItems) {
        System.out.println("- " + item);
      }
    }

    System.out.println("JAR filename: " + jarFilename);
    System.out.println("JAR argv: " + (jarArgv == null ? "- empty -" : String.join(" ", jarArgv)));

    File jarFile = new File(jarFilename);

    String mainClassName;

    try (JarFile jar = new JarFile(jarFile)) {
      mainClassName = jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
    }

    if (mainClassName == null) {
      throw new IllegalArgumentException("Provided `jar` doesn't have Main-Class in its manifest.");
    }

    System.out.println("JAR's Main-Class: " + mainClassName);

    List<URL> classpath = new ArrayList<>();

    classpath.add(jarFile.toURI().toURL());

    if (classpathItems != null) { // NOSONAR
      for (String item : classpathItems) {
        classpath.add(new File(item).toURI().toURL());
      }
    }

    try (URLClassLoader jarClassLoader =
        new URLClassLoader(classpath.toArray(new URL[0]), PathyJar.class.getClassLoader())) {
      Class<?> mainClass = Class.forName(mainClassName, true, jarClassLoader);

      System.out.println("---------");

      Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);

      mainMethod.invoke(null, (Object) (jarArgv == null ? new String[0] : jarArgv)); // NOSONAR
    }
  }
}
