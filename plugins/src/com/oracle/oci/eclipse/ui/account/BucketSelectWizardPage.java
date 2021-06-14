package com.oracle.oci.eclipse.ui.account;


import java.util.HashMap;

import java.util.List;
import java.util.Map;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;


public class BucketSelectWizardPage extends WizardPage {
	
    private static final String BUCKET_KEY = "bucket";
    private static final String OBJECT_KEY = "object";
    private static final String GRAND_CHILDREN_FETCHED = "grandChildrenFetched";
    private ISelection selection;
    private Tree tree;
    private Image IMAGE;
    private ApplicationLanguage language;
    private List<BucketSummary> buckets;
    private String compartmentId;
    
    public BucketSelectWizardPage(ISelection selection,String CompartmentId,ApplicationLanguage language) {
        super("wizardPage");
        setTitle("Select Bucket");     
        setDescription("Choose the Bucket");
        this.selection = selection;
        this.language= language;
        this.compartmentId= CompartmentId;
        IMAGE = Activator.getImage(Icons.BUCKET.getPath());
    }

    @Override
    public void createControl(Composite parent) {
    	
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        try {
			buckets = ObjStorageClient.getInstance().getBucketsinCompartment(compartmentId);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        

        Job job = new Job("Get Objects inside Bucket in User Compartment") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // Get root compartment children from server

                // update tree node using UI thread
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // add root compartment node to tree
                        	Map<BucketSummary, TreeItem> BucketTreeMap= new HashMap<BucketSummary,TreeItem>();
                        	for(BucketSummary bucket : buckets) {
                        		
                       		
                        		BucketTreeMap.put(bucket, new TreeItem(tree,0));
                        		BucketTreeMap.get(bucket).setText(bucket.getName());
                        		BucketTreeMap.get(bucket).setImage(IMAGE);
                        		BucketTreeMap.get(bucket).setData(BUCKET_KEY,bucket);

                        	}
                           
                        	for(BucketSummary bucket : buckets) {
                        		
                        		for(ObjectSummary objects :ObjStorageClient.getInstance().getBucketObjects(bucket.getName())) {
                        			Job job = new Job("Get contents of buckets") {
                                        @Override
                                        protected IStatus run(IProgressMonitor monitor) {                                            
                                            Display.getDefault().asyncExec(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                    	if(correctformat(objects.getName())) {
                                                    		TreeItem treeItem = new TreeItem(BucketTreeMap.get(bucket), 0);
                                                            treeItem.setText(objects.getName());
                                                            treeItem.setImage(IMAGE);
                                                            treeItem.setData(OBJECT_KEY,objects);
                                                    	}
                                                        
                                                    } catch(Exception e) {}
                                                }
                                            });
                                            return Status.OK_STATUS;
                                        }
                                    };
                                    job.schedule();
                        			}
                        	}
                        	
                           

                        } catch(Exception ex) {}
                    }
                });
                return Status.OK_STATUS;
            }
        };
        job.schedule();

        tree.addListener (SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(Event event) {}
        });

        tree.addListener(SWT.Expand, new Listener() {
            @Override
            public void handleEvent(Event e) {
                try {
                    handNodeExpanionEvent(e);
                } catch (Throwable ex) {}
            }
        });

        setControl(container);
    }
    
    private boolean correctformat (String Name) {
    	if(language == ApplicationLanguage.Java) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".jar")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Python) {
    		String ending = Name.substring(Name.length()-3);
    		if(!ending.equals(".py")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Sql) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".sql")) {
    			return false;
    		}
    	}
    	else if(language == ApplicationLanguage.Scala) {
    		String ending = Name.substring(Name.length()-4);
    		if(!ending.equals(".jar")) {
    			return false;
    		}
    	}
    	return true;
    }
    private void handNodeExpanionEvent(Event e) {
        TreeItem treeItem = (TreeItem) e.item;
        synchronized (treeItem) {
            String grandChildrenFetched = (String) treeItem.getData(GRAND_CHILDREN_FETCHED);
            if (grandChildrenFetched != null && grandChildrenFetched.equalsIgnoreCase("true"))
                return;
            treeItem.setData(GRAND_CHILDREN_FETCHED, "true");
        }

        TreeItem children[] = treeItem.getItems();
        if (children == null || (children.length == 0))
            return;

    }
    
    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getObjectSelected() {
        TreeItem[] items = tree.getSelection();
        if(items !=null && items.length>0) {
            TreeItem selectedItem = items[0];
            TreeItem parent = selectedItem.getParentItem();
            BucketSummary bucket = (BucketSummary)parent.getData(BUCKET_KEY);
            ObjectSummary object = (ObjectSummary)selectedItem.getData(OBJECT_KEY);
            
            String fileUri = "oci://"+bucket.getName()+"@" + ObjStorageClient.getInstance().getNamespace()+"/"+object.getName();
            return fileUri;
        }

        return null;
    }
    

}
