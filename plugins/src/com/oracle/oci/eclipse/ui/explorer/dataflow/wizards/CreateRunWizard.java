package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddRunApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;


public class CreateRunWizard  extends Wizard implements INewWizard{	
    private CreateRunWizardPage1 firstpage;
    private TagsPage secondpage;
    protected CreateRunWizardPage3 thirdpage;
    private ISelection selection;
    private Application application;
    
	public CreateRunWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
	}
    @Override
    public void addPages() {
    	 try {
           	IRunnableWithProgress op = new AddRunApplicationPagesAction(this);
               new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
           } catch (Exception e) {
           	MessageDialog.openError(getShell(), "Unable to add pages to Run Application wizard", e.getMessage());
           }         
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	DataTransferObject dto = new DataTransferObject();
    	monitor.subTask("Adding Main page");    	
    	firstpage = new CreateRunWizardPage1(selection,dto,application.getId());
        addPage(firstpage);
    	monitor.subTask("Adding Tags Page");
    	secondpage=new TagsPage(selection,application.getCompartmentId());
        addPage(secondpage);
        monitor.subTask("Adding Advanced Options page");
        thirdpage = new CreateRunWizardPage3(selection,dto,application.getId());
        addPage(thirdpage);
    }
        

    @Override
    public boolean performFinish() {     
    	
    	List<Object> validObjects = new ArrayList<Object>();
    	List<String> objectType = new ArrayList<String>();
    	
    	performValidations(validObjects,objectType);       	
    	String message=Validations.check(validObjects.toArray(),objectType.toArray(new String[1]));
    	
    	if(!message.isEmpty()) {
    		open("Improper Entries",message);
    		return false;
    	}
    	
    	
    	Builder runApplicationRequestBuilder = 
        CreateRunDetails.builder()
        .compartmentId(application.getCompartmentId())
        .applicationId(application.getId())
        .displayName(firstpage.getDisplayName())        
        .driverShape(firstpage.getDriverShape())
        .executorShape(firstpage.getExecutorShape())
        .numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))       
        .definedTags(secondpage.getOT())
        .freeformTags(secondpage.getFT())              
        .logsBucketUri(thirdpage.getApplicationLogLocation())
        .warehouseBucketUri(thirdpage.getWarehouseUri());
		
    	if(application.getExecute() != null) {
    		runApplicationRequestBuilder = runApplicationRequestBuilder.execute(firstpage.getSparkSubmit());
    	}
    	else {
    		runApplicationRequestBuilder = runApplicationRequestBuilder.arguments(application.getArguments())
    		        .parameters(firstpage.getParameters())
    		        .configuration(thirdpage.getSparkProperties());
    	}
		final CreateRunDetails runApplicationRequest;		
		runApplicationRequest = runApplicationRequestBuilder.build();		
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            	DataflowClient.getInstance().runApplication(runApplicationRequest);
                monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Run Application ", realException.getMessage());
            return false;
        }
        return true;
    }
   public void performValidations(List<Object> objectArray,List<String>nameArray) {
    	
    	objectArray.add(firstpage.getDisplayName());
    	nameArray.add("name");    	

       objectArray.add(thirdpage.getApplicationLogLocation());
       nameArray.add("loguri"); 
       
       if(thirdpage.getWarehouseUri() != null && !thirdpage.getWarehouseUri().isEmpty()) {
           objectArray.add(thirdpage.getWarehouseUri());
           nameArray.add("warehouseuri"); 
       }     
       
    	if(thirdpage.getSparkProperties() != null) {
 	       objectArray.add(thirdpage.getSparkProperties().keySet());
 	       nameArray.add("sparkprop" + firstpage.getSparkVersion().charAt(0));         
    	}

    }
    public void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}
