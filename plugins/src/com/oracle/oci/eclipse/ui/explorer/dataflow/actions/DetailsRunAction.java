package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

//import com.oracle.bmc.core.model.Instance;
//import com.oracle.bmc.core.model.Vnic;
//import com.oracle.bmc.core.model.VolumeAttachment;

import com.oracle.bmc.dataflow.model.Run;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.RunClient;
//import com.oracle.oci.eclipse.sdkclients.InstanceWrapper;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

public class DetailsRunAction extends BaseAction {

    private final RunTable table;
    private final List<RunSummary> runSelectionList;
    //private String instanceName;
    private String runID;
    private String title = "Run Details";
	private Run runObject;

    public DetailsRunAction (RunTable table){
        this.table = table;
        runSelectionList = (List<RunSummary>) table.getSelectedObjects();
    }

    @Override
    public String getText() {
        if ( runSelectionList.size() == 1 ) {
            return "Run Details";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (runSelectionList.size() > 0) {
            RunSummary object = runSelectionList.get(0);
            //instanceName = object.getDisplayName();
            runID = object.getId();
			try {
				runObject=RunClient.getInstance().getRunDetails(runID);
			} catch (Exception e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get Run details: ",e.getMessage());
			}
        }
        new Job("Get Run Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    //InstanceWrapper instance = ComputeInstanceClient.getInstance().getInstanceDetails(instanceID);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(runObject);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to get Run details: " + e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();


    }

    protected List<TablePair> createDataList(Run obj) {
        List<TablePair> data = new ArrayList<TablePair>();
        data.add(new TablePair("Application Configuration", ""));
        data.add(new TablePair("", ""));
		data.add(new TablePair("Application Type:", obj.getLanguage().toString()));
        data.add(new TablePair("OCID:", obj.getId()));
		data.add(new TablePair("File URL:", obj.getFileUri()));
		data.add(new TablePair("Archive URI:", obj.getArchiveUri()));
		data.add(new TablePair("Main Class Name:", obj.getClassName()));
		data.add(new TablePair("Arguments:", String.join(",",obj.getArguments())));
		data.add(new TablePair("Application Log Location:", obj.getLogsBucketUri()));
		data.add(new TablePair("", ""));
		
		data.add(new TablePair("Run Information", ""));
		data.add(new TablePair("", ""));
		data.add(new TablePair("State Details:", obj.getLifecycleDetails()));
		data.add(new TablePair("Created:", (new SimpleDateFormat("dd-M-yyyy hh:mm:ss")).format(obj.getTimeCreated())));
		data.add(new TablePair("Owner:", obj.getOwnerUserName()));
		data.add(new TablePair("Request Id:", obj.getOpcRequestId()));
		data.add(new TablePair("", ""));
		
		data.add(new TablePair("Resource Configuration", ""));
		data.add(new TablePair("", ""));
		data.add(new TablePair("Spark Version:", obj.getSparkVersion()));
		data.add(new TablePair("Driver Shape:", obj.getDriverShape()));
		data.add(new TablePair("Executor Shape:", obj.getExecutorShape()));
		data.add(new TablePair("Number of Executors:", obj.getNumExecutors().toString()));
		data.add(new TablePair("", ""));
        /*data.add(new TablePair("Instance OCID", instance.getInstance().getId()));

        List<VolumeAttachment> volAttachIterable = instance.getVolumeAttachments();
        for (VolumeAttachment volumeAttachment : volAttachIterable) {
            data.add(new TablePair("Attached Volume", volumeAttachment.getDisplayName()));
        }
        try {
            Vnic vnic = instance.getVnic();
            if(vnic != null) {
                data.add(new TablePair("Public IP", vnic.getPublicIp()));
                data.add(new TablePair("Private IP", vnic.getPrivateIp()));
            }
        }
        catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);;
        }*/
        return data;
    }

}
