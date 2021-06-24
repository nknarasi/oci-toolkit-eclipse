package com.oracle.oci.eclipse.ui.explorer.dataflow;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.ui.explorer.RootElement;
import com.oracle.oci.eclipse.ui.explorer.common.BaseContentProvider;

public class DataflowContentProvider extends BaseContentProvider
{
    private static DataflowContentProvider instance;
	private TreeViewer treeViewer;
	Object dataflowRootElement;
	Object dataflowApplicationElement;
	List<ApplicationSummary> applicationList = new ArrayList<ApplicationSummary>();
	List<PrivateEndpointSummary> privateendpointsList = new ArrayList<PrivateEndpointSummary>();
    boolean foundApplications = true;
    boolean foundPrivateEndpoints = true;
    
    
    public DataflowContentProvider() {
        instance = this;
    }
    
    public static DataflowContentProvider getInstance() {
        if (instance == null) {
            instance = new DataflowContentProvider();
        }
        return instance;
    }
    
	 @Override
	    public Object[] getChildren(Object parentElement)
	    {
	        if (parentElement instanceof RootElement) {
	            return new Object[] { new DataflowRootElement() };
	        } else if (parentElement instanceof DataflowRootElement) {	        	
	            return new Object[] {	            			            		
	                    new DataflowApplicationElement(),
	                    new DataflowRunElement(),
	                    new DataflowPrivateEndPointsElement()	                    
	            };
	        } 
	        else {
	            return new Object[0];
	        }
	    }

	    @Override
	    public boolean hasChildren(Object element)
	    {
	        return (element instanceof RootElement || element instanceof DataflowRootElement);
	    }

	    /*
	    public void getApplicationsAndRefresh() {
	        applicationList.clear();
	        new Job("Get Applications") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                try {
	                    ApplicationClient oci = ApplicationClient.getInstance();
	                    applicationList = oci.getApplications();
	                    if (applicationList.size() > 0) {
	                        foundApplications = true;
	                    } else {
	                        foundApplications = false;
	                    }
	                } catch (Exception e) {
	                    return ErrorHandler.reportException("Unable to get applications: " + e.getMessage(), e);
	                }
	                return Status.OK_STATUS;
	            }
	        }.schedule();
	    }
	    
	    public void getPrivateEndPointsAndRefresh() {
	    	privateendpointsList.clear();
	        new Job("Get Private End Points") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                try {
	                    PrivateEndPointsClient oci = PrivateEndPointsClient.getInstance();
	                    privateendpointsList = oci.getPrivateEndPoints();
	                    if (privateendpointsList.size() > 0) {
	                        foundPrivateEndpoints = true;
	                    } else {
	                        foundPrivateEndpoints = false;
	                    }
	                } catch (Exception e) {
	                    return ErrorHandler.reportException("Unable to get private end points: " + e.getMessage(), e);
	                }
	                refresh();
	                return Status.OK_STATUS;
	            }
	        }.schedule();
	    }
	    
	    */
	    @Override
	    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	    {
	        this.treeViewer = (TreeViewer) viewer;
	    }

	    public synchronized void refresh() {
	        Display.getDefault().asyncExec(new Runnable() {
	            @Override
	            public void run() {
	                if (treeViewer != null) {
	                    if (treeViewer.getTree().getSelectionCount() > 0)
	                        treeViewer.getTree().deselectAll();
	                    treeViewer.refresh(dataflowRootElement);
	                    treeViewer.expandToLevel(1);
	                }
	            }
	        });
	    }

}
