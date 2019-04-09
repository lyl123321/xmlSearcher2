package solution.MismatchSolution.xmlParser;

import java.io.Serializable;

public class ReplaceTableNode implements Serializable {
	private static final long serialVersionUID = 8331189051473234391L;
	
	private String type;
    private String xml;
    private int[] exLabel;
    
    public ReplaceTableNode(String type, String xml, int[] exLabel){
    	this.type = type;
    	this.xml = xml;
    	this.exLabel = exLabel;
    }
    
    public void setType(String type) {
    	this.type = type;
    }
    
    public void setXml(String xml) {
    	this.xml = xml;
    }
    
    public void setExLabel(int[] exLabel) {
    	this.exLabel = exLabel;
    }
    
    public String getType() {
        return type;
    }
    
    public String getXml() {
        return xml;
    }
    
    public int[] getExLabel() {
        return exLabel;
    }
}