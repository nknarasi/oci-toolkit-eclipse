package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;


public class EditApplicationWizard extends Wizard implements INewWizard {	
    private EditApplicationWizardPage1 firstPage;
    private EditApplicationWizardPage2 secondPage;
    private TagsPage tagPage;
    private ISelection selection;
    private Application application;
    private String warnings="";
	public EditApplicationWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}
	
    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstPage = new EditApplicationWizardPage1(selection, dto, application.getId());
        addPage(firstPage);     
        tagPage = new TagsPage(selection,application.getId());
        addPage(tagPage);
        secondPage = new EditApplicationWizardPage2(selection,dto, application.getId());
        addPage(secondPage);        
    }
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */   
    @Override
    public boolean performFinish() { 	  		
	    if(application.getExecute() != null && !application.getExecute().equals("")) {	    		
	    	 	Builder editApplicationRequestBuilder = 
	    	 	        UpdateApplicationDetails.builder()
	    	 			.displayName(firstPage.getDisplayName())
	    	 			.description(firstPage.getApplicationDescription())
	    	 			.sparkVersion(firstPage.getSparkVersion())
	    	 			.driverShape(firstPage.getDriverShape())
	    	 			.executorShape(firstPage.getExecutorShape())
	    	 			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
	    	 			.execute(firstPage.getSparkSubmit())
	    	 			.definedTags(tagPage.getOT())
	    	 			.freeformTags(tagPage.getFT());
	    	 	
	    	 	 		final UpdateApplicationDetails editApplicationRequest;	    	 	    	
	    	 			final boolean usesAdvancedOptions = secondPage.usesAdvancedOptions();
	    	 			if (usesAdvancedOptions) {
	    	 				editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(secondPage.getSparkProperties())
	    	 						.logsBucketUri(secondPage.getApplicationLogLocation())
	    	 						.warehouseBucketUri(secondPage.getWarehouseUri());	    	 				
	    	 				if(secondPage.usesPrivateSubnet()) {
	    	 					editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondPage.getPrivateEndPointId())
	    	 							.build();
	    	 				}
	    	 				else {	    	 					
	    	 					if(application.getPrivateEndpointId() != null) {					
	    	 						editApplicationRequestBuilder
	    	 						.privateEndpointId("");
	    	 					}
	    	 					editApplicationRequest = editApplicationRequestBuilder.build();
	    	 				}	    	 						
	    	 			} else {
	    	 				editApplicationRequest = editApplicationRequestBuilder.build();	    	 						
	    	 			}	        			
	    	 			 IRunnableWithProgress op = new IRunnableWithProgress() {
	     	 	            @Override
	     	 	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
	     	 	                ApplicationClient.getInstance().editApplication(application.getId(),editApplicationRequest);
	     	 	                monitor.done();
	     	 	            }
	     	 	        };
	     	 	        try {
	     	 	            getContainer().run(true, false, op);
	     	 	        } catch (InterruptedException e) {
	     	 	            return false;
	     	 	        } catch (InvocationTargetException e) {
	     	 	            Throwable realException = e.getTargetException();
	     	 	            MessageDialog.openError(getShell(), "Failed to Edit Application ", realException.getMessage());
	     	 	            return false;
	     	 	        }

	     	 	        return true;
    	}
    	else {
    	 	Builder editApplicationRequestBuilder = 
    	 	        UpdateApplicationDetails.builder()
    	 			.displayName(firstPage.getDisplayName())
    	 			.description(firstPage.getApplicationDescription())
    	 			.sparkVersion(firstPage.getSparkVersion())
    	 			.driverShape(firstPage.getDriverShape())
    	 			.executorShape(firstPage.getExecutorShape())
    	 			.numExecutors(Integer.valueOf(firstPage.getNumofExecutors()))
    	 			.language(firstPage.getLanguage())
    	 			.fileUri(firstPage.getFileUri())
    	 			.archiveUri(firstPage.getArchiveUri())
    	 			.parameters(firstPage.getParameters())
    	 			.definedTags(tagPage.getOT())
    	 			.freeformTags(tagPage.getFT());    	 	    	
    	 	        
    	 		   	if(firstPage.getLanguage() == ApplicationLanguage.Java || firstPage.getLanguage()== ApplicationLanguage.Scala){
    	 		   		editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstPage.getMainClassName())
    	 	    				.arguments(firstPage.getArguments());					
    	 	    	}
    	 	    	else if (firstPage.getLanguage() == ApplicationLanguage.Python) {
    	 	    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstPage.getArguments());			
    	 	    	}    	 	    	
    	 		   	editApplicationRequestBuilder = editApplicationRequestBuilder.parameters(firstPage.getParameters());				    	 			
    	 	 		final UpdateApplicationDetails editApplicationRequest;	 	    	
    	 			final boolean usesAdvancedOptions = secondPage.usesAdvancedOptions();
    	 			if (usesAdvancedOptions) {
    	 				editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(secondPage.getSparkProperties())
    	 						.logsBucketUri(secondPage.getApplicationLogLocation()).warehouseBucketUri(secondPage.getWarehouseUri());    	 				
    	 				if(secondPage.usesPrivateSubnet()) {
    	 					editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondPage.getPrivateEndPointId())
    	 							.build();
    	 				}
    	 				else {    	 					
    	 					if(application.getPrivateEndpointId() != null) {					
    	 						editApplicationRequestBuilder
    	 						.privateEndpointId("");
    	 					}
    	 					editApplicationRequest = editApplicationRequestBuilder.build();
    	 				}    	 						
    	 			} else {
    	 				editApplicationRequest = editApplicationRequestBuilder.build();    	 						
    	 			}
    	 	        IRunnableWithProgress op = new IRunnableWithProgress() {
    	 	            @Override
    	 	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
    	 	                ApplicationClient.getInstance().editApplication(application.getId(),editApplicationRequest);
    	 	                monitor.done();
    	 	            }
    	 	        };
    	 	        try {
    	 	            getContainer().run(true, false, op);
    	 	        } catch (InterruptedException e) {
    	 	            return false;
    	 	        } catch (InvocationTargetException e) {
    	 	            Throwable realException = e.getTargetException();
    	 	            MessageDialog.openError(getShell(), "Failed to Edit Application ", realException.getMessage());
    	 	            return false;
    	 	        }
    	 	        return true;
    	}   
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
