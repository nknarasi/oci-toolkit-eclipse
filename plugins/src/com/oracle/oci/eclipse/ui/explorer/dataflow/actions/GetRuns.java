package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class GetRuns implements IRunnableWithProgress{
	
	private ListRunsRequest.SortBy sortBy;
	private ListRunsRequest.SortOrder sortOrder;
	private String page=null;
	public List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
	public ListRunsResponse listrunsresponse;
	
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
        Object[] getRuns=DataflowClient.getInstance().getRuns(AuthProvider.getInstance().getCompartmentId(), sortBy, sortOrder, 20, page);
        runSummaryList=(List<RunSummary>)getRuns[0];
        listrunsresponse=(ListRunsResponse)getRuns[1];
        }
        catch (Exception e) {
        	MessageDialog.openInformation(Display.getDefault().getActiveShell(),"Unable to get Runs",e.getMessage());
        }
        
        // You are done
        monitor.done();
    }
}

