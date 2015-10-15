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
package org.jenkinsci.plugins.workflow.remoteloader.library;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import groovy.lang.Binding;
import groovy.lang.Script;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * Workflow Step that loads scripts and classes into your workflow from a remote git repository.
 * Similar to how CPS Global Library works.
 *
 * @author Robert Sandell &lt;rsandell@cloudbees.com&gt;.
 */
public class UseRemoteLibraryStep extends AbstractStepImpl implements Serializable {

    private static final String CLASSPATH_DIR = "src";
    private static final String VARS_PREFIX = "vars";
    private final String url;
    private String branch = "master";
    private String credentialsId;

    @DataBoundConstructor
    public UseRemoteLibraryStep(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getBranch() {
        return branch;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setBranch(String branch) {
        this.branch = branch;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmpty(credentialsId);
    }

    public static final class StepExecutionImpl extends AbstractSynchronousNonBlockingStepExecution<Void> {

        @Inject
        private transient UseRemoteLibraryStep step;
        @StepContextParameter
        private transient Run<?, ?> run;
        @StepContextParameter
        private transient TaskListener listener;
        @StepContextParameter
        private transient CpsFlowExecution flow;

        @Override
        protected Void run() throws Exception {
            FilePath ws = locateWorkArea(step.getUrl(), run);
            //TODO bare checkout in job dir and reference checkout to branch in job dir to save space
            checkout(ws);
            flow.getShell().getClassLoader().addURL(ws.child(CLASSPATH_DIR).toURI().toURL());
            Binding binding = flow.getShell().getContext();
            FilePath[] filePaths = ws.child(VARS_PREFIX).list("*.groovy");
            for (FilePath file : filePaths) {
                String name = file.getBaseName();
                if (!binding.hasVariable(name)) {
                    Script instance = flow.getShell().parse(new File(file.getRemote()));
                    binding.setVariable(name, instance);
                } else {
                    listener.getLogger().println("WARNING: A variable named " + name + " is already bound to the workflow context, " +
                            "ignoring implementation in remote library.");
                }
            }

            return null;
        }

        private void checkout(FilePath ws) throws IOException, InterruptedException, URISyntaxException {
            GitClient git = createGitClient(ws);
            String remoteName = "origin";
            List<RefSpec> refspecs = Collections.singletonList(new RefSpec("+refs/heads/*:refs/remotes/" + remoteName + "/*"));
            if (!git.hasGitRepo()) {
                git.clone_().url(step.getUrl()).repositoryName(remoteName).refspecs(refspecs).execute();
            } else {
                //TODO this will never happen until shared reference clone is implemented
                git.fetch_().from(new URIish(step.getUrl()), refspecs).tags(true).prune().execute();
            }
            git.checkout().ref(remoteName + "/" + step.getBranch()).execute();
        }

        private FilePath locateWorkArea(String url, Run<?, ?> run) {
            String hex = DigestUtils.sha1Hex(DigestUtils.sha1(url));
            return new FilePath(run.getRootDir()).child("wf-remote-libraries").child(hex);
        }

        private GitClient createGitClient(FilePath ws) throws IOException, InterruptedException {
            EnvVars environment = run.getEnvironment(listener);
            Git git = Git.with(listener, environment).in(ws);
            GitClient c = git.getClient();
            if (Util.fixEmpty(step.getCredentialsId()) != null) {
                StandardUsernameCredentials credentials = CredentialsMatchers
                        .firstOrNull(
                                CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, run.getParent(),
                                        ACL.SYSTEM, URIRequirementBuilder.fromUri(step.getUrl()).build()),
                                CredentialsMatchers.allOf(CredentialsMatchers.withId(step.getCredentialsId()),
                                        GitClient.CREDENTIALS_MATCHER));
                if (credentials != null) {
                    c.addCredentials(step.getUrl(), credentials);
                } else {
                    throw new IOException("Could not find credentials with id " + step.getCredentialsId() + " for url " + step.getUrl());
                }
            }
            return c;
        }

        private static final long serialVersionUID = 1L;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(StepExecutionImpl.class);
        }

        @Override
        public String getFunctionName() {
            return "useRemoteLibrary";
        }

        @Override
        public String getDisplayName() {
            return "Load a Workflow Library from a remote Git repository";
        }

        private static final long serialVersionUID = 1L;
    }
}
