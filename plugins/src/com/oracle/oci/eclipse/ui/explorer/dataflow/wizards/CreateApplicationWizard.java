package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
    private String warnings="";
    
	public CreateApplicationWizard(String COMPARTMENT_ID) {
		super();
		this.COMPARTMENT_ID= COMPARTMENT_ID;
		setNeedsProgressMonitor(true);
	}
	

    @Override
    public void addPages() {
    	DataTransferObject dto = new DataTransferObject();
        firstpage = new CreateApplicationWizardPage(selection,dto,COMPARTMENT_ID);
        addPage(firstpage);      
        thirdpage = new CreateApplicationWizardPage3(selection,dto);
        addPage(thirdpage);
        tagpage= new TagsPage(selection,COMPARTMENT_ID);
        addPage(tagpage);
        
    }
    private boolean validations(CreateApplicationWizardPage firstpage, TagsPage tagpage, CreateApplicationWizardPage3 thirdpage) {
    	
    	boolean valid = true;
    	if(firstpage.getDisplayName().length()<1 || firstpage.getDisplayName().length()>20)
    	{
    		warnings += "Application Name should satisfy constraints" + "\n"; 
    		valid = false;
    	}
    	
    	if(firstpage.getApplicationDescription().length() > 50 )
    	{
    		warnings += "Application Description should satisfy constraints" + "\n"; 
    		valid = false;
    	}
    	if(firstpage.getFileUri() ==null ||firstpage.getFileUri().equals("") ) {    		
    		warnings += "File Uri is absent" + "\n"; 
    		valid = false;    		
    	}
    	if(firstpage.getLanguage() == ApplicationLanguage.Java && 
    			(firstpage.getMainClassName() == null || firstpage.getMainClassName().equals(""))) {
    		warnings += "Main Class Name is absent" + "\n"; 
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
    	if (thirdpage.usesPrivateSubnet() && thirdpage.PrivateEndpointsCombo.getSelectionIndex()<0){
    		warnings += "Select a Private endpoint" + "\n"; 
			valid = false;
    	}
    	
    	if(firstpage.getArchiveUri() != null && !firstpage.getArchiveUri().equals("") ) {
    		String loglocation = firstpage.getArchiveUri();
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
    						 if( propertypresent.length() <= key.length() && propertypresent.substring(0, propertypresent.length()-1)
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
    	warnings = "";
    	if(!validations(firstpage,tagpage,thirdpage)) {
    	String title = "Warnings";
   		 String message = warnings;
   		 MessageDialog.openInformation(getShell(), title, message);    		
    	return false;
    	}
    	
    	
    	final String compartmentId = firstpage.getApplicationCompartmentId();
        
    	Builder createApplicationRequestBuilder = 
        CreateApplicationDetails.builder()
        .compartmentId(compartmentId)
		.displayName(firstpage.getDisplayName())
		.description(firstpage.getApplicationDescription())
		.sparkVersion(firstpage.getSparkVersion())
		.driverShape(firstpage.getDriverShape())
		.executorShape(firstpage.getExecutorShape())
		.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		.language(firstpage.getLanguage())
		.fileUri(firstpage.getFileUri())
		.archiveUri(firstpage.getArchiveUri())
		.definedTags(tagpage.getOT())
		.freeformTags(tagpage.getFT());
		
    	final CreateApplicationDetails createApplicationRequest;
    	
    	if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstpage.getMainClassName())
    				.arguments(firstpage.getArguments());					
    	}
    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstpage.getArguments());			
    	}
    	else if (firstpage.getLanguage()== ApplicationLanguage.Sql) {
    		createApplicationRequestBuilder = createApplicationRequestBuilder.parameters(firstpage.getParameters());			
    	}
    	
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
                ApplicationClient.getInstance().createApplication(createApplicationRequest);
                monitor.done();
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Failed to Create Application ", realException.getMessage());
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
