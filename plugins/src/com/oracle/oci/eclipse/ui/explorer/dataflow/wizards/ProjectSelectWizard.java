package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

import org.eclipse.jdt.core.IJavaProject;


public class ProjectSelectWizard extends Wizard implements INewWizard{
	
	 private ProjectSelectWizardPage page;
	 private JarSelectPage page2;
	 private ISelection selection;
		
		public ProjectSelectWizard() {
			super();
			setNeedsProgressMonitor(true);
		}

	    @Override
	    public void addPages() {
	        page = new ProjectSelectWizardPage(selection);
	        page2= new JarSelectPage(selection,page);
	        addPage(page);
	        addPage(page2);
	    }

	    /**
	     * This method is called when 'Finish' button is pressed in
	     * the wizard. We will create an operation and run it
	     * using wizard as execution context.
	     */
	    @Override
	    public boolean performFinish() {
	    	if(MakeJarAndZip.jarUri==null) {
	    		MessageDialog.openInformation(getShell(), "Cannot Finish", "Either create files or select Cancel");
	    		return false;
	    	}
	    	return true;
	    }
	    
	    public boolean performCancel() {
	    	MakeJarAndZip.jarUri=null;
	    	MakeJarAndZip.zipUri=null;
	    	return true;
	    }
	    /**
	     * We will accept the selection in the workbench to see if
	     * we can initialize from it.
	     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	     */
	    @Override
	    public void init(IWorkbench workbench, IStructuredSelection selection) {
	        this.selection = selection;
	    }
}
