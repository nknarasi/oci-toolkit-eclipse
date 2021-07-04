package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class GetPrivateEndpoints implements IRunnableWithProgress{
	
	private String page=null;
	private static String compid=IdentClient.getInstance().getRootCompartment().getId();
	public List<PrivateEndpointSummary> pepSummaryList = new ArrayList<PrivateEndpointSummary>();
	public ListPrivateEndpointsResponse listpepsresponse;
	private String errorMessage=null;
	
    public GetPrivateEndpoints(String page)
    {
        this.page=page;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
    	try {
        // Tell the user what you are doing
        monitor.beginTask("Getting Private Endpoints", IProgressMonitor.UNKNOWN);

        // Do your work
        
        String currentcompid=AuthProvider.getInstance().getCompartmentId();
        if(currentcompid!=null&&!compid.equals(currentcompid)) {
         	compid=currentcompid;
         	page=null;
         }
        
   		pepSummaryList = new ArrayList<PrivateEndpointSummary>();
            Object[] getPrivateEndpoints=DataflowClient.getInstance().getPrivateEndPoints(compid, 20, page);
            pepSummaryList=(List<PrivateEndpointSummary>)getPrivateEndpoints[0];
            listpepsresponse=(ListPrivateEndpointsResponse)getPrivateEndpoints[1];

        // You are done
        monitor.done();
    	 } 
        catch(Exception e) {
            MessageDialog.openInformation(Display.getDefault().getActiveShell(),"Unable to get Private Endpoints",e.getMessage());
        }
    }
    
    public String getCompid() {
    	return compid;
    }
    
    public String getErrorMessage() {
    	return errorMessage;
    }
}


