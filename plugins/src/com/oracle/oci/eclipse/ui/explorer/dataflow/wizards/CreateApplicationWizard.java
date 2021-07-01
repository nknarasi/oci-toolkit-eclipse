package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.AddCreateApplicationPagesAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.Validations;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class CreateApplicationWizard extends Wizard implements INewWizard {	
	private String COMPARTMENT_ID;
    private CreateApplicationWizardPage firstpage;
    private CreateApplicationWizardPage3 thirdpage;
    private TagsPage tagpage;
    private ISelection selection;
    
	public CreateApplicationWizard(String COMPARTMENT_ID) {
		super();
		this.COMPARTMENT_ID= COMPARTMENT_ID;
		setNeedsProgressMonitor(true);
	}
	
    @Override
    public void addPages() {
    	 try {
          	IRunnableWithProgress op = new AddCreateApplicationPagesAction(this);
              new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
          } catch (Exception e) {
          	MessageDialog.openError(getShell(), "Unable to add pages to Create Application wizard", e.getMessage());
          }   
    }
    
    public void addPagesWithProgress(IProgressMonitor monitor) {
    	DataTransferObject dto = new DataTransferObject();
    	monitor.subTask("Adding Main page");    	
    	firstpage = new CreateApplicationWizardPage(selection,dto,COMPARTMENT_ID);
       	addPage(firstpage);
    	monitor.subTask("Adding Tags Page");
        tagpage= new TagsPage(selection,COMPARTMENT_ID);
        addPage(tagpage);  
        monitor.subTask("Adding Advanced Options page");
        thirdpage = new CreateApplicationWizardPage3(selection,dto);
        addPage(thirdpage);
    }
    
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(firstpage)) {
        		return tagpage;
        }       
        if (page.equals(tagpage)) {
            return thirdpage;
        }    
        return null;       
    }
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    
    @Override
    public boolean performFinish() {  
    	List<Object> validObjects = new ArrayList<Object>();
    	List<String> objectType = new ArrayList<String>();
    	
    	performValidations(validObjects,objectType);       	
    	String message=Validations.check(validObjects.toArray(),objectType.toArray(new String[1]));
    	
    	if(!message.isEmpty()) {
    		open("Improper Entries",message);
    		return false;
    	}
    	
    	final String compartmentId = firstpage.getApplicationCompartmentId();    	
    	if(firstpage.usesSparkSubmit()) {   		
    		CreateRunDetails.Builder createApplicationRequestBuilder =  
    				CreateRunDetails.builder()
        	        .compartmentId(compartmentId)
        			.displayName(firstpage.getDisplayName())
        			.sparkVersion(firstpage.getSparkVersion())
        			.driverShape(firstpage.getDriverShape())
        			.executorShape(firstpage.getExecutorShape())
        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
        			.definedTags(tagpage.getOT())
        			.freeformTags(tagpage.getFT())   
        			.execute(firstpage.getSparkSubmit())
        	        .configuration(thirdpage.getSparkProperties())
        	        .logsBucketUri(thirdpage.getApplicationLogLocation())
        	        .warehouseBucketUri(thirdpage.getWarehouseUri());
        			
    				final CreateRunDetails createApplicationRequest;	
        			createApplicationRequest = createApplicationRequestBuilder.build();		
        	        IRunnableWithProgress op = new IRunnableWithProgress() {
        	            @Override
        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
        	            	DataflowClient.getInstance().runApplication(createApplicationRequest);
        	                monitor.done();
        	            }
        	        };
        	        try {
        	            getContainer().run(true, false, op);
        	        } 
        	        catch (Exception e) {
        	            MessageDialog.openError(getShell(), "Failed to Create Application ", e.getMessage());
        	            return false;
        	        }
        	        return true;
    	}
    	
    	else {
        	Builder createApplicationRequestBuilder = 
        	        CreateApplicationDetails.builder()
        	        .compartmentId(compartmentId)
        			.displayName(firstpage.getDisplayName())
        			.description(firstpage.getApplicationDescription())
        			.sparkVersion(firstpage.getSparkVersion())
        			.driverShape(firstpage.getDriverShape())
        			.executorShape(firstpage.getExecutorShape())
        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
        			.definedTags(tagpage.getOT())
        			.freeformTags(tagpage.getFT());
        	        			
        			if(firstpage.usesSparkSubmit() == false) {
        				createApplicationRequestBuilder = createApplicationRequestBuilder
        						.language(firstpage.getLanguage())
        						.fileUri(firstpage.getFileUri())
        						.archiveUri(firstpage.getArchiveUri());
        				
        				if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstpage.getMainClassName())
        		    				.arguments(firstpage.getArguments());					
        		    	}
        		    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstpage.getArguments());			
        		    	}        		    	
        		    createApplicationRequestBuilder = createApplicationRequestBuilder.parameters(firstpage.getParameters());			       		    	
        			}
        			else{
        				createApplicationRequestBuilder = createApplicationRequestBuilder.execute(firstpage.getSparkSubmit());
        			}
        			final CreateApplicationDetails createApplicationRequest;       	    	
        			final boolean usesAdvancedOptions = thirdpage.usesAdvancedOptions();
        			if (usesAdvancedOptions) {
        				createApplicationRequestBuilder = createApplicationRequestBuilder.configuration(thirdpage.getSparkProperties())
        						.logsBucketUri(thirdpage.getApplicationLogLocation()).warehouseBucketUri(thirdpage.getWarehouseUri());
        				
        				final boolean usesPrivateSubnet = thirdpage.usesPrivateSubnet();
        				if(usesPrivateSubnet) {
        					createApplicationRequest = createApplicationRequestBuilder.privateEndpointId(thirdpage.getPrivateEndPointId())
        							.build();
        				}
        				else {
        					createApplicationRequest = createApplicationRequestBuilder.build();
        				}        						
        			} else {
        				createApplicationRequest = createApplicationRequestBuilder.build();       						
        			}
        	    		
        	        IRunnableWithProgress op = new IRunnableWithProgress() {
        	            @Override
        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
        	            	DataflowClient.getInstance().createApplication(createApplicationRequest);
        	                monitor.done();
        	            }
        	        };
        	        try {
        	            getContainer().run(true, false, op);
        	        }catch (Exception e) {        	        
        	            MessageDialog.openError(getShell(), "Failed to Create Application ", e.getMessage());
        	            return false;
        	        }
        	        return true;
    	}
    }
    
    public void performValidations(List<Object> objectArray,List<String>nameArray) {
    	
    	objectArray.add(firstpage.getDisplayName());
    	nameArray.add("name");
    	
    	objectArray.add(firstpage.getApplicationDescription());
    	nameArray.add("description");

    	if(!firstpage.usesSparkSubmit()) {    		
        	objectArray.add(firstpage.getFileUri());
        	nameArray.add("fileuri");   		
    	}
    	if(!firstpage.usesSparkSubmit() && (firstpage.getLanguage() == ApplicationLanguage.Java )) {
        	objectArray.add(firstpage.getMainClassName());
        	nameArray.add("mainclassname"); 
    	}
    	
       objectArray.add(thirdpage.getApplicationLogLocation());
       nameArray.add("loguri"); 
       
       if(thirdpage.getWarehouseUri() != null && !thirdpage.getWarehouseUri().isEmpty()) {
           objectArray.add(thirdpage.getWarehouseUri());
           nameArray.add("warehouseuri"); 
       }     
    	if (thirdpage.usesPrivateSubnet()){
    	       objectArray.add(thirdpage.privateEndpointsCombo.getText());
    	       nameArray.add("subnetid"); 
    	}
    	
    	if(!firstpage.usesSparkSubmit() && firstpage.getArchiveUri() != null && !firstpage.getArchiveUri().isEmpty()) {
    	       objectArray.add(firstpage.getArchiveUri());
    	       nameArray.add("archiveuri"); 
    	}
    	
    	if(thirdpage.getSparkProperties() != null) {
 	       objectArray.add(thirdpage.getSparkProperties().keySet());
 	       nameArray.add("sparkprop" + firstpage.getSparkVersion().charAt(0));         
    	}

    }
    public void open(String h,String m) {
    	MessageDialog.openInformation(getShell(), h, m);
    }
    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
