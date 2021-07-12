package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class DownloadRunLogAction implements IRunnableWithProgress{
	
	private String errorMessage=null;
	private InputStream inStream;
	private File targetFile;
	
	 public DownloadRunLogAction(InputStream inStream,File targetFile)
	    {
	    		this.inStream=inStream;
	    		this.targetFile=targetFile;
	    }
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		
		try {
	        // Tell the user what you are doing
	        monitor.beginTask("Downloading...", IProgressMonitor.UNKNOWN);

	        // Do your work
            java.nio.file.Files.copy(
                    inStream,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(inStream);

	        // You are done
	        monitor.done();
	    	}
	    	catch (Exception e) {
	    		errorMessage=e.getMessage();
	    	}
	    }
	    
	    public String getErrorMessage() {
	    	return errorMessage;
	    }
	}