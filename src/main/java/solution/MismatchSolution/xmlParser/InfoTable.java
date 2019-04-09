package solution.MismatchSolution.xmlParser;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class InfoTable {
	private Database myDatabase;
    
    public InfoTable() {
    	myDatabase = null;
    }
    
	public void buildInfoTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(true);
		    myDatabase = myDbEnvironment.openDatabase(null, "infoTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: infomation table database can not be built");
		}
	}
	
	public void openInfoTableDB(Environment myDbEnvironment) {
		try {
		    DatabaseConfig dbConfig = new DatabaseConfig();
		    dbConfig.setAllowCreate(false);
		    myDatabase = myDbEnvironment.openDatabase(null, "infoTableDB", dbConfig);
		} catch (DatabaseException dbe) {
			System.err.println("ERROR: infomation table database can not be opened");
		}
	}
	
	public void setIndex(String infoName, String infoValue) {
		try {
			DatabaseEntry theKey = new DatabaseEntry(infoName.getBytes("UTF-8"));
			DatabaseEntry theData = new DatabaseEntry(infoValue.getBytes("UTF-8"));
			myDatabase.put(null, theKey, theData);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public String getIndex(String infoName) {
		String data = "";
		try {
			DatabaseEntry theKey = new DatabaseEntry(infoName.getBytes("UTF-8"));
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
	
	public void closeInfoTableDB() {
		if (myDatabase != null) {
			myDatabase.close();
		}
	}
}
