package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.File;
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
import com.oracle.bmc.dataflow.model.CreateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateRunDetails;
import com.oracle.bmc.dataflow.model.UpdateApplicationDetails;
import com.oracle.bmc.dataflow.model.CreateApplicationDetails.Builder;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class LocalFileSelectWizard extends Wizard implements INewWizard  {
	
	
	 private ProjectSelectWizardPage page1;
	 private JarSelectPage page2;
	 
	 
    LocalFileSelectWizardPage1 firstbpage;
    LocalFileSelectWizardPage2 secondbpage;
    LocalFileSelectWizardPage3 thirdbpage;
    private ISelection selection;
    private String warnings="";
	private String COMPARTMENT_ID;
	DataTransferObject dto;
    CreateApplicationWizardPage firstpage;
    CreateApplicationWizardPage3 thirdpage;
    private TagsPage tagpage;
    private Application application;
    private String filedir,archivedir,filename,archivename= "";
	public LocalFileSelectWizard() {
		super();
		this.COMPARTMENT_ID= AuthProvider.getInstance().getCompartmentId();
		setNeedsProgressMonitor(true);
	}	
	   @Override
	    public void addPages() {	   
	        dto = new DataTransferObject();
	        dto.setLocal(true);
	        
	        page1 = new ProjectSelectWizardPage(selection);
	        page2= new JarSelectPage(selection,page1,dto);
	        addPage(page1);
	        addPage(page2);
	        
	        firstbpage = new LocalFileSelectWizardPage1(selection,dto,COMPARTMENT_ID);
	        addPage(firstbpage);
	        secondbpage = new LocalFileSelectWizardPage2(selection,dto,COMPARTMENT_ID);
		    addPage(secondbpage);
	        thirdbpage = new LocalFileSelectWizardPage3(selection,dto,COMPARTMENT_ID);
		    addPage(thirdbpage);
	        
	        firstpage = new CreateApplicationWizardPage(selection,dto,COMPARTMENT_ID);
	        addPage(firstpage);      
	        thirdpage = new CreateApplicationWizardPage3(selection,dto);
	        addPage(thirdpage);
	        tagpage= new TagsPage(selection,COMPARTMENT_ID);
	        addPage(tagpage);
	    }
	   
	    @Override
	    public IWizardPage getNextPage(IWizardPage page) {
	    	
	    	if(page.equals(page1)) {
	    		return page2;
	    	}
	    	if(page.equals(page2)) {
	    		return firstbpage;
	    	}
	    	
	    	if(dto.getArchivedir() == null && page.equals(firstbpage)) {
	    		return thirdbpage;
	    	}
	    	
	    	if (dto.getArchivedir() != null && page.equals(firstbpage)) {
       		return secondbpage;
	    	}     
	    	
	    	if (page.equals(secondbpage)) {
	       		return thirdbpage;
	    	}    
	    	
	    	if( page.equals(thirdbpage)) {
	    		return firstpage;
	    	}
	    	
	        if (page.equals(firstpage)) {
	        		return tagpage;
	        }       
	        if (page.equals(tagpage)) {
	            return thirdpage;
	        }    
	        return null;       
	    }
	   
	   
	    private boolean validations(LocalFileSelectWizardPage1 firstbpage, 
	    		LocalFileSelectWizardPage2 secondbpage,
	    		CreateApplicationWizardPage firstpage, TagsPage tagpage, 
	    		CreateApplicationWizardPage3 thirdpage) {
	    	
	    	boolean valid = true;
	    	
	    	if(dto.getFiledir() == null) {
	    		warnings+="Please select a project and create files" +"\n";
	    		valid=false;
	    	}
	    	
	    	if(firstbpage.getBucketSelected() == null ) {
	    		warnings+="Please select a bucket for Application" +"\n";
	    		valid=false;
	    		
	    	}
	    	
	    	if(firstpage.getDisplayName().length()<1 || firstpage.getDisplayName().length()>20)
	    	{
	    		warnings += "Application Name should satisfy constraints" + "\n"; 
	    		valid = false;
	    	}
	    	
	    	if(firstpage.getApplicationDescription().length() > 255 )
	    	{
	    		warnings += "Application Description should satisfy constraints" + "\n"; 
	    		valid = false;
	    	}
	    	
	    	
	    	if(!firstpage.usesSparkSubmit() && (firstpage.getLanguage() == ApplicationLanguage.Java && 
	    			(firstpage.getMainClassName() == null || firstpage.getMainClassName().equals("")))) {
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
	    

	    /**
	     * This method is called when 'Finish' button is pressed in
	     * the wizard. We will create an operation and run it
	     * using wizard as execution context.
	     */
	    public boolean performCancel() {
	    	MakeJarAndZip.jarUri=null;
	    	MakeJarAndZip.zipUri=null;
	    	return true;
	    }
	    
	    @Override
	    public boolean performFinish() {
	    	
			
			warnings = "";
	    	if(!validations(firstbpage,secondbpage,firstpage,tagpage,thirdpage)) {
	    	String title = "Warnings";
	   		 String message = warnings;
	   		 MessageDialog.openInformation(getShell(), title, message);    		
	    	return false;
	    	}
	    	
	    	String bucketName = firstbpage.getBucketSelected();
	    	File f = new File(dto.getFiledir());
	    	try {
				ObjStorageClient.getInstance().uploadObject(bucketName, f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			
			if(dto.getArchivedir() != null && secondbpage.getBucketSelected() != null)
			{
				String archivebucketName = secondbpage.getBucketSelected();
		    	File f2 = new File(dto.getArchivedir());
		    	try {
					ObjStorageClient.getInstance().uploadObject(archivebucketName, f2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}

			if(dto.applicationId != null) {
				
				Application applicationold = ApplicationClient.getInstance().getApplicationDetails(dto.getApplicationId());
				
		    	final String compartmentId = firstpage.getApplicationCompartmentId();
		    	
		    	if(firstpage.usesSparkSubmit()) {
		    		//System.out.println("EXECUTE");
		    		
		    		CreateRunDetails.Builder createApplicationRequestBuilder =  
		    				CreateRunDetails.builder()
		        	        .compartmentId(compartmentId)
		        	        .applicationId(dto.getApplicationId())
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
		    		UpdateApplicationDetails.Builder editApplicationRequestBuilder = 
		        	        UpdateApplicationDetails.builder()
		        			.displayName(firstpage.getDisplayName())
		        			.description(firstpage.getApplicationDescription())
		        			.sparkVersion(firstpage.getSparkVersion())
		        			.driverShape(firstpage.getDriverShape())
		        			.executorShape(firstpage.getExecutorShape())
		        			.numExecutors(Integer.valueOf(firstpage.getNumofExecutors()))
		        			.definedTags(tagpage.getOT())
		        			.freeformTags(tagpage.getFT());	
		    		
		    		if(dto.getArchivedir() != null) {
			    		editApplicationRequestBuilder = editApplicationRequestBuilder
        						.language(firstpage.getLanguage())
        						.fileUri(firstbpage.getFileUri())
        						.archiveUri(secondbpage.getArchiveUri());
		    		}
		    		else{
		    			editApplicationRequestBuilder = editApplicationRequestBuilder
        						.language(firstpage.getLanguage())
        						.fileUri(firstbpage.getFileUri())
        						.archiveUri("");
		    		}

		        				
		        				if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
		        					editApplicationRequestBuilder = editApplicationRequestBuilder.className(firstpage.getMainClassName())
		        		    				.arguments(firstpage.getArguments());					
		        		    	}
		        		    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
		        		    		editApplicationRequestBuilder = editApplicationRequestBuilder.arguments(firstpage.getArguments());			
		        		    	}
		        		    	else if (firstpage.getLanguage()== ApplicationLanguage.Sql) {
		        		    		editApplicationRequestBuilder= editApplicationRequestBuilder.parameters(firstpage.getParameters());			
		        		    	}

		        	    	final UpdateApplicationDetails editApplicationRequest;
		        	    	
		        			final boolean usesAdvancedOptions = thirdpage.usesAdvancedOptions();
		        			if (usesAdvancedOptions) {
		        				editApplicationRequestBuilder = editApplicationRequestBuilder.configuration(thirdpage.getSparkProperties())
		        						.logsBucketUri(thirdpage.getApplicationLogLocation()).warehouseBucketUri(thirdpage.getWarehouseUri());
		        				
		        				final boolean usesPrivateSubnet = thirdpage.usesPrivateSubnet();
		        				if(usesPrivateSubnet) {
		        					editApplicationRequest = editApplicationRequestBuilder.privateEndpointId(thirdpage.getPrivateEndPointId())
		        							.build();
		        				}
		        				else {
		        					if(applicationold.getPrivateEndpointId() != null) {					
		        						editApplicationRequestBuilder
		        						.privateEndpointId("");
		        					}
		        					editApplicationRequest = editApplicationRequestBuilder.build();
		        				}
		        						
		        			} else {
		        				editApplicationRequest = editApplicationRequestBuilder.build();
		        						
		        			}
		        	    		
		        	        IRunnableWithProgress op = new IRunnableWithProgress() {
		        	            @Override
		        	            public void run(IProgressMonitor monitor) throws InvocationTargetException {
		        	               application= ApplicationClient.getInstance().editApplicationandgetId(dto.getApplicationId(),editApplicationRequest);       	               
		        	               monitor.done();
		        	            }
		        	        };
		        	        
		        	        
		        	        try {
		        	            getContainer().run(true, false, op);
		        	        } catch (InterruptedException e) {
		        	        	System.out.println("ERROR");
		        	            return false;
		        	        } catch (InvocationTargetException e) {
		        	            Throwable realException = e.getTargetException();
		        	            MessageDialog.openError(getShell(), "Failed to Create Application ", realException.getMessage());
		        	            return false;
		        	        }    
		        	      
	     	               final CreateRunDetails runApplicationRequest =CreateRunDetails.builder()
	     		        	        .compartmentId(application.getCompartmentId())
	     		        			.displayName(application.getDisplayName())
	     		    				.applicationId(application.getId())
	     		    				.archiveUri(application.getArchiveUri())
	     		    				.driverShape(application.getDriverShape())
	     		    		        .executorShape(application.getExecutorShape())
	     		    		        .numExecutors(application.getNumExecutors())
	     		    		        .configuration(application.getConfiguration())
	     		    		        .logsBucketUri(application.getLogsBucketUri())
	     		    		        .warehouseBucketUri(application.getWarehouseBucketUri())
	     		    		        .arguments(application.getArguments())
	     		    		        .parameters(application.getParameters())
	     		    		        .build();
	     	               
	     	              IRunnableWithProgress oprun = new IRunnableWithProgress() {
	     	                 @Override
	     	                 public void run(IProgressMonitor monitor) throws InvocationTargetException {
	     	                     ApplicationClient.getInstance().runApplication(runApplicationRequest);
	     	                     monitor.done();
	     	                 }
	     	             };
	     	             try {
	     	                 getContainer().run(true, false, oprun);
	     	             } catch (InterruptedException e) {
	     	                 return false;
	     	             } catch (InvocationTargetException e) {
	     	                 Throwable realException = e.getTargetException();
	     	                 MessageDialog.openError(getShell(), "Failed to Run Application ", realException.getMessage());
	     	                 return false;
	     	             }
	     	           
		        	        return true;
		    	}
				
			}
			else {
		    	final String compartmentId = firstpage.getApplicationCompartmentId();
		    	
		    	if(firstpage.usesSparkSubmit()) {
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
		        	        .configuration(thirdpage.getSparkProperties())
		        	        .logsBucketUri(thirdpage.getApplicationLogLocation())
		        	        .warehouseBucketUri(thirdpage.getWarehouseUri());
		        			
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
		        			createApplicationRequestBuilder = createApplicationRequestBuilder
		        						.language(firstpage.getLanguage())
		        						.fileUri(firstbpage.getFileUri())
		        						.archiveUri(secondbpage.getArchiveUri());
		        				
		        				if(firstpage.getLanguage()== ApplicationLanguage.Java || firstpage.getLanguage()== ApplicationLanguage.Scala){
		        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.className(firstpage.getMainClassName())
		        		    				.arguments(firstpage.getArguments());					
		        		    	}
		        		    	else if (firstpage.getLanguage()== ApplicationLanguage.Python) {
		        		    		createApplicationRequestBuilder = createApplicationRequestBuilder.arguments(firstpage.getArguments());			
		        		    	}
		        		    	
		        		    createApplicationRequestBuilder = createApplicationRequestBuilder.parameters(firstpage.getParameters());			
		        		    	

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
		        	               application= ApplicationClient.getInstance().createApplicationandgetId(createApplicationRequest);       	               
		        	               monitor.done();
		        	            }
		        	        };
		        	        
		        	        
		        	        try {
		        	            getContainer().run(true, false, op);
		        	        } catch (InterruptedException e) {
		        	        	System.out.println("ERROR");
		        	            return false;
		        	        } catch (InvocationTargetException e) {
		        	            Throwable realException = e.getTargetException();
		        	            MessageDialog.openError(getShell(), "Failed to Create Application ", realException.getMessage());
		        	            return false;
		        	        }    
		        	      
	     	               final CreateRunDetails runApplicationRequest =CreateRunDetails.builder()
	     		        	        .compartmentId(application.getCompartmentId())
	     		        			.displayName(application.getDisplayName())
	     		    				.applicationId(application.getId())
	     		    				.archiveUri(application.getArchiveUri())
	     		    				.driverShape(application.getDriverShape())
	     		    		        .executorShape(application.getExecutorShape())
	     		    		        .numExecutors(application.getNumExecutors())
	     		    		        .configuration(application.getConfiguration())
	     		    		        .logsBucketUri(application.getLogsBucketUri())
	     		    		        .warehouseBucketUri(application.getWarehouseBucketUri())
	     		    		        .arguments(application.getArguments())
	     		    		        .parameters(application.getParameters())
	     		    		        .build();
	     	               
	     	              IRunnableWithProgress oprun = new IRunnableWithProgress() {
	     	                 @Override
	     	                 public void run(IProgressMonitor monitor) throws InvocationTargetException {
	     	                     ApplicationClient.getInstance().runApplication(runApplicationRequest);
	     	                     monitor.done();
	     	                 }
	     	             };
	     	             try {
	     	                 getContainer().run(true, false, oprun);
	     	             } catch (InterruptedException e) {
	     	                 return false;
	     	             } catch (InvocationTargetException e) {
	     	                 Throwable realException = e.getTargetException();
	     	                 MessageDialog.openError(getShell(), "Failed to Run Application ", realException.getMessage());
	     	                 return false;
	     	             }
	     	           
		        	        return true;
		    	}
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
