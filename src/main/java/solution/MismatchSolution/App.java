package solution.MismatchSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String args[]){
        //阈值 τ
        double τ = 0.9;
        //url
        String xml = "data/dblp/dblp2.xml"; 
        
        /*
        //1、解析
        String dtd = "data/dblp/dblp.dtd";
        String dtdxml = "data/dblp/dtd.xml";
        PreParse parser = new PreParse(xml, dtd, dtdxml);
        parser.parse();
        */
        
        //2、关键字查询
        System.out.println("1. Search: ");
        String[] query = {"Xu", "2000", "Text", "TREC"};
        //获取前 K 个结果
        int K = 5;
        List<Map> results = Searcher.search(query, xml, K);
        System.out.println("query: ");
        System.out.println(Arrays.toString(query));
        System.out.println("results: ");
        for (Map result : results) {
        	System.out.println("vlca: " + result.get("vlca") + ", nodes: " + Arrays.toString((String[])result.get("nodes")));
		}
        
        //3、解决查询的失配问题
        System.out.println("--------------------------------------------------");
        System.out.println("2. Resolve MisMatch Problem: ");
        Resolver resolver = new Resolver(query, results, τ);
        ArrayList<HashMap> suggestedQueries = resolver.resolve();
        if(suggestedQueries != null) {
            for (HashMap sugQuery : suggestedQueries) {
            	System.out.println(sugQuery);
    		}
        }
        resolver.close();
    }
} 
