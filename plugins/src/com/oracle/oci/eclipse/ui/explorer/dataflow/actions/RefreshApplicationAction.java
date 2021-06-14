package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;

public class RefreshApplicationAction extends BaseAction {	
	 private final ApplicationTable objectTable;
	
	 public RefreshApplicationAction (ApplicationTable table){
	        objectTable = table;
	    }
	   
	 @Override
	 public String getText() {
	        return "Refresh List";
	    }
	
	 @Override
	 protected void runAction() {
	        objectTable.refresh(true);
	    }		
}
