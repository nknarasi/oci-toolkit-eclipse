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
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class CreateRunWizardPage1  extends WizardPage{
    private ScrolledComposite sc;
	private ISelection selection;
	private Application application;
	private DataTransferObject dto;
	private Text displayNameText;
	private Combo SparkVersionCombo;
	private Combo DriverShapeCombo;
	private Combo ExecutorShapeCombo;
	private Spinner NumofExecutorsSpinner;		
	private Text ArgumentsText;
	private Text ArchiveUriText;
    private Set<Parameters> sqlset=new HashSet<Parameters>();
    
	public CreateRunWizardPage1(ISelection selection,DataTransferObject dto, String applicationId) {
		super("page");
		setTitle("Schedule Run for Application");
		setDescription("This wizard run for a DataFlow Application. Please enter the required details.");
		this.selection = selection;
		this.dto = dto;
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}	
	
	@Override
	public void createControl(Composite parent) {
		
		sc=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );       
    	sc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite container = new Composite(sc, SWT.NULL);
		sc.setContent(container);
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
		SparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		SparkVersionCombo.setLayoutData(gd2);		 
		SparkVersionCombo.setItems(DataflowConstants.Versions);
		for(int i=0; i<DataflowConstants.Versions.length ; i++) {
			if(application.getSparkVersion().equals(DataflowConstants.Versions[i])) {
				SparkVersionCombo.select(i);
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
		ArgumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		ArgumentsText.setText(application.getArguments().toString());
		ArgumentsText.setEditable(false);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		ArgumentsText.setLayoutData(gd8);
		
		Composite basesqlcontainer = new Composite(container, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan = 2;
		basesqlcontainer.setLayoutData(grid1);
        GridLayout layout1 = new GridLayout();
        basesqlcontainer.setLayout(layout1);
        layout1.numColumns = 1;
		
        if(application.getParameters() != null)
        {
        	 for (ApplicationParameter parameter : application.getParameters()) {
             	Parameters newparameter = new Parameters(basesqlcontainer,container,sc, sqlset);
             	sqlset.add(newparameter);
        		newparameter.TagKey.setText(parameter.getName());
        		newparameter.TagKey.setEditable(false);
     			newparameter.TagValue.setText(parameter.getValue());
     			newparameter.CloseButton.setEnabled(false);
        	 		}         	
        }          
      	container.layout(true,true);
		setControl(sc);
	}
	private void createDriverShapeCombo(Composite container) {		
		DriverShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		DriverShapeCombo.setLayoutData(gd3);		 
		DriverShapeCombo.setItems(DataflowConstants.Shapes);
		for(int i=0; i<DataflowConstants.Shapes.length ; i++) {
			if(application.getDriverShape().equals(DataflowConstants.Shapes[i])) {
				DriverShapeCombo.select(i);
			}
		}		
	}
	
	private void createExecutorShapeCombo(Composite container) {		
		ExecutorShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		ExecutorShapeCombo.setLayoutData(gd4); 
		ExecutorShapeCombo.setItems(DataflowConstants.Shapes);
		for(int i=0; i<DataflowConstants.Shapes.length ; i++) {
			if(application.getExecutorShape().equals(DataflowConstants.Shapes[i])) {
				ExecutorShapeCombo.select(i);
			}
		}		
	}
	
	private void createNumofExecutorsSpinner(Composite container) {
		NumofExecutorsSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		NumofExecutorsSpinner.setLayoutData(gd5);
		NumofExecutorsSpinner.setMinimum(DataflowConstants.NUM_OF_EXECUTORS_MIN);
		NumofExecutorsSpinner.setMaximum(DataflowConstants.NUM_OF_EXECUTORS_MAX);
		NumofExecutorsSpinner.setIncrement(DataflowConstants.NUM_OF_EXECUTORS_INCREMENT);
		// default value
		NumofExecutorsSpinner.setSelection(application.getNumExecutors());
	}
		
	public String getDisplayName() {
		return displayNameText.getText();
	}
	
	public String getSparkVersion() {		
		return SparkVersionCombo.getText();
	}
	
	public String getDriverShape() {		
		return DriverShapeCombo.getText();
	}
	
	public String getExecutorShape() {		
		return ExecutorShapeCombo.getText();
	}
	
	public String getNumofExecutors() {		
		return NumofExecutorsSpinner.getText();
	}
	
	public String getArchiveUri() {		
		return ArchiveUriText.getText();
	}
	
	public  List<ApplicationParameter> getParameters(){
		List<ApplicationParameter> Parameters = new ArrayList<ApplicationParameter>();	 
		 for(Parameters parameter : sqlset) {	
			 Parameters.add(ApplicationParameter.builder()
					 .name(parameter.TagKey.getText())
					 .value(parameter.TagValue.getText())
					 .build());
		 }		 
		 return Parameters;
	 }
	
	 @Override
	    public IWizardPage getNextPage() {
	        dto.setData(this.SparkVersionCombo.getText().toString());	        
	        return super.getNextPage();
	    }
	 
}
