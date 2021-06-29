package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;


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
        page3=new AdvancedOptionsPage(selection,obj,page);
        addPage(page3);
        
    }
    
    @Override
    public boolean performFinish() {
    	
        try {
        	Object[] obj;
        	if(runSum!=null) obj=page.getDetails();
        	else obj=page.getDetails_app();
        	if(page3.ischecked()) {obj[11]=page3.loguri();obj[15]=page3.buckuri();}
        	
        	String d="";
        	if(!page3.ischecked()) d="!";
        	Object[] validObjects=new Object[] {obj[6],obj[11],obj[15],page3.getconfig().keySet()};
        	String[] objType=new String[] {"name",d+"loguri",d+"warehouseuri",d+"sparkprop"+((String)obj[14]).charAt(0)};
        	String message=Validations.check(validObjects, objType);
        	if(!message.isEmpty()) {
        		open("Improper Entries",message);
        		return false;
        	}
        	
        	DataFlowClient client=RunClient.getInstance().getDataFlowClient();
        	CreateRunDetails createRunDetails = CreateRunDetails.builder()
        		.applicationId((String)obj[0])
        		.archiveUri((String)obj[1])
        		.compartmentId((String)obj[3])
        		.configuration(page3.ischecked()?page3.getconfig():null)
        		.definedTags(page2.getOT())
        		.displayName((String)obj[6])
        		.driverShape((String)obj[7])
        		.execute((String)obj[8])
        		.executorShape((String)obj[9])
        		.freeformTags(page2.getFT())
        		.logsBucketUri((String)obj[11])
        		.numExecutors((Integer)obj[12])
        		.parameters((List<ApplicationParameter>)obj[13])
        		.warehouseBucketUri((String)obj[15]).build();
        	
        	CreateRunRequest createRunRequest;
        	if(runSum!=null){		
        		createRunRequest = CreateRunRequest.builder().createRunDetails(createRunDetails).opcRequestId((String)obj[16]).build();
        	}
        	else {		
        		createRunRequest = CreateRunRequest.builder().createRunDetails(createRunDetails).build();
        	}
        	
        	client.createRun(createRunRequest);
        	if(runSum!=null) MessageDialog.openInformation(getShell(),"Re-Run Succesful","A re-run of application is scheduled.");
        	else MessageDialog.openInformation(getShell(),"Run Application Succesful","A run of application is scheduled.");

        	runTable.refresh(true);
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Create Run ", e.getMessage());
        	return false;
        }
        return true;
    }
    
    void open(String h,String m) {
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