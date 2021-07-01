package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class CreateRunWizardPage1  extends WizardPage{
    private ScrolledComposite scrolledComposite;
	private ISelection selection;
	private Application application;
	private DataTransferObject dto;
	private Text displayNameText;
	private Combo sparkVersionCombo;
	private Combo driverShapeCombo;
	private Combo executorShapeCombo;
	private Spinner numofExecutorsSpinner;		
	private Text argumentsText;
	private Text archiveUriText;
    private Set<Parameters> parameterset;
    
	public CreateRunWizardPage1(ISelection selection,DataTransferObject dto, String applicationId) {
		super("page");
		setTitle("Start Run for Application");
		setDescription("This wizard starts a run for the DataFlow Application. Please enter the required details.");
		this.selection = selection;
		this.dto = dto;
		this.parameterset = new HashSet<Parameters>();
		application = DataflowClient.getInstance().getApplicationDetails(applicationId);
	}	
	
	@Override
	public void createControl(Composite parent) {
		
		scrolledComposite=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal( true );
		scrolledComposite.setExpandVertical( true );       
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite container = new Composite(scrolledComposite, SWT.NULL);
		scrolledComposite.setContent(container);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		final String defaultRunName = application.getDisplayName();		
		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name:");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText(defaultRunName);
		
		Label SparkVersionLabel = new Label(container, SWT.NULL);
		SparkVersionLabel.setText("&Spark Version:");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		sparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		sparkVersionCombo.setLayoutData(gd2);		 
		sparkVersionCombo.setItems(DataflowConstants.Versions);
		for(int i=0; i<DataflowConstants.Versions.length ; i++) {
			if(application.getSparkVersion().equals(DataflowConstants.Versions[i])) {
				sparkVersionCombo.select(i);
			}
		}
		
		Label DriverShapeLabel = new Label(container, SWT.NULL);
		DriverShapeLabel.setText("&Driver Shape:");
		createDriverShapeCombo(container);		

		Label ExecutorShapeLabel = new Label(container, SWT.NULL);
		ExecutorShapeLabel.setText("&Executor Shape:");
		createExecutorShapeCombo(container);
		
		Label NumofExecutorslabel = new Label(container, SWT.NULL);
		NumofExecutorslabel.setText("&Number of Executors:");
		createNumofExecutorsSpinner(container);
		
		Label Argumentslabel = new Label(container, SWT.NULL);
		Argumentslabel.setText("&Arguments:");
		argumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		argumentsText.setText(application.getArguments().toString());
		argumentsText.setEditable(false);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		argumentsText.setLayoutData(gd8);
		
		Composite parametercontainer = new Composite(container, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan = 2;
		parametercontainer.setLayoutData(grid1);
        GridLayout layout1 = new GridLayout();
        parametercontainer.setLayout(layout1);
        layout1.numColumns = 1;
		
        if(application.getParameters() != null)
        {
        	 for (ApplicationParameter parameter : application.getParameters()) {
             	Parameters newparameter = new Parameters(parametercontainer,container,scrolledComposite, parameterset);
             	parameterset.add(newparameter);
        		newparameter.tagKey.setText(parameter.getName());
        		newparameter.tagKey.setEditable(false);
     			newparameter.tagValue.setText(parameter.getValue());
     			newparameter.closeButton.setEnabled(false);
        	 	}         	
        }          
      	container.layout(true,true);
		setControl(scrolledComposite);
	}
	private void createDriverShapeCombo(Composite container) {		
		driverShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		driverShapeCombo.setLayoutData(gd3);		 
		driverShapeCombo.setItems(DataflowConstants.ShapesDetails);
		for(int i=0; i<DataflowConstants.ShapesDetails.length ; i++) {
			if(application.getDriverShape().equals(DataflowConstants.ShapesDetails[i].split(" ")[0])) {
				driverShapeCombo.select(i);
			}
		}		
	}
	
	private void createExecutorShapeCombo(Composite container) {		
		executorShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		executorShapeCombo.setLayoutData(gd4); 
		executorShapeCombo.setItems(DataflowConstants.ShapesDetails);
		for(int i=0; i<DataflowConstants.ShapesDetails.length ; i++) {
			if(application.getExecutorShape().equals(DataflowConstants.ShapesDetails[i].split(" ")[0])) {
				executorShapeCombo.select(i);
			}
		}		
	}
	
	private void createNumofExecutorsSpinner(Composite container) {
		numofExecutorsSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		numofExecutorsSpinner.setLayoutData(gd5);
		numofExecutorsSpinner.setMinimum(DataflowConstants.NUM_OF_EXECUTORS_MIN);
		numofExecutorsSpinner.setMaximum(DataflowConstants.NUM_OF_EXECUTORS_MAX);
		numofExecutorsSpinner.setIncrement(DataflowConstants.NUM_OF_EXECUTORS_INCREMENT);
		// default value
		numofExecutorsSpinner.setSelection(application.getNumExecutors());
	}
		
	public String getDisplayName() {
		return displayNameText.getText();
	}
	
	public String getSparkVersion() {		
		return sparkVersionCombo.getText();
	}
	
	public String getDriverShape() {		
		return driverShapeCombo.getText().split(" ")[0];
	}
	
	public String getExecutorShape() {		
		return executorShapeCombo.getText().split(" ")[0];
	}
	
	public String getNumofExecutors() {		
		return numofExecutorsSpinner.getText();
	}
	
	public String getArchiveUri() {		
		return archiveUriText.getText();
	}
	
	public  List<ApplicationParameter> getParameters(){
		List<ApplicationParameter> Parameters = new ArrayList<ApplicationParameter>();	 
		 for(Parameters parameter : parameterset) {	
			 Parameters.add(ApplicationParameter.builder()
					 .name(parameter.tagKey.getText())
					 .value(parameter.tagValue.getText())
					 .build());
		 }		 
		 return Parameters;
	 }
	
	 @Override
	    public IWizardPage getNextPage() {
	        dto.setData(this.sparkVersionCombo.getText().toString());	        
	        return super.getNextPage();
	    }
	 
}
