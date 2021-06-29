package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.sdkclients.ObjStorageClient;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class LocalFileSelectWizard extends Wizard implements INewWizard  {	
	private ProjectSelectWizardPage page1;
	private JarSelectPage page2;	 
    LocalFileSelectWizardPage1 firstbpage;
    LocalFileSelectWizardPage2 secondbpage;
    LocalFileSelectWizardPage3 thirdbpage;
    private ISelection selection;
	private String COMPARTMENT_ID;
	DataTransferObject dto;
    CreateApplicationWizardPage firstpage;
    CreateApplicationWizardPage3 thirdpage;
    private TagsPage tagpage;
    private Application application;
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
	    		page2.job.schedule();
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
	    	String bucketName = firstbpage.getBucketSelected();
	    	File applicationFile = new File(dto.getFiledir());
	    	String newfileName = dto.getFiledir().substring(0,dto.getFiledir().lastIndexOf('\\')+1);
	    	File applicationFileNew = new File(newfileName+firstbpage.getnewName());
	    	applicationFile.renameTo(applicationFileNew);
	    	try {
				ObjStorageClient.getInstance().uploadObject(bucketName, applicationFileNew);
				
			} catch (Exception e) {
				ErrorHandler.logError("Unable to upload application .jar to bucket: " + e.getMessage());
			};
			
			if(dto.getArchivedir() != null && secondbpage.getBucketSelected() != null)
			{
				String archivebucketName = secondbpage.getBucketSelected();
		    	File archiveFile = new File(dto.getArchivedir());
		    	String newfileName2 = dto.getArchivedir().substring(0,dto.getFiledir().lastIndexOf('\\')+1);
		    	File archiveFileNew = new File(newfileName2+secondbpage.getnewName());
		    	archiveFile.renameTo(archiveFileNew);
		    	try {
					ObjStorageClient.getInstance().uploadObject(archivebucketName, archiveFileNew);
				} catch (Exception e) {
					 ErrorHandler.logError("Unable to upload archive .zip to bucket: " + e.getMessage());					
				};
			}

			if(dto.applicationId != null) {				
				Application applicationOld = ApplicationClient.getInstance().getApplicationDetails(dto.getApplicationId());				
		    	final String compartmentId = firstpage.getApplicationCompartmentId();
		    	
		    	if(firstpage.usesSparkSubmit()) {		    		
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
		        			.freeformTags(tagpage.getFT())
    						.language(firstpage.getLanguage())
    						.fileUri(firstbpage.getFileUri());
				    		
		    		if(dto.getArchivedir() != null) {
			    		editApplicationRequestBuilder = editApplicationRequestBuilder
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
		        			if(applicationOld.getPrivateEndpointId() != null) {					
		        						editApplicationRequestBuilder
		        						.privateEndpointId("");
		        			}
		        			editApplicationRequest = editApplicationRequestBuilder.build();
		        			}		        						
		        		} 
		        	else {
		        				editApplicationRequest = editApplicationRequestBuilder.build();		        						
		        	}
		        	    		
		        		IRunnableWithProgress op = new IRunnableWithProgress() {	        	      
		        		@Override		        	            
		        		public void run(IProgressMonitor monitor) throws InvocationTargetException {
		        	               application= ApplicationClient.getInstance().editApplication(dto.getApplicationId(),editApplicationRequest);       	               
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
		        	               application= ApplicationClient.getInstance().createApplication(createApplicationRequest);       	               
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
