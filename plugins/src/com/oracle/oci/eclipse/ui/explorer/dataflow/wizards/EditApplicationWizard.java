package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class EditApplicationWizard extends Wizard implements INewWizard {	
    private EditApplicationWizardPage1 firstpage;
    private EditApplicationWizardPage2 secondpage;
    private TagsPage tagpage;
    private ISelection selection;
    private Application application;
    private String warnings="";
	public EditApplicationWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}
	
    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new EditApplicationWizardPage1(selection, dto, application.getId());
        addPage(firstpage);     
        tagpage = new TagsPage(selection,application.getId());
        addPage(tagpage);
        secondpage = new EditApplicationWizardPage2(selection,dto, application.getId());
        addPage(secondpage);        
    }
    
    private boolean validations(EditApplicationWizardPage1 firstpage2, TagsPage tagpage, EditApplicationWizardPage2 secondpage2) {
    	
    	boolean valid = true;
    	if(firstpage2.getDisplayName().length()<1 || firstpage2.getDisplayName().length()>20)
    	{
    		warnings += "Application Name should satisfy constraints" + "\n"; 
    		valid = false;
    	}
    	
    	if(firstpage2.getApplicationDescription().length() > 255 )
    	{
    		warnings += "Application Description should satisfy constraints" + "\n"; 
    		valid = false;
    	}
    	
    	if(application.getExecute() == null) {
    		if( firstpage2.getFileUri() ==null ||firstpage2.getFileUri().equals("")) {    		
        		warnings += "File Uri is absent" + "\n"; 
        		valid = false;    		
        	}
        	if(firstpage2.getLanguage() == ApplicationLanguage.Java && 
        			(firstpage2.getMainClassName() == null || firstpage2.getMainClassName().equals(""))) {
        		warnings += "Main Class Name is absent" + "\n"; 
        		valid = false;   
        	}
    	}
    	
    	if(secondpage2.getApplicationLogLocation() != null && !secondpage2.getApplicationLogLocation().equals("") ) {
    		String loglocation = secondpage2.getApplicationLogLocation();
    		if(loglocation.length() < 9) {
    			warnings += "Log Bucket Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else if( !loglocation.substring(0,6).equals("oci://") ) {
    			warnings += "Log Bucket Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else if(loglocation.charAt(loglocation.length()-1) != '/') {
    			warnings += "Log Bucket Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else {
    			boolean symbol = false;
    			for(int i= 6; i< loglocation.length()-1; i++) {
    				if(loglocation.charAt(i) == '@') {
    					symbol = true;
    					break;
    				}    				
    			}
    			if(symbol == false) {
    				warnings += "Log Bucket Uri format is invalid" + "\n"; 
        			valid = false;
    			}
    		}
    	}
    	
    	if(secondpage2.getWarehouseUri() != null && !secondpage2.getWarehouseUri().equals("") ) {
    		String loglocation = secondpage2.getWarehouseUri();
    		if(loglocation.length() < 9) {
    			warnings += "Warehouse Bucket Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else if( !loglocation.substring(0,6).equals("oci://") ) {
    			warnings += "Warehouse Bucket Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else {
    			boolean symbol = false;
    			for(int i= 6; i< loglocation.length(); i++) {
    				if(loglocation.charAt(i) == '@') {
    					symbol = true;
    					break;
    				}    				
    			}
    			if(symbol == false) {
    				warnings += "Warehouse Bucket Uri format is invalid" + "\n"; 
        			valid = false;
    			}
    		}
    	}
    	if (secondpage2.usesPrivateSubnet() && secondpage2.PrivateEndpointsCombo.getSelectionIndex()<0){
    		warnings += "Select a Private endpoint" + "\n"; 
			valid = false;
    	}
    	
    	if(application.getExecute() == null && firstpage2.getArchiveUri() != null && !firstpage2.getArchiveUri().equals("") ) {
    		String loglocation = firstpage2.getArchiveUri();
    		if(loglocation.length() < 9) {
    			warnings += "Archive Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else if( !loglocation.substring(0,6).equals("oci://") ) {
    			warnings += "Archive Uri format is invalid" + "\n"; 
    			valid = false;
    		}
    		else {
    			boolean symbol1 = false;
    			boolean symbol2= false;
    			for(int i= 6; i< loglocation.length(); i++) {
    				if(loglocation.charAt(i) == '@') {
    					symbol1 = true;
    				}
    				if(loglocation.charAt(i) == '/') {
    					symbol2 = true;
    				}
    			}
    			if(!symbol1 || !symbol2) {
    				warnings += "Archive Uri format is invalid" + "\n"; 
        			valid = false;
    			}
    		}
    	}
    	
    	//SPARK CONFIGURATION VALIDATIONS;
    	if(secondpage2.getSparkProperties() != null) {
    		
    		 for (Map.Entry<String,String> property : secondpage2.getSparkProperties().entrySet()) {
    			 
    			 boolean allowed= false;
    			 String key = property.getKey();
    			 if(firstpage2.getSparkVersion().equals(DataflowConstants.Versions[0])) {
    				 
    				 for(String propertypresent : DataflowConstants.Spark2PropertiesList ) {   					    					 
    					 if(propertypresent.charAt(propertypresent.length()-1) != '*') {
    						 if(key.equals(propertypresent)) {
    							 allowed = true;
    							 break;
    						 }
    					 }
    					 else {
    						 if(propertypresent.length() <= key.length() && 
    								 propertypresent.substring(0, propertypresent.length()-1)
    								 .equals(key.substring(0, propertypresent.length()-1))) {
    							 allowed = true;
    							 break;
    						 }
    					 }
    				 }
    				 
    			 }
    			 else {
    				 
    				 for(String propertypresent : DataflowConstants.Spark3PropertiesList ) {   					    					 
    					 if(propertypresent.charAt(propertypresent.length()-1) != '*') {
    						 if(key.equals(propertypresent)) {
    							 allowed = true;
    							 break;
    						 }
    					 }
    					 else {
    						 if(propertypresent.length() <= key.length() && propertypresent.substring(0, propertypresent.length()-1)
    								 .equals(key.substring(0, propertypresent.length()-1))) {
    							 allowed = true;
    							 break;
    						 }
    					 }
    				 }
    				 
    			 }
    			 
    			 if(!allowed) {
    				 warnings += "Sprak Property " + key + " is not allowed." + "\n"; 
         			valid = false;
    			 }
        	 }         	
    	}
    	
    	
    	return valid;
    }
    
    
    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    
    @Override
    public boolean performFinish() { 	  	
      	warnings = "";
    	if(!validations(firstpage,tagpage,secondpage)) {
    	String title = "Warnings";
   		 String message = warnings;
   		 MessageDialog.openInformation(getShell(), title, message);    		
    	return false;
    	}
    
			Application applicationold = ApplicationClient.getInstance().getApplicationDetails(application.getId());
			
	    	final String compartmentId = applicationold.getCompartmentId();
	    	
	    	if(applicationold.getExecute() != null && !applicationold.getExecute().equals("")) {
	    		//System.out.println("EXECUTE");
	    		
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
	        	        .configuration(secondpage.getSparkProperties())
	        	        .logsBucketUri(secondpage.getApplicationLogLocation())
	        	        .warehouseBucketUri(secondpage.getWarehouseUri());
	        			
	    				final CreateRunDetails createApplicationRequest;	
	        			createApplicationRequest = createApplicationRequestBuilder.build();		
	        	        IRunnableWithProgress op = new IRunnableWithProgress() {
	        	            @Override
	        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
	        	            	ApplicationClient.getInstance().runApplication(createApplicationRequest);
	        	                monitor.done();
	        	            }
	        	        };
	        	        try {
	        	            getContainer().run(true, false, op);
	        	        } catch (InterruptedException e) {
	        	            return false;
	        	        } catch (InvocationTargetException e) {
	        	            Throwable realException = e.getTargetException();
	        	            MessageDialog.openError(getShell(), "Failed to Run Application ", realException.getMessage());
	        	            return false;
	        	        }
	        	        return true;
    	}
    	else {
    	 	Builder editApplicationRequestBuilder = 
    	 	        UpdateApplicationDetails.builder()
    	 			.displayName(firstpage.getDisplayName())
    	 			.description(firstpage.getApplicationDescription())
    	 			.sparkVersion(firstpage.getSparkVersion())
    	 			.driverShape(firstpage.getDriverShape())
    	 			.executorShape(firstpage.getExecutorShape())
    	 			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
    	 			.language(firstpage.getLanguage())
    	 			.fileUri(firstpage.getFileUri())
    	 			.archiveUri(firstpage.getArchiveUri())
    	 			.parameters(firstpage.getParameters())
    	 			.definedTags(tagpage.getOT())
    	 			.freeformTags(tagpage.getFT());
    	 	    	
    	 	        
    	 		   	if(firstpage.getLanguage() == ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
    	 		   		editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstpage.getMainClassName())
    	 	    				.arguments(firstpage.getArguments());					
    	 	    	}
    	 	    	else if (firstpage.getLanguage() == ApplicationLanguage.Python) {
    	 	    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstpage.getArguments());			
    	 	    	}
    	 	    	
    	 		   	editApplicationRequestBuilder = editApplicationRequestBuilder.parameters(firstpage.getParameters());			
    	 	    	
    	 			
    	 	 		final UpdateApplicationDetails editApplicationRequest;
    	 	    	
    	 			final boolean usesAdvancedOptions = secondpage.usesAdvancedOptions();
    	 			if (usesAdvancedOptions) {
    	 				editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(secondpage.getSparkProperties())
    	 						.logsBucketUri(secondpage.getApplicationLogLocation()).warehouseBucketUri(secondpage.getWarehouseUri());
    	 				
    	 				if(secondpage.usesPrivateSubnet()) {
    	 					editApplicationRequest  = editApplicationRequestBuilder.privateEndpointId(secondpage.getPrivateEndPointId())
    	 							.build();
    	 				}
    	 				else {
    	 					
    	 					if(application.getPrivateEndpointId() != null) {					
    	 						editApplicationRequestBuilder
    	 						.privateEndpointId("");
    	 					}
    	 					editApplicationRequest = editApplicationRequestBuilder.build();
    	 				}
    	 						
    	 			} else {
    	 				editApplicationRequest = editApplicationRequestBuilder.build();
    	 						
    	 			}


    	 			
    	 		//	editApplicationRequest = editApplicationRequestBuilder.build();
    	 			
    	 	        IRunnableWithProgress op = new IRunnableWithProgress() {
    	 	            @Override
    	 	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
    	 	                ApplicationClient.getInstance().editApplication(application.getId(),editApplicationRequest);
    	 	                monitor.done();
    	 	            }
    	 	        };
    	 	        try {
    	 	            getContainer().run(true, false, op);
    	 	        } catch (InterruptedException e) {
    	 	            return false;
    	 	        } catch (InvocationTargetException e) {
    	 	            Throwable realException = e.getTargetException();
    	 	            MessageDialog.openError(getShell(), "Failed to Edit Application ", realException.getMessage());
    	 	            return false;
    	 	        }

    	 	        return true;
    	}
   
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
