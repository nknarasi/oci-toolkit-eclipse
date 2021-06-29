package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointCollection;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.requests.GetApplicationRequest;
import com.oracle.bmc.dataflow.requests.GetPrivateEndpointRequest;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.requests.ListPrivateEndpointsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.bmc.dataflow.requests.ListPrivateEndpointsRequest.Builder;

public class PrivateEndPointsClient  extends BaseClient {
	
    private static PrivateEndPointsClient single_instance = null;
    private static DataFlowClient dataflowClient;

    private PrivateEndPointsClient() {
        if (dataflowClient == null) {
        	dataflowClient = createPrivateEndPointsClient();
        }
    }

    public static PrivateEndPointsClient getInstance() {
        if (single_instance == null) {
            single_instance = new PrivateEndPointsClient();
        }
        return single_instance;
    }

    private DataFlowClient createPrivateEndPointsClient(){
        dataflowClient = new DataFlowClient(AuthProvider.getInstance().getProvider());
        dataflowClient.setRegion(AuthProvider.getInstance().getRegion());
        return dataflowClient;
    }


    public DataFlowClient getDataFLowClient() {
        return dataflowClient;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        dataflowClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createPrivateEndPointsClient();
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

    
    public static List<PrivateEndpointSummary> getPrivateEndPoints(String CompartmentId) {
        String nextToken = null;
   		List<PrivateEndpointSummary> privateEndpointsummary = new ArrayList<PrivateEndpointSummary>();   		
         Builder listPrivateEndpointsBuilder =  ListPrivateEndpointsRequest.builder()
        		 .compartmentId(CompartmentId);
         do {
             listPrivateEndpointsBuilder.page(nextToken);
             try {
                 ListPrivateEndpointsResponse listprivateEndpointresponse =
                         dataflowClient.listPrivateEndpoints(listPrivateEndpointsBuilder.build());                 
                 privateEndpointsummary.addAll(listprivateEndpointresponse.getPrivateEndpointCollection().getItems());
                 nextToken = listprivateEndpointresponse.getOpcNextPage();
             } catch(Throwable e) {
                 ErrorHandler.logError("Unable to list private endpoints: " + e.getMessage());
             }
         } while (nextToken != null);
        return privateEndpointsummary;
    }
        
    public PrivateEndpoint getPrivateEndpointDetails (String PrivateEndpointId) {
    	return dataflowClient.getPrivateEndpoint(
    			GetPrivateEndpointRequest.builder()
    			.privateEndpointId(PrivateEndpointId)
    			.build()).getPrivateEndpoint();   			
    }

}
