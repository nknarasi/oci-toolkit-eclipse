package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.CreateFiles;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class JarSelectPage extends WizardPage{
	
	    private ISelection selection;
	    private Tree tree;
	    private Image IMAGE;
	    private Composite pc,container;
	    private ScrolledComposite sc;
	    private DataTransferObject dto;
	    private ProjectSelectWizardPage page;
	    boolean fileCreated = false;
	    protected Job job;

	    public JarSelectPage(ISelection selection,ProjectSelectWizardPage page,DataTransferObject dto) {
	        super("wizardPage");
	        setTitle("Add Dependencies to Application");
	        setDescription("Choose the the external dependencies you want to include in the application.");
	        IMAGE = Activator.getImage(Icons.COMPARTMENT.getPath());
	        this.page=page;
	        this.dto= dto;
	    }

	    @Override
	    public void createControl(Composite parent) {
	    	
	        container = new Composite(parent, SWT.NULL);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
	        
	        job = new Job("Get External Jars") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {

	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	tree.removeAll();
	                            for (IClasspathEntry p : page.getSelectedProject().getRawClasspath()) {
	                                                try {
	                                                	String path=p.getPath().toString();
	                                                    if(!path.endsWith(".jar")) continue;
	                                                    TreeItem treeItem = new TreeItem(tree, 0);
	                                                    treeItem.setText(path.substring(path.lastIndexOf(File.separator)+1));
	                                                    treeItem.setImage(IMAGE);
	                                                    treeItem.setData("path", path);
	                                                } 
	                                                catch(Exception e) {
	                                                	MessageDialog.openError(getShell(), "Error", "Failed to load external jar files");
	                                                }
	                            }
	                            for(TreeItem e:tree.getItems()) {
	                            	e.setChecked(true);
	                            }
	                        } catch(Exception e) {
	                        	MessageDialog.openError(getShell(), "Error creating Jar tree items", e.getMessage());
	                        }
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        
	        pc=new Composite(container,SWT.NONE);
	        GridLayout gl=new GridLayout();
	        gl.numColumns=2;
	        pc.setLayout(gl);
	        pc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	        Button selDesel=new Button(pc,SWT.PUSH);
	        selDesel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        selDesel.setText("Deselect All");
	        selDesel.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		if(selDesel.getText().equals("Select All")) {
	            			for(TreeItem te:tree.getItems()) {
                            	te.setChecked(true);
                            }
	            			selDesel.setText("Deselect All");
	            		}
	            		else {
	            			for(TreeItem te:tree.getItems()) {
                            	te.setChecked(false);
                            }
	            			selDesel.setText("Select All");
	            		}
						
					} catch (Exception e1) {
						MessageDialog.openError(getShell(), "Error perfroming select/deselect operation", e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        tree = new Tree(container, SWT.CHECK | SWT.BORDER);
	        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	        
	        Composite dComp=new Composite(container,SWT.NONE);
	        GridLayout gl2=new GridLayout();
	        gl2.numColumns=2;
	        dComp.setLayout(gl2);
	        dComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        
	        Button createJar=new Button(dComp,SWT.PUSH);
	        createJar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        
	        Button createArchive=new Button(dComp,SWT.PUSH);
	        createJar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        createJar.setText("Create Application Jar File");
	        createArchive.setText("Create Archive Zip File");
	        createArchive.setEnabled(false);
	        
	        createJar.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		IJavaProject p=page.getSelectedProject();
	            		if(p==null) 
	            			throw new Exception("Improper Selection of Project");
	            		page.start(p);
	            		createJar.setEnabled(false);
	            		createArchive.setEnabled(true);
	            		fileCreated = true;
	            		canFlipToNextPage();
	            		getWizard().getContainer().updateButtons();
						
					} catch (Exception e1) {
						MessageDialog.openError(getShell(), "Error creating Jar", e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        createArchive.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		setJars();
	            		IRunnableWithProgress op = new CreateFiles(MakeJarAndZip.jarList.size()+1);
	                    new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
						
					} 
	            	catch (Exception e1) {
	            		MessageDialog.openError(getShell(), "Error", e1.getMessage());
	            	}
	            	createArchive.setEnabled(false);
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        setControl(container);
	    }

		public List<String> setJars() throws Exception{
			MakeJarAndZip.jarList=new ArrayList<String>();
			for(TreeItem e:tree.getItems()) {
				if(e.getChecked()) MakeJarAndZip.jarList.add(e.getData("path").toString());
			}
			return MakeJarAndZip.jarList;
		}
		 
		@Override
		public boolean canFlipToNextPage() {
			return fileCreated;
		}
		   
		@Override
		public IWizardPage getNextPage() { 		 				   			   
			dto.setFiledir(MakeJarAndZip.jarUri);
			dto.setArchivedir(MakeJarAndZip.zipUri);
			LocalFileSelectWizardPage1 firstpage = ((LocalFileSelectWizard)getWizard()).firstbpage;
			firstpage.onEnterPage();
			LocalFileSelectWizardPage2 secondpage = ((LocalFileSelectWizard)getWizard()).secondbpage;
			secondpage.onEnterPage();
			return firstpage;       
		}
}
