package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.PrivateEndPointsClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.CreatePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeletePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RefreshPrivateEndpointAction;

public class PrivateEndpointTable extends BaseTable {
    private int tableDataSize = 0;
    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
	private static final int CREATED_COL = 2;
	public static String compid;
	private static String compname;

    public PrivateEndpointTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        compid=AuthProvider.getInstance().getCompartmentId();
    }
    List<PrivateEndpointSummary> privateEndpointSummaryList = new ArrayList<PrivateEndpointSummary>();
    @Override
    public List<PrivateEndpointSummary> getTableData() {
        new Job("Get Private Endpoints") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if(compid==null) privateEndpointSummaryList =PrivateEndPointsClient.getInstance().getPrivateEndPoints(AuthProvider.getInstance().getCompartmentId());
					else privateEndpointSummaryList = PrivateEndPointsClient.getPrivateEndPoints(compid);
                    tableDataSize = privateEndpointSummaryList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();
        return privateEndpointSummaryList;
    }
    @Override
    public List<PrivateEndpointSummary> getTableCachedData() {
        return privateEndpointSummaryList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    /* Label provider */
    private final class TableLabelProvider extends BaseTableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                PrivateEndpointSummary s = (PrivateEndpointSummary) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case STATE_COL:
                    return s.getLifecycleState().toString();
				case CREATED_COL:
					return (new SimpleDateFormat("dd-M-yyyy hh:mm:ss")).format(s.getTimeCreated());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
    	
        createColumn(tableColumnLayout,tree, "Name", 15);
        createColumn(tableColumnLayout,tree, "State", 8);
		createColumn(tableColumnLayout,tree, "Created", 10);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshPrivateEndpointAction(PrivateEndpointTable.this));
		manager.add(new CreatePrivateEndpointAction(PrivateEndpointTable.this));
        manager.add(new Separator());

        /*if (getSelectedObjects().size() > 0) {
            for (String action: actionMap.keySet()) {
                manager.add(new InstanceAction(InstanceTable.this, action));
            }
        }*/
        if (getSelectedObjects().size() == 1) {
           String pepState=((PrivateEndpointSummary)getSelectedObjects().get(0)).getLifecycleState().toString();
           manager.add(new Separator());
           if(!pepState.equals("Creating")) manager.add(new DetailsPrivateEndpointAction(PrivateEndpointTable.this));
           if(!pepState.equals("Creating")) manager.add(new DeletePrivateEndpointAction(PrivateEndpointTable.this,(PrivateEndpointSummary)getSelectedObjects().get(0)));
		   if(pepState.equals("Creating")||pepState.equals("Failed")) {}
		   else {
			   manager.add(new EditPrivateEndpointAction((PrivateEndpointSummary)getSelectedObjects().get(0),PrivateEndpointTable.this));
		   }
		   //manager.add(new RunAction((RunSummary)getSelectedObjects().get(0),RunTable.this));
        }

    }
	
	@Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {
		
		ccb.setText("Change Compartment");ccb.setVisible(true);
        ccb.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				Consumer<Compartment> consumer=new Consumer<Compartment>() {

				@Override
				public void accept(Compartment comp) {
					compid = comp.getId();
					compname = comp.getName();
				}
				};
				CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
						new CompartmentSelectWizard(consumer, false));
				dialog.setFinishButtonText("Select");
				if (Window.OK == dialog.open()) {
					setCompartmentName(new String(compname));
					refresh(true);
				}
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }
}