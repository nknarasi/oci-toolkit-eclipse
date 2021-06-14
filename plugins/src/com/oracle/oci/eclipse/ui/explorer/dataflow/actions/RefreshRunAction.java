package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.RunTable;

public class RefreshRunAction extends BaseAction {

    private final RunTable runTable;

    public RefreshRunAction (RunTable table){
        runTable = table;
    }

    @Override
    public String getText() {
        return "Refresh List";
    }

    @Override
    protected void runAction() {
        runTable.refresh(true);
    }
}