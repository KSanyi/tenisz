package hu.kits.tennis.common;

import java.text.ParseException;
import java.text.RuleBasedCollator;

public class StringUtil {
    
    public static String cleanNameString(String value) {
        return value.toLowerCase()
                .replace("ő", "o").replace("ö", "o").replace("ó", "o")
                .replace("ű", "u").replace("ü", "u").replace("ú", "u")
                .replace("á", "a").replace("í", "i").replace("é", "e");
    }
    
    public static final RuleBasedCollator HUN_COLLATOR;
    
    static {
        String hungarianRules = "< a,A < á,Á < b,B < c,C < cs,Cs,CS < d,D < dz,Dz,DZ < dzs,Dzs,DZS \n" + 
                " < e,E < é,É < f,F < g,G < gy,Gy,GY < h,H < i,I < í,Í < j,J\n" + 
                " < k,K < l,L < ly,Ly,LY < m,M < n,N < ny,Ny,NY < o,O < ó,Ó \n" + 
                " < ö,Ö < ő,Ő < p,P < q,Q < r,R < s,S < sz,Sz,SZ < t,T \n" + 
                " < ty,Ty,TY < u,U < ú,Ú < ü,Ü < ű,Ű < v,V < w,W < x,X < y,Y < z,Z < zs,Zs,ZS";
        
        try {
            HUN_COLLATOR = new RuleBasedCollator(hungarianRules);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
