package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.CreateRunRequest;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

//import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class RunWizard extends Wizard implements INewWizard {
    private RunWizardPage page;
    private TagsPage page2;
    private AdvancedOptionsPage page3;
    private ISelection selection;
	private RunSummary runSum;
	private ApplicationSummary appSum;
	private RunTable runTable;
	private Object obj;

    public RunWizard(RunSummary runSum,RunTable runTable) {
        super();
        setNeedsProgressMonitor(true);
		this.runSum=runSum;
		this.runTable=runTable;
		this.obj=runSum;
    }
	
	public RunWizard(ApplicationSummary appSum) {
        super();
        setNeedsProgressMonitor(true);
		this.appSum=appSum;
		this.obj=appSum;
    }

    @Override
    public void addPages() {
        if(runSum!=null) page = new RunWizardPage(selection,runSum);
		else page = new RunWizardPage(selection,appSum);
        page2=new TagsPage(selection,runSum!=null?runSum.getCompartmentId():appSum.getCompartmentId());
        addPage(page);addPage(page2);
        page3=new AdvancedOptionsPage(selection,obj);
        addPage(page3);
        
    }
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        //final String bucketName = page.getBucketName();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                //ObjStorageClient.getInstance().createBucket(bucketName);
				//MessageDialog.openInformation(getShell(),"Details of Re-Run",page.getDetails());
                //monitor.done();
            }
        };
        /*try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Create Run ", realException.getMessage());
            return false;
        }*/

        // Refresh TreeView to show new nodes
        //ObjStorageContentProvider.getInstance().getBucketsAndRefresh();
        try {
        DataFlowClient client=RunClient.getInstance().getDataFlowClient();
        Object[] obj;
		if(runSum!=null) obj=page.getDetails();
		else obj=page.getDetails_app();
        CreateRunDetails createRunDetails = CreateRunDetails.builder()
        		.applicationId((String)obj[0])
        		.archiveUri((String)obj[1])
        		//.arguments(new ArrayList<>(Arrays.asList("EXAMPLE--Value")))
        		.compartmentId((String)obj[3])
        		.configuration(page3.getconfig())
        		.definedTags(page2.getOT())
        		.displayName((String)obj[6])
        		.driverShape((String)obj[7])
        		.execute((String)obj[8])
        		.executorShape((String)obj[9])
        		.freeformTags(page2.getFT())
        		.logsBucketUri((page3.loguri().equals("")||page3.loguri()==null)?(String)obj[11]:page3.loguri())
        		.numExecutors((Integer)obj[12])
        		.parameters((List<ApplicationParameter>)obj[13])
        		.warehouseBucketUri((page3.buckuri().equals("")||page3.buckuri()==null)?(String)obj[15]:page3.buckuri()).build();
		CreateRunRequest createRunRequest;
		if(runSum!=null){		
        createRunRequest = CreateRunRequest.builder()
        		.createRunDetails(createRunDetails)
		.opcRequestId((String)obj[16]).build();}
		else {		
        createRunRequest = CreateRunRequest.builder()
        		.createRunDetails(createRunDetails).build();}
        client.createRun(createRunRequest);
        if(runSum!=null) MessageDialog.openInformation(getShell(),"Re-Run Succesful","Successful");
        else MessageDialog.openInformation(getShell(),"Run Application Succesful","Successful");
		//DataFlowContentProvider.getInstance().getRunsAndRefresh();
		runTable.refresh(true);
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Create Run ", e.getMessage());
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