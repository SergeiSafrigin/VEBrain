package kcg.vebrain.communication;
/**
 * This class is used for compressing Geographic information (lat,lon, ...) into a short
 * ascci array of byte for the use of VE mesh system.
 * Assumptions:
 * 1. up to 20 chars (ascci) per message 
 * 2. any message should end with /n/r
 * 3. Radio channel of 9600 bps - VE mesh (must include ID)
 * 4. any char is mapped to a number 16-255 using ascci 
 * 
 * Message frame:
 * B0 B1-4 B5-8 B9-10  	B11-B12 B13-14 b15 		b16		b17-19 		
 * ID Lat  Lon  Alt 	Baro 	azm    azm-up  	err		Extra
 * 
 * 
 * Lat & lon are assumed to be double with 6 digits resolution (e.g. 32.123456)
 * Alt in meter convert to int   
 * Barometer positive value assumes -400 - 3000 meter alt.
 * Azm in deg.0! (int) 2402 --> 240.2 deg
 * Azm-up: 100 -->0,  0--> -12.5,  199--> _12.5 deg
 * Err: 0-OK, 1-No movement alarm, 10 - A way from the gun alarm.
 * 
 *  Example:
 */


public class VE_Short_MS {	
	private static double MIN_BAR=800, MIN_ALT=-400;
	public static final int RANGE = 200, MIN=33, MAX=MIN+RANGE;
	private static double CONV1 = 1000000;
	private static double CONV2 = 50;
	private static double CONV3 = 10;
	
	
	public static String fromMS(String ms) {
		String ans="";
		ans+="id= "+char2int(ms.charAt(0));
		ans+="   Lat= "+convertBackLatlon(ms.substring(1, 5));
		ans+="   Lon= "+convertBackLatlon(ms.substring(5, 9));
		ans+="   Alt= "+(string2int(ms.substring(9, 11))/CONV3+MIN_ALT);
		ans+="   Bar= "+(string2int(ms.substring(11, 13))/CONV2+MIN_BAR);
		ans+="   azm= "+(string2int(ms.substring(13, 15)))/CONV3;
		ans+="   azm_up= "+((char2int(ms.charAt(15))-(RANGE/2))/4.0);
		ans+="   err= "+char2int(ms.charAt(16));
		ans+="   ext= "+ms.substring(17);
		return ans;
	}
	public static double[] fromMS2(String ms) {
		double[] ans = new double[9];
		ans[0] = char2int(ms.charAt(0));
		ans[1] = convertBackLatlon(ms.substring(1, 5));
		ans[2] = convertBackLatlon(ms.substring(5, 9));
		ans[3] = (string2int(ms.substring(9, 11))/CONV3+MIN_ALT);
		ans[4] = (string2int(ms.substring(11, 13))/CONV2+MIN_BAR);
		ans[5] = (string2int(ms.substring(13, 15)))/CONV3;
		ans[6] = ((char2int(ms.charAt(15))-(RANGE/2))/4.0);
		ans[7] = char2int(ms.charAt(16));
//		ans[7] = Integer.parseInt(ms.charAt(16)+"");
		ans[8] = string2int(ms.substring(17));
		return ans;
	}
	public static String convertLatlon(double d) {
		String ans="";
		if(d<0 | d>90) throw new RuntimeException("Got wrong value for lat/lon conversion");
		double dd = d*CONV1;
		ans = convert_many(dd);
		return ans;
	}
	public static double convertBackLatlon(String d) {
		double ans;
		if(d==null || d.length()>4) throw new RuntimeException("Got wrong value for lat/lon conversion");
		ans = string2int(d);
		ans = ans/CONV1;
		return ans;
	}

	public static String toMS(int id, double lat, double lon, double alt, double bro, double azm, double azm_up, int err, String ext) {
		String ans = "";
		ans+=convert_single(id);
		//double[] ll= deltaUTM(lat,lon);
		String lats = convertLatlon(lat);
		String lons = convertLatlon(lon);
		ans=ans+pad(lats,4)+pad(lons,4);
		String alts = convert_many(d_alt(alt)*CONV3);
		ans=ans+pad(alts,2);
		String bars = convert_many(d_bar(bro)*CONV2);
		ans=ans+pad(bars,2);
		String azms = convert_many(azm*CONV3);
		ans=ans+pad(azms,2);
		String azm_ups = convert_many(RANGE/2+azm_up*4);
		ans=ans+pad(azm_ups,1);
		ans+=convert_many(convert_single(err));
		ans+=ext+'\n';
		return ans;
	}
	public static int char2int(char c){
		return c-MIN;
	}
	public static int string2int(String s){
		int ans =0;
		for(int i=0;i<s.length();i++){
			ans+= char2int(s.charAt(i));
			if(i<s.length()-1) {ans*=RANGE;}
		}
		return ans;
	}
	public static double d_alt(double alt) {
		return alt-MIN_ALT;
	}
	public static double d_bar(double bar) {
		return bar-MIN_BAR;
	}
	public static char convert_single(double d) { 
		int i = (int)(d+0.5);
		i = i%RANGE;
		char c = (char)(i+MIN);
		return c;
	}
	public static String convert_many(double dd) {
		int d = (int)(dd+0.5);
		String ans = "";
		while(d>0) {
			char c = convert_single(d%RANGE);
			ans=c+ans;  //msb left
			d=d/RANGE;
		}
		return ans;
	}
	
	public static String pad(String orig, int size) {
		char c = convert_single(0);
		while(orig.length()<size) {
			orig = c+orig;
		}
		return orig;
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		test2();
		
	}

	public static void test2() {
		double lat1 = 32.203721,lon1 = 35.309900;
		String ms = toMS(1,lat1, lon1, 3213.44,901.35,170.5, 1.5, 0, "333");
		String ms1 = fromMS(ms);
		System.out.println(ms);
		System.out.println(ms1);
		double[] ms3 = fromMS2(ms);
		for(int i=0;i<ms3.length;i++) {
			System.out.println(i+") "+ms3[i]);
		}
	}
}
