package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.List;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.EditApplicationWizard;

public class EditApplicationAction extends BaseAction{	
	   private final ApplicationTable table;
	    private final List<ApplicationSummary> applicationSelectionList;
	    private String applicationID;
	    private String title = "Edit Application";
	    
	    @SuppressWarnings("unchecked")
		public EditApplicationAction (ApplicationTable table){
	        this.table = table;
	        applicationSelectionList = (List<ApplicationSummary>) table.getSelectedObjects();
	    }
	    @Override
	    public String getText() {
	        if ( applicationSelectionList.size() == 1 ) {
	            return title;
	        }
	        return "";
	    }
	 @Override
	 protected void runAction() {
		 if (applicationSelectionList.size() > 0) {
	        	ApplicationSummary object = applicationSelectionList.get(0);
	        	applicationID = object.getId();
	        }
		 Application application = ApplicationClient.getInstance().getApplicationDetails(applicationID);
	       CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new EditApplicationWizard(application.getId()));
	        dialog.setFinishButtonText("Create");
	        if (Window.OK == dialog.open()) {
	        	table.refresh(true);
	        }  		 
	    }
}
