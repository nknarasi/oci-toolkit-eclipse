package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;

public class GetApplications implements IRunnableWithProgress{

	private DataFlowClient dataflowClient=ApplicationClient.getInstance().getDataFlowClient();
	private String compartmentId;
	private ListApplicationsRequest.SortBy sortBy;
	private ListApplicationsRequest.SortOrder sortOrder;
	private String page=null;
	public List<ApplicationSummary> applicationSummaryList = new ArrayList<ApplicationSummary>();
	public ListApplicationsResponse listApplicationsResponse;
	
	   public GetApplications(String givenCompartmentId,ListApplicationsRequest.SortBy sortBy,ListApplicationsRequest.SortOrder sortOrder,String page)
	    {
	        this.compartmentId=givenCompartmentId;
	        this.sortBy=sortBy;
	        this.sortOrder=sortOrder;
	        this.page=page;
	    }
	   
	    @Override
	    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{
	        monitor.beginTask("Getting Applications", IProgressMonitor.UNKNOWN);
	   		applicationSummaryList = new ArrayList<ApplicationSummary>();	   			   		
	   		ListApplicationsRequest.Builder listApplicationsBuilder =  ListApplicationsRequest.builder()
	        		 .compartmentId(compartmentId)
	        		 .sortBy(sortBy)
	        		 .sortOrder(sortOrder)
	        		 .limit(20)
	        		 .page(page);
	             try {
	            	 listApplicationsResponse =
	                 dataflowClient.listApplications(listApplicationsBuilder.build());
	                 applicationSummaryList.addAll(listApplicationsResponse.getItems());
	             } catch(Throwable e) {
	                 ErrorHandler.logError("Unable to list applications: " + e.getMessage());
	             }
	        monitor.done();
	    }
}
