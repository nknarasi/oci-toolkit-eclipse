package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.ApplicationTable;

public class RefreshApplicationAction extends BaseAction {	
	 private final ApplicationTable applicationTable;
	
	 public RefreshApplicationAction (ApplicationTable table){
		 applicationTable = table;
	 }
	   
	 @Override
	 public String getText() {
	        return "Refresh List";
	 }
	
	 @Override
	 protected void runAction() {
		 applicationTable.refresh(true);
	 }		
}
