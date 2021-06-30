package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.requests.ListPrivateEndpointsRequest;
import com.oracle.bmc.dataflow.requests.ListPrivateEndpointsRequest.Builder;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;

public class GetPrivateEndpoints implements IRunnableWithProgress{
	
	private DataFlowClient dataflowClient=PrivateEndPointsClient.getInstance().getDataFLowClient();
	private String compid;
	private String page=null;
	public List<PrivateEndpointSummary> pepSummaryList = new ArrayList<PrivateEndpointSummary>();
	public ListPrivateEndpointsResponse listpepsresponse;
	
    public GetPrivateEndpoints(String compid,String page)
    {
        this.compid=compid;
        this.page=page;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Getting Private Endpoints", IProgressMonitor.UNKNOWN);

        // Do your work
   		pepSummaryList = new ArrayList<PrivateEndpointSummary>();
   		
   		
         Builder listRunsBuilder =  ListPrivateEndpointsRequest.builder()
        		 .compartmentId(AuthProvider.getInstance().getCompartmentId()).limit(20).page(page);
             try {
                 listpepsresponse =dataflowClient.listPrivateEndpoints(listRunsBuilder.build());
                 pepSummaryList.addAll(listpepsresponse.getPrivateEndpointCollection().getItems());
             } catch(Throwable e) {
                 MessageDialog.openInformation(Display.getDefault().getActiveShell(),"Unable to get Private Endpoints",e.getMessage());
             }
        

        // You are done
        monitor.done();
    }
}


