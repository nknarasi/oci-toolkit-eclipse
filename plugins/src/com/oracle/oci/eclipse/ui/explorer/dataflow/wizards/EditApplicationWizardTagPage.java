package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;

public class EditApplicationWizardTagPage extends WizardPage {
	
    private ISelection selection;
	private Application application;
    private Composite container;
    private ScrolledComposite sc;
    private Set<Tags> set=new HashSet<Tags>();

    public EditApplicationWizardTagPage(ISelection selection,String applicationId) {
        super("wizardPage");
        application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
        setTitle("Tag Wizard");
        setDescription("This wizard adds Tags.");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL| SWT.H_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );
    	sc.setLayoutData(new GridData());
    	
        container = new Composite(sc,SWT.NULL);
        sc.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 1;
        
        Button addTag = new Button(container,SWT.PUSH);
        addTag.setText("Add a Tag");        
        addTag.addSelectionListener(new SelectionAdapter() {        	
            public void widgetSelected(SelectionEvent e) {          	
            	Tags newtag= new Tags(container,sc,set);
            	set.add(newtag);
            }
          });     
        /*
        if(application.getDefinedTags() != null) {
        	for(Entry<String, Map<String, Object>> maintag : application.getDefinedTags().entrySet()) {
        		for(Entry<String,Object> tag : maintag.getValue().entrySet() ) {
        			Tags newtag= new Tags(container,sc,set);
                	set.add(newtag);
                	newtag.MainTagCombo.select(0);
                	if(tag.getKey().equals("CreatedBy"))
                		newtag.OracleTagCombo.select(0);
                	else if (tag.getKey().equals("CreatedOn"))
                		newtag.OracleTagCombo.select(1);
                	else
                		newtag.OracleTagCombo.select(0);
                	
                	newtag.TagValue.setText((String) tag.getValue());            		
        		}
        	}
        }
        */
        if(application.getFreeformTags() != null) {
        	for(Entry<String,String> tag : application.getFreeformTags().entrySet() ) {
    			Tags newtag= new Tags(container,sc,set);
            	set.add(newtag);
            	newtag.MainTagCombo.select(1);
            	newtag.FreeformTagKey.setText(tag.getKey());
            	newtag.TagValue.setText(tag.getValue());            		
    		}        	
        }
        
        setControl(sc);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	 
	 Map<String,Map<String,Object>> getOracleTags(){	 
		 Map<String,Map<String,Object>> OracleTags=new HashMap<String,Map<String,Object>>();
		 Map<String,Object> TempMap = new HashMap<String,Object>();		 
		 for(Tags tag:set) {
			 if(tag.MainTagCombo.getText().equals("Oracle-Tags")) {
				 if(tag.OracleTagCombo.getText() != null && tag.TagValue.getText() !=null)
					 TempMap.put(tag.OracleTagCombo.getText(),tag.TagValue.getText());
			 }
		 }
		 OracleTags.put("Oracle-Tags",TempMap);
		 return OracleTags;
	 }
	 
	 Map<String,String> getFreeformTags(){
		 Map<String,String> FreeformTags=new HashMap<String,String>();		 
		 for(Tags tag : set) {
			 if(!tag.MainTagCombo.getText().equals("Oracle-Tags")) {
				 if(tag.FreeformTagKey.getText()!=null && tag.TagValue.getText() != null)
				 FreeformTags.put(tag.FreeformTagKey.getText(), tag.TagValue.getText());
			 }
		 }		 
		 return FreeformTags;
	 }
}
