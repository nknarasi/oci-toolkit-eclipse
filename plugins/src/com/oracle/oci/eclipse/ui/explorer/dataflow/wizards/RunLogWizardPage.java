package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.oracle.bmc.dataflow.model.RunLogSummary;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class RunLogWizardPage extends WizardPage{
	private ISelection selection;
    private Tree tree;
    private Image IMAGE;
    private Composite container;
    private String runId;

    public RunLogWizardPage(ISelection selection,String runId) {
        super("wizardPage");
        setTitle("Select the log file you want to download");
        this.runId=runId;
    }

    @Override
    public void createControl(Composite parent) {
    	
        container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        
        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Job job = new Job("Get Projects") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            for (RunLogSummary log : DataflowClient.getInstance().getRunLogs(runId)) {
                                TreeItem treeItem = new TreeItem(tree, 0);
                                treeItem.setText(log.getName());
                                treeItem.setImage(IMAGE);
                                treeItem.setData("log", log);
                            }
                        } catch(Exception e) {
                        	MessageDialog.openError(getShell(),"Unable to get projects" , e.getMessage());
                        }
                    }
                });
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	 TreeItem[] items = tree.getSelection();
	       	     if(items !=null && items.length>0) {     	           
	       	    	 ((RunLogWizard)getWizard()).canFinish=true;
	       	    	 getWizard().canFinish();
	       	    	 getWizard().getContainer().updateButtons();
	       	     }
            }
        });	
        
        setControl(container);
    }
    
    public RunLogSummary getSelectedLog() {
        TreeItem[] items = tree.getSelection();
        if(items !=null && items.length==1) {
            TreeItem selectedItem = items[0];
            RunLogSummary log = (RunLogSummary)selectedItem.getData("log");
            return log;
        }
        return null;
    }
}
