About
-----

Creates JINI bindings for given class(es). CLASS can be in form `java.lang.Class` to generate binding
for one class only or `java.lang.*` to generate it for all `java.lang` classes (but not `java.class.reflect` classes).
When a subclass is provided (that is, when `$` is present), it will be replaced with the outer-most class.

Requirements
------------

This tools uses [Argparse4J](http://argparse4j.sourceforge.net).

Building
--------

For Maven:

    $ cd jini/tools/Refelct
    $ mvn clear depdendency:copy-depedencies package

From now on, you can call it with

	$ java -jar targets/Reflect-0.1.jar --help

For Eclipse, create a Java project, *TODO* 
