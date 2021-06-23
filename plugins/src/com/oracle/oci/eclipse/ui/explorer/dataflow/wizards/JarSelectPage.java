package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class JarSelectPage extends WizardPage{
	
	    private ISelection selection;
	    private Tree tree,tree2;
	    private Image IMAGE;
	    Button doZip;
	    Label progress;
	    Composite pc,jarComp,container;
	    ScrolledComposite sc;
	    String jarUri,zipUri;
	    DataTransferObject dto;
	    private ProjectSelectWizardPage page;
	    boolean fileCreated = false;
	    Job job2;
	    

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
	        
	        job2 = new Job("Get External Jars") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {
	                // update tree node using UI thread
	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                        	tree2.removeAll();//tree2.layout(true,true);
	                            for (IClasspathEntry p : page.getSelectedProject().getRawClasspath()) {
	                                                try {
	                                                	String path=p.getPath().toString();
	                                                    if(!path.endsWith(".jar")) continue;
	                                                    TreeItem treeItem = new TreeItem(tree2, 0);
	                                                    treeItem.setText(path.substring(path.lastIndexOf(File.separator)+1));
	                                                    treeItem.setImage(IMAGE);
	                                                    treeItem.setData("path", path);
	                                                } catch(Exception e) {}
	                            }
	                            for(TreeItem e:tree2.getItems()) {
	                            	e.setChecked(true);
	                            }
	                        } catch(Exception e) {}
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        
	        pc=new Composite(container,SWT.NONE);GridLayout gl=new GridLayout();gl.numColumns=2;
	        pc.setLayout(gl);pc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        
	       Button showJars=new Button(pc,SWT.PUSH);showJars.setLayoutData(new GridData());
	        showJars.setText("Show/Refresh External Jar Files");
	        showJars.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	job2.schedule();
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	       Button sd=new Button(pc,SWT.PUSH);sd.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        sd.setText("Deselect All");
	        sd.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		if(sd.getText().equals("Select All")) {
	            			for(TreeItem te:tree2.getItems()) {
                            	te.setChecked(true);
                            }
	            			sd.setText("Deselect All");
	            		}
	            		else {
	            			for(TreeItem te:tree2.getItems()) {
                            	te.setChecked(false);
                            }
	            			sd.setText("Select All");
	            		}
						
					} catch (Exception e1) {
						setLabel("Failed: "+e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        tree2 = new Tree(container, SWT.CHECK | SWT.BORDER);
	        tree2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	        
	        Composite dComp=new Composite(container,SWT.NONE);GridLayout gl2=new GridLayout();gl2.numColumns=2;dComp.setLayout(gl2);dComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        Button b=new Button(dComp,SWT.PUSH);b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        Button b2=new Button(dComp,SWT.PUSH);b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
	        b.setText("Create Jar");b2.setText("Create External Jar Zip");b2.setEnabled(false);
	        b.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		setLabel("Creating .jar.......");
	            		IJavaProject p=page.getSelectedProject();if(p==null) throw new Exception("Improper Selection of Project");
	            		page.start(p);
	            		setLabel("Created .jar");
	            		b.setEnabled(false);
	            		b2.setEnabled(true);
	            		fileCreated = true;
	            		canFlipToNextPage();
	            		getWizard().getContainer().updateButtons();
						
					} catch (Exception e1) {
						setLabel("Failed: "+e1.getMessage());
					}
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        
	        b2.addSelectionListener(new SelectionListener() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            		Job job = new Job( "Import Data" ) {
	            			  @Override
	            			  protected IStatus run( IProgressMonitor monitor ) {
	            				  Display.getDefault().asyncExec(new Runnable() {
	            	                    @Override
	            	                    public void run() {
	            	                    	SubMonitor subMonitor = SubMonitor.convert(monitor);
	            	                    	subMonitor.beginTask("Creating Zip...", SubMonitor.UNKNOWN);
	            			   // monitor.beginTask( "Creating Zip...", IProgressMonitor.UNKNOWN );
	            			    try {
	            			    setLabel("Creating .zip.......");
	            			    IJavaProject p=page.getSelectedProject();if(p==null) throw new Exception("Improper Selection of Project");
	            			    setJars();
	            			    page.createJarZip(MakeJarAndZip.jarList);
	            			    b2.setEnabled(false);
	            			    setLabel("Created .zip");
	            			    fileCreated = true;}
	            			    catch (Exception e1) {
	        						setLabel("Failed: "+e1.getMessage());
	        					}
	            			    finally {
	            			      subMonitor.done();}
	            			    }
	            			    });
	            			    return Status.OK_STATUS;
	            			  }
	            			};
	            			job.schedule();
						
	            }

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	        });
	        progress=new Label(container,SWT.NONE);progress.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));progress.setText("Status Here");
	        setControl(container);
	    }
	    
	    public void setLabel(String t) {
	    	progress.setText(t);
	    	pc.layout(true,true);
	    }

		   public List<String> setJars() throws Exception{
			   MakeJarAndZip.jarList=new ArrayList<String>();
			   for(TreeItem e:tree2.getItems()) {
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
