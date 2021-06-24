package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.ApplicationClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeleteApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RefreshApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RunApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.CreateApplicationWizard;

public class ApplicationTable extends BaseTable{
   
	private int tableDataSize = 0;
	
    private static final int ID_COL = 1;
    private static final int NAME_COL = 0;
    private static final int LANGUAGE_COL = 2;
    private static final int OWNER_COL = 3;
    private static final int CREATED_COL = 4;
    private static final int UPDATED_COL = 5;
    
    private static String COMPARTMENT_ID;
    private static String COMPARTMENT_NAME;
    
    ListApplicationsRequest.SortBy s=ListApplicationsRequest.SortBy.TimeCreated;
	ListApplicationsRequest.SortOrder so=ListApplicationsRequest.SortOrder.Desc;
	
    public ApplicationTable(Composite parent, int style) {
        super(parent, style);
        COMPARTMENT_ID = AuthProvider.getInstance().getCompartmentId();
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        s=ListApplicationsRequest.SortBy.TimeCreated;
        so=ListApplicationsRequest.SortOrder.Desc;
    }
    
    List<ApplicationSummary> applicationList = new ArrayList<ApplicationSummary>();
    
    @Override
    public List<ApplicationSummary> getTableData() {
        new Job("Get Applications") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ApplicationClient oci = ApplicationClient.getInstance();
                    applicationList = oci.getApplicationsbyCompartmentId(COMPARTMENT_ID,s,so);
                    for (Iterator<ApplicationSummary> it = applicationList.iterator(); it.hasNext(); ) {
                        ApplicationSummary instance = it.next();
                        if (instance.getLifecycleState().getValue().equals("TERMINATED")) {
                            it.remove();
                        }
                    }
                    tableDataSize = applicationList.size();
                } catch (Exception e) {
                    return ErrorHandler.reportException(e.getMessage(), e);
                }
                refresh(false);
                return Status.OK_STATUS;
            }
        }.schedule();
        return applicationList;
    }

    @Override
    public List<ApplicationSummary> getTableCachedData() {
        return applicationList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    private final class TableLabelProvider extends BaseTableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                ApplicationSummary s = (ApplicationSummary) element;
  
                switch (columnIndex) {
                case ID_COL:
                    return s.getId();
                case NAME_COL:
                    return s.getDisplayName();
                case LANGUAGE_COL:
                    return s.getLanguage().toString();
                case OWNER_COL:
                    return s.getOwnerUserName();
                case CREATED_COL:
                    return s.getTimeCreated().toString();
                case UPDATED_COL:
                    return s.getTimeUpdated().toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
       	
    	tree.setSortDirection(SWT.UP);
        TableColumn tc;        
        tc = createColumn(tableColumnLayout,tree, "Name", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                s=ListApplicationsRequest.SortBy.DisplayName;
                if(so==ListApplicationsRequest.SortOrder.Desc) so=ListApplicationsRequest.SortOrder.Asc;
                else so=ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "OCI ID", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Language", 6);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                s=ListApplicationsRequest.SortBy.Language;
                if(so==ListApplicationsRequest.SortOrder.Desc) so=ListApplicationsRequest.SortOrder.Asc;
                else so=ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Owner", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Created", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                s=ListApplicationsRequest.SortBy.TimeCreated;
                if(so==ListApplicationsRequest.SortOrder.Desc) so=ListApplicationsRequest.SortOrder.Asc;
                else so=ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Updated", 10);

    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshApplicationAction(ApplicationTable.this));
       // manager.add(new CreateApplicationAction(ApplicationTable.this,COMPARTMENT_ID));
        manager.add(new Separator());
        if (getSelectedObjects().size() == 1) {
            manager.add(new DetailsApplicationAction(ApplicationTable.this));
            manager.add(new EditApplicationAction(ApplicationTable.this));
            manager.add(new RunApplicationAction(ApplicationTable.this));
            manager.add(new DeleteApplicationAction(ApplicationTable.this));
        }        
    }

    @Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {
    	
    	
    	//14-June 
		ccb.setText("Change Compartment");ccb.setVisible(true);
        ccb.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				Consumer<Compartment> consumer=new Consumer<Compartment>() {

				@Override
				public void accept(Compartment comp) {
					COMPARTMENT_ID = comp.getId();
					COMPARTMENT_NAME = comp.getName();
				}
				};
				CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
						new CompartmentSelectWizard(consumer, false));
				dialog.setFinishButtonText("Select");
				if (Window.OK == dialog.open()) {
					setCompartmentName(new String(COMPARTMENT_NAME));
					refresh(true);
				}
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        Button createApplicationButton = toolkit.createButton(right,"Create Application", SWT.PUSH);
        createApplicationButton.setText("Create Application");
        createApplicationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new CreateApplicationWizard(COMPARTMENT_ID));
     	       	dialog.setFinishButtonText("Create");
   	        	if (Window.OK == dialog.open()) {
   	        	refresh(true);
   	        	}      	          	
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });	
        
    }
    
 
}
