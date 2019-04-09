package solution.MismatchSolution;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import solution.MismatchSolution.xmlParser.InvertedTable;

public class Searcher {
	private Environment myDbEnvironment;
	private InvertedTable invertedTable;
	
	public Searcher(String[] Q) {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setAllowCreate(false);
		    myDbEnvironment = new Environment(new File("data/dbEnv"), envConfig);
		} catch (Exception e) {
			System.err.println("ERROR: database environment can not be opened");
		}
		invertedTable = new InvertedTable();
		invertedTable.openInvertedTableDB(myDbEnvironment, Q);
	}
	
	public List<Map> search() {
		return invertedTable.getResults();
	}
	
	public void close() {
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
