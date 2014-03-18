package kcg.vebrain.communication;

/**
 * this class is used for compressing Geographic information (lat,lon, ...) into a short
 * ascci array of byte for the use of VE mesh system.
 * Assumptions:
 * 1. up to 20 chars (ascci) per message 
 * 2. any message should end with /n
 * 3. Radio channel of 9600 bps - VE mesh (must include ID)
 * 4. any char is mapped to a number 0-99 using ascci 
 * 
 * Message frame:
 * B0 B1-4 B5-8 B9-10  	B11-B12 B13-14 b15 		b16		b17-19 		
 * ID Lat  Lon  Alt 	Baro 	azm    azm-up  	err		Extra
 * 
 * 
 * Lat & lon are UTM values in meter delta from reference point (must be positive)
 * Alt in meter delta from reference point (must be positive).
 * Baro positive delta value from a reference in Delta-Bar
 * Azm in deg.0! (int) 2402 --> 240.2 deg
 * Azm-up: 100 -->0,  0--> -12.5,  199--> _12.5 deg
 * Err: 0-OK, 1-No movement alarm, 10 - A way from the gun alarm.
 * 
 * Assumes Testing in Elyakim 36S min E 690000, min N 3610000, min elev 0 
 */


public class Short_MS {
	//public static CoordinateConversion _CORDS = new CoordinateConversion();
	
	private static double min_east = 690000, min_north = 3550000, MIN_BAR=900, MIN_ALT=-400;
	//private static double min_lat = 32.0702810615051,  min_lon=  35.01288792193477;
	private static double min_lat = 32.070280,  min_lon=  35.012880;
	private static double min_lat2 = 32.070281,  min_lon2=  35.012881;
	private static String min_utm1 = "36S "+min_east+" "+min_north;
	//private static String min_utm2 = _CORDS.latLon2UTM(min_lat, min_lon);
	//private static String min_utm3 = _CORDS.latLon2UTM(min_lat2, min_lon2);
	public static final int RANGE = 200, MIN=33, MAX=MIN+RANGE;
	private static double CONV1 = 1000000;
	/**
	 * gets osition from the GPS and convert it to compact delta UTM representation 
	 * @param lat
	 * @param lon
	 * @return
	 */
	/*public static double[] deltaUTM(double lat, double lon) {
		return _CORDS.latlon_diff(min_lat, min_lon, lat, lon);
	}*/
	/**
	 * main function 
	 * @param ms
	 * @return
	 */
	public static String fromMS_old(String ms) {
		String ans="";
		ans+="id= "+char2int(ms.charAt(0));
		ans+="   Dlat= "+string2int(ms.substring(1, 5));
		ans+="   Dlon= "+string2int(ms.substring(5, 9));
		ans+="   Dalt= "+string2int(ms.substring(9, 11));
		ans+="   Dbar= "+string2int(ms.substring(11, 13));
		ans+="   azm= "+(string2int(ms.substring(13, 15)))/10.0;
		ans+="   azm_up= "+((char2int(ms.charAt(15))-(RANGE/2))/4.0);
		ans+="   err= "+char2int(ms.charAt(16));
		ans+="   ext= "+ms.substring(17);
		return ans;
	}
	public static String fromMS(String ms) {
		String ans="";
		ans+="id= "+char2int(ms.charAt(0));
		ans+="   Lat= "+convertBackLatlon(ms.substring(1, 5));
		ans+="   Lon= "+convertBackLatlon(ms.substring(5, 9));
		ans+="   Dalt= "+string2int(ms.substring(9, 11));
		ans+="   Dbar= "+string2int(ms.substring(11, 13));
		ans+="   azm= "+(string2int(ms.substring(13, 15)))/10.0;
		ans+="   azm_up= "+((char2int(ms.charAt(15))-(RANGE/2))/4.0);
		ans+="   err= "+char2int(ms.charAt(16));
		ans+="   ext= "+ms.substring(17);
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
	/**
	 * Main function 
	 * @param id
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param bro
	 * @param azm
	 * @param azm_up
	 * @param err
	 * @param ext
	 * @return
	 */
	/*public static String toMS_old(int id, double lat, double lon, double alt, double bro, double azm, double azm_up, int err, String ext) {
		String ans = "";
		ans+=convert_single(id);
		double[] ll= deltaUTM(lat,lon);
		String lats = convert_many(ll[0]);
		String lons = convert_many(ll[1]);
		ans=ans+pad(lats,4)+pad(lons,4);
		String alts = convert_many(d_alt(alt));
		ans=ans+pad(alts,2);
		String bars = convert_many(d_bar(bro));
		ans=ans+pad(bars,2);
		String azms = convert_many(azm*10);
		ans=ans+pad(azms,2);
		String azm_ups = convert_many(RANGE/2+azm_up*4);
		ans=ans+pad(azm_ups,1);
		ans+=convert_single(err);
		ans+=ext+'\n';
		return ans;
	}*/
	
	public static String toMS(int id, double lat, double lon, double alt, double bro, double azm, double azm_up, int err, String ext) {
		String ans = "";
		ans+=convert_single(id);
		//double[] ll= deltaUTM(lat,lon);
		String lats = convertLatlon(lat);
		String lons = convertLatlon(lon);
		ans=ans+pad(lats,4)+pad(lons,4);
		String alts = convert_many(d_alt(alt));
		ans=ans+pad(alts,2);
		String bars = convert_many(d_bar(bro));
		ans=ans+pad(bars,2);
		String azms = convert_many(azm*10);
		ans=ans+pad(azms,2);
		String azm_ups = convert_many(RANGE/2+azm_up*4);
		ans=ans+pad(azm_ups,1);
		ans+=convert_single(err);
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
		String ms = toMS(1,lat1, lon1, 200,1000,170.5, 1.5, 0, "   ");
		String ms1 = fromMS(ms);
		System.out.println(ms);
		System.out.println(ms1);
	}
	/*public static void test3() {
		System.out.println(min_utm2);
		System.out.println(min_utm3);
		int c = 200;
		for(int i=0;i<5;i++) {
			System.out.println(i+") "+c);
			c=c*200;
		}
		double lat1 = 32.2037219111111,lon1 = 35.309900;
		String s1 = convertLatlon(lat1);
		double lat1b =  convertBackLatlon(s1);
		System.out.println("orig: "+lat1+"  conv: "+lat1b+"  diff: "+(lat1-lat1b)+"  string: "+s1);
		
	}*/
}
