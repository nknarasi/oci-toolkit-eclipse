package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import com.oracle.oci.eclipse.ui.explorer.common.BaseAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.editor.PrivateEndpointTable;

public class RefreshPrivateEndpointAction extends BaseAction {

    private final PrivateEndpointTable privateEndpointTable;

    public RefreshPrivateEndpointAction (PrivateEndpointTable table){
        privateEndpointTable = table;
    }

    @Override
    public String getText() {
        return "Refresh List";
    }

    @Override
    protected void runAction() {
        privateEndpointTable.refresh(true);
    }
}