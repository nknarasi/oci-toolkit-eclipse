package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class Parameters {
	 Composite composite;
	 Text TagValue;
	 Text TagKey;
	 Button CloseButton;
	 
	 Parameters(Composite current,Composite container, ScrolledComposite scrolledcomposite,Set<Parameters> CreatedParametersSet){
		 
		 composite=new Composite(current,SWT.NONE);
		 composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 GridLayout GridLayout1 = new GridLayout();
		 GridLayout1.numColumns = 3 ;
		 composite.setLayout(GridLayout1);
		 
		 TagKey = new Text(composite,SWT.BORDER);
		 TagKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 TagKey.setMessage("Key");
		 
		 TagValue = new Text(composite,SWT.BORDER);
		 TagValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 TagValue.setMessage("Value");
		 CloseButton=new Button(composite,SWT.PUSH);
		 CloseButton.setText("Remove");
		 //refresh(composite, container, scrolledcomposite);
		 addClose(composite,container, scrolledcomposite,CreatedParametersSet);
	 }
	
	 void refresh(Composite current,Composite container, ScrolledComposite scrolledcomposite ) {
		 current.layout(true,true);
		 container.layout(true,true);
     	 scrolledcomposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	 }
	 
	 void addClose(Composite current,Composite container, ScrolledComposite scrolledcomposite,Set<Parameters> CreatedPropertiesSet) {
		 CloseButton.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
					 composite.dispose();
					 CreatedPropertiesSet.remove(Parameters.this);
					 refresh(current,container, scrolledcomposite);
	            }
	        });
	 }
}
