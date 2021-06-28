package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;


public class EditPrivateEndpointPage extends WizardPage {
	
    Text nameText,dnsText;
	Combo dshapeCombo;
    private ISelection selection;
	private PrivateEndpoint pep;
	private Application app;

    public EditPrivateEndpointPage(ISelection selection,PrivateEndpointSummary pepSum) {
        super("wizardPage");
        setTitle("Edit Private Endpoint Wizard");
        setDescription("This wizard creates a edit private endpoint request. Please enter the following details.");
        this.selection = selection;
		try {
			this.pep=PrivateEndPointsClient.getInstance().getPrivateEndpointDetails(pepSum.getId());
		} catch (Exception e) {
			
		}
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
		
        Label nameLabel = new Label(container, SWT.NULL);
        nameLabel.setText("&Name:");
        nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
        nameText.setText(pep.getDisplayName());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label dnsLabel = new Label(container, SWT.NULL);
        dnsLabel.setText("&DNS zones to resolve:");
		dnsText = new Text(container, SWT.BORDER);
		dnsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dnsText.setText(String.join(",", pep.getDnsZones()));
		
        setControl(container);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public List<String> getDNS() {
        
		return Arrays.asList(dnsText.getText().split(","));
    }
	
	public String getName() {
        
		return nameText.getText();
    }
	
}