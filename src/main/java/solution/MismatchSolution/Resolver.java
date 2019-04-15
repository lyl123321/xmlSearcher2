package solution.MismatchSolution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import solution.MismatchSolution.xmlParser.InfoTable;
import solution.MismatchSolution.xmlParser.InvertedTable;
import solution.MismatchSolution.xmlParser.ReplaceTable;

public class Resolver {
	private Environment myDbEnvironment;
	private ReplaceTable replaceTable;
	private InfoTable infoTable;
	private InvertedTable invertedTable;
	private List<String> typeList;
	private int[][] maxContain;
	private int[] Ft;
	private double τ;
	private String[][] K;
	private double[] D;
	private String[] Q;
	private List<Map> R;
	
	public Resolver(String[] Q, List<Map> R, double τ) {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
		    envConfig.setAllowCreate(false);
		    myDbEnvironment = new Environment(new File("data/dbEnv"), envConfig);
		} catch (Exception e) {
			System.err.println("ERROR: database environment can not be opened");
		}
		replaceTable = new ReplaceTable();
		replaceTable.openReplaceTableDB(myDbEnvironment);
		infoTable = new InfoTable();
		infoTable.openInfoTableDB(myDbEnvironment);
		invertedTable = new InvertedTable();
		invertedTable.openInvertedTableDB(myDbEnvironment, Q);
		this.typeList = Arrays.asList(infoTable.getIndex("typeList").replaceAll("[\\[\\]]", "").split(",\\s"));
		this.τ = τ;
		this.Q = Q;
		this.R = R;
		String[] temp = infoTable.getIndex("Ft").replaceAll("[\\[\\]]", "").split(",\\s");
		int len = temp.length;
		this.Ft = new int[len];
		for (int i = 0; i < len; i++) {
			this.Ft[i] = Integer.parseInt(temp[i]);
		}
		setMaxContain("book");
	}
	
	public ArrayList<HashMap> resolve() {
		ArrayList<HashMap> suggestedQueries = new ArrayList<HashMap>();
    	
    	//Detector
		Date date1 = new Date();
    	for(Map r : R) {
    		String vlca = (String)r.get("vlca");
    		String vlcaType = replaceTable.getIndex(vlca).getType();
    		String[] nodes = (String[])r.get("nodes");
    		String targetType = getTNT(nodes);
    		System.out.println("tnt: " + targetType);
    		if(vlcaType.contentEquals(targetType)) {
    			System.out.println("This query doesn't exist mismatch problem");
    			return null;
    		}
    		
        }
    	Date date2 = new Date();
    	System.out.println("Detector time: " + (date2.getTime() - date1.getTime()) + "ms");
    	
    	//Suggester
    	for(Map r : R) {
    		String vlca = (String)r.get("vlca");
    		int vlcaLen = vlca.split("\\.").length;
    		String[] nodes = (String[])r.get("nodes");
    		String targetType = getTNT(nodes);
    		int[] rExLable = constructExlabel(nodes);
    		ArrayList<String> vlcais = new ArrayList<String>();
    		int len = nodes.length;
    		K = new String[len][];
    		D = new double[len];
    		for (int i = 0; i < len; i++) {
    			K[i] = getKeywords(nodes[i]);
    			D[i] = getDist(nodes[i], K[i]);
    		}
    		//Phase 1
    		for (int i = 0; i < len; i++) {
    			//System.out.println(D[i]);
    			if(D[i] < τ) continue;
				String[] ids = nodes[i].split("\\.");
				int nodeLen = ids.length;
				for (int j = vlcaLen; j < nodeLen; j++) {
					String vlcai = ids[0];
					for (int k = 1; k <= j; k++) {
						vlcai += "." + ids[k];
					}
					String vlcaiType = replaceTable.getIndex(vlcai).getType();
					if (vlcaiType.contentEquals(targetType)) {
						int[] exLable = replaceTable.getIndex(vlcai).getExLabel();
						//如果 vlcai 的 exLable 包含 rExLable 
						if(contain(exLable, rExLable)) {
							//避免对相同的 vlcai 进行重复的 QuerySuggester 运算 
							if(vlcais.contains(vlcai)) break;
							vlcais.add(vlcai);
							QuerySuggester(vlcai, vlcaLen, nodes, suggestedQueries);
						}
					}
    			}
    		}
    		//Phase 2
    		sort(nodes);
    		for (int i = 0; i < len - 1; i++) {
				String lca = getLCA(nodes[i], nodes[i + 1]);
				String[] keywords = getKeywords(lca);
				double dist = getDist(lca, keywords);
				//System.out.println(dist);
				if(dist < τ) continue;
				String[] ids = lca.split("\\.");
				int nodeLen = ids.length;
				for (int j = vlcaLen; j < nodeLen; j++) {
					String vlcai = ids[0];
					for (int k = 1; k <= j; k++) {
						vlcai += "." + ids[k];
					}
					String vlcaiType = replaceTable.getIndex(vlcai).getType();
					if (vlcaiType.contentEquals(targetType)) {
						int[] exLable = replaceTable.getIndex(vlcai).getExLabel();
						if(contain(exLable, rExLable)) {
							if(vlcais.contains(vlcai)) break;
							vlcais.add(vlcai);
							QuerySuggester(vlcai, vlcaLen, nodes, suggestedQueries);
						}
					}
    			}
			}
        }
    	
    	sort(suggestedQueries);
    	System.out.println("Suggester time: " + ((new Date()).getTime() - date2.getTime()) + "ms");
    	
    	return suggestedQueries;
    }
	
	//Algorithm 2
	private void QuerySuggester(String vlcai, int vlcaLen, String[] nodes, ArrayList<HashMap> sugQueries) {
		int len = nodes.length;
		boolean[] bool = new boolean[len];
		String[][] replace = new String[len][];
		
		for (int i = 0; i < len; i++) {
			String node = nodes[i];
			//node 不是 vlcai 的子节点
			if(node.indexOf(vlcai) != 0) {
				String type = replaceTable.getIndex(node).getType();
				replace[i] = replaceTable.getReplacement(vlcai, type);
				bool[i] = true;
			} else {
				replace[i] = new String[] {nodes[i]};
				bool[i] = false;
			}
		}

		// 计算分数 score
		double cn = 0.0;
		double dt = vlcai.split("\\.").length - vlcaLen;
		double SD = 0.0;
		double e = 2.71828;
		double score = 0.0;

		for (int i = 0; i < len; i++) {
			if(!bool[i]) continue;
			cn += K[i].length;
			SD += D[i];
		}
		
		score = 1.0 / Math.pow(e, cn) * (1.0 - 1.0 / Math.pow(e, dt)) * 1.0 / Math.pow(e, SD);
		
		//各种可能的替换节点列表
		ArrayList<ArrayList<String>> expNodesArray = multiCartesian(replace);
		
		for(ArrayList<String> expNodes : expNodesArray) {
			String[] eNodes = expNodes.toArray(new String[0]);
			//Set 保证没有重复的查询关键字
			HashSet<String> query = new HashSet<String>();
			HashMap sugQuery = new HashMap();
			HashMap expResult = new HashMap();

			for(int i = 0; i < len; i++) {
				String node = eNodes[i];
				String type = replaceTable.getIndex(node).getType();
				String subtree = replaceTable.getIndex(node).getXml();
				if(!bool[i]) {
					query.addAll(Arrays.asList(K[i]));
					continue;
				}
				Pattern pattern = Pattern.compile("<.+>[\\r\\n\\s]*([^\\r\\n]+)[\\r\\n\\s]*<.+>");
				Matcher matcher = pattern.matcher(subtree);
				matcher.find();
				query.add(matcher.group(1));
				//System.out.println("(" + type + "): " + String.join(" ", K[i]) + " -> " + matcher.group(1));
			}
			
			//添加查询 sugQuery 到 sugQueries 中
			expResult.put("vlca", vlcai);
			//expResult.put("nodes", eNodes); 数组形式
			expResult.put("nodes", Arrays.toString(eNodes));
			sugQuery.put("result", expResult);
			//sugQuery.put("query", query.toArray(new String[0])); 数组形式
			sugQuery.put("query", query);
			sugQuery.put("score", score);
			sugQueries.add(sugQuery);
		}
	}
	
	//根据 mnodes 获取目标节点类型 tnt
	private String getTNT(String[] nodes) {
		int len = nodes.length;
		int[] count = new int [len];	//m1 到 mn 中 ti 类型的不同关键字匹配节点的数量
		String[] types = new String[len];
		List<String> typesNoRe = new ArrayList<String>();
		for(int i = 0; i < len; i++) {
			types[i] = replaceTable.getIndex(nodes[i]).getType();
		}
		
		//去重，计算重复类型节点数
		Arrays.fill(count, 1);
		Arrays.sort(types);
		typesNoRe.add(0, types[0]);
		for(int i = 1, j = 0; i < len; i++) {
			if(types[i].contentEquals(typesNoRe.get(j))) {
				count[j]++;
			} else {
				typesNoRe.add(++j, types[i]);
			}
		}
		types = typesNoRe.toArray(new String[0]);
		len = types.length;
		
		//获取 tnt
		String[] commonPref = getCommonPref(types);
		int cLen = commonPref.length;
		for(int i = cLen - 1; i >= 0; i--) {
			boolean b = true;
			int anIndex = typeList.indexOf(commonPref[i]);
			for(int k = 0; k < len; k++) {
				int chIndex = typeList.indexOf(types[k]);
				if(maxContain[anIndex][chIndex] < count[k]) {
					b = false;
					break;
				}
			}
			if(b) return commonPref[i];
		}
		
		return "";
	}
	
	//获取公共前缀
	private String[] getCommonPref(String[] types) {
		List<String> commonPref = new ArrayList<String>();
		
		int len = types.length;
		String[][] arrays = new String[len][];
		for(int i = 0; i < len; i++) {
			arrays[i] = types[i].split("/");
		}
		
		int minLen = arrays[0].length;
		for(int i = 1; i < len; i++) {
			if(arrays[i].length < minLen) {
				minLen = arrays[i].length;
			}
		}
		
		for(int i = 1; i < minLen; i++) {
			boolean b = true;
			String temp = arrays[0][i];
			for(int j = 1; j < len; j++) {
				if(temp.contentEquals(arrays[j][i])) continue;
				b = false;
				break;
			}
			if(b) {
				commonPref.add(temp);
			} else {
				break;
			}
		}
		
		String[] commonPrefs = commonPref.toArray(new String[0]);
		int cLen = commonPrefs.length;
		
		for (int i = cLen - 1; i >= 0; i--) {
			String temp = "";
			for(int j = 0; j <= i; j++) {
				temp += "/" + commonPrefs[j];
			}
			commonPrefs[i] = temp;
		}
		
		return commonPrefs;
	}

	//为 mnodes 构造 exlable
	private int[] constructExlabel(String[] nodes) {
		int[] res = new int[typeList.size()];
		for(String node : nodes) {
			String type = replaceTable.getIndex(node).getType();
			res[typeList.indexOf(type)] = 1;
		}
		return res;
	}

	//获取节点 node 匹配的查询关键字
	private String[] getKeywords(String node) {
		String subtree = replaceTable.getIndex(node).getXml();
		ArrayList<String> keywords = new ArrayList<String>();
		for(String keyword : Q) {
			if(subtree.indexOf(keyword) >= 0) {
				keywords.add(keyword);
			}
		}
		return keywords.toArray(new String[0]);
	}

	//根据节点 node 和它匹配的关键字 keywords 计算关键字对它的可区分性值
	private double getDist(String node, String[] keywords) {
		String type = replaceTable.getIndex(node).getType();
		double ft = Ft[typeList.indexOf(type)] + 0.0;
		double ftK = invertedTable.getFtK(type, keywords) + 0.0;
		double dist = 1.0 - ftK / ft + 1.0 / ft;
    	return dist;
	}

	//Algorithm 3, exLable1 是否包含 exLable2
	private boolean contain(int[] exLable1, int[] exLable2) {
		int len = exLable1.length;
		int[] temp = new int[len];
		for (int i = 0; i < len; i++) {
			temp[i] = exLable1[i] == 1 && exLable2[i] == 1 ? 1 : 0;
		}
		return Arrays.equals(exLable2, temp);
	}
	
	//升序排列节点
	private void sort(String[] nodes) {
		Arrays.sort(nodes, new Comparator<String>() {
			@Override
			public int compare(String node1, String node2) {
				String[] sArr1 = node1.split("\\."),
						 sArr2 = node2.split("\\.");
				int i = 0,
					len1 = sArr1.length,
					len2 = sArr2.length,
					len = len1 < len2 ? len1 : len2;
				int[] iArr1 = new int[len1],
					  iArr2 = new int[len2];
				for(i = 0; i < len1; i++) {
					iArr1[i] = Integer.parseInt(sArr1[i]);
				}
				for(i = 0; i < len2; i++) {
					iArr2[i] = Integer.parseInt(sArr2[i]);
				}
				for(i = 0; i < len; i++) {
					if(iArr1[i] != iArr2[i]) return iArr1[i] - iArr2[i];
				}
				return len1 - len2;
			}
		});
	}
	
	//按分数 score 降序排列查询
	private void sort(ArrayList<HashMap> sugQueries) {
		sugQueries.sort(new Comparator<HashMap>() {
			@Override
			public int compare(HashMap sugQuery1, HashMap sugQuery2) {
				double score1 = (double)sugQuery1.get("score");
				double score2 = (double)sugQuery2.get("score");
				int sd = (int) ((score2 - score1) * 1E4);
				return sd;
			}
		});
	}
	
	//获取 node1 和 node2 的 LCA，ids1[i].contentEquals(ids2[i]) 而不是 ids1[i] == ids2[i]，引用类型 == 比较的是地址而不是内容
    private String getLCA(String node1, String node2) {
		String[] ids1 = node1.split("\\.");
		String[] ids2 = node2.split("\\.");
		String lca = ids1[0];
		int minLen = ids1.length < ids2.length ? ids1.length : ids2.length;
		for (int i = 1; i < minLen; i++) {
			if(ids1[i].contentEquals(ids2[i])) {
				lca += "." + ids1[i];
			} else {
				break;
			}
		}
		return lca;
	}
    
	//求多个数组的笛卡尔积
	private ArrayList<ArrayList<String>> multiCartesian(String[][] arries) {
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		
		for(String a : arries[0]) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(a);
			res.add(temp);
		}
		
		for (int i = 1, len = arries.length; i < len; i++) {
			res = Cartesian(res, arries[i]);
		}
		
		return res;
	}
	
	private ArrayList<ArrayList<String>> Cartesian(ArrayList<ArrayList<String>> arries, String[] array) {
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		
       	for(ArrayList<String> arr : arries) {
       		for(String a : array) {
       			ArrayList<String> temp = new ArrayList<String>(arr);
       			temp.add(a);
       			res.add(temp);
    		}
		}
       	
       	return res;
	}
	
	
	//手动设置 MaxContain
	private void setMaxContain(String model) {
		int len = typeList.size();
		maxContain = new int[len][];
		switch (model) {
		case "dblp":
			for (int i = 0; i < len; i++) {
				maxContain[i] = new int[len];
				Arrays.fill(maxContain[i], 100);;
			}
			break;
		case "book":
			for (int i = 0; i < len; i++) {
				maxContain[i] = new int[len];
				Arrays.fill(maxContain[i], 100);;
			}
			break;
		default:
			break;
		}
	}
	
	public void close() {
    	replaceTable.closeReplaceTableDB();
    	infoTable.closeInfoTableDB();
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
