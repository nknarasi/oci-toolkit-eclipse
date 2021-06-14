package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;

public class DeleteApplicationAction extends BaseAction{
	
	   private final ApplicationTable table;
	    private final List<ApplicationSummary> applicationSelectionList;
	    private String applicationID;
	    private String title = "Delete Application";
	    
	    @SuppressWarnings("unchecked")
		public DeleteApplicationAction (ApplicationTable table){
	        this.table = table;
	        applicationSelectionList = (List<ApplicationSummary>)table.getSelectedObjects();
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
		 String title = "Delete Dataflow Application Bucket";
		 String message = "Are you sure you want to delete Application: " + application.getDisplayName();
	     Dialog dialog =  new MessageDialog(Display.getDefault().getActiveShell(), title, null, message, MessageDialog.QUESTION, new String[] {"Yes","No"}, 1);
	        if (dialog.open() != Dialog.OK) {
	            return;
	        }
	        new Job("Deleting Application") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                try {
	                    ApplicationClient.getInstance().deleteApplication(application.getId());
	                    table.refresh(true);
	                    return Status.OK_STATUS;
	                } catch (Exception e) {
	                    return ErrorHandler.reportException("Error: Unable to delete Application. " + e.getMessage(), e);
	                }
	            }
	        }.schedule();     
	}
}
