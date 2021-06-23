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

import com.oracle.bmc.dataflow.DataFlowClient;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.model.UpdatePrivateEndpointDetails;
import com.oracle.bmc.dataflow.requests.UpdatePrivateEndpointRequest;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

//import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;

public class EditPrivateEndpointWizard extends Wizard implements INewWizard {
    private EditPrivateEndpointPage page;
    private TagsPage page2;
    private AdvancedOptionsPage page3;
    private ISelection selection;
	private PrivateEndpointSummary pepSum;
	private PrivateEndpointTable pepTable;
	private Object obj;

    public EditPrivateEndpointWizard(PrivateEndpointSummary pepSum,PrivateEndpointTable pepTable) {
        super();
        setNeedsProgressMonitor(true);
		this.pepSum=pepSum;
		this.pepTable=pepTable;
		this.obj=pepSum;
    }
    
    @Override
    public void addPages() {
        page=new EditPrivateEndpointPage(selection,pepSum);
        addPage(page);
        page2=new TagsPage(selection,pepSum.getCompartmentId());
        addPage(page2);
        
    }
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
            }
        };
        if(!check()) return false;
        try {
        DataFlowClient client=PrivateEndPointsClient.getInstance().getDataFLowClient();
        
        UpdatePrivateEndpointDetails updatePrivateEndpointDetails = UpdatePrivateEndpointDetails.builder()
        		.definedTags(page2.getOT())
        		.displayName(page.getName())
        		.dnsZones(page.getDNS()).build();

        UpdatePrivateEndpointRequest updatePrivateEndpointRequest = UpdatePrivateEndpointRequest.builder()
        		.updatePrivateEndpointDetails(updatePrivateEndpointDetails)
        		.privateEndpointId(pepSum.getId()).build();

        client.updatePrivateEndpoint(updatePrivateEndpointRequest);
        
		pepTable.refresh(true);
        }
        catch (Exception e) {
        	MessageDialog.openError(getShell(), "Failed to Update Private Endpoint", e.getMessage());
        	return false;
        }
        return true;
    }
    
    boolean check() {
    	if(page.nameText.getText()==null||page.nameText.getText().isEmpty()) {open("Improper Details","Provide proper name for the Private Endpoint");return false;}
    	if(page.dnsText.getText()==null||page.dnsText.getText().isEmpty()) {open("Improper Details","Provide proper DNS Zones for the Private Endpoint");return false;}    	
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