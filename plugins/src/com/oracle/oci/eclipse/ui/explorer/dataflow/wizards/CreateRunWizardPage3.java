package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;

public class CreateRunWizardPage3 extends WizardPage  {

	private ISelection selection;
	private Application application;
    private Composite container;
    private ScrolledComposite scrolledcomposite;
   	private Text LogLocationText;
	private Text WarehouseLocationText;	
    private Set<SparkProperty> CreatedPropertiesSet=new HashSet<SparkProperty>();
	private Composite PropertiesSection;
	private  DataTransferObject dto; 
	
	public CreateRunWizardPage3(ISelection selection,DataTransferObject dto,String applicationId) {
		super("Page 3");
		setTitle("Create DataFlow Application Page 3");
		setDescription("Advanced Options");
		this.dto = dto;
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
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
		
        PropertiesSection = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout4 = new GridLayout();
        innerTopLayout4.numColumns = 1;
        PropertiesSection.setLayout(innerTopLayout4);
        PropertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Button addProperty = new Button(PropertiesSection,SWT.PUSH);
        addProperty.setText("Add a Spark Property");        
        addProperty.addSelectionListener(new SelectionAdapter() {       	
            public void widgetSelected(SelectionEvent e) {           	
            	CreatedPropertiesSet.add(new SparkProperty(PropertiesSection,container,scrolledcomposite,CreatedPropertiesSet,dto.getData()));          	
            }
          });
        
        Label LogLocationlabel = new Label(container, SWT.NULL);
		LogLocationlabel.setText("&Application Log Location:");
		LogLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		LogLocationText.setText(application.getLogsBucketUri());
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		LogLocationText.setLayoutData(gd1);
		LogLocationText.setText(application.getLogsBucketUri());
				
		Label WarehouseLocationlabel = new Label(container, SWT.NULL);
		WarehouseLocationlabel.setText("&Warehouse Bucket Uri:");
		WarehouseLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		WarehouseLocationText.setLayoutData(gd2);
		if(application.getWarehouseBucketUri()!=null){
			WarehouseLocationText.setText(application.getWarehouseBucketUri());
		}		
		container.layout(true,true);
		setControl(container);		
	}

	public String getWarehouseUri() {		
		return WarehouseLocationText.getText();
	}
	
	public String getApplicationLogLocation() {		
		return LogLocationText.getText();
	}
	
	 public Map<String,String> getSparkProperties(){
		 Map<String,String> SparkProperties=new HashMap<String,String>();		 
		 for(SparkProperty Property : CreatedPropertiesSet) {			
			 SparkProperties.put(Property.TagKey.getText(), Property.TagValue.getText());	
		 }		 
		 return SparkProperties;
	 }
}
