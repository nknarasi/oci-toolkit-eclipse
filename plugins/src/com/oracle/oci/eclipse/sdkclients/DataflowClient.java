package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.model.Run;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.requests.CreateApplicationRequest;
import com.oracle.bmc.dataflow.requests.CreateRunRequest;
import com.oracle.bmc.dataflow.requests.DeleteApplicationRequest;
import com.oracle.bmc.dataflow.requests.GetApplicationRequest;
import com.oracle.bmc.dataflow.requests.GetPrivateEndpointRequest;
import com.oracle.bmc.dataflow.requests.GetRunRequest;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.requests.ListPrivateEndpointsRequest;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.requests.UpdateApplicationRequest;
import com.oracle.bmc.dataflow.responses.CreateApplicationResponse;
import com.oracle.bmc.dataflow.responses.CreateRunResponse;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.bmc.dataflow.responses.UpdateApplicationResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class DataflowClient extends BaseClient {
	
    private static DataflowClient single_instance = null;
    private static DataFlowClient dataflowClient;

    private DataflowClient() {
        if (dataflowClient == null) {
        	dataflowClient = createDataflowClient();
        }
    }

    public static DataflowClient getInstance() {
        if (single_instance == null) {
            single_instance = new DataflowClient();
        }
        return single_instance;
    }

    private DataFlowClient createDataflowClient(){
        dataflowClient = new DataFlowClient(AuthProvider.getInstance().getProvider());
        dataflowClient.setRegion(AuthProvider.getInstance().getRegion());
        return dataflowClient;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        dataflowClient.setRegion(evt.getNewValue().toString());
    }

    
    @Override
    public void updateClient() {
        close();
        createDataflowClient();
    }
   
    public DataFlowClient getDataFlowClient() {
        return dataflowClient;
    }
    
    @Override
    public void close() {
        try {
            if (dataflowClient != null) {
                dataflowClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }
  
    
    public List<ApplicationSummary> getApplications(String CompartmentId,ListApplicationsRequest.SortBy s,ListApplicationsRequest.SortOrder so){    	
        String nextToken = null;
   		List<ApplicationSummary> applicationssummary = new ArrayList<ApplicationSummary>();
   		if(CompartmentId == null){
   			CompartmentId= AuthProvider.getInstance().getCompartmentId();
   		}   		
   		ListApplicationsRequest.Builder listApplicationsBuilder =  ListApplicationsRequest.builder()
        		 .compartmentId(CompartmentId).sortBy(s).sortOrder(so);
         do {
             listApplicationsBuilder.page(nextToken);
             try {
                 ListApplicationsResponse listapplicationresponse =
                         dataflowClient.listApplications(listApplicationsBuilder.build());
                 
                 applicationssummary.addAll(listapplicationresponse.getItems());
                 nextToken = listapplicationresponse.getOpcNextPage();
             } catch(Throwable e) {
                 ErrorHandler.logError("Unable to list applications: " + e.getMessage());
             }
         } while (nextToken != null);               	
        return applicationssummary;
    }
    
    public Application getApplicationDetails(final String applicationId) {    	
        return dataflowClient.getApplication(
        		GetApplicationRequest.builder()
        		.applicationId(applicationId)
        		.build())
        		.getApplication();
    }
    
    public Application createApplication(final CreateApplicationDetails request) {
        CreateApplicationResponse response =
                dataflowClient.createApplication(
                        CreateApplicationRequest.builder()
                        .createApplicationDetails(request)
                        .build());
        
        return response.getApplication();
        
    }
    
    public void deleteApplication(String ApplicationId) throws Exception {
        dataflowClient.deleteApplication(DeleteApplicationRequest.builder().applicationId(ApplicationId).build());
        ErrorHandler.logInfo("Application deleted: " + ApplicationId);
    }

    public void runApplication(final CreateRunDetails request) {    	
        CreateRunResponse response = dataflowClient.createRun(
        		CreateRunRequest.builder()
        		.createRunDetails(request)
        		.build());   
    }
        
    public Application editApplication(String applicationId,final UpdateApplicationDetails request ) {
    	UpdateApplicationResponse response=	dataflowClient.updateApplication(UpdateApplicationRequest.builder()
    			.applicationId(applicationId)
    			.updateApplicationDetails(request)
    			.build()) ;
    	return response.getApplication();
    	
    }
    
   public Object[] getRuns(String CompartmentId,ListRunsRequest.SortBy s,ListRunsRequest.SortOrder so,int limit,String page) throws Exception {

   		List<RunSummary> runssummary = new ArrayList<RunSummary>();
   		
   		
   		ListRunsRequest.Builder listRunsBuilder =  ListRunsRequest.builder()
   				.compartmentId(CompartmentId).sortBy(s).sortOrder(so).limit(limit).page(page);
        ListRunsResponse listrunsresponse =dataflowClient.listRuns(listRunsBuilder.build());
                 
        runssummary.addAll(listrunsresponse.getItems());
       	
        return new Object[] {runssummary,listrunsresponse};
    }
    
    
	public Run getRunDetails(String runid) throws Exception {		 
		 GetRunRequest req = GetRunRequest.builder().runId(runid).build();
		 return dataflowClient.getRun(req).getRun();
	 }
	
    public static List<PrivateEndpointSummary> getPrivateEndPoints(String CompartmentId) {

   		List<PrivateEndpointSummary> privateEndpointsummary = new ArrayList<PrivateEndpointSummary>();  
   		
   		ListPrivateEndpointsRequest.Builder listPrivateEndpointsBuilder =  ListPrivateEndpointsRequest.builder()
        		 .compartmentId(CompartmentId);
   		
        ListPrivateEndpointsResponse listPrivateEndpointresponse =
                         dataflowClient.listPrivateEndpoints(listPrivateEndpointsBuilder.build());  
        
        privateEndpointsummary.addAll(listPrivateEndpointresponse.getPrivateEndpointCollection().getItems());
        
        return privateEndpointsummary;
    }
    
    public Object[] getPrivateEndPoints(String CompartmentId,int limit,String page) {

   		List<PrivateEndpointSummary> privateEndpointSummary = new ArrayList<PrivateEndpointSummary>();  
   		
   		ListPrivateEndpointsRequest.Builder listPrivateEndpointsBuilder =  ListPrivateEndpointsRequest.builder()
        		 .compartmentId(CompartmentId).limit(limit).page(page);
   		
        ListPrivateEndpointsResponse listPrivateEndpointResponse =
                         dataflowClient.listPrivateEndpoints(listPrivateEndpointsBuilder.build());  
        
        privateEndpointSummary.addAll(listPrivateEndpointResponse.getPrivateEndpointCollection().getItems());
        
        return new Object[] {privateEndpointSummary,listPrivateEndpointResponse};
    }
    
    public PrivateEndpoint getPrivateEndpointDetails (String PrivateEndpointId) {
        try {
        	return dataflowClient.getPrivateEndpoint(
        			GetPrivateEndpointRequest.builder()
        		 	.privateEndpointId(PrivateEndpointId)
        			.build()).getPrivateEndpoint();  
           
        } catch(Throwable e) {
        	MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to get Private EndpointDetails details ", e.getMessage());	          
            return null;
        }
			
    }
	
    

}
