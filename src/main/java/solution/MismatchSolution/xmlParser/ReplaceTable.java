package solution.MismatchSolution.xmlParser;

import java.util.ArrayList;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ReplaceTable {
	private Database myDatabase;
    private Database myClassDb;		//用来存储类信息的库
    private EntryBinding<ReplaceTableNode> dataBinding;		//绑定对象
    
    public ReplaceTable() {
    	myDatabase = null;
        myClassDb = null;
        dataBinding = null;
    }
    
	public void buildReplaceTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(true);
		    myClassDb = myDbEnvironment.openDatabase(null, "replaceTableNodeDB", dbConfig);
		    StoredClassCatalog classCatalog = new StoredClassCatalog(myClassDb); 	//用来存储可序列化对象
			dataBinding = new SerialBinding<ReplaceTableNode>(classCatalog, ReplaceTableNode.class);
		    //使用自定义的 key 值比较器，以便用 getReplacement 获取替换节点
		    dbConfig.setBtreeComparator(ReplaceTableComparator.class);
		    dbConfig.setOverrideBtreeComparator(true);
		    myDatabase = myDbEnvironment.openDatabase(null, "replaceTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: replacement table database can not be built");
		}
	}
	
	public void openReplaceTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(false);
		    myClassDb = myDbEnvironment.openDatabase(null, "replaceTableNodeDB", dbConfig);
		    StoredClassCatalog classCatalog = new StoredClassCatalog(myClassDb);
			dataBinding = new SerialBinding<ReplaceTableNode>(classCatalog, ReplaceTableNode.class);
		    dbConfig.setBtreeComparator(ReplaceTableComparator.class);
		    dbConfig.setOverrideBtreeComparator(true);
		    myDatabase = myDbEnvironment.openDatabase(null, "replaceTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: replacement table database can not be opened");
		}
	}
	
	public void setIndex(String deweyID, String type, String xml, int[] exLabel) {
		try {
			ReplaceTableNode node = new ReplaceTableNode(type, xml, exLabel);
			DatabaseEntry theKey = new DatabaseEntry(deweyID.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry();
			//向DatabaseEntry里写数据
			dataBinding.objectToEntry(node, theData);
			myDatabase.put(null, theKey, theData);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public ReplaceTableNode getIndex(String deweyID) {
		ReplaceTableNode theNode = null;
		try {
			DatabaseEntry theKey = new DatabaseEntry(deweyID.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry();
			myDatabase.get(null, theKey, theData, LockMode.DEFAULT);
			theNode = (ReplaceTableNode)dataBinding.entryToObject(theData);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return theNode;
	}
	
	public String[] getReplacement(String vlcai, String type) {
		ArrayList<String> replaceNodes = new ArrayList<String>();
		Cursor cursor = myDatabase.openCursor(null, null);
		try {
			DatabaseEntry theKey = new DatabaseEntry(vlcai.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry();
			cursor.getSearchKey(theKey, theData, LockMode.DEFAULT);
			while (cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				String theDeweyID = new String(theKey.getData(), "UTF-8");
				if(theDeweyID.indexOf(vlcai) < 0) break;
		        String theType = dataBinding.entryToObject(theData).getType();
		        if(theType.contentEquals(type)) replaceNodes.add(theDeweyID);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			cursor.close();
		}
		return replaceNodes.toArray(new String[0]);
	}
	
	public void closeReplaceTableDB() {
		if (myDatabase != null) {
			myDatabase.close();
		}
		if (myClassDb != null) {
			myClassDb.close();
		}
	}
}