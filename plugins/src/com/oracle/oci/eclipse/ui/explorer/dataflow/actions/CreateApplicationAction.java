package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.CreateApplicationWizard;

public class CreateApplicationAction extends BaseAction  {	
	 private final ApplicationTable table;
	 private final String COMPARTMENT_ID;
	 public CreateApplicationAction(ApplicationTable table,String COMPARTMENT_ID)  {
		 	this.table = table;
		 	this.COMPARTMENT_ID = COMPARTMENT_ID;
	        setText("Create Applications");
	    }

	 @Override
	 protected void runAction() {		 
	       CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new CreateApplicationWizard(COMPARTMENT_ID));
	        dialog.setFinishButtonText("Create");
	        if (Window.OK == dialog.open()) {
	        	table.refresh(true);
	        }	        	
	    }
}
