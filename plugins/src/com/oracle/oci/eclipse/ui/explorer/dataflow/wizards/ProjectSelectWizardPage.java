package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;
import com.oracle.oci.eclipse.ui.explorer.objectstorage.actions.MakeJarAndZip;

public class ProjectSelectWizardPage extends WizardPage{
	
	    private ISelection selection;
	    private Tree tree;
	    private Image IMAGE;
	    Button doZip;
	    Label progress;
	    Composite pc,jarComp,container;
	    ScrolledComposite sc;
	    String jarUri,zipUri;
	    boolean projectSelected = false;
	    File theDir;

	    public ProjectSelectWizardPage(ISelection selection) {
	        super("wizardPage");
	        setTitle("Select Project");
	        setDescription("Choose the Project");
	        IMAGE = Activator.getImage(Icons.COMPARTMENT.getPath());
	    }

	    @Override
	    public void createControl(Composite parent) {
	    	
	        container = new Composite(parent, SWT.NULL);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
	        
	        tree = new Tree(container, SWT.RADIO | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	        Job job = new Job("Get Projects") {
	            @Override
	            protected IStatus run(IProgressMonitor monitor) {

	                Display.getDefault().asyncExec(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {

	                            for (IJavaProject p : getProjects()) {
	                                                try {
	                                                    TreeItem treeItem = new TreeItem(tree, 0);
	                                                    treeItem.setText(p.getElementName());
	                                                    treeItem.setImage(IMAGE);
	                                                    treeItem.setData("project", p);
	                                                } catch(Exception e) {}
	                            }
	                        } catch(Exception e) {}
	                    }
	                });
	                return Status.OK_STATUS;
	            }
	        };
	        job.schedule();
	        
	        tree.addSelectionListener(new SelectionAdapter() {
	            @Override
	            public void widgetSelected(SelectionEvent e) {
	            	 TreeItem[] items = tree.getSelection();
		       	        if(items !=null && items.length>0) {
		       	            TreeItem selectedItem = items[0];		       	           
		       	            projectSelected = true;
		       	            canFlipToNextPage();
		       	            getWizard().getContainer().updateButtons();
		       	        }
	            }
	        });	
	        
	        setControl(container);
	    }
	    
	    public IJavaProject getSelectedProject() {
	        TreeItem[] items = tree.getSelection();
	        if(items !=null && items.length==1) {
	            TreeItem selectedItem = items[0];
	            IJavaProject p = (IJavaProject)selectedItem.getData("project");
	            return p;
	        }

	        return null;
	    }

	    public void setLabel(String t) {
	    	progress.setText(t);
	    	pc.layout(true,true);
	    }
	    
	    public void start(IJavaProject proj) throws Exception {		
	    		String projectUri=proj.getProject().getLocation().toString();
	        	String tl=System.getProperty("java.io.tmpdir");
	        	theDir = new File(tl+"\\dataflowtempdir");
	        	if (!theDir.exists()){
	        		theDir.mkdirs();
	        	}
	        	String dff=File.createTempFile("dataflowtempdir\\dfspark-",".jar",theDir).getAbsolutePath();
	        	Manifest manifest = new Manifest();
	        	manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
	        	      
	        	JarOutputStream target = new JarOutputStream(new FileOutputStream(dff), manifest);
	        	File inputDirectory = new File(projectUri+File.separator+"bin");
	        	      
	        	for (File nestedFile : inputDirectory.listFiles())
	        	    add("", nestedFile, target);
	        	target.close();
	        	MakeJarAndZip.jarUri=dff;
		}

		   private void add(String parents, File source, JarOutputStream target) throws IOException {
		      BufferedInputStream in = null;
		      try
		      {
		         String name = (parents + source.getName()).replace("\\", "/");

		         if (source.isDirectory())
		         {
		            if (!name.isEmpty())
		            {
		               if (!name.endsWith("/"))
		                  name += "/";
		               JarEntry entry = new JarEntry(name);
		               entry.setTime(source.lastModified());
		               target.putNextEntry(entry);
		               target.closeEntry();
		            }
		            for (File nestedFile : source.listFiles())
		               add(name, nestedFile, target);
		            return;
		         }

		         JarEntry entry = new JarEntry(name);
		         entry.setTime(source.lastModified());
		         target.putNextEntry(entry);
		         in = new BufferedInputStream(new FileInputStream(source));

		         byte[] buffer = new byte[1024];
		         while (true)
		         {
		            int count = in.read(buffer);
		            if (count == -1)
		               break;
		            target.write(buffer, 0, count);
		         }
		         target.closeEntry();
		      }
		      catch (Exception e) {
		    	  MessageDialog.openError(getShell(), "Error", e.getMessage());
		      }
		      finally
		      {
		         if (in != null)
		            in.close();
		      }
		   }
		   
		   public String createJarZip(List<String> classPathEntries) throws Exception {
			   
			   if(MakeJarAndZip.jarUri==null) throw new Exception("Create .jar file first");
			   byte[] buffer = new byte[1024];
			   File dff=File.createTempFile("dataflowtempdir\\dflib-",".zip",theDir);
			   ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dff));
			   
			   
			   StringBuffer mcp=new StringBuffer("");
			   FileInputStream in=null;
			   for (String classpathEntry : new HashSet<String>(classPathEntries)) {
			        if (classpathEntry.endsWith(".jar")) {
			            File jar = new File(classpathEntry);
			            String p=jar.getAbsolutePath();
			            String n=p.substring(p.lastIndexOf('\\')+1),n0=p.substring(0,p.lastIndexOf('\\'));
			            mcp.append("java" + "/" + n+" ");
			            ZipEntry e = new ZipEntry("java" + "/" + n);
			            out.putNextEntry(e);
			            try {
		                    in = new FileInputStream(n0 + "/" + n);
		                    int len;
		                    while ((len = in.read(buffer)) > 0) {
		                        out.write(buffer, 0, len);
		                    }
		                }
			            catch (Exception e1) {
			            	MessageDialog.openError(getShell(), "Error", e1.getMessage());
			            }
			            finally {
		                	if(in!=null)
		                    in.close();
		                }
			            out.closeEntry();
			        }
			    }
			   out.close();
			   MakeJarAndZip.zipUri=dff.getAbsolutePath();
			   return mcp.toString();
			}
		   
		   public List<IJavaProject> getProjects() {
			      List<IJavaProject> projectList = new LinkedList<IJavaProject>();
			      try {
			         IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			         IProject[] projects = workspaceRoot.getProjects();
			         for(int i = 0; i < projects.length; i++) {
			            IProject project = projects[i];
			            if(project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
			               projectList.add(JavaCore.create(project));
			            }
			         }
			      }
			      catch(CoreException ce) {
			    	  MessageDialog.openError(getShell(), "Error", ce.getMessage());
			      }
			      return projectList;
			   }
		   
			@Override
			public boolean canFlipToNextPage() {
				return projectSelected;
			}
}
