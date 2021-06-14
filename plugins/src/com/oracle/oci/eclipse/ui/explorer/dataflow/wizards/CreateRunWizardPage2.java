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

public class CreateRunWizardPage2 extends WizardPage{
	
    private ISelection selection;
    private Composite container;
    private ScrolledComposite sc;
    private Set<Tags> set=new HashSet<Tags>();

    public CreateRunWizardPage2(ISelection selection) {
        super("wizardPage");
        setTitle("Run Wizard Page 2");
        setDescription("This wizard adds Tags.");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL);
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
            	set.add(new Tags(container,sc,set));
            }
          });        
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
				 FreeformTags.put(tag.FreeformTagKey.getText(), tag.TagValue.getText());
			 }
		 }		 
		 return FreeformTags;
	 }
}
