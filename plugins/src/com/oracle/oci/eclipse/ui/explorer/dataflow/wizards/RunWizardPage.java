package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Shape;
import com.oracle.bmc.core.requests.ListShapesRequest;
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
	private Application app;

    public RunWizardPage(ISelection selection,RunSummary runSum) {
        super("wizardPage");
        setTitle("Re Run Wizard");
        setDescription("This wizard creates a re-run request. Please enter the following details.");
        this.selection = selection;
		try {
			this.run=RunClient.getInstance().getRunDetails(runSum.getId());
		} catch (Exception e) {
			
		}
    }
	
	public RunWizardPage(ISelection selection,ApplicationSummary appSum) {
        super("wizardPage");
        setTitle("Run Application Wizard");
        setDescription("This wizard creates a run application request. Please enter the following details.");
        this.selection = selection;
		try {
			this.app=ApplicationClient.getInstance().getApplicationDetails(appSum.getId());
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
        if(run!=null) nameText.setText(run.getDisplayName());
		else nameText.setText(app.getDisplayName());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        nameText.setLayoutData(gd);
		
		Label dshapeLabel = new Label(container, SWT.NULL);
        dshapeLabel.setText("&Driver Shape:");
		dshapeCombo = new Combo(container, SWT.READ_ONLY);
		dshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			dshapeCombo.setItems(DataflowConstants.Shapes);
		} catch (Exception e) {
			
		}
		if(run!=null) dshapeCombo.setText(run.getDriverShape());
		else dshapeCombo.setText(app.getDriverShape());
		
		Label eshapeLabel = new Label(container, SWT.NULL);
        eshapeLabel.setText("&Executor Shape:");
		eshapeCombo = new Combo(container, SWT.READ_ONLY);
		eshapeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			eshapeCombo.setItems(DataflowConstants.Shapes);
		} catch (Exception e) {
			
		}
		if(run!=null) eshapeCombo.setText(run.getExecutorShape());
		else eshapeCombo.setText(app.getExecutorShape());
		
		Label numExecLabel = new Label(container, SWT.NULL);
        numExecLabel.setText("&Number of Executors:");
		numExecSpinner = new Spinner(container, SWT.BORDER);
		numExecSpinner.setMinimum(1);
		numExecSpinner.setMaximum(128);
		if(run!=null) numExecSpinner.setSelection(run.getNumExecutors());
		else numExecSpinner.setSelection(app.getNumExecutors());
		numExecSpinner.setIncrement(1);
		
		Label argLabel = new Label(container, SWT.NULL);
        argLabel.setText("&Arguments:");
        Text argText = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        if(run!=null) argText.setText(run.getArguments().toString());
		else argText.setText(app.getArguments().toString());
        GridData gdd = new GridData(GridData.FILL_HORIZONTAL);
        argText.setLayoutData(gdd);
		
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
        
		return (new Object[]{app.getId(),app.getArchiveUri(),null,app.getCompartmentId(),null,null,
				nameText.getText().trim(),dshapeCombo.getText(),app.getExecute(),eshapeCombo.getText(),null,
				app.getLogsBucketUri(),numExecSpinner.getSelection(),app.getParameters(),app.getSparkVersion(),
				app.getWarehouseBucketUri()
				});
    }
	
	private List<String[]> getShapes() throws Exception{
		List<String[]> rl=new ArrayList<String[]>();
		Path path=Path.of("D:\\\\Oracle\\\\Oracle_docs\\\\test.txt");
		String[] s=Files.readString(path).split("#");
		List<String> l=new ArrayList<String>(),l1=new ArrayList<String>(),l2=new ArrayList<String>();
		for(String e:s) {
			String[] sl=e.split(":");
			String ns=sl[0].trim();
			if(ns.equalsIgnoreCase("driver-shapes")) l=l1;
			else if(ns.equalsIgnoreCase("executor-shapes")) l=l2;
			for(String ne:sl[1].split(",")) l.add(ne.trim());
		}
		rl.add(l1.toArray(new String[0]));rl.add(l2.toArray(new String[0]));
		return rl;
	}
}