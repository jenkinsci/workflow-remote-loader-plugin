package org.jenkinsci.plugins.workflow.remoteloader.library;

import groovy.lang.Script;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Queue;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert Sandell &lt;rsandell@cloudbees.com&gt;.
 */
@Extension
public class GlobalVariableSet extends org.jenkinsci.plugins.workflow.cps.GlobalVariableSet {
    @Override
    public Iterator<GlobalVariable> iterator() {
        CpsFlowExecution execution = CpsThread.current().getExecution();

        List<GlobalVariable> list = new LinkedList<GlobalVariable>();

        try {
            Queue.Executable executable = execution.getOwner().getExecutable();
            if (executable instanceof WorkflowRun) {
                WorkflowRun run = (WorkflowRun)executable;
                FilePath ws = UseRemoteLibraryStep.getLibraryRootFor(run);
                if (ws != null && ws.exists() && ws.isDirectory()) {
                    for (FilePath path : ws.listDirectories()) {
                        FilePath[] filePaths = path.child(UseRemoteLibraryStep.VARS_PREFIX).list("*.groovy");

                        for (FilePath file : filePaths) {
                            list.add(new Variable(file));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return list.iterator();
    }

    static class Variable extends GlobalVariable {

        private final FilePath file;

        Variable(FilePath file) {
            this.file = file;
        }

        @Nonnull
        @Override
        public String getName() {
            return file.getBaseName();
        }

        @Nonnull
        @Override
        public Object getValue(@Nonnull CpsScript script) throws Exception {
            return script.evaluate(new File(file.getRemote()));
        }
    }
}
