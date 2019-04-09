package solution.MismatchSolution.xmlParser;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class SubtreeTable {
	private Database myDatabase;
    
    public SubtreeTable() {
    	myDatabase = null;
    }
    
	public void buildSubtreeTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(true);
		    //使用自定义的比较器
		    dbConfig.setBtreeComparator(ReplaceTableComparator.class);
		    dbConfig.setOverrideBtreeComparator(true);
		    myDatabase = myDbEnvironment.openDatabase(null, "subtreeTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: subtree table database can not be built");
		}
	}
	
	public void openSubtreeTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(false);
		    dbConfig.setBtreeComparator(ReplaceTableComparator.class);
		    dbConfig.setOverrideBtreeComparator(true);
		    myDatabase = myDbEnvironment.openDatabase(null, "subtreeTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: subtree table database can not be opened");
		}
	}
	
	public void setIndex(String deweyID, String subtree) {
		try {
			DatabaseEntry theKey = new DatabaseEntry(deweyID.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry(subtree.getBytes("UTF-8"));
			myDatabase.put(null, theKey, theData);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public String getIndex(String deweyID) {
		String data = "";
		try {
			DatabaseEntry theKey = new DatabaseEntry(deweyID.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry();
			if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				data = new String(theData.getData(), "UTF-8");
			} else {
				System.out.println("No record found for key '" + theKey + "'.");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return data;
	}
	
	public void closeSubtreeTableDB() {
		if (myDatabase != null) {
			myDatabase.close();
		}
	}
}