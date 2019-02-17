package ca.jahed.papyrusrt.remote;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.osgi.framework.Bundle;

public class PapyrusrtLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
	
	@Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {

		String serverURL = configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_SERVER_URL, "");	
		String modelPath = configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_MODEL_FILE, "");	
		String programArgs = configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_PROGRAM_ARGS, "");	
		
		if(!modelPath.isEmpty()) {
			try {
				Bundle bundle = Platform.getBundle("ca.jahed.papyrusrt.remote");
				File executor = new File(FileLocator.toFileURL(FileLocator.find(bundle, 
						new Path("tools"+Path.SEPARATOR+"executor.jar"), null)).toURI());
				
				ProcessBuilder builder = new ProcessBuilder("java", "-jar", executor.getAbsolutePath(), serverURL, modelPath, programArgs, "-Djavax.net.debug=all");
				Process executorProcess = builder.start();
				DebugPlugin.newProcess(launch, executorProcess, "executor");
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
    }
}
