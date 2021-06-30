package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.model.CreatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.CreatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;

public class ScheduleCreatePrivateEndpointAction implements IRunnableWithProgress{
	
	private Object[] obj;
	private Map<String,Map<String,Object>> OT;
	private Map<String,String> FT;
	private ArrayList<String> nsgs;
	private String compid;
	
    public ScheduleCreatePrivateEndpointAction(Object[] obj,Map<String,Map<String,Object>> OT,Map<String,String> FT,ArrayList<String> nsgs,String compid)
    {
    	this.obj=obj;
    	this.OT=OT;
    	this.FT=FT;
    	this.compid=compid;
    	this.nsgs=nsgs;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Create Private Endpoint request processing", IProgressMonitor.UNKNOWN);

        // Do your work
        CreatePrivateEndpointDetails createPrivateEndpointDetails = CreatePrivateEndpointDetails.builder()
				.compartmentId(compid==null?(String)obj[0]:compid)
				.definedTags(OT)
				.displayName((String)obj[3])
				.dnsZones(Arrays.asList((String[])obj[4]))
				.freeformTags(FT)
				.maxHostCount((int)obj[9])
				.nsgIds(nsgs)
				.subnetId((String)obj[8]).build();
		
		CreatePrivateEndpointRequest createPrivateEndpointRequest = CreatePrivateEndpointRequest.builder()
				.createPrivateEndpointDetails(createPrivateEndpointDetails)
				.build();
    // Send request to the Client 
    PrivateEndPointsClient.getInstance().getDataFLowClient().createPrivateEndpoint(createPrivateEndpointRequest);

        // You are done
        monitor.done();
    }
}

