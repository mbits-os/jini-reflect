About
-----

Creates JINI bindings for given class(es). CLASS can be in form `java.lang.Class` to generate binding for one class only or `java.lang.*` to generate it for all `java.lang` classes (but not `java.class.reflect` classes). When an internal class is provided (that is, when `$` is present), it will be replaced with the outer-most class.

Requirements
------------

This tools uses [Argparse4j](http://argparse4j.sourceforge.net) for argument parsing. Also, building and packaging is done with [Maven](http://maven.apache.org/).

Building
--------

Build and package the project with

    $ mvn package

Maven will generate the bundle in `./target/` as `reflect-VERSION-bundle.tar.gz`, `reflect-VERSION-bundle.tar.bz2` and `reflect-VERSION-bundle.zip`. The bundle will contain all the Jar files needed to run the Reflect.

Examples
--------

To see the help:

    $ java -jar path/to/reflect.jar --help

To generate Android bindings big enough to help with native bitmaps and canvases:

    $ java -jar path/to/reflect.jar -a 17 --parents android.graphics.Bitmap android.graphics.Canvas

This will create headers and code for both `Bitmap` and `Canvas` and for their superclasses up to and including `Object`. This will not, however, generate binding for [getScaledHeight(DisplayMetrics)](http://developer.android.com/reference/android/graphics/Bitmap.html#getScaledHeight%28android.util.DisplayMetrics%29), as neither `android.util.DisplayMetrics`, `--preserve-refs` nor `--all-deps` was given as an argument.

Contributing with Eclipse
-------------------------

If you want to use Eclipse, you can use `eclipse:eclipse` mojo to create the Exlipse projects **before** you start adding the projects to the IDE:

    $ mvn eclipse:eclipse

Then, since the generated projects already have all the necessary references, include each module in order Maven would process them: `libplugins`, `libreflect`, `libcppwriter`, `application` and finally `plugin-android-reflect`.

To include a module:

- select `File` &raquo; `New` &raquo; `Java Project`
- deselect `Use default location`
- select `Browse...`
- point to the root directory of the module
- select `OK` &raquo; `Finish`

API Documentation
-----------------

You can create Javadocs with `javadoc:aggregate` mojo:

    $ mvn javadoc:aggregate

This will generate entire javadoc in `./target/site/apidocs`. Alternatively, you can go to [online docs](http://mbits-os.github.com/jini-reflect/apidocs).