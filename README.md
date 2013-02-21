About
-----

Creates JINI bindings for given class(es). CLASS can be in form `java.lang.Class` to generate binding for one class only or `java.lang.*` to generate it for all `java.lang` classes (but not `java.class.reflect` classes). When a subclass is provided (that is, when `$` is present), it will be replaced with the outer-most class.

Requirements
------------

This tools uses [Argparse4j](http://argparse4j.sourceforge.net).

Building
--------

### Maven

    $ cd jini/tools/Refelct
    $ mvn clean dependency:copy-dependencies package

Maven will output Argparse4j in `./target/dependencies` and the Reflect in `./target`.

###Eclipse

*TODO* 

Examples
--------

To generate Android bindings big enough to help with native bitmaps and canvases:

    $ java -jar path/to/Reflect-0.1.jar -a 17 --parents android.graphics.Bitmap android.graphics.Canvas

This will create headers and code for both `Bitmap` and `Canvas` and for their superclasses up to and including `Object`. This will not, however, generate binding for [getScaledHeight(DisplayMetrics)](http://developer.android.com/reference/android/graphics/Bitmap.html#getScaledHeight%28android.util.DisplayMetrics%29), as neither `android.util.DisplayMetrics`, `--preserve-refs` nor `--all-deps` was given as an argument.
