package solution.MismatchSolution;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import solution.MismatchSolution.dtd2xml.Docu;
import solution.MismatchSolution.dtd2xml.Parser;
import solution.MismatchSolution.xmlParser.XmlFlat;
import solution.MismatchSolution.xmlParser.XmlParser;

public class PreParse {
	private String[] Q;
	private String xml;
	private String dtd;
	private String dtdxml;
	
	public PreParse(String[] Q, String xml, String dtd, String dtdxml) {
		this.Q = Q;
		this.xml = xml;
		this.dtd = dtd;
		this.dtdxml = dtdxml;
	}
	
	public void parse() {
        Vector<String> typeList = new Vector<String>();
        Docu docu = new Docu();
        SAXReader saxReader = new SAXReader();
        
        try {
        	Parser parser = new Parser(docu);
        	parser.parse(new File(dtd));            
        } catch( Exception e ) {
            System.err.println("ERROR: file can not be opened");
        }
        
        //将DTD转成XML
        try {
            PrintWriter output1 = new PrintWriter(dtdxml);
            output1.print(docu.toXml(dtd));
            output1.close();
        } catch( Exception e ) {
        	System.err.println("ERROR: can not change dtd to xml");
        }
        
        //解析dtd.xml
        try {
            Document document = saxReader.read(new File(dtdxml));
            XmlFlat xmlFlat = new XmlFlat();
            xmlFlat.flat(document.getRootElement());
            typeList = xmlFlat.typeList;
        } catch (DocumentException e) {
            System.err.println("ERROR: due to an IOException,the parser could not encode "+ dtdxml); 
		}
        
        //解析reed.xml
        try {
        	Document document = saxReader.read(new File(xml));
        	Element rootElement = document.getRootElement();
	        XmlParser xmlParser = new XmlParser(Q);
	        xmlParser.process1(rootElement, "", 0, typeList);
	        xmlParser.process2(rootElement, "", 0);
	        xmlParser.close();
	    } catch (DocumentException e) { 
	        System.err.println("ERROR: due to an IOException,the parser could not encode "+ xml); 
	    }
	}
}
