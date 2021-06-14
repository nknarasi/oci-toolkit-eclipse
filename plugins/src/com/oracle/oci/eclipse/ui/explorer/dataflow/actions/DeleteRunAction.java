package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.dialogs.MessageDialog;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.DeleteRunRequest;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

public class DeleteRunAction extends BaseAction {
    
	private RunTable runTable;
	private RunSummary rs;
	
	public DeleteRunAction(RunSummary rs,RunTable runTable) {
        setText("Cancel Run");
		this.runTable=runTable;
		this.rs=rs;
    }

    @Override
    protected void runAction() {
    	
    	boolean result = MessageDialog.openConfirm(runTable.getShell(), "Confirm", "Please confirm");
		if (result){
			try{
			DataFlowClient client = RunClient.getInstance().getDataFlowClient();
			DeleteRunRequest deleteRunRequest = DeleteRunRequest.builder()
					.runId(this.rs.getId())
					.opcRequestId(this.rs.getOpcRequestId())
					.build();
			        client.deleteRun(deleteRunRequest);
			MessageDialog.openInformation(runTable.getShell(), "Successful", "Run Cancelling...");
			runTable.refresh(true);
			}
			catch (Exception e){
				MessageDialog.openError(runTable.getShell(), "Failed to Cancel Run ", e.getMessage());
			}
		} else {
			// Cancel Button selected do something
		}
    }
}
