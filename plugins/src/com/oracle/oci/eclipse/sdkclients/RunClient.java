package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;


import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.Run;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.CreateRunRequest;
import com.oracle.bmc.dataflow.requests.GetRunRequest;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.bmc.dataflow.requests.ListRunsRequest.Builder;
import com.oracle.bmc.dataflow.responses.CreateRunResponse;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;


public class RunClient extends BaseClient {
	
    private static RunClient single_instance = null;
	private static String compid;
    private static DataFlowClient dataflowClient;
    
    private RunClient() {
        if (dataflowClient == null) {
        	dataflowClient = createRunClient();
        }
    }

    public static RunClient getInstance() {
        if (single_instance == null) {
            single_instance = new RunClient();
        }
        return single_instance;
    }

    private DataFlowClient createRunClient(){
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
        createRunClient();
    }
    
    private DataFlowClient getRunClient() {
        return dataflowClient;
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
    
    public List<RunSummary> getRuns() throws Exception {
    	
        String nextToken = null;
   		List<RunSummary> runssummary = new ArrayList<RunSummary>();
   		
   		
         Builder listRunsBuilder =  ListRunsRequest.builder()
        		 .compartmentId(AuthProvider.getInstance().getCompartmentId());
         do {
             listRunsBuilder.page(nextToken);
             try {
                 ListRunsResponse listrunsresponse =
                         dataflowClient.listRuns(listRunsBuilder.build());
                 
                 runssummary.addAll(listrunsresponse.getItems());
                 nextToken = listrunsresponse.getOpcNextPage();
             } catch(Throwable e) {
                 ErrorHandler.logError("Unable to list runs: " + e.getMessage());
             }

         } while (nextToken != null);
        
       	
        return runssummary;
    }
    
   public List<RunSummary> getRunsinCompartment(String CompartmentId) throws Exception {
    	
        String nextToken = null;
   		List<RunSummary> runssummary = new ArrayList<RunSummary>();
   		
   		
         Builder listRunsBuilder =  ListRunsRequest.builder()
        		 .compartmentId(CompartmentId);
         do {
             listRunsBuilder.page(nextToken);
             try {
                 ListRunsResponse listrunsresponse =
                         dataflowClient.listRuns(listRunsBuilder.build());
                 
                 runssummary.addAll(listrunsresponse.getItems());
                 nextToken = listrunsresponse.getOpcNextPage();
             } catch(Throwable e) {
                 ErrorHandler.logError("Unable to list runs: " + e.getMessage());
             }

         } while (nextToken != null);
        
       	
        return runssummary;
    }
    
    
    
 /*
	public List<RunSummary> getRuns() throws Exception {
        String nextToken = null;
        List<RunSummary> rsList = new ArrayList<RunSummary>();
		ListRunsRequest.Builder listRunsBuilder =
                ListRunsRequest.builder()
                .compartmentId(compid).sortBy(ListRunsRequest.SortBy.TimeCreated);

        do {
            listRunsBuilder.page(nextToken);
            try {
                ListRunsResponse listRunsResponse =
                        dataflowClient.listRuns(listRunsBuilder.build());
                rsList.addAll(listRunsResponse.getItems());
                nextToken = listRunsResponse.getOpcNextPage();
            } catch(Throwable e) {
                ErrorHandler.logError("Unable to list runs: " + e.getMessage());
                return rsList;
            }

        } while (nextToken != null);

        return rsList;
    }
	*/
    
	public Run getRunDetails(String runid) throws Exception {		 
		 GetRunRequest req = GetRunRequest.builder().runId(runid).build();
		 return dataflowClient.getRun(req).getRun();
	 }

	

}
