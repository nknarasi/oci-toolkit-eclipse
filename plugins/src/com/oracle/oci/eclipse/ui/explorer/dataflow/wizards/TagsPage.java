package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.TagNamespace;
import com.oracle.bmc.identity.model.TagNamespaceSummary;
import com.oracle.bmc.identity.requests.GetTagNamespaceRequest;
import com.oracle.bmc.identity.requests.ListTagNamespacesRequest;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;


public class TagsPage extends WizardPage {
    private ISelection selection;
    private String compid;
    private Composite container;
    private ScrolledComposite sc;
    private Set<Tags> set=new HashSet<Tags>();
    private String[] namespacesList;

    public TagsPage(ISelection selection,String compid) {
        super("wizardPage");
        setTitle("Tag Wizard");
        setDescription("This wizard lets you add Tags.");
        this.selection = selection;
        this.compid=compid;
        this.namespacesList=getNamespaces();
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

        Button check=new Button(container,SWT.PUSH);check.setText("Check for duplicacy");
        check.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP,true,false));
        check.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
            	if(!check()) MessageDialog.openInformation(getShell(), "Duplicates Found", "Duplicates are found in the tags used.");
            	else MessageDialog.openInformation(getShell(), "No Duplicates", "There are no duplicates in the tags used.");
            }
          });
        Button addNsg=new Button(container,SWT.PUSH);
        addNsg.setText("Additional Tags");
        
        addNsg.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
            	set.add(new Tags());
            }
          });
        
        setControl(sc);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
	 
	 class Tags{
		 
		 Composite comp;
		 Combo tc,dtc;
		 Text ftk,val;
		 Button c;
		 
		 Tags(){
			 
			 comp=new Composite(container,SWT.NONE);comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 GridLayout gl=new GridLayout();gl.numColumns=4;comp.setLayout(gl);
			 c=new Button(comp,SWT.PUSH);c.setText("Remove");
			 tc=new Combo(comp,SWT.READ_ONLY);
			 tc.setItems(namespacesList);tc.setText("Free Form Tags");
			 ftk=new Text(comp,SWT.BORDER);ftk.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));ftk.setMessage("key");
			 val=new Text(comp,SWT.BORDER);val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));val.setMessage("value");
			 addtcl();
			 addClose();
			 refresh();
		 }
		 
		 void refresh() {
			 container.layout(true,true);
         	 sc.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		 }
		 
		 void addClose() {
			 c.addSelectionListener(new SelectionAdapter() {
				 public void widgetSelected(SelectionEvent e) {
					 if(tc!=null) tc.dispose();
					 if(dtc!=null) dtc.dispose();
			    	 if(ftk!=null) ftk.dispose();
			    	 if(val!=null) val.dispose();
					 comp.dispose();
					 set.remove(Tags.this);
					 refresh();
				 }
			 });
		 }
		
		 void addtcl() {
			 
			tc.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			    	  if(dtc!=null) dtc.dispose();
			    	  if(ftk!=null) ftk.dispose();
			    	  if(val!=null) val.dispose();
			    	  if(!tc.getText().equals("Free Form Tags")) {
			    		  dtc=new Combo(comp,SWT.READ_ONLY);dtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  dtc.setItems(new String[] {"CreatedBy","CreatedOn"});
			    	  }
			    	  else {
				  			ftk=new Text(comp,SWT.BORDER);ftk.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));ftk.setMessage("key");
			    	  }
			    	  val=new Text(comp,SWT.BORDER);val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));val.setMessage("value");
			    	  refresh();
				   }
			});
		 }
		 
	 }
	 
	 String[] getNamespaces() {
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		 
		compid=IdentClient.getInstance().getRootCompartment().getId();
		ListTagNamespacesRequest listTagNamespacesRequest = ListTagNamespacesRequest.builder().compartmentId(compid)
			//.limit(570)
			.includeSubcompartments(true).lifecycleState(TagNamespace.LifecycleState.Active)
			.build();

	        /* Send request to the Client */
	        List<TagNamespaceSummary> l = client.listTagNamespaces(listTagNamespacesRequest).getItems();
	        String[] rl=new String[l.size()+1];
	        for(int i=0;i<l.size();i++) {
	        	rl[i]=l.get(i).getName();
	        }
	        rl[l.size()]="Free Form Tags";
	        return rl;
	 }
	 
	 TagNamespace getTagNamespace(String id) {
		 
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		GetTagNamespaceRequest getTagNamespaceRequest = GetTagNamespaceRequest.builder()
			.tagNamespaceId(id).build();
	   return client.getTagNamespace(getTagNamespaceRequest).getTagNamespace();
	 }
	 
	 Map<String,Map<String,Object>> getOT(){
		 
		 Map<String,Map<String,Object>> ots=new HashMap<String,Map<String,Object>>();
		 Map<String,Object> m=new HashMap<String,Object>();
		 
		 for(Tags t:set) {
			 if(t.tc.getText().equals("Oracle-Tags")) {
				 m.put(t.dtc.getText(),t.val.getText());
			 }
		 }
		 ots.put("Oracle-Tags",m);
		 return ots;
	 }
	 
	 Map<String,String> getFT(){
		 Map<String,String> m=new HashMap<String,String>();
		 
		 for(Tags t:set) {
			 if(!t.tc.getText().equals("Oracle-Tags")) {
				 m.put(t.ftk.getText(), t.val.getText());
			 }
		 }
		 
		 return m;
	 }
	 
	 boolean check() {
		 
		 Set<String> cs=new HashSet<String>();
		 for(Tags e:set) {
			 String ti=new String("");
			 if(e.tc.getText().equals("Oracle-Tags")) {ti=ti+"Oracle-Tags,"+e.dtc.getText();}
			 else {ti=ti+"Free Form Tags,"+e.ftk.getText();}
			 if(cs.contains(ti)) {return false;}
			 cs.add(ti);
		 }
		 return true;
	 }
}