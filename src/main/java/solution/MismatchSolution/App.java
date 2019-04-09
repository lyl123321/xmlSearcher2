package solution.MismatchSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App { 
    public static void main(String args[]){
        //查询 Q
        String[] Q = {"344", "Makley", "10:30AM", "BIOL"};
        //查询结果 R
        List<Map> R = new ArrayList<Map>();
        
        Map r1 = new HashMap();
        r1.put("vlca", "0");
        String[] M1 = {"0.1.2", "0.1.6", "0.1.8.0", "0.2.1"};
        r1.put("nodes", M1);
        
        Map r2 = new HashMap();
        r2.put("vlca", "0");
        String[] M2 = {"0.1.2", "0.1.6", "0.1.8.0", "0.2.1"};
        r2.put("nodes", M2);

        R.add(r1);
        R.add(r2);
        //阈值
        double τ = 0.9;
        
        //url
        /*
        String xml = "data/dblp/dblp.xml"; 
        String dtd = "data/dblp/dblp.dtd";
        String dtdxml = "data/dblp/dtd.xml";
        */
        String xml = "data/reed/reed.xml"; 
        String dtd = "data/reed/reed.dtd";
        String dtdxml = "data/reed/dtd.xml";
        PreParse parser = new PreParse(Q, xml, dtd, dtdxml);
        parser.parse();
        
        Resolver resolver = new Resolver(Q, R, τ);
        ArrayList<HashMap> suggestedQueries = resolver.resolve();
        for (HashMap sugQuery : suggestedQueries) {
        	System.out.println(sugQuery);
		}
        resolver.close();
    }
} 
