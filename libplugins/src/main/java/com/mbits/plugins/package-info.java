/*
 * Copyright (C) 2013 midnightBITS
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Plugins Engine base. If you want to use it in your project, you
 * should provide your plugins vendors with the plugin interface
 * and the location, where the jars should be placed.
 * For an example, let's see a plugin system allowing to perform
 * <code>foo()</code> from the plugins.
 * 
 * <h3>FooPlugin.java</h3>
 * 
 * <p>This is the interface class that must be known to the plugin
 * vendors.
 * 
 * <pre class="prettyprint">package com.example.plugins;
 *
 *import com.mbits.plugins.Plugin;
 * 
 *public interface FooPlugin extends Plugin {
 *    void foo();
 *}</pre>
 * 
 * <h3>FooPlugins.java</h3>
 * 
 * <p>Here you prepare the entire environment for the Plugin Engine.
 * First, you implement your own <code>Plugins.Impl</code> providing
 * any pass-through methods from you application code to the plugins.
 * Then, you may think about providing your version of <code>loadPlugins</code>
 * hiding the interface class from the rest of the application.
 * You may also provide code locating the plugins directory here.
 * 
 * <pre class="prettyprint">package com.example.plugins;
 * 
 *import java.io.File;
 *import com.mbits.plugins.Plugins;
 *
 *public class FooPlugins extends Plugins {
 *
 *    private static class Impl extends Plugins.Impl<FooPlugin> {
 *        public void foo() {
 *            for (FooPlugin plug: m_plugins) {
 *                plug.foo();
 *            }
 *        }
 *    }
 *
 *    private static Impl impl = null;
 *    private static boolean hasImpl() {
 *        if (impl == null)
 *            impl = new Impl();
 *        return impl != null;
 *    }
 *
 *    public static void loadPlugins(File file) {
 *        if (!hasImpl()) return;
 *        try {
 *            impl.loadPlugins(file, FooPlugin.class);
 *        } catch (Exception e) {
 *            e.printStackTrace();
 *        }
 *    }
 *
 *    public static void foo() {
 *        if (!hasImpl()) return;
 *        impl.foo();
 *    }
 *}</pre>
 * 
 * <h3>SuperFooPlugin.java</h3>
 * 
 * <p>This is an implementation of a FooPlugin. The code should
 * be packed into the jar file and placed into plugins directory.
 * 
 * <pre class="prettyprint">package com.example.superfoo;
 *
 *import com.example.plugins.FooPlugin;
 *
 *public class SuperFooPlugin implements FooPlugin {
 *    public SuperFooPlugin() {
 *        //...
 *    }
 *
 *    &#x40;Override
 *    public String getName() { return "SuperFoo"; }
 *
 *    &#x40;Override
 *    void foo() {
 *        //...
 *    }
 *}</pre>
 * 
 * <p>This class must be packed into a Jar file. For the Engine to
 * find your plugin, you must add <code>Plugin-Class</code> entry
 * to the manifest file:
 * 
 * <pre>Plugin-Class: com.example.superfoo.SuperFooPlugin</pre>
 * 
 */

package com.mbits.plugins;