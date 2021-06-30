// types: name,loguri,warehouseuri,sparkprop2,sparkprop3,subnetid,dnszones,nsgl


package com.oracle.oci.eclipse.ui.explorer.dataflow.actions;

import java.util.List;
import java.util.Set;

import com.oracle.oci.eclipse.ui.explorer.dataflow.DataflowConstants;

public class Validations {
	
	public static String check(Object[] obj,String[] type){
		
		StringBuffer message=new StringBuffer("");
		
		for(int i=0;i<obj.length;i++) {
			switch (type[i]) {
			
			case "name":
				String name=(String)obj[i];
				if(!checkName(name)) {
					message.append("Name cannot be empty.\n");
				}
				break;
				
			case "fileuri":
				String fileuri=(String)obj[i];
				if(!checkName(fileuri)) {
					message.append("FileUri cannot be empty.\n");
				}
				break;
				
			case "mainclassname":
				String mainclassname=(String)obj[i];
				if(!checkName(mainclassname)) {
					message.append("Main Class Name cannot be empty.\n");
				}
				break;
				
			case "loguri":
				String logUri=(String)obj[i];
				if(!checkLogUri(logUri)) {
					message.append("Log Uri format invalid.\n");
				}
				break;
				
			case "archiveuri":
				String archiveUri=(String)obj[i];
				if(!checkArchiveUri(archiveUri)) {
					message.append("Archive Uri format invalid.\n");
				}
				break;
				
			case "warehouseuri":
				String warehouseUri=(String)obj[i];
				if(!checkWarehouseUri(warehouseUri)) {
					message.append("Warehouse Uri format invalid.\n");
				}
				break;
			
			case "sparkprop2":
				Set<String> key2List=(Set<String>)obj[i];
				if(!checkSparkProperties(2,key2List)) {
					message.append("Spark2 property invalid.\n");
				}
				break;
			
			case "sparkprop3":
				Set<String> key3List=(Set<String>)obj[i];
				if(!checkSparkProperties(3,key3List)) {
					message.append("Spark3 property invalid.\n");
				}
				break;
			
			case "subnetid":
				String id=(String)obj[i];
				if(!checkName(id)) {
					message.append("Subnet Id invalid.\n");
				}
				break;
			
			case "dnszones":
				String[] dns=(String[])obj[i];
				if(!checkDnsZones(dns)) {
					message.append("Dns zones invalid.\n");
				}
				break;
			
			case "nsgl":
				List<String> nsgl=(List<String>)obj[i];
				if(!checkNsgls(nsgl)) {
					message.append("Network Security Groups invalid.\n");
				}
				break;
				
			case "description":
				String description = (String)obj[i];
				if(!checkdescription(description)) {
					message.append("Description Size exceeded. Allowed Size = 255 characters.\n");
				}
				break;
			}
		}
		return message.toString();
	}
	
	private static boolean checkName(String name) {
		return (name!=null&&!name.isEmpty());
	}
	
	private static boolean checkLogUri(String uri) {
		return (uri.length()>9)&&
				(uri.substring(0,6).equals("oci://"))&&
				(uri.endsWith("/"))&&
				(uri.split("@").length==2);
	}
	
	private static boolean checkArchiveUri(String uri) {
		return (uri.length()>9)&&
				(uri.substring(0,6).equals("oci://"))&&
				(uri.split("@").length==2);
	}
	
	private static boolean checkWarehouseUri(String uri) {
		return (uri.length()>9)&&
				(uri.substring(0,6).equals("oci://"))&&
				(!uri.endsWith("/"))&&
				(uri.split("@").length==2);
	}
	
	private static boolean checkSparkProperties(int version,Set<String> list) {
    	 
		String[] l;
    	if(version==3) l=DataflowConstants.Spark3PropertiesList;
    	else l=DataflowConstants.Spark2PropertiesList;
    	boolean b;
    	for(String e:list) {
    		b=false;
    		for(String ie:l) {
    			String nie=new String(ie);
    			if(nie.charAt(nie.length()-1)=='*') nie=nie.substring(0, nie.length()-1);
    			if(e.indexOf(nie)==0) {
    				b=true;
    				break;
    			}
    		}
    		if(b) continue;
    		else return false;
    	}
    	
    	return true;
    }
	
	private static boolean checkDnsZones(String[] dns) {
		if(dns==null||dns.length==0) return false;
		for(String e:dns) {
			if(e.isEmpty()) return false;
		}
		return true;
	}
	
	private static boolean checkNsgls(List<String> nsgl) {
		return (nsgl!=null);
	}
	
	private static boolean checkdescription(String description) {
		return (description.length() <= 255);
	}
}