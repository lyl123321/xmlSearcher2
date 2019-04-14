package solution.MismatchSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class App {
    public static void main(String args[]){
        //阈值 τ
        double τ = 0.9;
        //url
        String xml = "data/book/book.xml"; 
        
        /*
        //1、解析
        String dtd = "data/book/book.dtd";
        String dtdxml = "data/book/dtd.xml";
        PreParse parser = new PreParse(xml, dtd, dtdxml);
        parser.parse();
        */
        
        //2、关键字查询
        Date date1 = new Date();
        System.out.println("1. Search: ");
        String[] query = {"sections", "active", "victory"};
        //获取前 K 个结果
        int K = 5;
        List<Map> results = Searcher.search(query, xml, K);
        System.out.println("query: ");
        System.out.println(Arrays.toString(query));
        System.out.println("results: ");
        for (Map result : results) {
        	System.out.println("vlca: " + result.get("vlca") + ", nodes: " + Arrays.toString((String[])result.get("nodes")));
		}
        Date date2 = new Date();
        System.out.println("search time: " + (date2.getTime() - date1.getTime()) + "ms");
        
        //3、解决查询的失配问题
        System.out.println("--------------------------------------------------");
        System.out.println("2. Resolve MisMatch Problem: ");
        Resolver resolver = new Resolver(query, results, τ);
        ArrayList<HashMap> suggestedQueries = resolver.resolve();
        resolver.close();
        if(suggestedQueries != null) {
            int num = suggestedQueries.size();
            System.out.println("suggested queries number: " + num);
            System.out.println("top-5 suggested queries: ");
            num = num < 5 ? num : 5;
            for (int i = 0; i < num; i++) {
            	System.out.println(suggestedQueries.get(i));
    		}
        }
        System.out.println("resolve time: " + ((new Date()).getTime() - date2.getTime()) + "ms");
    }
} 
