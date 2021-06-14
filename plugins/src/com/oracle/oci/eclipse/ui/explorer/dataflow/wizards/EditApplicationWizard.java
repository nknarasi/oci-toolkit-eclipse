package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
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
    private EditApplicationWizardPage1 firstpage;
    private EditApplicationWizardPage2 secondpage;
    private TagsPage tagpage;
    private ISelection selection;
    private Application application;
    
	public EditApplicationWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}
	
    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new EditApplicationWizardPage1(selection, dto, application.getId());
        addPage(firstpage);     
        tagpage = new TagsPage(selection,application.getId());
        addPage(tagpage);
        secondpage = new EditApplicationWizardPage2(selection,dto, application.getId());
        addPage(secondpage);        
    }
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    
    @Override
    public boolean performFinish() { 	
    	
    	Builder editApplicationRequestBuilder = 
        UpdateApplicationDetails.builder()
		.displayName(firstpage.getDisplayName())
		.description(firstpage.getApplicationDescription())
		.sparkVersion(firstpage.getSparkVersion())
		.driverShape(firstpage.getDriverShape())
		.executorShape(firstpage.getExecutorShape())
		.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		.language(firstpage.getLanguage())
		.fileUri(firstpage.getFileUri())
		.archiveUri(firstpage.getArchiveUri())
		
		.definedTags(tagpage.getOT())
		.freeformTags(tagpage.getFT());
    	
        
	   	if(firstpage.getLanguage() == ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
	   		editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstpage.getMainClassName())
    				.arguments(firstpage.getArguments());					
    	}
    	else if (firstpage.getLanguage() == ApplicationLanguage.Python) {
    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstpage.getArguments());			
    	}
		
 		final UpdateApplicationDetails editApplicationRequest;
    	
		final boolean usesAdvancedOptions = secondpage.usesAdvancedOptions();
		if (usesAdvancedOptions) {
			editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(secondpage.getSparkProperties())
					.logsBucketUri(secondpage.getApplicationLogLocation()).warehouseBucketUri(secondpage.getWarehouseUri());
			
			final boolean usesPrivateSubnet = secondpage.usesPrivateSubnet();
			if(usesPrivateSubnet) {
				editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondpage.getPrivateEndPointId())
						.build();
			}
			else {
				editApplicationRequest = editApplicationRequestBuilder.build();
			}
					
		} else {
			editApplicationRequest = editApplicationRequestBuilder.build();
					
		}


		
	//	editApplicationRequest = editApplicationRequestBuilder.build();
		
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
