package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.requests.ListRunsRequest.Builder;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.RunClient;

public class GetRuns implements IRunnableWithProgress{
	
	DataFlowClient dataflowClient=RunClient.getInstance().getDataFlowClient();
	String compid;
	ListRunsRequest.SortBy sortBy;
	ListRunsRequest.SortOrder sortOrder;
	String page=null;
	public List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
	public ListRunsResponse listrunsresponse;
    public GetRuns(String compid,ListRunsRequest.SortBy sortBy,ListRunsRequest.SortOrder sortOrder,String page)
    {
        this.compid=compid;
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
   		runSummaryList = new ArrayList<RunSummary>();
   		
   		
         Builder listRunsBuilder =  ListRunsRequest.builder()
        		 .compartmentId(AuthProvider.getInstance().getCompartmentId()).sortBy(sortBy).sortOrder(sortOrder).limit(20).page(page);
             try {
                 listrunsresponse =dataflowClient.listRuns(listRunsBuilder.build());
                 runSummaryList.addAll(listrunsresponse.getItems());
             } catch(Throwable e) {
                 MessageDialog.openInformation(Display.getDefault().getActiveShell(),"Unable to get Runs",e.getMessage());
             }
        

        // You are done
        monitor.done();
    }
}

