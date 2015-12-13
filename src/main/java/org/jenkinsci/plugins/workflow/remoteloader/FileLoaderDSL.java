/*
 * The MIT License
 *
 * Copyright (c) 2015 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.workflow.remoteloader;

import hudson.Extension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Provides a &quot;fileLoader&quot; global variable.
 * This variable allows to load Workflow objects from remote locations (e.g. Git repositories).
 * @author Oleg Nenashev
 */
@Extension
public class FileLoaderDSL extends GroovyFileGlobalVariable {

    @Override 
    public String getName() {
        return "fileLoader";
    }
    
    /**
     * Loads a snippet from the resource file.
     * @param name Name of the snippet (e.g. &quot;loadMultipleFiles&quot;)
     * @return Workflow script text
     * @throws IOException Loading error
     */
    @Restricted(NoExternalUse.class)
    public static String getSampleSnippet(String name) throws IOException {
        final String resourceName = "FileLoaderDSL/sample_" + name + ".groovy";
        final InputStream scriptStream = FileLoaderDSL.class.getResourceAsStream(resourceName);
        if (scriptStream == null) {
            throw new IOException("Cannot find sample script in " + resourceName);
        }
        return IOUtils.toString(scriptStream, "UTF-8");
    }
    
    @Extension
    public static class MiscWhitelist extends ProxyWhitelist {

        public MiscWhitelist() throws IOException {
            super(new StaticWhitelist(
                    "method groovy.lang.Closure call java.lang.Object",
                    "method java.lang.Object toString",
                    "method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object"
            ));
        }
    }
}
