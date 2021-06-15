package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.ApplicationParameter;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.BucketSelectWizard;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class CreateApplicationWizardPage extends WizardPage {
	
	private Composite container;
	
	private Text displayNameText;
	private Text ApplicationDescriptionText;
	private Text compartmentText;
	
    private ScrolledComposite sc;
    
	private ISelection selection;
	
	private String selectedApplicationCompartmentId= AuthProvider.getInstance().getCompartmentId();
	private String selectedApplicationCompartmentName= AuthProvider.getInstance().getCompartmentName();
	
	
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

    private Composite basesqlcontainer;

    private Set<Parameters> sqlset=new HashSet<Parameters>();
		
	private Label MainClassNamelabel;
	private Label Argumentslabel;
	private Text MainClassNameText;
	private Text ArgumentsText;
	private Text ArchiveUriText;
	private Text FileUriText;
	private DataTransferObject dto;
	
	public CreateApplicationWizardPage(ISelection selection,DataTransferObject dto,String COMPARTMENT_ID) {
		super("page");
		setTitle("Create DataFlow Application");
		setDescription("This wizard creates a new DataFlow Application. Please enter the required details.");
		this.selection = selection;
		
		this.dto=dto;

		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		for(Compartment compartment : Allcompartments) {
			if(compartment.getId().equals(COMPARTMENT_ID)) {
				this.selectedApplicationCompartmentId= compartment.getId();
				this.selectedApplicationCompartmentName= compartment.getName();
				break;
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {	
		
		sc=new ScrolledComposite(parent,SWT.V_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );       
    	sc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		container = new Composite(sc, SWT.NULL);
		sc.setContent(container);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		
		Label compartmentLabel = new Label(container, SWT.NULL);
		compartmentLabel.setText("&Choose a compartment:");
		Composite innerTopContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compartmentText.setEditable(false);
        compartmentText.setText(selectedApplicationCompartmentName);

        Button compartmentButton = new Button(innerTopContainer, SWT.PUSH);
        compartmentButton.setText("Choose...");
        compartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectApplicationCompartmentEvent();
            }
        });
        
		final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
		final String defaultApplicationName = DATE_TIME_FORMAT.format(new Date());
		
		Label displayNameLabel = new Label(container, SWT.NULL);
		displayNameLabel.setText("&Display name:");
		displayNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		displayNameText.setLayoutData(gd);
		displayNameText.setText("App " + defaultApplicationName);
		
		
		Label Applicationdescriptionlabel = new Label(container, SWT.NULL);
		Applicationdescriptionlabel.setText("&Application Description:");
		ApplicationDescriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		ApplicationDescriptionText.setLayoutData(gd1);
		
		Label SparkVersionLabel = new Label(container, SWT.NULL);
		SparkVersionLabel.setText("&Spark Version:");
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		SparkVersionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		SparkVersionCombo.setLayoutData(gd2);		 
		SparkVersionCombo.setItems(DataflowConstants.Versions);
		SparkVersionCombo.select(0);		      
		
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
		 
		 LanguageUsed  = ApplicationLanguage.Java;            		
		 JavaLanguageSelected(container);		 
		 
		 setControl(sc);
		
	}
	
	private void createDriverShapeCombo(Composite container) {	
		DriverShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		DriverShapeCombo.setLayoutData(gd3);
		DriverShapeCombo.setItems(DataflowConstants.Shapes);		
		DriverShapeCombo.select(0);
		
	}
	
	private void createExecutorShapeCombo(Composite container) {
		ExecutorShapeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		ExecutorShapeCombo.setLayoutData(gd4);	 
		ExecutorShapeCombo.setItems(DataflowConstants.Shapes);		
		ExecutorShapeCombo.select(0);
		
	}
	
	private void createNumofExecutorsSpinner(Composite container) {
		NumofExecutorsSpinner = new Spinner(container, SWT.BORDER | SWT.SINGLE);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		NumofExecutorsSpinner.setLayoutData(gd5);
		NumofExecutorsSpinner.setMinimum(DataflowConstants.NUM_OF_EXECUTORS_MIN);
		NumofExecutorsSpinner.setMaximum(DataflowConstants.NUM_OF_EXECUTORS_MAX);
		NumofExecutorsSpinner.setIncrement(DataflowConstants.NUM_OF_EXECUTORS_INCREMENT);
		// default value
		NumofExecutorsSpinner.setSelection(DataflowConstants.NUM_OF_EXECUTORS_DEFAULT);
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
            	if(LanguageUsed != ApplicationLanguage.Java )
            	{
            		FileUriText.setText("");
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
            	if(LanguageUsed != ApplicationLanguage.Python )
            	{
            		disposePrevious();
            		FileUriText.setText("");
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
		LanguageGroupSQLRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(LanguageUsed != ApplicationLanguage.Sql )
            	{
            		disposePrevious();
            		FileUriText.setText("");
            		LanguageUsed  = ApplicationLanguage.Sql;            		
            		SQLLanguageSelected(container);            		
            	}
            	currentcontainer.layout(true,true);
            	currentcontainer.pack();
            	container.layout(true,true);

            }
        });	
		
		LanguageGroupScalaRadioButton = new Button(LanguageGroup, SWT.RADIO);
		LanguageGroupScalaRadioButton.setText("Scala");
		LanguageGroupScalaRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(LanguageUsed != ApplicationLanguage.Scala )
            	{
            		disposePrevious();
            		FileUriText.setText("");
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
		

		for(Parameters item : sqlset) {
			item.composite.dispose();
		}


		
		if(basesqlcontainer != null) {
			basesqlcontainer.dispose();
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
	
	private void SQLLanguageSelected(Composite parent) {
		
		basesqlcontainer = new Composite(parent, SWT.NULL);
		GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
		grid1.horizontalSpan = 2;
		basesqlcontainer.setLayoutData(grid1);
        GridLayout layout1 = new GridLayout();
        basesqlcontainer.setLayout(layout1);
        layout1.numColumns = 1;
		

        
        Button addParameter = new Button(basesqlcontainer,SWT.PUSH);
        addParameter.setLayoutData(new GridData());
        addParameter.setText("Add a Parameter");        
        addParameter.addSelectionListener(new SelectionAdapter() {        	
            public void widgetSelected(SelectionEvent e) {          	
            	Parameters newtag= new Parameters(basesqlcontainer,container,sc, sqlset);
            	sqlset.add(newtag);
            }
          });        

	}
	
	private void handleSelectApplicationCompartmentEvent() {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedApplicationCompartmentId = compartment.getId();
					selectedApplicationCompartmentName = compartment.getName();
					compartmentText.setText(selectedApplicationCompartmentName);
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
    	new CompartmentSelectWizard(consumer, false));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
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
				new BucketSelectWizard(consumer,selectedApplicationCompartmentId,LanguageUsed));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	public String getApplicationCompartmentId() {
		return selectedApplicationCompartmentId;
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
		return ArchiveUriText.getText().trim();
	}
	
	public String getMainClassName() {		
		return MainClassNameText.getText();
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
	
	public List<String> getArguments(){		
	    List<String> arguments = new ArrayList<String>();
	    String argumentsunseperated = ArgumentsText.getText();
	   boolean invertedcomma = false;
	   String currentword = "";
	   for(int i= 0 ; i < argumentsunseperated.length() ; i++) {
		   if(Character.isWhitespace(argumentsunseperated.charAt(i)))
		   {
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
