package solution.MismatchSolution.dtd2xml;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Structure 
{
    private String identifier;
    private List<Object> structures = null;
        
    public Structure(String identifier)
    {
        this.identifier = identifier;
        structures = new LinkedList<>();
    }
    
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    public List<Object> getObjects()
    {
        return Collections.unmodifiableList(structures);
    }
    
    public void addObject(Object object)
    {
        structures.add(object);
    }
    
    public String toXml() {
        String ret = new String();
        for(Object o:structures)
        {
            if(o.getClass().equals(Elem.class)) {                
                ret += ((Elem)o).toXml();
            } else {                
                ret += ((Structure)o).toXml();   
            }
        }
              
        return ret;
    }
}


