package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class RunLogWizard extends Wizard implements INewWizard{

	private IStructuredSelection selection;
	private RunLogWizardPage page;
	private String runId;
	protected boolean canFinish=false;
	
	public RunLogWizard(String runId) {
		super();
		this.runId=runId;
		setNeedsProgressMonitor(true);
	}
	
	@Override
    public void addPages() {	   
	   page=new RunLogWizardPage(selection,runId);
	   addPage(page);
    }
	
	@Override
	public boolean performFinish() {
		DataflowClient.getInstance().downloadRunLog(runId,page.getSelectedLog().getName());
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		
	}
	
	@Override
	public boolean canFinish() {
		return canFinish;
	}
}
