package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.requests.DeletePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;


public class DeletePrivateEndpointAction extends BaseAction {
    
	private PrivateEndpointTable pepTable;
	private PrivateEndpointSummary pep;
	
	public DeletePrivateEndpointAction(PrivateEndpointTable pepTable,PrivateEndpointSummary pep) {
        setText("Delete Private Endpoint");
		this.pepTable=pepTable;
		this.pep=pep;
    }

    @Override
    protected void runAction() {
    	if(pep.getLifecycleState().toString()=="Creating") {
    		
    		MessageDialog.openInformation(pepTable.getShell(), "Failed to Delete Private Endpoint", "Still in the create state");
    		return;
    	}
    	boolean result = MessageDialog.openConfirm(pepTable.getShell(), "Please Confirm", "Are you sure you want to delete "+pep.getDisplayName()+".");
    	
		if (result){
			try{
			DataFlowClient client = PrivateEndPointsClient.getInstance().getDataFLowClient();
			DeletePrivateEndpointRequest deletePrivateEndpointRequest = DeletePrivateEndpointRequest.builder()
			.privateEndpointId(pep.getId())
			.build();

			client.deletePrivateEndpoint(deletePrivateEndpointRequest);
			MessageDialog.openInformation(pepTable.getShell(), "Successful", "Private Endpoint Deleting...");
			pepTable.refresh(true);
			}
			catch (Exception e){
				MessageDialog.openError(pepTable.getShell(), "Failed to Delete Private Endpoint ", e.getMessage());
			}
		} else {
			// Cancel Button selected do something
		}
    }
}