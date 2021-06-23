package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.UpdateRunDetails;
import com.oracle.bmc.dataflow.requests.CreateApplicationRequest;
import com.oracle.bmc.dataflow.requests.CreateRunRequest;
import com.oracle.bmc.dataflow.requests.DeleteApplicationRequest;
import com.oracle.bmc.dataflow.requests.GetApplicationRequest;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest.Builder;
import com.oracle.bmc.dataflow.requests.UpdateApplicationRequest;
import com.oracle.bmc.dataflow.responses.CreateApplicationResponse;
import com.oracle.bmc.dataflow.responses.CreateRunResponse;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.bmc.dataflow.responses.UpdateApplicationResponse;
import com.oracle.bmc.dataflow.responses.UpdateRunResponse;

public class ApplicationClient extends BaseClient {

    private static ApplicationClient single_instance = null;
    private static DataFlowClient dataflowClient;

    private ApplicationClient() {
        if (dataflowClient == null) {
        	dataflowClient = createApplicationClient();
        }
    }

    public static ApplicationClient getInstance() {
        if (single_instance == null) {
            single_instance = new ApplicationClient();
        }
        return single_instance;
    }

    private DataFlowClient createApplicationClient(){
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
        createApplicationClient();
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
  
    public List<ApplicationSummary> getApplications() throws Exception {   	
        String nextToken = null;
   		List<ApplicationSummary> applicationssummary = new ArrayList<ApplicationSummary>(); 		
         Builder listApplicationsBuilder =  ListApplicationsRequest.builder()
        		 .compartmentId(AuthProvider.getInstance().getCompartmentId());
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
    
    public List<ApplicationSummary> getApplicationsbyCompartmentId(String CompartmentId,ListApplicationsRequest.SortBy s,ListApplicationsRequest.SortOrder so){    	
        String nextToken = null;
   		List<ApplicationSummary> applicationssummary = new ArrayList<ApplicationSummary>();
   		if(CompartmentId == null){
   			CompartmentId= AuthProvider.getInstance().getCompartmentId();
   		}   		
         Builder listApplicationsBuilder =  ListApplicationsRequest.builder()
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
    
    public void createApplication(final CreateApplicationDetails request) {
        CreateApplicationResponse response =
                dataflowClient.createApplication(
                        CreateApplicationRequest.builder()
                        .createApplicationDetails(request)
                        .build());
    }
    
    public Application createApplicationandgetId(final CreateApplicationDetails request) {
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
    
    public void runEditApplication(String applicationId, final UpdateApplicationDetails request) {    
    	UpdateApplicationResponse response=	dataflowClient.updateApplication(UpdateApplicationRequest.builder()
    			.applicationId(applicationId)
    			.updateApplicationDetails(request)
    			.build()) ;
    	
    	
    }
    
    public void editApplication(String applicationId,final UpdateApplicationDetails request ) {
    	UpdateApplicationResponse response=	dataflowClient.updateApplication(UpdateApplicationRequest.builder()
    			.applicationId(applicationId)
    			.updateApplicationDetails(request)
    			.build()) ;
    }
    
    
    public Application editApplicationandgetId(String applicationId,final UpdateApplicationDetails request ) {
    	UpdateApplicationResponse response=	dataflowClient.updateApplication(UpdateApplicationRequest.builder()
    			.applicationId(applicationId)
    			.updateApplicationDetails(request)
    			.build()) ;
    	return response.getApplication();
    	
    }
}
