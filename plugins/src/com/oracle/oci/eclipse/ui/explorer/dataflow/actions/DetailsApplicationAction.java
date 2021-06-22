package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.Application;
import com.oracle.bmc.dataflow.model.ApplicationLanguage;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;

import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;


public class DetailsApplicationAction extends BaseAction {
	
	    private final List<ApplicationSummary> applicationSelectionList;
	    private String applicationID;
	    private String title = "Application Details";

	    @SuppressWarnings("unchecked")
		public DetailsApplicationAction (ApplicationTable table){
	        applicationSelectionList = (List<ApplicationSummary>) table.getSelectedObjects();
	    }

	    @Override
	    public String getText() {
	        if ( applicationSelectionList.size() == 1 ) {
	            return title;
	        }
	        return "";
	    }

	    @Override
	    protected void runAction() {
	        if (applicationSelectionList.size() > 0) {
	        	ApplicationSummary object = applicationSelectionList.get(0);
	        	applicationID = object.getId();
	        }
	        new Job("Get Application Details") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {

	                try {
	                	Application application = ApplicationClient.getInstance().getApplicationDetails(applicationID);

	                    Display.getDefault().asyncExec(new Runnable() {
	                        @Override
	                        public void run() {
	                            List<TablePair> dataList = createDataList(applicationID, application);
	                            DetailsTable detailsTable= new DetailsTable(title, dataList);
	                            detailsTable.openTable();
	                        }
	                    });
	                } catch (Exception e) {
	                    return ErrorHandler.reportException("Unable to get Application details: " + e.getMessage(), e);
	                }
	               return Status.OK_STATUS;
	            }
	        }.schedule();
	    }

	    protected List<TablePair> createDataList(String name, Application application){  	
	        List<TablePair> data = new ArrayList<TablePair>();
	        data.add(new TablePair("Application ID", application.getId()));
	        data.add(new TablePair("Language", application.getLanguage().getValue()));
	        data.add(new TablePair("Spark Version", application.getSparkVersion()));
	        data.add(new TablePair("Driver Shape", application.getDriverShape()));
	        data.add(new TablePair("Executor Shape", application.getExecutorShape()));
	        data.add(new TablePair("Number of Executors", application.getNumExecutors().toString()));
	        data.add(new TablePair("File URL", application.getFileUri()));
	        
	        if(application.getArchiveUri().equals(null) || application.getArchiveUri().equals("") )
	        	data.add(new TablePair("Archive URL", "NO VALUE"));
	        else
	        	data.add(new TablePair("Archive URL", application.getArchiveUri()));
	        
	        if(application.getLanguage() == ApplicationLanguage.Java)
	        	data.add(new TablePair("Main Class Name", application.getClassName()));
	        
	        
	        if(application.getArguments().size() == 0)
	        	data.add(new TablePair("Arguments", "NO VALUE"));
	        else
	        	data.add(new TablePair("Arguments", application.getArguments().toString()));
	        
	        data.add(new TablePair("Application Log Location", application.getLogsBucketUri()));
	        
	        data.add(new TablePair("Oracle Tags", application.getDefinedTags().toString()));
	        
	        if(application.getFreeformTags().size() != 0)
	        	data.add(new TablePair("FreeForm Tags", application.getFreeformTags().toString()));
	        
	        if(application.getConfiguration() != null)
	        	data.add(new TablePair("Configuration", application.getConfiguration().toString()));
	        
	        if(application.getLanguage() == ApplicationLanguage.Sql &&  application.getParameters() != null) {
	        	data.add(new TablePair("Parameters", application.getParameters().toString()));
	        }
	        if(application.getPrivateEndpointId() != null) {
	        	data.add(new TablePair("Private Endpoints", application.getPrivateEndpointId()));
	        }
	        return data;
	    }
}
