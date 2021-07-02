package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeleteApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetApplications;
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
	private String pageToShow=null;
    private ListApplicationsRequest.SortBy sortBy=ListApplicationsRequest.SortBy.TimeCreated;
	private ListApplicationsRequest.SortOrder sortOrder=ListApplicationsRequest.SortOrder.Desc;
	private ListApplicationsResponse listApplicationsResponse;
	private Button previouspage,nextpage;
	
    public ApplicationTable(Composite parent, int style) {
        super(parent, style);
        COMPARTMENT_ID = AuthProvider.getInstance().getCompartmentId();
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        sortBy=ListApplicationsRequest.SortBy.TimeCreated;
        sortOrder=ListApplicationsRequest.SortOrder.Desc;
    }
    
    List<ApplicationSummary> applicationList = new ArrayList<ApplicationSummary>();
    
    @Override
    public List<ApplicationSummary> getTableData() {  
    	if(COMPARTMENT_ID != AuthProvider.getInstance().getCompartmentId()) {
    		COMPARTMENT_ID = AuthProvider.getInstance().getCompartmentId();
    	}
    	if(COMPARTMENT_ID== null) {
    		COMPARTMENT_ID = IdentClient.getInstance().getRootCompartment().getCompartmentId();
    	}
        try {
        	IRunnableWithProgress op = new GetApplications(COMPARTMENT_ID,sortBy,sortOrder,pageToShow);
        	new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            listApplicationsResponse = ((GetApplications)op).listApplicationsResponse;
            applicationList=((GetApplications)op).applicationSummaryList;
            tableDataSize = applicationList.size();
       } catch (Exception e) {
           MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get applications: ",e.getMessage());               
        }
        refresh(false);            
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
            	MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to set Application table details: ",ex.getMessage());
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
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.DisplayName;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc)
                	sortOrder=ListApplicationsRequest.SortOrder.Asc;
                else
                	sortOrder=ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "OCI ID", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Language", 6);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.Language;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc) 
                	sortOrder = ListApplicationsRequest.SortOrder.Asc;
                else 
                	sortOrder = ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Owner", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Created", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.TimeCreated;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc) 
                	sortOrder = ListApplicationsRequest.SortOrder.Asc;
                else 
                	sortOrder = ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Updated", 10);

    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshApplicationAction(ApplicationTable.this));
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
  
    	changeCompartmentButton.setText("Change Compartment");
    	changeCompartmentButton.setVisible(true);
    	changeCompartmentButton.addSelectionListener(new SelectionListener() {
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
        
        Composite page=new Composite(right.getParent(),SWT.NONE);
        GridLayout gl=new GridLayout();
        gl.numColumns=2;
        page.setLayout(gl);
        GridData gdpage = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
        gdpage.horizontalSpan = 2;
        page.setLayoutData(gdpage);
        
        previouspage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
        nextpage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        previouspage.setText("<");
        nextpage.setText(">");
        previouspage.setLayoutData(new GridData());
        nextpage.setLayoutData(new GridData());
        
        nextpage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	pageToShow = listApplicationsResponse.getOpcNextPage();
				refresh(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previouspage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {                
            	pageToShow = listApplicationsResponse.getOpcPrevPage();
				refresh(true);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });        
    } 
}
