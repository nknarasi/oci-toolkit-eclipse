package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class ScheduleUploadObjectAction implements IRunnableWithProgress { 
	private String bucketName;
	private File applicationFile;
	
	public ScheduleUploadObjectAction(String bucketName , File applicationFile){
	    	this.bucketName = bucketName;
	    	this.applicationFile = applicationFile;
	    }
	   @Override
	    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	    {
	        // Tell the user what you are doing
	        monitor.beginTask("Uploading in Progress", IProgressMonitor.UNKNOWN);
	        // Do your work
	    	try {
				ObjStorageClient.getInstance().uploadObject(bucketName, applicationFile);				
			} catch (Exception e) {
				ErrorHandler.logError("Unable to upload objects to bucket: " + e.getMessage());
			};
	        // You are done
	        monitor.done();
	    }
}
