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

import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

public class LocalFileSelectWizardPage3  extends WizardPage{
	
    private static final String APPLICATION_KEY = "application";
    private ISelection selection;
    private Tree tree;
    private Text compartmentText;
    private Image IMAGE;
    private List<ApplicationSummary> applications;
    private DataTransferObject dto;
    ApplicationSummary application;
	private Compartment selectedApplicationCompartment;
	Map<ApplicationSummary, TreeItem> ApplicationTreeMap;
	String ApplicationIdSelected = null;
    ListApplicationsRequest.SortBy s=ListApplicationsRequest.SortBy.TimeCreated;
	ListApplicationsRequest.SortOrder so=ListApplicationsRequest.SortOrder.Desc;
	boolean allow = false;
	
	   public LocalFileSelectWizardPage3(ISelection selection, DataTransferObject dto, String COMPARTMENT_ID) {
	        super("wizardPage");
	        setTitle("Choose an Application to edit or Create new.");     
	        setDescription("To edit a previously created application, select an application. Ignore this page"
	        		+ " if you want to create a new application.");
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
	                
	        createApplicationSection();    
	        setControl(container);
	    }
	    
	    private void createApplicationSection() {

	    	if(ApplicationTreeMap != null)
	    	{
	        	for(Entry<ApplicationSummary,TreeItem> item: ApplicationTreeMap.entrySet() ) {        		
	        		item.getValue().removeAll();
	        		item.getValue().dispose();
	        		//BucketTreeMap.remove(item.getKey());       		
	        	}
	    	}
	    	try {
	    		applications =ApplicationClient.getInstance().getApplicationsbyCompartmentId(selectedApplicationCompartment.getId(),s,so);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

	   	 Job job = new Job("Get Applications in User Compartment") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	            	// update tree node using UI thread
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                            // add root compartment node to tree
	                        	ApplicationTreeMap= new HashMap<ApplicationSummary,TreeItem>();
	                        	for(ApplicationSummary application : applications) {                       		
	                        		ApplicationTreeMap.put(application, new TreeItem(tree,0));
	                        		ApplicationTreeMap.get(application).setText(application.getDisplayName());
	                        		ApplicationTreeMap.get(application).setImage(IMAGE);
	                        		ApplicationTreeMap.get(application).setData(APPLICATION_KEY,application);
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
	        tree.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	 TreeItem[] items = tree.getSelection();
		       	        if(items !=null && items.length>0) {
		       	            TreeItem selectedItem = items[0];
		       	            ApplicationSummary application = (ApplicationSummary)selectedItem.getData(APPLICATION_KEY);	      
		       	            ApplicationIdSelected= application.getId();
		       	            allow = true;
		       	            isPageComplete();
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
						createApplicationSection();
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
	    
	    public String getApplicationSelected() {
	        TreeItem[] items = tree.getSelection();
	        if(items !=null && items.length>0) {
	            TreeItem selectedItem = items[0];
	            ApplicationSummary Application = (ApplicationSummary)selectedItem.getData(APPLICATION_KEY);	      
	            
	            return Application.getId();
	        }
	        return null;
	    }
	    
	    @Override
	    public boolean isPageComplete() {
	    return allow;
	    }
	    
		@Override
		public boolean canFlipToNextPage() {
			return true;
		}
	    
		 @Override
		    public IWizardPage getNextPage() { 
			 	
			 	allow = true;
			 	isPageComplete();
			 	getWizard().getContainer().updateButtons();
			   dto.setApplicationId(ApplicationIdSelected); 			   
			   CreateApplicationWizardPage page = ((LocalFileSelectWizard)getWizard()).firstpage;
			   page.onEnterPage();
			   CreateApplicationWizardPage3 advpage = ((LocalFileSelectWizard)getWizard()).thirdpage;
			   advpage.onEnterPage();
			   
			   return page;       
		    }

}
