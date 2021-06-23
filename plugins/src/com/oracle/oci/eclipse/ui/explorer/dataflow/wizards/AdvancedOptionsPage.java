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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.bmc.dataflow.model.ApplicationSummary;

public class AdvancedOptionsPage extends WizardPage {
    private Text nameText,text1,text2;
	private Combo vcnCombo;
	private Combo subnetCombo;
	private Text dnszText;
    private ISelection selection;
    private Object obj;
    private Composite c,c2,c3;
    private ScrolledComposite sc;
    private Set<sparkkv> set=new HashSet<sparkkv>();
    Button add,show;
    Label sn;

    public AdvancedOptionsPage(ISelection selection,Object obj) {
        super("wizardPage");
        setTitle("Advanced Options Wizard");
        setDescription("This wizard lets you choose certain advanced functionalities.");
        this.selection = selection;
        this.obj=obj;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL | SWT.H_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );
    	sc.setLayoutData(new GridData());
    	
        c = new Composite(sc,SWT.NONE);
        sc.setContent(c);GridLayout l=new GridLayout();l.numColumns=1;c.setLayout(l);
        
        show=new Button(c,SWT.CHECK);show.setText("Show Advanced Options");show.setLayoutData(new GridData());
        show.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (show.getSelection()) {
                	c2.setVisible(true);
                	//refresh();
                }
                else {
                	c2.setVisible(false);
                	//refresh();
                }
                    
            }
        });
        
        c2=new Composite(c,SWT.NONE);c2.setVisible(false);
        c2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout l2=new GridLayout();l2.numColumns=2;
        c2.setLayout(l2);
        
        c3=new Composite(c2,SWT.NONE);GridData gd=new GridData(GridData.FILL_HORIZONTAL);gd.horizontalSpan=2;
        c3.setLayoutData(gd);GridLayout l3=new GridLayout();l3.numColumns=1;c3.setLayout(new GridLayout());
        
        add=new Button(c3,SWT.PUSH);add.setLayoutData(new GridData());
        add.setText("Add Spark Property");
        
        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
            	set.add(new sparkkv());
            }
          });
        
        Label label1=new Label(c2,SWT.NONE);label1.setText("Application Log Location");
        text1=new Text(c2,SWT.BORDER);text1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        String s="";
        if(obj instanceof RunSummary) {
        	try {
				s=RunClient.getInstance().getRunDetails(((RunSummary)obj).getId().toString()).getLogsBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Error",e1.getMessage());
			}
        }
        else {
        	try {
				s=ApplicationClient.getInstance().getApplicationDetails(((ApplicationSummary)obj).getId()).getLogsBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Error",e1.getMessage());
			}
        }
        text1.setText(s);
        if(obj instanceof RunSummary) {
        	try {
				s=RunClient.getInstance().getRunDetails(((RunSummary)obj).getId().toString()).getWarehouseBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Error",e1.getMessage());
			}
        }
        else {
        	try {
				s=ApplicationClient.getInstance().getApplicationDetails(((ApplicationSummary)obj).getId()).getWarehouseBucketUri();
			} catch (Exception e1) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Error",e1.getMessage());
			}
        }
        Label label2=new Label(c2,SWT.NONE);label2.setText("Warehouse Bucket URI");
        text2=new Text(c2,SWT.BORDER);text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text2.setText(s);
        
        setControl(sc);
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
		 Text k,v;
		 Button close;
		 
		 sparkkv(){
			 
			 comp=new Composite(c3,SWT.NONE);comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 GridLayout glc=new GridLayout();glc.numColumns=3;comp.setLayout(glc);
			 close=new Button(comp,SWT.PUSH);close.setLayoutData(new GridData());close.setText("X");
			 k=new Text(comp,SWT.BORDER);k.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));k.setMessage("key");
			 v=new Text(comp,SWT.BORDER);v.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));v.setMessage("value");
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
		c.layout(true,true);
   	 	sc.setMinSize( c.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	}
	
	Map<String,String> getconfig(){
		Map<String,String> m=new HashMap<String,String>();
		for(sparkkv e:set) {
			m.put(e.k.getText(),e.v.getText());
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