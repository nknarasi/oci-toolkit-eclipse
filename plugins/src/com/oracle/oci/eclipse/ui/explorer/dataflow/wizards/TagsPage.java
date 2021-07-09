package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.oracle.bmc.identity.requests.ListTagsRequest;
import com.oracle.bmc.identity.responses.GetTagResponse;
import com.oracle.bmc.identity.responses.ListTagsResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.BaseTagDefinitionValidator;
import com.oracle.bmc.identity.model.TagNamespace;
import com.oracle.bmc.identity.model.TagNamespaceSummary;
import com.oracle.bmc.identity.model.TagSummary;
import com.oracle.bmc.identity.requests.GetTagRequest;
import com.oracle.bmc.identity.requests.ListTagNamespacesRequest;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;

public class TagsPage extends WizardPage {
    private ISelection selection;
    private Composite container;
    private ScrolledComposite sc;
    private Set<Tags> set=new HashSet<Tags>();
    private String[] namespacesList;
    private Map<String,String> namespaceMap=new HashMap<String,String>();
    private IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
    private Map<String,Map<String,String[]>> defTagMap= new HashMap<String,Map<String,String[]>>();
    private Map<String,Map<String,Object>> defMap;
    private Map<String,String> freeMap;
    
    public TagsPage(ISelection selection,String compid,Map<String,Map<String,Object>> defMap,Map<String,String> freeMap) {
        super("wizardPage");
        setTitle("Tags");
        setDescription("Tagging is a metadata system that allows you to organize and track resources within your tenancy. Tags are composed of keys and values that can be attached to resources.\r\n"
        		+ "<a href=\"/https://docs.oracle.com/en-us/iaas/Content/Tagging/Concepts/taggingoverview.htm\">Learn more about tagging </a>");
        this.selection = selection;
        this.namespacesList=getNamespaces();
        this.defMap=defMap;
        this.freeMap=freeMap;
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

        Button addNsg=new Button(container,SWT.PUSH);
        addNsg.setText("Additional Tags");
        
        setTags(defMap,freeMap);
        
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
		 Combo tc,dtc,vtc;
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
					 if(vtc!=null) vtc.dispose();
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
			    	  if(vtc!=null) vtc.dispose();
			    	  if(ftk!=null) ftk.dispose();
			    	  if(val!=null) val.dispose();
			    	  refresh();
			    	  
			    	  if(!tc.getText().equals("Free Form Tags")) {
			    		  dtc=new Combo(comp,SWT.READ_ONLY);dtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  vtc=new Combo(comp,SWT.READ_ONLY);vtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  refresh();
			    		  dtc.setItems(getTagKeys(namespaceMap.get(tc.getText())));
			    		  adddtcl();
			    	  }
			    	  else {
				  			ftk=new Text(comp,SWT.BORDER);ftk.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));ftk.setMessage("key");
				  			val=new Text(comp,SWT.BORDER);val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));val.setMessage("value");
			    	  }
			    	  refresh();
				   }
			});
		 }
		 
		 void adddtcl() {
			 dtc.addSelectionListener(new SelectionAdapter() {
			      public void widgetSelected(SelectionEvent e) {
			    	  if(vtc==null||vtc.isDisposed()) {
			    		  vtc=new Combo(comp,SWT.READ_ONLY);vtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  refresh();
			    	  }
			    	  String[] items=getTagValues(dtc.getText(),namespaceMap.get(tc.getText()));
			    	  if(items==null) {
			    		  if(vtc!=null)
			    			  vtc.dispose();
			    		  val=new Text(comp,SWT.BORDER);val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));val.setMessage("value");
			    	  }
			    	  else {
			    		  if(vtc==null||vtc.isDisposed()) vtc=new Combo(comp,SWT.READ_ONLY);vtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			    		  vtc.setItems(items);
			    	  }
			    	  refresh();
				   }
			});
		 }
		 
	 }
	 
	 String[] getNamespaces() {
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		 
		String compid=IdentClient.getInstance().getRootCompartment().getId();
		ListTagNamespacesRequest listTagNamespacesRequest = ListTagNamespacesRequest.builder().compartmentId(compid)
			//.limit(570)
			.includeSubcompartments(true).lifecycleState(TagNamespace.LifecycleState.Active)
			.build();

	        /* Send request to the Client */
	        List<TagNamespaceSummary> l = client.listTagNamespaces(listTagNamespacesRequest).getItems();
	        System.out.print(l.get(0).getName()+l.get(0).getId());
	        String[] rl=new String[l.size()+1];
	        for(int i=0;i<l.size();i++) {
	        	rl[i]=l.get(i).getName();
	        	namespaceMap.put(rl[i], l.get(i).getId());
	        }
	        
	        rl[l.size()]="Free Form Tags";
	        return rl;
	 }
	 
	 String[] getTagKeys(String namespaceId) {
		 if(defTagMap.containsKey(namespaceId))
			 return defTagMap.get(namespaceId).keySet().toArray(new String[0]);
		 IdentityClient client = new IdentityClient(AuthProvider.getInstance().getProvider());
		 ListTagsRequest listTagsRequest = ListTagsRequest.builder().tagNamespaceId(namespaceId).build();
		 ListTagsResponse response = client.listTags(listTagsRequest);
		 List<String> l=new ArrayList<String>();
		 Map<String,String[]> tm=new HashMap<String,String[]>();
		 for(TagSummary ts:response.getItems()) {
			 l.add(ts.getName());
			 tm.put(ts.getName(),new String[] {""});
		 }		 
		 defTagMap.put(namespaceId,tm);
		 return l.toArray(new String[0]);
	 }
	 
	 String[] getTagValues(String key,String namespaceId) {
		 String[] tagValues=defTagMap.get(namespaceId).get(key);
		 if(tagValues==null||!tagValues[0].isEmpty())
			 return tagValues;
			 
		 GetTagRequest getTagRequest = GetTagRequest.builder()
					.tagNamespaceId(namespaceId).tagName(key).build();
		 GetTagResponse res = client.getTag(getTagRequest);
		 BaseTagDefinitionValidator validator=res.getTag().getValidator();
		 if(validator==null) {
			 defTagMap.get(namespaceId).put(key, null);
			 return null;
		 }
		 String v=validator.toString();
		 tagValues=v.split("values=\\[")[1].split("\\]")[0].split(",");
		 for(int i=0;i<tagValues.length;i++)
			 tagValues[i]=tagValues[i].trim();
		 defTagMap.get(namespaceId).put(key, tagValues);
		 return tagValues;
	 }
	 
	 public Map<String,Map<String,Object>> getOT(){
		 
		 Map<String,Map<String,Object>> ots=new HashMap<String,Map<String,Object>>();
		 
		 for(Tags t:set) {
			 if(!t.tc.getText().equals("Free Form Tags")) {
				 Map<String,Object> tm=ots.get(t.tc.getText());
				 if(tm==null) tm=new  HashMap<String,Object>();
				 if(t.vtc!=null&&!t.vtc.isDisposed()) tm.put(t.dtc.getText(), t.vtc.getText());
				 else tm.put(t.dtc.getText(), t.val.getText());
				 ots.put(t.tc.getText(), tm);
			 }
		 }
		 return ots;
	 }
	 
	 public Map<String,String> getFT(){
		 Map<String,String> m=new HashMap<String,String>();
		 
		 for(Tags t:set) {
			 if(t.tc.getText().equals("Free Form Tags")) {
				 m.put(t.ftk.getText(), t.val.getText());
			 }
		 }
		 
		 return m;
	 }
	 
	 public void setTags(Map<String,Map<String,Object>> defMap,Map<String,String> freeMap) {
		 if(defMap==null||freeMap==null) return;
		 for(Map.Entry<String,Map<String,Object>> me:defMap.entrySet()) {
			 for(Map.Entry<String,Object> mee:me.getValue().entrySet()) {
				 Tags t=new Tags();
				 t.tc.setText(me.getKey());
				 t.ftk.dispose();
				 t.val.dispose();
				 t.dtc=new Combo(t.comp,SWT.READ_ONLY);t.dtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				 t.dtc.setItems(getTagKeys(namespaceMap.get(t.tc.getText())));
				 t.dtc.setText(mee.getKey());
				 String[] val=getTagValues(mee.getKey(),namespaceMap.get(me.getKey()));
				 if(val!=null) {
		    		  if(t.vtc==null||t.vtc.isDisposed()) t.vtc=new Combo(t.comp,SWT.READ_ONLY);t.vtc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    		  t.vtc.setItems(val);
		    		 if(mee.getValue()!=null) t.vtc.setText((String)mee.getValue());
		    	 }
				 else {
					 t.val=new Text(t.comp,SWT.BORDER);t.val.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					 if(mee.getValue()!=null) t.val.setText((String)mee.getValue());
				 }
				 set.add(t);
			 }
		 }
		 
		 for(Map.Entry<String, String> me:freeMap.entrySet()) {
			 Tags t=new Tags();
			 t.ftk.setText(me.getKey());
			 if(me.getValue()!=null) t.val.setText(me.getValue());
			 set.add(t);
		 }
	 }
}