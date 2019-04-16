package solution.MismatchSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String args[]){
        //url
        String xml = "data/book/book2.xml"; 
        //获取前 K 个结果
        int K = 10;
        //阈值 τ
        double τ = 0.9;
        
        /*
        //1、解析
        String dtd = "data/book/book.dtd";
        String dtdxml = "data/book/dtd.xml";
        PreParse parser = new PreParse(xml, dtd, dtdxml);
        parser.parse();
        */
        
        /* 1\ String[] query = {"The victory of", "In a pair of", "12"};
         * 2\ String[] query = {"is located in", "from other countries", "Abraham Lincoln"};
         * 3\ String[] query = {"Unusual accordions", "In common usage", "The field of", "first album"};
         * 4\ String[] query = {"According to", "no childhood", "453"};
         * 5\ String[] query = {"multiple occurrences", "no childhood", "March"};
         * 6\ String[] query = {"possible meaning", "the children of", "periods"};
         * 7\ String[] query = {"introduction", "major advances", "public school"};
         * 8\ String[] query = {"connections to", "most-adopted"};
         * 9\ String[] query = {"A.", "the Second World War", "logic of"};
         * 10\ String[] query = {"you", "look like", "a dog"};
         */
        
        //2、关键字查询
        Date date1 = new Date();
        System.out.println("1. Search: ");
        String[] query = {"you", "look like", "a dog"};
        List<Map> results = Searcher.search(query, xml, K);
        System.out.println("query: ");
        System.out.println(Arrays.toString(query));
        System.out.println("results: ");
        for (Map result : results) {
        	System.out.println("vlca: " + result.get("vlca") + ", nodes: " + Arrays.toString((String[])result.get("nodes")));
		}
        Date date2 = new Date();
        System.out.println("search time: " + (date2.getTime() - date1.getTime()) + " ms");
        
        //3、解决查询的失配问题
        System.out.println("--------------------------------------------------");
        System.out.println("2. Resolve MisMatch Problem: ");
        Resolver resolver = new Resolver(query, results, τ);
        ArrayList<HashMap> suggestedQueries = resolver.resolve();
        resolver.close();
        if(suggestedQueries != null) {
            System.out.println("suggested queries number: " + suggestedQueries.size());
        }
    }
} 
