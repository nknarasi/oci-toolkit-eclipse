package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.EditPrivateEndpointWizard;

public class AddEditPrivateEndpointPagesAction implements IRunnableWithProgress{
	
	private EditPrivateEndpointWizard wizard;
	
    public AddEditPrivateEndpointPagesAction(EditPrivateEndpointWizard wizard)
    {
    		this.wizard=wizard;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        // Tell the user what you are doing
        monitor.beginTask("Opening Edit Private Endpoint Wizard", IProgressMonitor.UNKNOWN);

        // Do your work
   		
        wizard.addPagesWithProgress(monitor);

        // You are done
        monitor.done();
    }
}