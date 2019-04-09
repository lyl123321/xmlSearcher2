package solution.MismatchSolution.dtd2xml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Elem 
{   
    private String name = null;
    private List<Object> objects = null;
    private List<Elem> elems = null;
    
    public Elem(String name)
    {
        this.name = name;     
        objects = new LinkedList<>();
        elems = new ArrayList<>();
    } 
    
    public void addObject(Object object)
    {     
        objects.add(object);
    }
    
    public void addElement(Elem element)
    {
        elems.add(element);
    }
    
    public String getName()
    {
        return name;
    }
    
    public String toXml()
    {
        String ret = new String();
        ret += "<" + name + ">";
        
        if( !objects.isEmpty()) {
        	ret += "\n";
            for(Object o:objects) {
                if(o.getClass().equals(Elem.class)) {                 
                    ret += ((Elem)o).toXml();
                } else {
                    ret += ((Structure)o).toXml();
                }
            }
        }
        
        ret += "</" + name + ">\n";
        
        return ret;
    }
}
