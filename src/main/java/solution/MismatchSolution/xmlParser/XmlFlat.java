package solution.MismatchSolution.xmlParser;

import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;

public class XmlFlat {
	public Vector<String> typeList = new Vector<String>();
	
	public void flat(Element element) {
		Iterator<Element> iterator = element.elementIterator();
		typeList.addElement(element.getPath());
        while (iterator.hasNext()) { 
        	Element child = (Element) iterator.next();
        	flat(child);
        }
    }
}
