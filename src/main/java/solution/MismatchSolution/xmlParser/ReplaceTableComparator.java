package solution.MismatchSolution.xmlParser;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;

public class ReplaceTableComparator implements Comparator<byte[]> {
	public ReplaceTableComparator() {}
	
    public int compare(byte[] d1, byte[] d2) {
    	String s1 = "", s2 = "";
    	
		try {
			s1 = new String(d1, "UTF-8");
			s2 = new String(d2, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String[] sArr1 = s1.split("\\."),
				 sArr2 = s2.split("\\.");
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
}
