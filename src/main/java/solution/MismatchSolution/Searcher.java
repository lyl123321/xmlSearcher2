package solution.MismatchSolution;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import solution.MismatchSolution.xmlParser.InvertedTable;
import solution.MismatchSolution.xmlParser.XmlParser;

public class Searcher {
	public static List<Map> search(String[] Q, String xml, int K) {
		//生成 invertedTable
		SAXReader saxReader = new SAXReader();
		try {
        	Document document = saxReader.read(new File(xml));
	        XmlParser xmlParser = new XmlParser(Q);
	        xmlParser.process2(document.getRootElement(), "", 0);
	        xmlParser.close();
	    } catch (DocumentException e) { 
	        System.err.println("ERROR: due to an IOException,the parser could not encode "+ xml); 
	    }
		
		//打开 invertedTable，获取查询结果
		Environment myDbEnvironment = null;
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setAllowCreate(false);
		    myDbEnvironment = new Environment(new File("data/dbEnv"), envConfig);
		} catch (Exception e) {
			System.err.println("ERROR: database environment can not be opened");
		}
		InvertedTable invertedTable = new InvertedTable();
		invertedTable.openInvertedTableDB(myDbEnvironment, Q);
		List<Map> results = invertedTable.getResults(K);
		
		//关闭 invertedTable
		invertedTable.closeInvertedTableDB();
		try {
		    if (myDbEnvironment != null) {
		    	myDbEnvironment.cleanLog();
		    	myDbEnvironment.close();
	        }
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: database environment can not be closed");
		}
		
		return results;
	}
}
