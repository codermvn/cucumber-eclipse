package cucumber.eclipse.launching;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.osgi.framework.Bundle;

import cucumber.eclipse.editor.Activator;

public class CucumberFeatureLocalApplicationLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate2 {

	

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IVMInstall vm = verifyVMInstall(config);
		IVMRunner runner = vm.getVMRunner(mode);
		String[] classpath = getClasspathAndModulepath(config)[0];
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(CucumberFeatureLaunchConstants.CUCUMBER_API_CLI_MAIN, classpath);

		verifyWorkingDirectory(config);

		String[] bootpath = getBootpath(config);
		runConfig.setBootClassPath(bootpath);
		runConfig.setVMArguments(DebugPlugin.parseArguments(getVMArguments(config)));
		runConfig.setWorkingDirectory(getWorkingDirectory(config).getAbsolutePath());
		
		String featurePath = "" ;
		String gluePath = "";
		boolean isMonochrome = false;
		boolean isPretty = false;
		boolean isProgress= false;
		boolean isJunit= false;
		boolean isJson= false;
		boolean isHtml= false;
		boolean isRerun= false;
		boolean isUsage= false;
		boolean isVersion= false;
		
		featurePath = substituteVar(config.getAttribute(CucumberFeatureLaunchConstants.ATTR_FEATURE_PATH, featurePath));
		gluePath  = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_GLUE_PATH, gluePath);
		isMonochrome = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_MONOCHROME, isMonochrome);
		isPretty = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PRETTY,isPretty);
		isProgress = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_PROGRESS, isProgress);
		isJunit = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JUNIT, isJunit);
		isJson = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_JSON, isJson);
		isHtml = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_HTML, isHtml);
		isRerun = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_RERUN, isRerun);
		isUsage = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_USAGE, isUsage);
		isVersion = config.getAttribute(CucumberFeatureLaunchConstants.ATTR_IS_VERSION, isVersion);
		
		System.out.println("Launching ....................... " + featurePath);
		System.out.println("Glueing ....................... " + gluePath);
		System.out.println("is monochrome.................." + isMonochrome);
		System.out.println("is pretty.................." + isPretty);
		System.out.println("is progress.................." + isProgress);
		System.out.println("is html.................." + isHtml);
		System.out.println("is json.................." + isJson);
		System.out.println("is junit.................." + isJunit);
		System.out.println("is usage.................." + isUsage);
		System.out.println("is rerun.................." + isRerun);
		System.out.println("is version.................." + isVersion);
		
		
		String glue = "--glue";
		String formatter = "--plugin"; // Cucumber-JVM's --format option is deprecated. Please use --plugin instead.
		Collection<String> args = new ArrayList<String>();
		//String[] args = new String[6];
		args.add(featurePath);
		args.add(glue);
		args.add(gluePath);
		
		if (isPretty) {
			args.add(formatter);
			args.add("pretty");	
		}
		
		if (isJson) {
			args.add(formatter);
			args.add("json");	
		}
		
		if (isJunit) {
			IPath path = new Path(featurePath);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			
			IFile resource = root.findFilesForLocation(path)[0];
			String out = resource.getProject().getRawLocation().makeAbsolute().toString()+"/target/test-results.xml";
			
			args.add(formatter);
			args.add("junit:" + out);	
		}
		
		if (isProgress) {
			args.add(formatter);
			args.add("progress");	
		}
		
		if (isRerun) {
			args.add(formatter);
			args.add("rerun");	
		}
		
		if (isHtml) {
			args.add(formatter);
			args.add("html:target");	
		}
		
		if (isUsage) {
			args.add(formatter);
			args.add("usage");	
		}
		
		if (isVersion) {
			args.add("--version");	
		}
		
		if (isMonochrome) args.add("--monochrome");

		args.addAll(Arrays.asList(DebugPlugin.parseArguments(getProgramArguments(config))));
		
		runConfig.setProgramArguments(args.toArray(new String[0]));
		runner.run(runConfig, launch, monitor);

	}
	
	/**
	 * Substitute any variable
	 */
	private static String substituteVar(String s) {
		if (s == null) {
			return s;
		}
		try {
			return VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(s);
		} catch (CoreException e) {
			System.out.println("Could not substitute variable " + s);
			return null;
		}
	}

}
