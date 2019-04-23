package solution.MismatchSolution.xmlParser;

import java.io.Serializable;

public class ReplaceTableNode implements Serializable {
	private static final long serialVersionUID = 8331189051473234391L;
	
	private String type;
    private String xml;
    private int[] bitVec;
    
    public ReplaceTableNode(String type, String xml, int[] bitVec){
    	this.type = type;
    	this.xml = xml;
    	this.bitVec = bitVec;
    }
    
    public void setType(String type) {
    	this.type = type;
    }
    
    public void setXml(String xml) {
    	this.xml = xml;
    }
    
    public void setBitVec(int[] bitVec) {
    	this.bitVec = bitVec;
    }
    
    public String getType() {
        return type;
    }
    
    public String getXml() {
        return xml;
    }
    
    public int[] getBitVec() {
        return bitVec;
    }
}