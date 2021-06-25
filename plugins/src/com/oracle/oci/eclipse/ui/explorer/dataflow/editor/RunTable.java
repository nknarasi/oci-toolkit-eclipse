package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.ErrorHandler;
//import com.oracle.oci.eclipse.sdkclients.BlockStorageClient;
import com.oracle.oci.eclipse.sdkclients.RunClient;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeleteRunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsRunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetRuns;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RefreshRunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RunAction;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class RunTable extends BaseTable {
    private int tableDataSize = 0;
    private static final int NAME_COL = 0;
    private static final int LANGUAGE_COL = 1;
    private static final int STATE_COL = 2;
    private static final int OWNER_COL = 3;
	private static final int CREATED_COL = 4;
	private static final int DURATION_COL = 5;
	private static final int TOTAL_OCPU_COL = 6;
	private static final int DATA_READ_COL = 8;
	private static final int DATA_WRITTEN_COL = 7;
	private static String compid;
	private static String compname;
	public HashMap<String,String> actionMap = createActionMap();
	ListRunsRequest.SortBy s=ListRunsRequest.SortBy.TimeCreated;
	ListRunsRequest.SortOrder so=ListRunsRequest.SortOrder.Desc;
	String pagetoshow=null;
	ListRunsResponse listrunsresponse;
	Button pp,np;
	
    public RunTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        s=ListRunsRequest.SortBy.TimeCreated;
        so=ListRunsRequest.SortOrder.Desc;
    }
    List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
    @Override
    public List<RunSummary> getTableData() {
                try {
                	IRunnableWithProgress op = new GetRuns(compid,s,so,pagetoshow);
                    new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
                    listrunsresponse=((GetRuns)op).listrunsresponse;
                    runSummaryList=((GetRuns)op).runSummaryList;
                    tableDataSize = runSummaryList.size();
                } catch (Exception e) {
                }
                refresh(false);
        return runSummaryList;
    }
    @Override
    public List<RunSummary> getTableCachedData() {
        return runSummaryList;
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
                RunSummary s = (RunSummary) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case LANGUAGE_COL:
                    return s.getLanguage().toString();
                case STATE_COL:
                    return s.getLifecycleState().toString();
                case OWNER_COL:
                    return s.getOwnerUserName();
				case CREATED_COL:
					return (new SimpleDateFormat("dd-M-yyyy hh:mm:ss")).format(s.getTimeCreated());
				case DURATION_COL:
                    return Long.toString(s.getRunDurationInMilliseconds()/1000);
				case TOTAL_OCPU_COL:
                    return s.getTotalOCpu().toString();
				case DATA_WRITTEN_COL:
                    return s.getDataWrittenInBytes().toString();
				case DATA_READ_COL:
                    return s.getDataReadInBytes().toString();
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
        tc=createColumn(tableColumnLayout,tree, "Name", 15);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.DisplayName;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "Language", 6);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.Language;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "State", 8);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.LifecycleState;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        createColumn(tableColumnLayout,tree, "Owner", 15);
        tc=createColumn(tableColumnLayout,tree, "Created", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.TimeCreated;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "Duration", 5);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.RunDurationInMilliseconds;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "Total OCPU", 5);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.TotalOCpu;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "Data Written", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.DataWrittenInBytes;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        tc=createColumn(tableColumnLayout,tree, "Data Read", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                s=ListRunsRequest.SortBy.DataReadInBytes;
                if(so==ListRunsRequest.SortOrder.Desc) so=ListRunsRequest.SortOrder.Asc;
                else so=ListRunsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        manager.add(new RefreshRunAction(RunTable.this));
        manager.add(new Separator());

        /*if (getSelectedObjects().size() > 0) {
            for (String action: actionMap.keySet()) {
                manager.add(new InstanceAction(InstanceTable.this, action));
            }
        }*/
        if (getSelectedObjects().size() == 1) {
            manager.add(new Separator());
            manager.add(new DetailsRunAction(RunTable.this));
			manager.add(new RunAction((RunSummary)getSelectedObjects().get(0),RunTable.this));
			String lcs=((RunSummary)getSelectedObjects().get(0)).getLifecycleState().toString();
			if(lcs.equals("Failed")||lcs.equals("Succeeded")||lcs.equals("Canceling")||lcs.equals("Canceled"));
			else manager.add(new DeleteRunAction((RunSummary)getSelectedObjects().get(0),RunTable.this));
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
        Composite page=new Composite(right,SWT.NONE);GridLayout gl=new GridLayout();gl.numColumns=2;
        page.setLayout(gl);
        pp=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);np=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        pp.setText("<");np.setText(">");
        pp.setLayoutData(new GridData());np.setLayoutData(new GridData());
        pp.setEnabled(false);
        //if(tableDataSize<20) np.setEnabled(false);
        
        np.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listrunsresponse.getOpcNextPage();
				refresh(true);
				pp.setEnabled(true);
				//if(pagetoshow.equals(listrunsresponse.getOpcNextPage())) np.setEnabled(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        pp.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listrunsresponse.getOpcPrevPage();
				refresh(true);
				//if(pagetoshow.equals(listrunsresponse.getOpcPrevPage())) pp.setEnabled(false);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
    }

	
	protected HashMap<String,String> createActionMap() {
        actionMap = new HashMap<String,String>();
        actionMap.put("View Details","VIEW_DETAILS");
        actionMap.put("Re-run","RE_RUN");
        actionMap.put("Spark UI","SPARK_UI");
        return actionMap;
    }
}