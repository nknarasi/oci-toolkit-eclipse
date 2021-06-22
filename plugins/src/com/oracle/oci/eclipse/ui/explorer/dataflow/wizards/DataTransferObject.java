package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.List;

import com.oracle.bmc.dataflow.model.ApplicationLanguage;

public class DataTransferObject {
    String data=null;
    ApplicationLanguage language;
    List<String> arguments;
    boolean local =false;
    String fileUri = null;
    String applicationId = null;
    String filedir= null;
    String archivedir = null;
    
    
    
    public String getFiledir() {
		return filedir;
	}

	public void setFiledir(String filedir) {
		this.filedir = filedir;
	}

	public String getArchivedir() {
		return archivedir;
	}

	public void setArchivedir(String archivedir) {
		this.archivedir = archivedir;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}


	public String getFileUri() {
		return fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

	public boolean isLocal() {
		return local;
	}
	
	public void setLocal(boolean local) {
		this.local = local;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public ApplicationLanguage getLanguage() {
		return language;
	}

	public void setLanguage(ApplicationLanguage language) {
		this.language = language;
	}

	public String getData(){
        return this.data;
    }

    public void setData(String data){
        this.data=data;
    }
}
