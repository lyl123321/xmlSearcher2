package solution.MismatchSolution.dtd2xml;

import java.util.ArrayList;
import java.util.List;


public class Docu 
{
    private String version;
    private String encoding;
    private Elem rootElement = null;
    private List<Elem> allElements = null;
    
    public Docu()
    {
        allElements = new ArrayList<>();
        version = "1.0";
        encoding = "UTF-8";
    }
    public void addElement(String parentName, Elem element)
    {
        if(parentName == null) {
            rootElement = element;            
        } else {
            findElement(parentName).addElement(element);            
        }
        
        if(parentName == null || !parentName.matches("^(sub|sup|i|tt|ref)$")) {
        	allElements.add(element);
        }
    }
    
    public void addObject(String parentName, Structure structure)
    {
        //findElement(parentName).addObject(structure);
    	for(Elem elem : allElements)
    	{
            if(elem.getName().equals(parentName))
            {
            	elem.addObject(structure);
            }
        }
    }
    
    public Elem findElement(String parentName)
    {
        for(Elem i:allElements)
        {
            if(i.getName().equals(parentName))
            {
                return i;
            }
        }
        return null;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public String getEncoding()
    {
        return encoding;
    }
    
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    public Elem getRootElement()
    {
        return rootElement;
    }
    
    public String toXml(String dtdUrl) {
    	String ret = new String();
    	String[] names = dtdUrl.split("[\\./]");
        ret += String.format( "<?xml version=\"%s\" encoding=\"%s\"?>\n", getVersion(), getEncoding() );  
        ret += "<!DOCTYPE "+ names[names.length - 2] + " SYSTEM \"" + dtdUrl + "\">\n";
        ret += rootElement.toXml();
        return ret;
    }
}
