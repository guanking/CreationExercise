package dealdata;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;

public class Main {
	public static void main(String[] Args) throws UnsupportedEncodingException{
		Calendar cal=Calendar.getInstance(Locale.CHINA);
		cal.set(2015, 11, 12, 14, 32);
		System.out.println(cal.get(Calendar.MONTH));
	}
}
