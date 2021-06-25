package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.requests.ListRunsRequest.Builder;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class GetRuns implements IRunnableWithProgress{
	
	DataFlowClient dataflowClient=RunClient.getInstance().getDataFlowClient();
	String compid;
	ListRunsRequest.SortBy s;
	ListRunsRequest.SortOrder so;
	String page=null;
	public List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
	public ListRunsResponse listrunsresponse;
    public GetRuns(String compid,ListRunsRequest.SortBy s,ListRunsRequest.SortOrder so,String page)
    {
        this.compid=compid;
        this.s=s;
        this.so=so;
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
        		 .compartmentId(AuthProvider.getInstance().getCompartmentId()).sortBy(s).sortOrder(so).limit(20).page(page);
             try {
                 listrunsresponse =
                         dataflowClient.listRuns(listRunsBuilder.build());
                 runSummaryList.addAll(listrunsresponse.getItems());
             } catch(Throwable e) {
                 ErrorHandler.logError("Unable to list runs: " + e.getMessage());
             }
        

        // You are done
        monitor.done();
    }
}

