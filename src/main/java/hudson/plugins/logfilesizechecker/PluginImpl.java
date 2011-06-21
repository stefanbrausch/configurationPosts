package hudson.plugins.logfilesizechecker;

import hudson.Plugin;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildWrappers;

/**
 * @plugin
 * @author Stefan Brausch
 */
public class PluginImpl extends Plugin {
    public void start() {
        BuildWrappers.WRAPPERS.add(LogfilesizecheckerWrapper.DESCRIPTOR);
    }
}
