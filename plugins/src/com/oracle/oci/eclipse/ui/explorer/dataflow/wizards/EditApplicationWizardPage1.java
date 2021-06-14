package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.BucketSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class EditApplicationWizardPage1  extends WizardPage {
	
	private Text displayNameText;
	private Text ApplicationDescriptionText;
	private Application application;
	private ISelection selection;
	private Composite container;
	private Compartment selectedApplicationCompartment;
	
	private Combo SparkVersionCombo;
	private Combo DriverShapeCombo;
	private Combo ExecutorShapeCombo;
	private Spinner NumofExecutorsSpinner;
	
	private Group LanguageGroup;
	private Button LanguageGroupJavaRadioButton;
	private Button LanguageGroupPythonRadioButton;
	private Button LanguageGroupSQLRadioButton;
	private Button LanguageGroupScalaRadioButton;
	
	private ApplicationLanguage LanguageUsed;
		
	private Label MainClassNamelabel;
	private Label Argumentslabel;
	private Text MainClassNameText;
	private Text ArgumentsText;
	private Text ArchiveUriText;
	private Text FileUriText;	
	private DataTransferObject dto;

	public EditApplicationWizardPage1(ISelection selection,DataTransferObject dto,String applicationId) {
		super("Page 1");
		setTitle("EditDataFlow Application");
		setDescription("This wizard edits a new DataFlow Application. Please enter the required details.");
		this.selection = selection;
		
		this.dto=dto;
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
		String compartmentId = application.getCompartmentId();	
		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : Allcompartments) {
			if(compartment.getId().equals(compartmentId)) {
				this.selectedApplicationCompartment= compartment;
				break;
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name:");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText(application.getDisplayName());
		
		Label Applicationdescriptionlabel = new Label(container, SWT.NULL);
		Applicationdescriptionlabel.setText("&Application Description:");
		ApplicationDescriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		ApplicationDescriptionText.setLayoutData(gd1);
		if(application.getDescription()!=null)
			ApplicationDescriptionText.setText(application.getDescription());
		
		Label SparkVersionLabel = new Label(container, SWT.NULL);
		SparkVersionLabel.setText("&Spark Version:");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		SparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		SparkVersionCombo.setLayoutData(gd2);		 
		SparkVersionCombo.setItems(DataflowConstants.Versions);
		if(application.getSparkVersion().equals(DataflowConstants.Versions[0])) {
			SparkVersionCombo.select(0);
		}
		else {
			SparkVersionCombo.select(1);
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
					
		Label LanguageLabel = new Label(container, SWT.NULL);
		LanguageLabel.setText("&Language:");
		createLanguageCombo(container);
		
		Label FileUriLabel = new Label(container, SWT.NULL);
		FileUriLabel.setText("&Choose a File:");
		Composite FileUriContainer = new Composite(container, SWT.NONE);
        GridLayout FileUriLayout = new GridLayout();
        FileUriLayout.numColumns = 2;
        FileUriContainer.setLayout(FileUriLayout);
        FileUriContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        FileUriText = new Text(FileUriContainer, SWT.BORDER | SWT.SINGLE);
        FileUriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FileUriText.setEditable(false);
        FileUriText.setText(application.getFileUri());

        Button compartmentButton2 = new Button(FileUriContainer, SWT.PUSH);
        compartmentButton2.setText("Choose...");
        compartmentButton2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectObjectEvent();
            }
        });
        
		 Label ArchiveUrilabel = new Label(container, SWT.NULL);
		 ArchiveUrilabel.setText("&Archive URL:");
		 ArchiveUriText = new Text(container, SWT.BORDER | SWT.SINGLE);
		 GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
		 ArchiveUriText.setLayoutData(gd10);		 
		 if(application.getArchiveUri()!=null)
			 ArchiveUriText.setText(application.getArchiveUri());

		 LanguageUsed  = application.getLanguage();
		 if(LanguageUsed == ApplicationLanguage.Java ||LanguageUsed == ApplicationLanguage.Scala ) {
			 JavaLanguageSelected(container);
		 }
		 else if(LanguageUsed == ApplicationLanguage.Python ) {
			 PythonLanguageSelected(container);
		 }		 			
		setControl(container);		
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
	
	private void createLanguageCombo(Composite currentcontainer) {
				
		
		LanguageGroup = new Group(currentcontainer, SWT.NONE);
		RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
		LanguageGroup.setLayout(rowLayout1);
		GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
		LanguageGroup.setLayoutData(gd6);
		
		LanguageGroupJavaRadioButton = new Button(LanguageGroup, SWT.RADIO);
		LanguageGroupJavaRadioButton.setText("Java");		
		LanguageGroupJavaRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(LanguageUsed != ApplicationLanguage.Java ){
            		disposePrevious();
            		LanguageUsed  = ApplicationLanguage.Java;            		
            		JavaLanguageSelected(container);            		
            	}
            	currentcontainer.layout(true,true);
            	container.layout(true,true);
            	currentcontainer.pack();
            }
        });
      
		LanguageGroupJavaRadioButton.setSelection(true); 
		
		LanguageGroupPythonRadioButton = new Button(LanguageGroup, SWT.RADIO);
		LanguageGroupPythonRadioButton.setText("Python");		
		LanguageGroupPythonRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(LanguageUsed != ApplicationLanguage.Python ){
            		disposePrevious();
            		LanguageUsed  = ApplicationLanguage.Python;            		
            		PythonLanguageSelected(container);            		
            	}
            	currentcontainer.layout(true,true);
            	currentcontainer.pack();
            	container.layout(true,true);
            }
        });	
		
		LanguageGroupSQLRadioButton = new Button(LanguageGroup, SWT.RADIO);
		LanguageGroupSQLRadioButton.setText("SQL");		
		
		LanguageGroupScalaRadioButton = new Button(LanguageGroup, SWT.RADIO);
		LanguageGroupScalaRadioButton.setText("Scala");
		LanguageGroupScalaRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(LanguageUsed != ApplicationLanguage.Scala )
            	{
            		disposePrevious();
            		LanguageUsed  = ApplicationLanguage.Scala;            		
            		JavaLanguageSelected(container);            		
            	}
            	
            	currentcontainer.layout(true,true);
            	container.layout(true,true);
            	currentcontainer.pack();
            }
        });

	}
	private void disposePrevious() {
		if(MainClassNameText != null) {
			MainClassNameText.dispose();
		}
		if(ArgumentsText != null) {
			ArgumentsText.dispose();
		}
		if(Argumentslabel != null) {
			Argumentslabel.dispose();
		}
		if(MainClassNamelabel != null) {
			MainClassNamelabel.dispose();
		}
	}
	private void JavaLanguageSelected(Composite container) {	
		MainClassNamelabel = new Label(container, SWT.NULL);
		MainClassNamelabel.setText("&Main Class Name:");
		MainClassNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd7 = new GridData(GridData.FILL_HORIZONTAL);
		MainClassNameText.setLayoutData(gd7);		
		Argumentslabel = new Label(container, SWT.NULL);
		Argumentslabel.setText("&Arguments:");
		ArgumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		ArgumentsText.setLayoutData(gd8);
	}
	
	private void PythonLanguageSelected(Composite container) {		
		Argumentslabel = new Label(container, SWT.NULL);
		Argumentslabel.setText("&Arguments:");
		ArgumentsText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
		ArgumentsText.setLayoutData(gd8);
	}
	
	
	private void handleSelectObjectEvent() {
    	Consumer<String> consumer=new Consumer<String>() {
			@Override
			public void accept(String object) {
				if (object != null) {
					FileUriText.setText(object);
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
				new BucketSelectWizard(consumer,selectedApplicationCompartment.getId(),LanguageUsed));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
		
	public String getDisplayName() {
		return displayNameText.getText();
	}
	
	public String getApplicationDescription() {
		return ApplicationDescriptionText.getText();
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
	
	public ApplicationLanguage getLanguage() {		
		if(LanguageGroupJavaRadioButton.getSelection()) {
			return ApplicationLanguage.Java;
		}
		else if(LanguageGroupPythonRadioButton.getSelection()) {
			return ApplicationLanguage.Python;
		}
		else if(LanguageGroupSQLRadioButton.getSelection()) {
			return ApplicationLanguage.Sql;
		}
		else{
			return ApplicationLanguage.Scala;
		}
	}
	
	public String getFileUri() {		
		return FileUriText.getText();
	}
	
	public String getArchiveUri() {		
		return ArchiveUriText.getText();
	}
	
	public String getMainClassName() {		
		return MainClassNameText.getText();
	}
			
	public List<String> getArguments(){		
	    List<String> arguments = new ArrayList<String>();
	    String argumentsunseperated = ArgumentsText.getText();
	   boolean invertedcomma = false;
	   String currentword = "";
	   for(int i= 0 ; i < argumentsunseperated.length() ; i++) {
		   if(Character.isWhitespace(argumentsunseperated.charAt(i))){
			   if(invertedcomma == true){
				   currentword+= argumentsunseperated.charAt(i);
			   }
			   else{
				   if(currentword != ""){
					   arguments.add(currentword);
					   currentword="";
				   }
			   }
		   }
		   else if (argumentsunseperated.charAt(i) == '"') {
			   if(invertedcomma == false) {
				   if(currentword == "") {
					   invertedcomma=true;
					   currentword+=argumentsunseperated.charAt(i);
				   }
				   else {
					   currentword+=argumentsunseperated.charAt(i);
				   }
			   }
			   else {
				   currentword += argumentsunseperated.charAt(i);
				   invertedcomma=false;
			   }	  
		   }
		   else{
			   currentword += argumentsunseperated.charAt(i);
		   }		   
	   }
	  if(currentword!="")
		   arguments.add(currentword);
		   
	    return arguments;		
	}
	
	 @Override
	    public IWizardPage getNextPage() {
	        dto.setData(this.SparkVersionCombo.getText().toString());	        
	        return super.getNextPage();
	    }
}
