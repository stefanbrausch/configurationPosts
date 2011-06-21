package hudson.plugins.logfilesizechecker;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;

import java.io.IOException;
import java.util.TimerTask;

import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link BuildWrapper} that terminates a build if it's log file size too big.
 *
 * @author Stefan Brausch
 */
public class LogfilesizecheckerWrapper extends BuildWrapper {
	/**
	 * If the build log file size bigger than this amount of mBytes, it will be
	 * terminated.
	 */

	public Environment setUp(final AbstractBuild build, Launcher launcher,
			final BuildListener listener) throws IOException,
			InterruptedException {
		class EnvironmentImpl extends Environment {
			private final TimerTask logtask;

			public EnvironmentImpl() {

				logtask = new SafeTimerTask() {
					public void doRun() {
						Executor e = build.getExecutor();
						if (e != null) {
							if (build.getLogFile().length() > DESCRIPTOR.maxLogSizeBytes * 1024L * 1024L) {
								if (!e.isInterrupted()) {
									listener
											.getLogger()
											.println(
													">>> Max Log Size reached. Aborting <<<");

									e.interrupt();
								}
								build.setResult(Result.FAILURE);

							}
						}
					}
				};
				if (DESCRIPTOR.maxLogSizeBytes > 0)
					Trigger.timer.scheduleAtFixedRate(logtask, 10000L, 10000L);
			}

			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {

				if (DESCRIPTOR.maxLogSizeBytes > 0)
					logtask.cancel();

				return true;
			}
		}

		listener.getLogger().println(
				"Executor: " + build.getExecutor().getNumber());
		return new EnvironmentImpl();
	}

	public Descriptor<BuildWrapper> getDescriptor() {
		return DESCRIPTOR;
	}

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		public int maxLogSizeBytes;

		DescriptorImpl() {
			super(LogfilesizecheckerWrapper.class);
			load();
		}

		public String getDisplayName() {
			return "Abort the build if it's log file size too big";
		}

		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		public boolean configure(StaplerRequest req) throws FormException {

			String size = req.getParameter("logfilesizechecker.maxLogSizeBytes");
			if (size != null)
				maxLogSizeBytes = Integer.parseInt(size);
			else
				maxLogSizeBytes = 0;

			save();
			return super.configure(req);
		}

		public int maxLogSizeBytes() {
			return maxLogSizeBytes;
		}

		public LogfilesizecheckerWrapper newInstance(StaplerRequest req)
				throws Descriptor.FormException {
			LogfilesizecheckerWrapper w = new LogfilesizecheckerWrapper();
			req.bindParameters(w, "logfilesizechecker.");
			return w;
		}
	}

}
