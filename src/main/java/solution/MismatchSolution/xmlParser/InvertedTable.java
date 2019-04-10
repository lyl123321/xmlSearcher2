package solution.MismatchSolution.xmlParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class InvertedTable {
	private int myDBNumber;
	private String[] myDBName;
	private String[] keywords;
	private Database[] myDB;
    
    public InvertedTable() {
    	myDBNumber = 0;
		myDBName = null;
		keywords = null;
		myDB = null;
    }
    
	public void buildInvertedTableDB(Environment myDbEnvironment, String[] qList) {
		keywords = qList;
		myDBNumber = keywords.length;
		myDBName = new String[myDBNumber];
		for(int i = 0; i < myDBNumber; i++) {
			myDBName[i] = "invertedTableDB_" + keywords[i];
		}
		myDB = new Database[myDBNumber];
		DatabaseConfig dbConfig = new DatabaseConfig();
	    dbConfig.setAllowCreate(true);
	    dbConfig.setSortedDuplicates(true);
	    for (int i = 0; i < myDBNumber; i++) {
			try {
				myDB[i] = myDbEnvironment.openDatabase(null, myDBName[i], dbConfig);
			} catch (DatabaseException dbe) {
				System.err.println("ERROR: inverted table database can not be built");
			}
	    }
	}
	
	public void openInvertedTableDB(Environment myDbEnvironment, String[] qList) {
		keywords = qList;
		myDBNumber = keywords.length;
		myDBName = new String[myDBNumber];
		for(int i = 0; i < myDBNumber; i++) {
			myDBName[i] = "invertedTableDB_" + keywords[i];
		}
		myDB = new Database[myDBNumber];
		DatabaseConfig dbConfig = new DatabaseConfig();
	    dbConfig.setAllowCreate(false);
	    dbConfig.setSortedDuplicates(true);
		try {
			for (int i = 0; i < myDBNumber; i++) {
				myDB[i] = myDbEnvironment.openDatabase(null, myDBName[i], dbConfig);
			}
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: inverted table database can not be opened");
		}
	}
	
	public void setIndex(String type, String deweyID, String subtree) {
		for(int i = 0; i < myDBNumber; i++) {
			if(subtree.contains(keywords[i])) {
				try {
					DatabaseEntry theKey = new DatabaseEntry(type.getBytes("UTF-8"));
					DatabaseEntry theData = new DatabaseEntry(deweyID.getBytes("UTF-8"));
					myDB[i].put(null, theKey, theData);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
	//求 FtK， 首先取出K中每个关键字关于类型 t的 deweyID数组，然后找到长度最短的数组，遍历这个数组，依次判断其中每个deweyID是否在剩余的数组中也存在，count++,最后求得fkt
	public int getFtK(String type, String[] K) {
		ArrayList<ArrayList<String>> idArray = new ArrayList<ArrayList<String>>();
		List<String> names = Arrays.asList(myDBName);
		
		for(String keyword : K) {
			int index = names.indexOf("invertedTableDB_" + keyword);
			Database database = myDB[index];
			Cursor cursor = database.openCursor(null, null);
			ArrayList<String> deweyIDs = new ArrayList<String>();
			try {
				DatabaseEntry theKey = new DatabaseEntry(type.getBytes("UTF-8"));
				DatabaseEntry theData = new DatabaseEntry();
				OperationStatus retVal = cursor.getSearchKey(theKey, theData, LockMode.DEFAULT);
				while (retVal == OperationStatus.SUCCESS) {
			        String deweyID = new String(theData.getData(), "UTF-8");
			        deweyIDs.add(deweyID);
			        retVal = cursor.getNextDup(theKey, theData, LockMode.DEFAULT);
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				idArray.add(deweyIDs);
				cursor.close();
			}
		}
		
		ArrayList<String> refer = idArray.get(0);
        for(int i = 1, len = idArray.size(); i < len; i++) {
        	refer.retainAll(idArray.get(i));
		}
        
		return refer.size();
	}
	
	//获取查询结果
	public List<Map> getResults(int K) {
		ArrayList<ArrayList<String>> nodesArr = new ArrayList<ArrayList<String>>();
		ArrayList<Map> R = new ArrayList<Map>();
		
		for (int i = 0; i < myDBNumber; i++) {
			Database database = myDB[i];
			Cursor cursor = database.openCursor(null, null);
			ArrayList<String> nodes = new ArrayList<String>();
			try {
				DatabaseEntry theKey = new DatabaseEntry();
				DatabaseEntry theData = new DatabaseEntry();
				while (cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			        nodes.add(new String(theData.getData(), "UTF-8"));
					while (cursor.getNextDup(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				        nodes.add(new String(theData.getData(), "UTF-8"));
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				nodesArr.add(nodes);
				cursor.close();
			}
		}
		
		ArrayList<String> comAn = (ArrayList<String>) nodesArr.get(0).clone();
		for (int i = 1; i < myDBNumber; i++) {
        	comAn.retainAll(nodesArr.get(i));
		}
		
		int len = comAn.size();
		if(len > K) {
			len = K;
			for (int i = 0; i < K; i++) {
				if(comAn.get(i).contentEquals("0")) comAn.remove(i);
			}
		}
		
		for (int i = 0; i < len; i++) {
			String vlca = comAn.get(i);
			Map result = new HashMap();
			ArrayList<String> mNodes = new ArrayList<String>();
			for (ArrayList<String> nodes : nodesArr) {
				ArrayList<String> temp = new ArrayList<String>();
				for(String node : nodes) {
					if(node.indexOf(vlca + ".") == 0) {
						temp.add(node);
					}
				}
				String res = temp.get(0);
				int maxLen = res.split("\\.").length;
				for (int k = 1, size = temp.size(); k < size; k++) {
					String node = temp.get(k);
					int theLen = node.split("\\.").length;
					if(theLen > maxLen) {
						res = node;
						maxLen = theLen;
					}
				}
				mNodes.add(res);
			}
	        result.put("vlca", vlca);
	        result.put("nodes", mNodes.toArray(new String[0]));
	        R.add(result);
		}
		
		return R;
	}
	
	public void closeInvertedTableDB() {
		for (int i = 0; i < myDBNumber; i++) {
			if (myDB[i] != null) {
				myDB[i].close();
			}
		}
	}
}