package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.requests.CreateRunRequest;
import com.oracle.oci.eclipse.sdkclients.RunClient;

public class ScheduleRerunAction implements IRunnableWithProgress{
	
	private Object[] obj;
	private Map<String,Map<String,Object>> OT;
	private Map<String,String> FT;
	private Map<String,String> config;
	private boolean isChecked;
	
    public ScheduleRerunAction(Object[] obj,Map<String,Map<String,Object>> OT,Map<String,String> FT,Map<String,String> config,boolean isChecked)
    {
    	this.obj=obj;
    	this.OT=OT;
    	this.FT=FT;
    	this.isChecked=isChecked;
    	this.config=config;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Re-run request processing", IProgressMonitor.UNKNOWN);

        // Do your work
   		
        //if(Window.OK==dialog.open()) {}
        DataFlowClient client=RunClient.getInstance().getDataFlowClient();
    	CreateRunDetails createRunDetails = CreateRunDetails.builder()
    		.applicationId((String)obj[0])
    		.archiveUri((String)obj[1])
    		.compartmentId((String)obj[3])
    		.configuration(isChecked?config:null)
    		.definedTags(OT)
    		.displayName((String)obj[6])
    		.driverShape((String)obj[7])
    		.execute((String)obj[8])
    		.executorShape((String)obj[9])
    		.freeformTags(FT)
    		.logsBucketUri((String)obj[11])
    		.numExecutors((Integer)obj[12])
    		.parameters((List<ApplicationParameter>)obj[13])
    		.warehouseBucketUri((String)obj[15]).build();
    	
    	CreateRunRequest createRunRequest;
    	createRunRequest = CreateRunRequest.builder().createRunDetails(createRunDetails).opcRequestId((String)obj[16]).build();
    	client.createRun(createRunRequest);

        // You are done
        monitor.done();
    }
}