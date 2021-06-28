package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.dataflow.model.CreatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.CreatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;


public class CreatePrivateEndpointWizard extends Wizard implements INewWizard {
    private CreatePrivateEndpointWizardPage page;
    private NsgPage page2;
    private TagsPage page3;
    private ISelection selection;
	private PrivateEndpointTable pepTable;

    public CreatePrivateEndpointWizard(PrivateEndpointTable pepTable) {
        super();
        setNeedsProgressMonitor(true);
		this.pepTable=pepTable;
    }

    @Override
    public void addPages() {
        page = new CreatePrivateEndpointWizardPage(selection,pepTable.compid);
        addPage(page);
        page2=new NsgPage(selection,"");
        page3=new TagsPage(selection,pepTable.compid);
        addPage(page2);addPage(page3);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
    	
        try {
        	Object[] obj=page.getDetails();
        	ArrayList<String> nsgl=page2.getnsgs();
        	
        	Object[] validObjects=new Object[] {obj[3],obj[4],obj[8],nsgl};
        	String[] objType=new String[] {"name","dnszones","subnetid","nsgl"};
        	String message=Validations.check(validObjects, objType);
        	if(!message.isEmpty()) {
        		open("Improper Entries",message);
        		return false;
        	}
        	
			CreatePrivateEndpointDetails createPrivateEndpointDetails = CreatePrivateEndpointDetails.builder()
					.compartmentId(pepTable.compid==null?(String)obj[0]:pepTable.compid)
					.definedTags(page3.getOT())
					.displayName((String)obj[3])
					.dnsZones(Arrays.asList((String[])obj[4]))
					.freeformTags(page3.getFT())
					.maxHostCount((int)obj[9])
					.nsgIds(page2.getnsgs())
					.subnetId((String)obj[8]).build();
			CreatePrivateEndpointRequest createPrivateEndpointRequest = CreatePrivateEndpointRequest.builder()
					.createPrivateEndpointDetails(createPrivateEndpointDetails)
					.build();
        /* Send request to the Client */
        PrivateEndPointsClient.getInstance().getDataFLowClient().createPrivateEndpoint(createPrivateEndpointRequest);
	    
        MessageDialog.openInformation(getShell(),"Succesful","Private Endpoint created successfully.");
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Create Private Endpoint ", e.getMessage());
        	return false;
        }
        
		pepTable.refresh(true);
		
        return true;
    }
    
    void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}