package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.dataflow.model.CreatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.CreatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
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
        //final String bucketName = page.getBucketName();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                //ObjStorageClient.getInstance().createBucket(bucketName);
				//MessageDialog.openInformation(getShell(),"Details of Re-Run",page.getDetails());
                //monitor.done();
            }
        };
        try {
        	Object[] obj=page.getDetails();
        	ArrayList<String> nsgl=page2.getnsgs();
        	if(!check((String)obj[3],(String[])obj[4],(String)obj[8],nsgl)) return false;
			CreatePrivateEndpointDetails createPrivateEndpointDetails = CreatePrivateEndpointDetails.builder()
		.compartmentId(pepTable.compid==null?(String)obj[0]:pepTable.compid)
		.definedTags(page3.getOT())
		/*.description("EXAMPLE-description-Value")*/
		.displayName((String)obj[3])
		.dnsZones(Arrays.asList((String[])obj[4]))
		.freeformTags(page3.getFT())
		.maxHostCount((int)obj[9])
		.nsgIds(page2.getnsgs())
		.subnetId((String)obj[8]).build();
	CreatePrivateEndpointRequest createPrivateEndpointRequest = CreatePrivateEndpointRequest.builder()
		.createPrivateEndpointDetails(createPrivateEndpointDetails)
		//.opcRetryToken("EXAMPLE-opcRetryToken-Value")
		//.opcRequestId("RASUMKE9AV1FLPILTT29/OpcRequestIdExample/<unique_ID>")
		.build();
        /* Send request to the Client */
        PrivateEndPointsClient.getInstance().getDataFLowClient().createPrivateEndpoint(createPrivateEndpointRequest);
			
			
			MessageDialog.openInformation(getShell(),"Succesful","");
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Create Private Endpoint ", e.getMessage());
        	return false;
        }
		pepTable.refresh(true);
        return true;
    }
	
    boolean check(String name,String[] dnsl,String sid,ArrayList<String> nsgl) {
    	
    	if(name.isEmpty()) {open("Improper Values","Enter proper name for the Private-Endpoint");return false;}
    	if(sid==null||sid.isEmpty()) {open("Improper Values","Select proper Sub-net Id for the Private-Endpoint");return false;}
    	if(dnsl==null||dnsl.length==0||dnsl[0].isEmpty()) {open("Improper Values","Enter proper DNS zones for the Private-Endpoint");return false;}
    	if(nsgl==null) {open("Improper Values","Enter proper NSG IDs for the Private-Endpoint");return false;}
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