package ca.jahed.papyrusrt.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PapyrusrtLaunchTab extends AbstractLaunchConfigurationTab implements ModifyListener {

	private Text modelFile;
	private Text programArgs;
	private Text serverURL;

    @Override
    public void createControl(Composite parent) {

        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);

        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);

        Label modelFileLabel = new Label(comp, SWT.NONE);
        modelFileLabel.setText("Model file:");
        GridDataFactory.swtDefaults().applyTo(modelFileLabel);

        modelFile = new Text(comp, SWT.BORDER);
        modelFile.addModifyListener(this);
        modelFile.setMessage("model.uml");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(modelFile);
        
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);

        Label programArgsLabel = new Label(comp, SWT.NONE);
        programArgsLabel.setText("Program arguments:");
        GridDataFactory.swtDefaults().applyTo(programArgsLabel);

        programArgs = new Text(comp, SWT.BORDER);
        programArgs.addModifyListener(this);
        programArgs.setMessage("args");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(programArgs);
        
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);

        Label serverUrlLabel = new Label(comp, SWT.NONE);
        serverUrlLabel.setText("Server URL:");
        GridDataFactory.swtDefaults().applyTo(serverUrlLabel);

        serverURL = new Text(comp, SWT.BORDER);
        serverURL.addModifyListener(this);
        serverURL.setMessage("http://");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(serverURL);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
        		modelFile.setText(configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_MODEL_FILE, ""));
        		programArgs.setText(configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_PROGRAM_ARGS, ""));
        		serverURL.setText(configuration.getAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_SERVER_URL, ""));
        } catch (CoreException e) {}
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_MODEL_FILE, modelFile.getText());
    		configuration.setAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_PROGRAM_ARGS, programArgs.getText());
    		configuration.setAttribute(PapyrusrtLaunchConfigurationAttributes.ATTR_SERVER_URL, serverURL.getText());
    }

    @Override
    public String getName() {
        return "Configuration";
    }

	@Override
	public void modifyText(ModifyEvent arg0) {
		updateLaunchConfigurationDialog();
	}

}
