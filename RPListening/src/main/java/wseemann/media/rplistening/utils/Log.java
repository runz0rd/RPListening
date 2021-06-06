package wseemann.media.rplistening.utils;

public class Log {

	public static boolean suppressLogs = false;
	
	private Log() {
		
	}
	
	public static void d(String tag, String msg) {
		if (!suppressLogs) {
			System.out.println(tag + " " + msg);
		}
	}
}
