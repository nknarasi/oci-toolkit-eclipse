package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.RunLogWizard;

public class ShowLogsAction extends BaseAction {
    
	private String runId;
	
	public ShowLogsAction(String runId) {
        setText("Show Logs");
		this.runId=runId;
    }

    @Override
    protected void runAction() {
    	CustomWizardDialog dialog;
        dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new RunLogWizard(runId));
        dialog.setFinishButtonText("Download");
        dialog.setCancelButtonText("Exit");
        if (Window.OK == dialog.open()) {}
    }
}