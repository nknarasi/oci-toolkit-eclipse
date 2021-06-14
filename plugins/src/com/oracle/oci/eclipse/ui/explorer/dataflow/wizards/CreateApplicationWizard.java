package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
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

public class CreateApplicationWizard extends Wizard implements INewWizard {	
	private String COMPARTMENT_ID;
    private CreateApplicationWizardPage firstpage;
    private CreateApplicationWizardPage3 thirdpage;
    private TagsPage tagpage;
    private ISelection selection;
    
	public CreateApplicationWizard(String COMPARTMENT_ID) {
		super();
		this.COMPARTMENT_ID= COMPARTMENT_ID;
		setNeedsProgressMonitor(true);
	}
	

    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new CreateApplicationWizardPage(selection,dto,COMPARTMENT_ID);
        addPage(firstpage);      
        thirdpage = new CreateApplicationWizardPage3(selection,dto);
        addPage(thirdpage);
        tagpage= new TagsPage(selection,COMPARTMENT_ID);
        addPage(tagpage);
        
    }
    
    
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(firstpage)) {
             return tagpage;
        }       
        if (page.equals(tagpage)) {
            return thirdpage;
        }    
        return null;       
    }
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    
    @Override
    public boolean performFinish() {   	
    	final String compartmentId = firstpage.getApplicationCompartmentId();
        
    	Builder createApplicationRequestBuilder = 
        CreateApplicationDetails.builder()
        .compartmentId(compartmentId)
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
		
    	final CreateApplicationDetails createApplicationRequest;
    	
    	if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstpage.getMainClassName())
    				.arguments(firstpage.getArguments());					
    	}
    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstpage.getArguments());			
    	}
    	
		final boolean usesAdvancedOptions = thirdpage.usesAdvancedOptions();
		if (usesAdvancedOptions) {
			createApplicationRequestBuilder = createApplicationRequestBuilder.configuration(thirdpage.getSparkProperties())
					.logsBucketUri(thirdpage.getApplicationLogLocation()).warehouseBucketUri(thirdpage.getWarehouseUri());
			
			final boolean usesPrivateSubnet = thirdpage.usesPrivateSubnet();
			if(usesPrivateSubnet) {
				createApplicationRequest = createApplicationRequestBuilder.privateEndpointId(thirdpage.getPrivateEndPointId())
						.build();
			}
			else {
				createApplicationRequest = createApplicationRequestBuilder.build();
			}
					
		} else {
			createApplicationRequest = createApplicationRequestBuilder.build();
					
		}
    		
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                ApplicationClient.getInstance().createApplication(createApplicationRequest);
                monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Create Application ", realException.getMessage());
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
