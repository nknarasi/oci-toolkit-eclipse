package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class RunasDataflowApplication extends AbstractHandler implements IElementUpdater  {
		
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if ( null == event || ! ( event.getTrigger() instanceof Event ) ) { return null;}

        Event eventWidget = (Event)event.getTrigger();
        // Makes sure event came from a ToolItem.
        if ( eventWidget.widget instanceof ToolItem )  {
        	
        	MakeJarAndZip.jarUri=null;MakeJarAndZip.zipUri=null;
                	
        	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
        			new LocalFileSelectWizard());
        	dialog.setFinishButtonText("Run");
        	if (Window.OK == dialog.open()) {
        	}
        }
        return null;
    }
	
	  @Override
	    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
	    	
	    }
	

}
