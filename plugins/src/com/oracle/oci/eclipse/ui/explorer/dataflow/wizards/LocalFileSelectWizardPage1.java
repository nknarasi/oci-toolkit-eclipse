package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

public class LocalFileSelectWizardPage1 extends WizardPage {	
    private static final String BUCKET_KEY = "bucket";
    private ISelection selection;
    private Tree tree;
    private Text compartmentText;
    private Image IMAGE;
    private String fileName;
    private List<BucketSummary> buckets;
    private DataTransferObject dto;
	private Compartment selectedApplicationCompartment;
	private Map<BucketSummary, TreeItem> bucketTreeMap;
	private String fileUriSelected;
	boolean fileSelected = false;
	private Text fileUriText;
	
	   public LocalFileSelectWizardPage1(ISelection selection, DataTransferObject dto, String COMPARTMENT_ID) {
	        super("wizardPage");
	        setTitle("Bucket Selection Wizard for Application Jar");     
	        setDescription("Choose a Bucket for uploading Application Jar file.");
	        this.selection = selection;
	        this.dto = dto;
	        IMAGE = Activator.getImage(Icons.BUCKET.getPath());
	        
	        this.selectedApplicationCompartment = IdentClient.getInstance().getRootCompartment();
	        
			Compartment rootCompartment = IdentClient.getInstance().getRootCompartment();
			List<Compartment> Allcompartments = IdentClient.getInstance().getCompartmentList(rootCompartment);
			for(Compartment compartment : Allcompartments) {
				if(compartment.getId().equals(COMPARTMENT_ID)) {
					this.selectedApplicationCompartment= compartment;
					break;
				}
			}
	    }
	   
	    @Override
	    public void createControl(Composite parent) {
	    	
	        Composite container = new Composite(parent, SWT.NULL);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
			Composite innerTopContainer = new Composite(container, SWT.NONE);
	        GridLayout innerTopLayout = new GridLayout();
	        innerTopLayout.numColumns = 3;
	        innerTopContainer.setLayout(innerTopLayout);
	        innerTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label compartmentLabel = new Label(innerTopContainer, SWT.NULL);
			compartmentLabel.setText("&Choose a compartment:"); 
	        compartmentText = new Text(innerTopContainer, SWT.BORDER | SWT.SINGLE);
	        compartmentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        compartmentText.setEditable(false);
	        compartmentText.setText(selectedApplicationCompartment.getName());

	        Button compartmentButton = new Button(innerTopContainer, SWT.PUSH);
	        compartmentButton.setText("Choose...");
	        compartmentButton.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	handleSelectApplicationCompartmentEvent();
	            }
	        });
	        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));                
	        createBucketSection();	        
			Composite FileUriContainer = new Composite(container, SWT.NONE);
	        GridLayout FileUriLayout = new GridLayout();
	        FileUriLayout.numColumns = 2;
	        FileUriContainer.setLayout(FileUriLayout);
	        FileUriContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Label FileUriLabel = new Label(FileUriContainer, SWT.NULL);
			FileUriLabel.setText("&File Uri :"); 
			fileUriText = new Text(FileUriContainer, SWT.BORDER | SWT.SINGLE);
			fileUriText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fileUriText.setText("");	        
	        setControl(container);
	    }
	    
	    private void createBucketSection() {
	    	if(bucketTreeMap != null)
	    	{
	        	for(Entry<BucketSummary,TreeItem> item: bucketTreeMap.entrySet() ) {        		
	        		item.getValue().removeAll();
	        		item.getValue().dispose();       		
	        	}
	    	}
	    	try {
				buckets = ObjStorageClient.getInstance().getBucketsinCompartment(selectedApplicationCompartment.getId());
			} catch (Exception e1) {
				 ErrorHandler.logError("Unable to list buckets: " + e1.getMessage());
			}
	   	 	Job job = new Job("Get Objects inside Bucket in User Compartment") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	bucketTreeMap= new HashMap<BucketSummary,TreeItem>();
	                        	for(BucketSummary bucket : buckets) {                       		
	                        		bucketTreeMap.put(bucket, new TreeItem(tree,0));
	                        		bucketTreeMap.get(bucket).setText(bucket.getName());
	                        		bucketTreeMap.get(bucket).setImage(IMAGE);
	                        		bucketTreeMap.get(bucket).setData(BUCKET_KEY,bucket);
	                        	}	                         
	                        } catch(Exception ex) {
	                        	 ErrorHandler.logError("Unable to create tree Item: " + ex.getMessage());
	                        }
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        job.schedule();  

	        tree.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	 TreeItem[] items = tree.getSelection();
		       	        if(items !=null && items.length>0) {
		       	            TreeItem selectedItem = items[0];
		       	            BucketSummary bucket = (BucketSummary)selectedItem.getData(BUCKET_KEY);	      
		       	            fileUriSelected = "oci://"+ bucket.getName() +"@" + ObjStorageClient.getInstance().getNamespace()+"/"+fileName;
		       	            fileUriText.setText(fileUriSelected);
		       	            fileSelected = true;
		       	            canFlipToNextPage();
		       	            getWizard().getContainer().updateButtons();
		       	        }
	            }
	        });	        
	   }
	   private void handleSelectApplicationCompartmentEvent() {
	    	Consumer<Compartment> consumer=new Consumer<Compartment>() {
				@Override
				public void accept(Compartment compartment) {
					if (compartment != null) {
						selectedApplicationCompartment = compartment;
						compartmentText.setText(selectedApplicationCompartment.getName());
						 createBucketSection();
					}
				}
			};
	    	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
	    	new CompartmentSelectWizard(consumer, false));
			dialog.setFinishButtonText("Select");
			if (Window.OK == dialog.open()) {
			}
	    }
		
	    private void updateStatus(String message) {
	        setErrorMessage(message);
	        setPageComplete(message == null);
	    }
	    
	    public String getBucketSelected() {
	        TreeItem[] items = tree.getSelection();
	        if(items !=null && items.length>0) {
	            TreeItem selectedItem = items[0];
	            BucketSummary bucket = (BucketSummary)selectedItem.getData(BUCKET_KEY);	                  
	            return bucket.getName();
	        }
	        return null;
	    }
	    
	    public String getFileUri() {
	               return fileUriText.getText();
	    }	    

		void onEnterPage()
		{
		    final DataTransferObject dto = ((LocalFileSelectWizard) getWizard()).dto;
		    if(dto.getFiledir() != null) {
		    	String fileDirectory = dto.getFiledir();
		    	this.fileName = fileDirectory.substring(fileDirectory.lastIndexOf('\\')+1);
		    }
		}
		
		@Override
		public boolean canFlipToNextPage() {
		return fileSelected;
		}
		
		public String getnewName() {			
			return fileUriText.getText().substring(fileUriText.getText().lastIndexOf('/')+1);
		}
		
		 @Override
		    public IWizardPage getNextPage() {
			 dto.setFileUri(fileUriSelected);  
	        return super.getNextPage();
		    }
}
