package solution.MismatchSolution.xmlParser;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import org.dom4j.Element;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class XmlParser {
	private Environment myDbEnvironment;
	private ReplaceTable replaceTable;
	private InfoTable infoTable;
	private InvertedTable invertedTable;
	private int BITPERWORD = 32;
	private int SHIFT = 5;
	private int MASK = 0x1f;
	
	public XmlParser(String[] Q) {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setAllowCreate(true);
		    myDbEnvironment = new Environment(new File("data/dbEnv"), envConfig);
		} catch (Exception e) {
			System.err.println("ERROR: database environment can not be opened");
		}
		replaceTable = new ReplaceTable();
		replaceTable.buildReplaceTableDB(myDbEnvironment);
		infoTable = new InfoTable();
		infoTable.buildInfoTableDB(myDbEnvironment);
		invertedTable = new InvertedTable();
		invertedTable.buildInvertedTableDB(myDbEnvironment, Q);
	}
    
    public void process1(Element element, String s, int n, Vector<String> typeList){
    	int len = typeList.size();
    	int[] ft = new int[len];
    	Arrays.fill(ft, 0);
    	
    	int[] bitVec = new int[(len-1)/BITPERWORD + 1];
    	process1Rec(element, s, n, typeList, ft, bitVec, bitVec, bitVec);
    	
    	String root = "0";
    	String type = replaceTable.getIndex(root).getType();
        String xml = replaceTable.getIndex(root).getXml();
        Arrays.fill(bitVec, -1);
    	replaceTable.setIndex(root, type, xml, bitVec);
    	
    	infoTable.setIndex("typeList", typeList.toString());
    	infoTable.setIndex("Ft", Arrays.toString(ft));
    }
    
    private void process1Rec(Element element, String s, int n, Vector<String> typeList, int[] ft, int[] faBitVec, int[] grBitVec, int[] ggrBitVec){
    	Iterator<Element> iterator = element.elementIterator();
        String deweyID = s + (new Integer(n)).toString();
        String type = element.getPath();
        String xml = element.asXML();
        int m = 0, len = typeList.size(), index1 = typeList.indexOf(type);
        
        if(index1 == -1) {
        	System.out.println("type: " + type);
        	return;
        }
        
        ft[index1]++;
        
        int intLen = (len-1)/BITPERWORD + 1;
        int[] bitVec = new int[intLen];
        for(int i = 0; i < intLen; i++) {
        	bitVec[i] = 0;
        }
        set(bitVec, index1);
        set(faBitVec, index1);
        set(grBitVec, index1);
        set(ggrBitVec, index1);
        
        while (iterator.hasNext()) { 
        	Element child = iterator.next();
        	int index2 = typeList.indexOf(element.getPath());
        	set(bitVec, index2);
            set(faBitVec, index2);
            set(grBitVec, index2);
            set(ggrBitVec, index2);
        	process1Rec(child, s + (new Integer(n)).toString() + ".", m, typeList, ft, bitVec, faBitVec, grBitVec);
            m++;
        }
        replaceTable.setIndex(deweyID, type, xml, bitVec);
    }
    
    public void process2(Element element, String s, int n){
    	Iterator<Element> iterator = element.elementIterator();
        String type = element.getPath();
        String deweyID = s + (new Integer(n)).toString(); 
        String subtree = replaceTable.getIndex(deweyID).getXml();
        int m = 0;
        while (iterator.hasNext()) {
        	Element child = iterator.next();
            process2(child, s + (new Integer(n)).toString() + ".", m);
            m++;
        }
        invertedTable.setIndex(type, deweyID, subtree);
    }
    
    //位向量操作，将第i位设为1
    private void set(int[] bitVec, int i) {
    	bitVec[i >> SHIFT] |= (1 << (i & MASK));
    }
    
    /*位向量操作，将第i位设为0
    private void clr(int[] bitVec, int i) {
    	bitVec[i >> SHIFT] &= ~(1 << (i & MASK));
    }
    */
    
    public void close() {
    	replaceTable.closeReplaceTableDB();
    	infoTable.closeInfoTableDB();
    	invertedTable.closeInvertedTableDB();
		try {
		    if (myDbEnvironment != null) {
		    	myDbEnvironment.cleanLog();
		    	myDbEnvironment.close();
	        }
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: database environment can not be closed");
		}
	}
}