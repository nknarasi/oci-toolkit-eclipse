package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Tags {
	 Composite composite;
	 Combo MainTagCombo,OracleTagCombo;
	 Text FreeformTagKey,TagValue;
	 Button CloseButton;
	 String SelectedTag;
	 Label WarningLabel;
	 
	 Tags(Composite container, ScrolledComposite scrolledcomposite,Set<Tags> CreatedTagsSet){
		 
		 composite=new Composite(container,SWT.NONE);
		 composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 GridLayout GridLayout1 = new GridLayout();
		 GridLayout1.numColumns = 4 ;
		 composite.setLayout(GridLayout1);
		 CloseButton=new Button(composite,SWT.PUSH);
		 CloseButton.setText("Remove");
		 MainTagCombo=new Combo(composite,SWT.READ_ONLY);
		 MainTagCombo.setItems(new String[] {"Oracle-Tags","Free Form Tags"});
		 MainTagCombo.setText("Free Form Tags");
		 SelectedTag = "Free Form Tags";
		 FreeformTagKey=new Text(composite,SWT.BORDER);
		 FreeformTagKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 FreeformTagKey.setMessage("key");
		 TagValue = new Text(composite,SWT.BORDER);
		 TagValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 TagValue.setMessage("value");
		 refresh( container, scrolledcomposite);
		 addTagComboList(container, scrolledcomposite,CreatedTagsSet);
		 addClose(container, scrolledcomposite,CreatedTagsSet);
	 }
	
	 void refresh(Composite container, ScrolledComposite scrolledcomposite ) {
		 container.layout(true,true);
     	 scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	 }
	 

	 void addClose(Composite container, ScrolledComposite scrolledcomposite,Set<Tags> CreatedTagsSet) {
		 CloseButton.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
					 composite.dispose();
					 CreatedTagsSet.remove(Tags.this);
					 refresh( container, scrolledcomposite);
	            }
	        });
	 }
	
	 void addTagComboList(Composite container, ScrolledComposite scrolledcomposite,Set<Tags> CreatedTagsSet) {
		 
		MainTagCombo.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  
		    	  if(!MainTagCombo.getText().equals(SelectedTag)) {
		    		  if(SelectedTag.equals("Oracle-Tags")) {
		    			  //not--;
		    			  SelectedTag="Free Form Tags";
		    		  }
		    		  else {		    			
		    				 // not++;
		    				  SelectedTag="Oracle-Tags";
		    			  
		    		  }
		    	  }
		    	  
		    	  if(OracleTagCombo!=null) OracleTagCombo.dispose();
		    	  if(FreeformTagKey!=null) FreeformTagKey.dispose();
		    	  if(TagValue!=null) TagValue.dispose();
		    	  if(WarningLabel!=null) WarningLabel.dispose();
		    	  
		    	  if(MainTagCombo.getText().equals("Oracle-Tags")) {
		    		  OracleTagCombo=new Combo(composite,SWT.READ_ONLY);
		    		  OracleTagCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    		  OracleTagCombo.setItems(new String[] {"CreatedBy","CreatedOn"});
		    		  OracleTagCombo.addSelectionListener(new SelectionAdapter() {
		    			      public void widgetSelected(SelectionEvent e) {
		    			    	  if(check(CreatedTagsSet, OracleTagCombo.getText()) ) {
		    			    		  	if(TagValue!=null) TagValue.dispose();
		    			    		  	if(WarningLabel==null) {
		    			    		  		WarningLabel = new Label(composite, SWT.NULL);
			    			    		  	WarningLabel.setText("&This Tag already exits");
		    			    		  	}
		    			    		  	
		    			    		  	refresh( container, scrolledcomposite);
		    			    		  	return;
		    			    	  }
		    			    	  else {
		    				    	  if(TagValue!=null) TagValue.dispose();
		    				    	  if(WarningLabel!=null) WarningLabel.dispose();
		    				    	  TagValue = new Text(composite,SWT.BORDER);
		    				    	  TagValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    				    	  TagValue.setMessage("Tag Value");
		    				    	  
		    				    	  refresh( container, scrolledcomposite);
		    				    	  
		    				    	  return;
		    			    	  }
		    			    	  
		    			    	  }
		    			});
		    	  }
		    	  else {
			  			FreeformTagKey=new Text(composite,SWT.BORDER);
			  			FreeformTagKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			  			FreeformTagKey.setMessage("Tag Key");
		    	  }
		    	  
		    	  TagValue = new Text(composite,SWT.BORDER);
		    	  TagValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    	  TagValue.setMessage("Tag Value");
		    	  
		    	  refresh( container, scrolledcomposite);
			   }
		});
	 }
	 
	 boolean check(Set<Tags> CreatedTagsSet,String selection ) {
		 int num=0;
		 for(Tags tag : CreatedTagsSet) {
			// System.out.println(tag.toString());
			 if(tag.MainTagCombo.getText().equals("Oracle-Tags") && tag.OracleTagCombo.getText().equals(selection) ) {
				 num++;
			 }
		 }
		 if(num>1)
			 return true;
		
     	return false;
	 }
	 
}
