# PathyJar

A simple wrapper that allows to run JAR files with custom classpath.

## Why?

Java doesn't allow you to specify `-classpath` argument together with `-jar`,
the first will be  simply ignored.

If you want to do so, you must manually read into the JAR, find manifest there,
read it to find out what `Main-Class` you need to run, and then run it. Why
bother so much when Java has APIs for the whole process.

## How to use it?

The wrapper accepts two arguments:
- `-classpath [item1;item2;...]` / `-cp [item1;item2;...]`

   Specifies the classpath, items are separated by `;`.
- `-jar [jar-file] <... argv>`

   Specifies JAR name, which main class must be run (read from manifest).
   
   Once `-jar` specified, everything that follows will be passed to the JAR main
   method.

```sh
java -jar pathy-jar-1.0-SNAPSHOT.jar -cp cool-lib-1.0.0.jar -jar app.jar --app-argument
```

## Unimplemented

- It completely ignores classpath specified by the JAR in manifest.
- GitHub Actions to automatically build and create release with artifact.

## Downloads

Latest artifact should be in GitHub [Releases][releases], but you can always
build the project yourself.

[releases]: https://github.com/Brawaru/pathy-jar/releases