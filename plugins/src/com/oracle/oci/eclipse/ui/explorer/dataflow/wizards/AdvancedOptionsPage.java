package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.bmc.dataflow.model.ApplicationSummary;

public class AdvancedOptionsPage extends WizardPage {
    private ISelection selection;
    private Object obj;
    private Composite comp1,comp2,comp3;
    private ScrolledComposite scrollComp;
    //private Set<sparkkv> set=new HashSet<sparkkv>();
    private Set<SparkProperty> set=new HashSet<SparkProperty>();
    RunWizardPage page;
    Button add,show;
    Text text1,text2;

    public AdvancedOptionsPage(ISelection selection,Object obj,RunWizardPage page) {
        super("wizardPage");
        setTitle("Advanced Options Wizard");
        setDescription("This wizard lets you choose certain advanced functionalities.");
        this.selection = selection;
        this.obj=obj;
        this.page=page;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	scrollComp=new ScrolledComposite(parent,SWT.V_SCROLL | SWT.H_SCROLL);
    	scrollComp.setExpandHorizontal( true );
    	scrollComp.setExpandVertical( true );
    	scrollComp.setLayoutData(new GridData());
    	
        comp1 = new Composite(scrollComp,SWT.NONE);
        scrollComp.setContent(comp1);
        GridLayout l=new GridLayout();
        l.numColumns=1;
        comp1.setLayout(l);
        
        show=new Button(comp1,SWT.CHECK);
        show.setText("Show Advanced Options");
        show.setLayoutData(new GridData());
        show.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (show.getSelection()) {
                	comp2.setVisible(true);
                }
                else {
                	comp2.setVisible(false);
                }
                    
            }
        });
        
        comp2=new Composite(comp1,SWT.NONE);
        comp2.setVisible(false);
        comp2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout l2=new GridLayout();
        l2.numColumns=2;
        comp2.setLayout(l2);
        
        comp3=new Composite(comp2,SWT.NONE);
        GridData gd=new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan=2;
        comp3.setLayoutData(gd);
        GridLayout l3=new GridLayout();
        l3.numColumns=1;
        comp3.setLayout(new GridLayout());
        
        add=new Button(comp3,SWT.PUSH);add.setLayoutData(new GridData());
        add.setText("Add Spark Property");
        
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
            	//set.add(new sparkkv());
            	set.add(new SparkProperty(comp3,comp1,scrollComp,set,(String)page.getDetails()[14]));
            }
          });
        
        Label label1=new Label(comp2,SWT.NONE);
        label1.setText("Application Log Location");
        text1=new Text(comp2,SWT.BORDER);
        text1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        String s="";
        if(obj instanceof RunSummary) {
        	try {
				s=RunClient.getInstance().getRunDetails(((RunSummary)obj).getId().toString()).getLogsBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unbale to fetch Logs Bucket Uri",e1.getMessage());
			}
        }
        else {
        	try {
				s=ApplicationClient.getInstance().getApplicationDetails(((ApplicationSummary)obj).getId()).getLogsBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unbale to fetch Logs Bucket Uri",e1.getMessage());
			}
        }
        text1.setText(s);
        if(obj instanceof RunSummary) {
        	try {
				s=RunClient.getInstance().getRunDetails(((RunSummary)obj).getId().toString()).getWarehouseBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unbale to fetch Warehouse Bucket Uri",e1.getMessage());
			}
        }
        else {
        	try {
				s=ApplicationClient.getInstance().getApplicationDetails(((ApplicationSummary)obj).getId()).getWarehouseBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unbale to fetch Warehouse Bucket Uri",e1.getMessage());
			}
        }
        Label label2=new Label(comp2,SWT.NONE);
        label2.setText("Warehouse Bucket URI");
        text2=new Text(comp2,SWT.BORDER);
        text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text2.setText(s);
        
        setControl(scrollComp);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	
	 boolean ischecked() {
		 return show.getSelection();
	 }
	 
	 class sparkkv{
		 
		 Composite comp;
		 Text key,val;
		 Button close;
		 
		 sparkkv(){
			 
			 comp=new Composite(comp3,SWT.NONE);
			 comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 GridLayout glc=new GridLayout();
			 glc.numColumns=3;
			 comp.setLayout(glc);
			 
			 close=new Button(comp,SWT.PUSH);
			 close.setLayoutData(new GridData());
			 close.setText("Remove");
			 
			 key=new Text(comp,SWT.BORDER);
			 key.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 key.setMessage("key");
			 
			 val=new Text(comp,SWT.BORDER);
			 val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 val.setMessage("value");
			 
			 addClose();
			 refresh();
		 }
		 
		 void addClose() {
			 close.addSelectionListener(new SelectionAdapter() {
				 public void widgetSelected(SelectionEvent e) {
					 comp.dispose();
					 set.remove(sparkkv.this);
					 refresh();
				 }
			 });
		 }
	 }
	 
	 
	void refresh() {
		comp1.layout(true,true);
   	 	scrollComp.setMinSize( comp1.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	}
	
	Map<String,String> getconfig(){
		Map<String,String> m=new HashMap<String,String>();
		for(SparkProperty e:set) {
			m.put(e.tagKey.getText(),e.tagValue.getText());
		}
		return m;
	}
	 String loguri() {
		 return text1.getText().trim();
	 }
	 String buckuri() {
		 return text2.getText().trim();
	 }
}