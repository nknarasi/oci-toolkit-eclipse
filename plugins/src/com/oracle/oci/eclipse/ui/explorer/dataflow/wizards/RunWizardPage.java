package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.Run;
import com.oracle.bmc.dataflow.model.RunSummary;


public class RunWizardPage extends WizardPage {
    private Text nameText;
	private Combo dshapeCombo;
	private Combo eshapeCombo;
	private Spinner numExecSpinner;
    private ISelection selection;
	private Run run;
	private Application application;

    public RunWizardPage(ISelection selection,RunSummary runSum) {
        super("wizardPage");
        setTitle("Re Run Wizard");
        setDescription("This wizard creates a re-run request. Please enter the following details.");
        this.selection = selection;
		try {
			this.run=RunClient.getInstance().getRunDetails(runSum.getId());
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
    }
	
	public RunWizardPage(ISelection selection,ApplicationSummary appSum) {
        super("wizardPage");
        setTitle("Run Application Wizard");
        setDescription("This wizard creates a run application request. Please enter the following details.");
        this.selection = selection;
		try {
			this.application=ApplicationClient.getInstance().getApplicationDetails(appSum.getId());
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
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
        if(run!=null) 
        	nameText.setText(run.getDisplayName());
		else 
			nameText.setText(application.getDisplayName());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label dshapeLabel = new Label(container, SWT.NULL);
        dshapeLabel.setText("&Driver Shape:");
		dshapeCombo = new Combo(container, SWT.READ_ONLY);
		dshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			dshapeCombo.setItems(DataflowConstants.Shapes);
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
		
		if(run!=null) 
			dshapeCombo.setText(run.getDriverShape());
		else 
			dshapeCombo.setText(application.getDriverShape());
		
		Label eshapeLabel = new Label(container, SWT.NULL);
        eshapeLabel.setText("&Executor Shape:");
		eshapeCombo = new Combo(container, SWT.READ_ONLY);
		eshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			eshapeCombo.setItems(DataflowConstants.Shapes);
		} 
		catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
		
		if(run!=null) 
			eshapeCombo.setText(run.getExecutorShape());
		else 
			eshapeCombo.setText(application.getExecutorShape());
		
		Label numExecLabel = new Label(container, SWT.NULL);
        numExecLabel.setText("&Number of Executors:");
		numExecSpinner = new Spinner(container, SWT.BORDER);
		numExecSpinner.setMinimum(1);
		numExecSpinner.setMaximum(128);
		
		if(run!=null) 
			numExecSpinner.setSelection(run.getNumExecutors());
		else 
			numExecSpinner.setSelection(application.getNumExecutors());
		
		numExecSpinner.setIncrement(1);
		
		Label argLabel = new Label(container, SWT.NULL);
        argLabel.setText("&Arguments:");
        Text argText = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        
        if(run!=null) 
        	argText.setText(run.getArguments().toString());
		else 
			argText.setText(application.getArguments().toString());
        
        argText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        setControl(container);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public Object[] getDetails() {
        
		return (new Object[]{run.getApplicationId(),run.getArchiveUri(),null,run.getCompartmentId(),null,null,
				nameText.getText().trim(),dshapeCombo.getText(),run.getExecute(),eshapeCombo.getText(),null,
				run.getLogsBucketUri(),numExecSpinner.getSelection(),run.getParameters(),run.getSparkVersion(),
				run.getWarehouseBucketUri(),run.getOpcRequestId()
				});
    }
	
	public Object[] getDetails_app() {
        
		return (new Object[]{application.getId(),application.getArchiveUri(),null,application.getCompartmentId(),null,null,
				nameText.getText().trim(),dshapeCombo.getText(),application.getExecute(),eshapeCombo.getText(),null,
				application.getLogsBucketUri(),numExecSpinner.getSelection(),application.getParameters(),application.getSparkVersion(),
				application.getWarehouseBucketUri()
				});
    }
}