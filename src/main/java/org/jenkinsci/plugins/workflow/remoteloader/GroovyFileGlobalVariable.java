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

import groovy.lang.Binding;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Implements Global variable, which is implemented via Groovy file.
 * Exclusions should be configured separately using {@link ProxyWhitelist}.
 * @author Oleg Nenashev
 */
@Restricted(NoExternalUse.class)
public abstract class GroovyFileGlobalVariable extends GlobalVariable {
    
    /**
     * Canonical name of the class to be loaded.
     * @return Canonical class name
     */
    @Nonnull
    public String getClassName() {
        return getClass().getName() + ".Impl";
    }

    @Override 
    public final Object getValue(CpsScript script) throws Exception {   
        final Binding binding = script.getBinding();
        final Object loadedObject;
        if (binding.hasVariable(getName())) {
            loadedObject = binding.getVariable(getName());
        } else {
            // Note that if this were a method rather than a constructor, we would need to mark it @NonCPS lest it throw CpsCallableInvocation.
            loadedObject = script.getClass().getClassLoader().loadClass(getClassName()).getConstructor(CpsScript.class).newInstance(script);
            binding.setVariable(getName(), loadedObject);
        }
        return loadedObject;
    }
}
