package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

public class CreateApplicationWizardPage3   extends WizardPage {

    private Composite container;
    private ScrolledComposite scrolledcomposite;
	private ISelection selection;
	private Text LogLocationText;
	private Text WarehouseLocationText;
	private Group NetworkGroup;
	private Button NetworkGroupInternetAccessRadioButton;
	private Button NetworkGroupPrivateSubnetRadioButton;	
	private Composite NetworkSection;
	private Composite PrivateEndpointSection;
	private Composite innerTopContainer;
	private Button compartmentButton;
	private Text compartmentText;
	private Compartment selectedApplicationCompartment;
	public Combo PrivateEndpointsCombo;
	private Label compartmentLabel;
	private Label PrivateEndpointsLabel;
	private List<PrivateEndpointSummary> PrivateEndpoints;	
    private Set<SparkProperty> CreatedPropertiesSet=new HashSet<SparkProperty>();    
	private Composite PropertiesSection;
	private Composite ButtonComposite;
	private Composite AdvancedOptionsComposite;
	private  DataTransferObject dto; 	
	private boolean NetworkSectionSelected=false;
	private boolean UsesAdvancedOptions=false;
	private int intial = -1; 
	
	public CreateApplicationWizardPage3(ISelection selection,DataTransferObject dto) {
		super("Page 3");
		setTitle("Create DataFlow Application Page 3");
		setDescription("Advanced Options");
		this.selection = selection;		
		this.dto=dto;
		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		this.selectedApplicationCompartment = rootCompartment;
		if(dto.getApplicationId() != null)
		{
			Application application = ApplicationClient.getInstance().getApplicationDetails(dto.getApplicationId());
			String compartmentId = application.getCompartmentId();	
			List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
			for(Compartment compartment : Allcompartments) {
				if(compartment.getId().equals(compartmentId)) {
					this.selectedApplicationCompartment= compartment;
					break;
				}
			}
		}		
	}
	
	@Override
	public void createControl(Composite parent) {    	
    	scrolledcomposite=new ScrolledComposite(parent,SWT.V_SCROLL);
    	scrolledcomposite.setExpandHorizontal( true );
    	scrolledcomposite.setExpandVertical( true );
    	scrolledcomposite.setLayoutData(new GridData());    	
     	
        container = new Composite(scrolledcomposite,SWT.NULL);
        scrolledcomposite.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;					

        ButtonComposite = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout1 = new GridLayout();
        innerTopLayout1.numColumns = 1;
        ButtonComposite.setLayout(innerTopLayout1);
        ButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        AdvancedOptionsComposite = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout2 = new GridLayout();
        innerTopLayout2.numColumns = 1;
        AdvancedOptionsComposite.setLayout(innerTopLayout2);
        AdvancedOptionsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));      
 
        Button useadvancedoptionsButton =  new Button(ButtonComposite,SWT.CHECK);
     	useadvancedoptionsButton.setText("Show Advanced Options");
     	useadvancedoptionsButton.addSelectionListener(new SelectionAdapter() {
     	    @Override
     	    public void widgetSelected(SelectionEvent event) {
     	    	 Button btn = (Button) event.getSource();
     	    	 if(btn.getSelection()) {
     	    		UsesAdvancedOptions=true;
     	    		AdvancedOptionsComposite.setVisible(UsesAdvancedOptions);
     	    	 }
     	    	 else
     	    	 {
     	    		 UsesAdvancedOptions=false;
     	    		AdvancedOptionsComposite.setVisible(UsesAdvancedOptions); 
     	    	 }
     	    }
     	});
		
        PropertiesSection = new Composite(AdvancedOptionsComposite, SWT.NONE);
        GridLayout innerTopLayout3 = new GridLayout();
        innerTopLayout3.numColumns = 1;
        PropertiesSection.setLayout(innerTopLayout3);
        PropertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Button addProperty = new Button(PropertiesSection,SWT.PUSH);
        addProperty.setText("Add a Spark Property");       
        addProperty.addSelectionListener(new SelectionAdapter() {      	
            public void widgetSelected(SelectionEvent e) {
            CreatedPropertiesSet.add(new SparkProperty(PropertiesSection,AdvancedOptionsComposite,scrolledcomposite,CreatedPropertiesSet,dto.getData()));
    		 container.layout(true,true);
         	 scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
            }
          });

		Label LogLocationlabel = new Label(AdvancedOptionsComposite, SWT.NULL);
		LogLocationlabel.setText("&Application Log Location:");
		LogLocationText = new Text(AdvancedOptionsComposite, SWT.BORDER | SWT.SINGLE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		LogLocationText.setLayoutData(gd1);
		LogLocationText.setMessage("oci://dataflow-logs@" + ObjStorageClient.getInstance().getNamespace()+"/");
		
		Label WarehouseLocationlabel = new Label(AdvancedOptionsComposite, SWT.NULL);
		WarehouseLocationlabel.setText("&Warehouse Bucket Uri:");
		WarehouseLocationText = new Text(AdvancedOptionsComposite, SWT.BORDER | SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		WarehouseLocationText.setLayoutData(gd2);	
		Label NetworkAccesslabel = new Label(AdvancedOptionsComposite, SWT.NULL);
		NetworkAccesslabel.setText("&Choose Network Access:");

		NetworkSection = new Composite(AdvancedOptionsComposite, SWT.NONE);
        GridLayout innerTopLayout4 = new GridLayout();
        innerTopLayout4.numColumns = 1;
        NetworkSection.setLayout(innerTopLayout4);
        NetworkSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createNetworkSection(NetworkSection);

		AdvancedOptionsComposite.setVisible(UsesAdvancedOptions);
		container.layout(true,true);
		
		setControl(scrolledcomposite);
	}
	
	private void createNetworkSection(Composite currentcontainer) {		
		NetworkGroup = new Group(currentcontainer, SWT.NULL);
		RowLayout rowLayout1 = new RowLayout(SWT.HORIZONTAL);
        rowLayout1.spacing = 100;
        NetworkGroup.setLayout(rowLayout1);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		NetworkGroup.setLayoutData(gd3);
		NetworkGroupInternetAccessRadioButton = new Button(NetworkGroup, SWT.RADIO);
		NetworkGroupInternetAccessRadioButton.setText("Internet Access(No Subnet)");
		NetworkGroupInternetAccessRadioButton.setSelection(true);
		NetworkGroupPrivateSubnetRadioButton = new Button(NetworkGroup, SWT.RADIO);
		NetworkGroupPrivateSubnetRadioButton.setText("Secure Access to Private Subnet");
		NetworkGroupPrivateSubnetRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(!NetworkSectionSelected)
            	{
            		NetworkSectionSelected= true;
            		PrivateEndpointSection= new Composite(currentcontainer, SWT.NONE);
                    GridLayout innerTopLayout = new GridLayout();
                    innerTopLayout.numColumns = 1;
                    PrivateEndpointSection.setLayout(innerTopLayout);
                    PrivateEndpointSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    choosePrivateSubnet(PrivateEndpointSection);     
            	}
            	AdvancedOptionsComposite.layout(true,true);
        		container.layout(true,true);
        		scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
                container.pack();
            }
        });
		
		NetworkGroupInternetAccessRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if(NetworkSectionSelected){
            		NetworkSectionSelected= false;
            		compartmentText.dispose();
            		compartmentButton.dispose();
            		PrivateEndpointsCombo.dispose();
            		innerTopContainer.dispose();            		
            		PrivateEndpointSection.dispose();
            	} 
            	AdvancedOptionsComposite.layout(true,true);
        		container.layout(true,true);
                container.pack();
            }
        });
        container.layout(true,true);
        scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();
	}
	
	private void choosePrivateSubnet(Composite currentcontainer) {		
		compartmentLabel = new Label(currentcontainer, SWT.NULL);
		compartmentLabel.setText("&Choose a compartment:");
		innerTopContainer = new Composite(currentcontainer, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.numColumns = 2;
        innerTopContainer.setLayout(innerTopLayout);
        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compartmentText.setEditable(false);
        compartmentText.setText(selectedApplicationCompartment.getName());

        compartmentButton = new Button(innerTopContainer, SWT.PUSH);
        compartmentButton.setText("Choose...");
        compartmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleSelectApplicationCompartmentEvent(currentcontainer);
            }
        });	
        chooseSubnet(currentcontainer,selectedApplicationCompartment.getId());
        
        currentcontainer.layout(true,true);
        AdvancedOptionsComposite.layout(true,true);
        container.layout(true,true);
        scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();                
	}
	
	private void chooseSubnet(Composite currentcontainer, String compartmentId) {		     
		PrivateEndPointsClient oci = PrivateEndPointsClient.getInstance();
		PrivateEndpoints = oci.getPrivateEndPointsinCompartment(compartmentId);		
		int sizeoflist= PrivateEndpoints.size();
		String[] PrivateEndpointsList = new String[sizeoflist];
		for(int i = 0; i < PrivateEndpoints.size(); i++){  
			PrivateEndpointsList[i]= PrivateEndpoints.get(i).getDisplayName();
		}
		
		PrivateEndpointsLabel = new Label(currentcontainer, SWT.NULL);
		PrivateEndpointsLabel.setText("&Choose Private Endpoint:");
		
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		PrivateEndpointsCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		PrivateEndpointsCombo.setLayoutData(gd4);		 
		PrivateEndpointsCombo.setItems(PrivateEndpointsList);
		if(intial != -1)
			PrivateEndpointsCombo.select(intial);
        currentcontainer.layout(true,true);
        AdvancedOptionsComposite.layout(true,true);
        container.layout(true,true);
        scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        container.pack();
	}
	
	private void handleSelectApplicationCompartmentEvent(Composite currentcontainer) {
    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
			@Override
			public void accept(Compartment compartment) {
				if (compartment != null) {
					selectedApplicationCompartment = compartment;
					compartmentText.setText(selectedApplicationCompartment.getName());
					if(PrivateEndpointsCombo != null)
						PrivateEndpointsCombo.dispose();
					if(PrivateEndpointsLabel != null)
						PrivateEndpointsLabel.dispose();					
					chooseSubnet(currentcontainer,selectedApplicationCompartment.getId());
				}
			}
		};
    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
				new CompartmentSelectWizard(consumer, false));
		dialog.setFinishButtonText("Select");
		if (Window.OK == dialog.open()) {
		}
    }	
	
	public String getWarehouseUri() {		
		return WarehouseLocationText.getText().trim();
	}
	
	public String getApplicationLogLocation() {		
		return LogLocationText.getText().trim();
	}
	
	public String getPrivateEndPointId() {
		return PrivateEndpoints.get(PrivateEndpointsCombo.getSelectionIndex()).getId()   ;		
	}
	
	 public Map<String,String> getSparkProperties(){
		 Map<String,String> SparkProperties=new HashMap<String,String>();
		 
		 for(SparkProperty Property : CreatedPropertiesSet) {			
			 SparkProperties.put(Property.TagKey.getText(), Property.TagValue.getText());	
		 }
		 return SparkProperties;
	 }
	 
	 public boolean usesAdvancedOptions(){
		 return UsesAdvancedOptions;		 
	 }
	 
	 public boolean usesPrivateSubnet(){
		 return NetworkSectionSelected;		 
	 }	
	 

		void onEnterPage()
		{
		    final DataTransferObject dto = ((LocalFileSelectWizard) getWizard()).dto;
		    String applicationId = dto.applicationId;
		    
		    if(applicationId != null) {
		    		Application application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
		    	   if(application.getConfiguration() != null) {        	
		          	 for (Map.Entry<String,String> property : application.getConfiguration().entrySet()) {
		          		 SparkProperty propertypresent = new SparkProperty(PropertiesSection,AdvancedOptionsComposite,scrolledcomposite,CreatedPropertiesSet,application.getSparkVersion());
		          		 CreatedPropertiesSet.add(propertypresent);
		          		 propertypresent.TagKey.setText(property.getKey());
		  				 propertypresent.TagValue.setText(property.getValue());
		          	 }         	
		      		 container.layout(true,true);
		           	 scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );		          	
		          } 
		    	   
		    	   LogLocationText.setText(application.getLogsBucketUri());
		    	   
		    	   if(application.getWarehouseBucketUri() != null) {
		   			WarehouseLocationText.setText(application.getWarehouseBucketUri());
		   		}
		    	
		    	
		    	   if(NetworkSectionSelected)
	            	{	            		
	            		NetworkSectionSelected= false;	            		
	            		compartmentLabel.dispose();
	            		compartmentText.dispose();
	            		compartmentButton.dispose();
	            		PrivateEndpointsCombo.dispose();
	            		innerTopContainer.dispose();            		
	            		PrivateEndpointSection.dispose();	
	            	} 
		    	   
		    	NetworkGroupInternetAccessRadioButton.setSelection(false);
		   		if(application.getPrivateEndpointId() != null && !application.getPrivateEndpointId().equals("")) {
					NetworkGroupPrivateSubnetRadioButton.setSelection(true);
					NetworkSectionSelected= true;
					
					PrivateEndpointSection= new Composite(NetworkSection, SWT.NONE);
		            GridLayout innerTopLayout = new GridLayout();
		            innerTopLayout.numColumns = 1;
		            PrivateEndpointSection.setLayout(innerTopLayout);
		            PrivateEndpointSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		            PrivateEndPointsClient oci = PrivateEndPointsClient.getInstance();
		            PrivateEndpoint current = oci.getPrivateEndpointDetails(application.getPrivateEndpointId());
		    		Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
		    		List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
		    		for(Compartment compartment : Allcompartments) {
		    			if(compartment.getId().equals(current.getCompartmentId())) {
		    				 selectedApplicationCompartment = compartment;   			
		    				 break;
		    			}
		    		}
		    		
		    		PrivateEndpoints = oci.getPrivateEndPointsinCompartment(selectedApplicationCompartment.getId());		
		    		int sizeoflist= PrivateEndpoints.size();
		    		String[] PrivateEndpointsList = new String[sizeoflist];
		    		for(int i = 0; i < PrivateEndpoints.size(); i++)
		    		{  
		    			PrivateEndpointsList[i]= PrivateEndpoints.get(i).getDisplayName();
		    			if(PrivateEndpoints.get(i).getId().equals(application.getPrivateEndpointId())) {
		    				intial = i;
		    				break;
		    			}
		    		}
		    		
		            choosePrivateSubnet(PrivateEndpointSection);     								

				}
				else {
					NetworkGroupInternetAccessRadioButton.setSelection(true);
					NetworkSectionSelected= false;
				}

		    	   
		    }
		    
		    container.pack();
		    container.layout(true,true);
		    scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		}
	 
	 
}
