package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class GetRuns implements IRunnableWithProgress{
	
	private ListRunsRequest.SortBy sortBy;
	private ListRunsRequest.SortOrder sortOrder;
	private String page=null;
	private static String compid=IdentClient.getInstance().getRootCompartment().getId();
	public List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
	public ListRunsResponse listrunsresponse;
	private String errorMessage=null;
	
    public GetRuns(ListRunsRequest.SortBy sortBy,ListRunsRequest.SortOrder sortOrder,String page)
    {
        this.sortBy=sortBy;
        this.sortOrder=sortOrder;
        this.page=page;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Getting Runs", IProgressMonitor.UNKNOWN);

        // Do your work
        try {
        	
       String currentcompid=AuthProvider.getInstance().getCompartmentId();
       if(currentcompid!=null&&!compid.equals(currentcompid)) {
        	compid=currentcompid;
        	page=null;
        }
        Object[] getRuns=DataflowClient.getInstance().getRuns(compid, sortBy, sortOrder, 20, page);
        runSummaryList=(List<RunSummary>)getRuns[0];
        listrunsresponse=(ListRunsResponse)getRuns[1];
        }
        catch (Exception e) {
        	errorMessage=e.getMessage();
        }
        
        // You are done
        monitor.done();
    }
    
    public String getErrorMessage() {
    	return errorMessage;
    }
}

