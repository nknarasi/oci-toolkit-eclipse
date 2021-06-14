package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

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

import com.oracle.bmc.dataflow.model.PrivateEndpoint;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
//import com.oracle.oci.eclipse.sdkclients.InstanceWrapper;
import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable;
import com.oracle.oci.eclipse.ui.explorer.common.DetailsTable.TablePair;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

public class DetailsPrivateEndpointAction extends BaseAction {

    private final PrivateEndpointTable table;
    private final List<PrivateEndpointSummary> privateEndpointSelectionList;
    //private String instanceName;
    private String pepID;
    private String title = "Private Endpoint Details";
	private PrivateEndpoint privateEndpointObject;

    public DetailsPrivateEndpointAction (PrivateEndpointTable table){
        this.table = table;
        privateEndpointSelectionList = (List<PrivateEndpointSummary>) table.getSelectedObjects();
    }

    @Override
    public String getText() {
        if ( privateEndpointSelectionList.size() == 1 ) {
            return "PrivateEndpoint Details";
        }
        return "";
    }

    @Override
    protected void runAction() {
        if (privateEndpointSelectionList.size() > 0) {
            PrivateEndpointSummary object = privateEndpointSelectionList.get(0);
            //instanceName = object.getDisplayName();
            pepID = object.getId();
			try {
				privateEndpointObject=PrivateEndPointsClient.getInstance().getPrivateEndpointDetails(pepID);
			} catch (Exception e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get Private Endpoint details: ",e.getMessage());
			}
        }
        new Job("Get PrivateEndpoint Details") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    //InstanceWrapper instance = ComputeInstanceClient.getInstance().getInstanceDetails(instanceID);

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<TablePair> dataList = createDataList(privateEndpointObject);
                            DetailsTable detailsTable= new DetailsTable(title, dataList);
                            detailsTable.openTable();
                        }
                    });

                } catch (Exception e) {
                    return ErrorHandler.reportException("Unable to get Private Endpoint details: " + e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();


    }

    protected List<TablePair> createDataList(PrivateEndpoint obj) {
        List<TablePair> data = new ArrayList<TablePair>();
		data.add(new TablePair("Subnet:", obj.getSubnetId()));
        data.add(new TablePair("DNS Zones to Resolve:", String.join(",",obj.getDnsZones())));
		data.add(new TablePair("State Details:", obj.getLifecycleDetails()));
		data.add(new TablePair("Number of Hosts to Access:", obj.getMaxHostCount().toString()));
		if(obj.getNsgIds()!=null) data.add(new TablePair("Network Security Groups:", String.join(",",obj.getNsgIds())));
		else data.add(new TablePair("Network Security Groups:",""));
		data.add(new TablePair("OCID", obj.getId()));
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
