package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.text.SimpleDateFormat;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.ui.account.CompartmentSelectWizard;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.CreatePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeletePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetPrivateEndpoints;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RefreshPrivateEndpointAction;

public class PrivateEndpointTable extends BaseTable {
    private int tableDataSize = 0;
    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
	private static final int CREATED_COL = 2;
	public static String compid;
	private static String compname;
	private List<PrivateEndpointSummary> pepSummaryList = new ArrayList<PrivateEndpointSummary>();
	private String pagetoshow=null;
	private ListPrivateEndpointsResponse listpepsresponse;
	private Button previousPage,nextPage;

    public PrivateEndpointTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        compid=AuthProvider.getInstance().getCompartmentId();
    }
    
    @Override
    public List<PrivateEndpointSummary> getTableData() {
    	
    	 try {
         	IRunnableWithProgress op = new GetPrivateEndpoints(compid,pagetoshow);
             new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
             listpepsresponse=((GetPrivateEndpoints)op).listpepsresponse;
             pepSummaryList=((GetPrivateEndpoints)op).pepSummaryList;
             tableDataSize = pepSummaryList.size();
         } catch (Exception e) {
         	MessageDialog.openError(getShell(), "Unable to get Private Endpoints list", e.getMessage());
         }
         refresh(false);
        
        return pepSummaryList;
    }
    
    @Override
    public List<PrivateEndpointSummary> getTableCachedData() {
        return pepSummaryList;
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
            } 
            catch (Exception ex) {
            	MessageDialog.openError(getShell(), "Error forming table", ex.getMessage());
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

        if (getSelectedObjects().size() == 1) {
           String pepState=((PrivateEndpointSummary)getSelectedObjects().get(0)).getLifecycleState().toString();
           manager.add(new Separator());
           if(!pepState.equals("Creating")) 
        	   manager.add(new DetailsPrivateEndpointAction(PrivateEndpointTable.this));
           if(!pepState.equals("Creating")&&!pepState.equals("Deleting")) 
        	   manager.add(new DeletePrivateEndpointAction(PrivateEndpointTable.this,(PrivateEndpointSummary)getSelectedObjects().get(0)));
		   if(pepState.equals("Active")||pepState.equals("Inactive"))
			   manager.add(new EditPrivateEndpointAction((PrivateEndpointSummary)getSelectedObjects().get(0),PrivateEndpointTable.this));
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
		
		Composite page=new Composite(right,SWT.NONE);
        GridLayout gl=new GridLayout();
        gl.numColumns=2;
        page.setLayout(gl);
        previousPage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
        nextPage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        previousPage.setText("<");
        nextPage.setText(">");
        previousPage.setLayoutData(new GridData());
        nextPage.setLayoutData(new GridData());
        previousPage.setEnabled(false);
        
        nextPage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listpepsresponse.getOpcNextPage();
				refresh(true);
				previousPage.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previousPage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listpepsresponse.getOpcPrevPage();
				refresh(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }
}