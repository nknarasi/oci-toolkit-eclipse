package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.NetworkSecurityGroup;
import com.oracle.bmc.core.requests.ListNetworkSecurityGroupsRequest;
import com.oracle.bmc.core.responses.ListNetworkSecurityGroupsResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;


public class NsgPage extends WizardPage {
    private ISelection selection;
    private String compid;
    private Composite container,p;
    private ScrolledComposite sc;
    private ArrayList<String> l;
    private Set<Nsg> set=new HashSet<Nsg>();

    public NsgPage(ISelection selection,String compid) {
        super("wizardPage");
        setTitle("Network Security Group Wizard");
        setDescription("This wizard lets you add National Security Groups(NSGs).");
        this.selection = selection;
        this.compid=compid;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	p=parent;
    	sc=new ScrolledComposite(parent,SWT.V_SCROLL);
    	sc.setExpandHorizontal( true );
    	sc.setExpandVertical( true );
    	sc.setLayoutData(new GridData());
    	
        container = new Composite(sc,SWT.NULL);sc.setContent(container);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        
        sc.addListener( SWT.Resize, event -> {
  		  int width = sc.getClientArea().width;
  		  sc.setMinSize( container.computeSize( width, SWT.DEFAULT ));
  		});
		
        Button addNsg=new Button(container,SWT.PUSH);
        addNsg.setText("Another Network Security Group");
        GridData gd=new GridData();gd.horizontalSpan=3;addNsg.setLayoutData(gd);
        
        addNsg.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	set.add(new Nsg());
            }
        });
        
        setControl(sc);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	 
	 class Nsg{

		 Button close;
		 Button selComp;
		 Combo combo;
		 String compid2,compName2,nsgid;
		 Map<String,String> m;
		 
		 Nsg(){
			 close=new Button(container,SWT.PUSH);
			 close.setText("X");
			 selComp=new Button(container,SWT.PUSH);
			 selComp.setText("Select Compartment");
			 selComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 
			 combo=new Combo(container,SWT.READ_ONLY);
			 combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 
			 refresh();
			 addClose();
			 addSelectComp();
			 addComboListener();
		 }
		 
		 void refresh() {
			 container.layout(true,true);
         	 sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		 }
		 
		 void addComboListener() {
			 combo.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			          nsgid=m.get(combo.getText());
			       }
			 });
		 }
		 
		 void addClose() {
			 
			 close.addSelectionListener(new SelectionAdapter() {
				 public void widgetSelected(SelectionEvent e) {
					 selComp.dispose();
					 combo.dispose();
					 close.dispose();
					 set.remove(Nsg.this);
					 refresh();
				 }
			 });
		 }
		 
		 void addSelectComp() {
			 
			 selComp.addSelectionListener(new SelectionAdapter() {
				    @Override
		            public void widgetSelected(SelectionEvent e) {
		                
						Consumer<Compartment> consumer=new Consumer<Compartment>() {

						@Override
						public void accept(Compartment comp) {
							
							selComp.setText(comp.getName());
							VirtualNetworkClient client = new VirtualNetworkClient(AuthProvider.getInstance().getProvider());
							ListNetworkSecurityGroupsRequest listNetworkSecurityGroupsRequest = ListNetworkSecurityGroupsRequest.builder().compartmentId(comp.getId()).build();

					        ListNetworkSecurityGroupsResponse response = client.listNetworkSecurityGroups(listNetworkSecurityGroupsRequest);
					        l=new ArrayList<String>();
					        List<NetworkSecurityGroup> nsgl=response.getItems();
					        m=new HashMap<String,String>();
					        
					        String[] sl=new String[0];
					        if(nsgl!=null&&nsgl.size()>0) {
					        	sl=new String[nsgl.size()];int i=0;
					        	for(NetworkSecurityGroup e:nsgl) {
					        		sl[i]=e.getDisplayName();m.put(e.getDisplayName(),e.getId());i++;
					        	}
					        }
					        combo.setItems(sl);
						}
						};
						CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),new CompartmentSelectWizard(consumer, false));
						dialog.setFinishButtonText("Select");
						if (Window.OK == dialog.open()) {}
		            }
			 });
		 }
	 }
	 
	 ArrayList<String> getnsgs(){
		 l=new ArrayList<String>();
		 for(Nsg e:set) {
			 if(e.nsgid==null) return null;
			 l.add(e.nsgid);
		 }
		 return l;
	 }

}