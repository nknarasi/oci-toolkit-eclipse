package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TestPage extends WizardPage {
    private ISelection selection;

    public TestPage(ISelection selection) {
        super("wizardPage");
        this.selection = selection;
    }

    @Override
    public void createControl(Composite parent) {
    	
    	Composite c=new Composite(parent,SWT.NONE);c.setLayout(new GridLayout());
    	Text t=new Text(c,SWT.BORDER);
    	Label l=new Label(c,SWT.BORDER);l.setText("invalid");
    	t.addModifyListener(new ModifyListener() {
    	    public void modifyText(ModifyEvent e) {
    	        String s=t.getText();
    	        if(s.length()<4) {l.setText("invalid");return;}
    	        if(s.substring(s.length()-4).equals(".jar")) {l.setText("valid");}
    	        else l.setText("invalid");
    	    }
    	});
        setControl(c);
    }
	
	 private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
}