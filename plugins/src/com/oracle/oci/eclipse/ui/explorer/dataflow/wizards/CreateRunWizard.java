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
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;


public class CreateRunWizard  extends Wizard implements INewWizard{	
    private CreateRunWizardPage1 firstpage;
    private TagsPage secondpage;
    private CreateRunWizardPage3 thirdpage;
    private ISelection selection;
    private Application application;
    private String warnings= "";
    
	public CreateRunWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}
    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new CreateRunWizardPage1(selection,dto,application.getId());
        addPage(firstpage);
        secondpage=new TagsPage(selection,application.getCompartmentId());
        addPage(secondpage);
        thirdpage = new CreateRunWizardPage3(selection,dto,application.getId());
        addPage(thirdpage);       
    }
        

    @Override
    public boolean performFinish() {      	   	
    	Builder runApplicationRequestBuilder = 
        CreateRunDetails.builder()
        .compartmentId(application.getCompartmentId())
        .applicationId(application.getId())
        .displayName(firstpage.getDisplayName())
        .arguments(application.getArguments())
        .parameters(firstpage.getParameters())
        .driverShape(firstpage.getDriverShape())
        .executorShape(firstpage.getExecutorShape())
        .numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))       
        .definedTags(secondpage.getOT())
        .freeformTags(secondpage.getFT())               
        .configuration(thirdpage.getSparkProperties())
        .logsBucketUri(thirdpage.getApplicationLogLocation())
        .warehouseBucketUri(thirdpage.getWarehouseUri());
		
		final CreateRunDetails runApplicationRequest;		
		runApplicationRequest = runApplicationRequestBuilder.build();		
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                ApplicationClient.getInstance().runApplication(runApplicationRequest);
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
