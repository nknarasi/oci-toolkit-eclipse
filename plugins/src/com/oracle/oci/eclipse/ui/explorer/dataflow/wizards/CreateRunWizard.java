package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.TagsPage;

public class CreateRunWizard  extends Wizard implements INewWizard{	
    private CreateRunWizardPage1 firstpage;
    private TagsPage secondpage;
    private CreateRunWizardPage3 thirdpage;
    private ISelection selection;
    private Application application;
    private String warnings= "";
    
	public CreateRunWizard(String applicationId) {
		super();
		setNeedsProgressMonitor(true);
		application = ApplicationClient.getInstance().getApplicationDetails(applicationId);
	}
    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new CreateRunWizardPage1(selection,dto,application.getId());
        addPage(firstpage);
        //secondpage= new CreateRunWizardPage2(selection);
        secondpage=new TagsPage(selection,application.getCompartmentId());
        addPage(secondpage);
        thirdpage = new CreateRunWizardPage3(selection,dto,application.getId());
        addPage(thirdpage);       
    }
    
    
   private boolean validations(CreateRunWizardPage1 firstpage, TagsPage tagpage, CreateRunWizardPage3 thirdpage) {
    	
    	boolean valid = true;
    	if(firstpage.getDisplayName().length()<1 || firstpage.getDisplayName().length()>20)
    	{
    		warnings += "Application Name should satisfy constraints" + "\n"; 
    		valid = false;
    	}
    	
    	if(thirdpage.getApplicationLogLocation() != null && !thirdpage.getApplicationLogLocation().equals("") ) {
    		String loglocation = thirdpage.getApplicationLogLocation();
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
    	
    	if(thirdpage.getWarehouseUri() != null && !thirdpage.getWarehouseUri().equals("") ) {
    		String loglocation = thirdpage.getWarehouseUri();
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
    	
    	//SPARK CONFIGURATION VALIDATIONS;
    	if(thirdpage.getSparkProperties() != null) {
    		
    		 for (Map.Entry<String,String> property : thirdpage.getSparkProperties().entrySet()) {
    			 
    			 boolean allowed= false;
    			 String key = property.getKey();
    			 if(firstpage.getSparkVersion().equals(DataflowConstants.Versions[0])) {
    				 
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
    
    
    @Override
    public boolean performFinish() {   
    	
      	warnings = "";
    	if(!validations(firstpage,secondpage,thirdpage)) {
    	String title = "Warnings";
   		 String message = warnings;
   		 MessageDialog.openInformation(getShell(), title, message);    		
    	return false;
    	}
    	
    	Builder runApplicationRequestBuilder = 
        CreateRunDetails.builder()
        .compartmentId(application.getCompartmentId())///////
        .applicationId(application.getId())
        .displayName(firstpage.getDisplayName())
        .archiveUri(application.getArchiveUri())
        .arguments(firstpage.getArguments())
        .driverShape(firstpage.getDriverShape())
        .executorShape(firstpage.getExecutorShape())
        .numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
        
        .definedTags(secondpage.getOT())
        .freeformTags(secondpage.getFT())
        
        .configuration(thirdpage.getSparkProperties())
        .logsBucketUri(thirdpage.getApplicationLogLocation())
        .warehouseBucketUri(thirdpage.getWarehouseUri());
		
		final CreateRunDetails runApplicationRequest;		
		runApplicationRequest = runApplicationRequestBuilder.build();		
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                ApplicationClient.getInstance().runApplication(runApplicationRequest);
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
