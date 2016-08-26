package dealdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CityWeather {
	static private final String parURL = "http://www.weather.com.cn/weather/";
	static private final String assertDir = "F:\\CreationExercises\\assert\\data";
	static private String[] rank = new String[] { "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "10" };
	static public LinkedList<LinkedList<String>> citys = null, numbers = null;
	static public LinkedList<String> provinces = null;
	private String year, month, dayOfMonth, hour, minute;
	private String provinceName, cityName;
	private Hashtable<String, LinkedList<String>> status;
	boolean finish;
	static {
		citys = getCityNames(getContent("city-old.txt"));
		numbers = getCityNumbers(getContent("cityNum-old.txt"));
		provinces = getProvince(getContent("province-old.txt"));
	}

	public CityWeather(final String provinceName, final String cityName)
			throws Exception {
		this.provinceName = provinceName;
		this.cityName = cityName;
		status = new Hashtable<String, LinkedList<String>>();
		finish = false;
		Runnable run = new Runnable() {
			@Override
			public void run() {
				String t = null;
				try {
					t = queryID(provinceName, cityName);
					// t = getContent(
					// "【烟台天气】烟台天气预报,天气预报一周,天气预报15天查询 09月29日20时 周二 多云转阴 18 25°C.html",
					// "utf-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
				t = getInternetContent(t);
				try {
					dealData(t);
				} catch (Exception e) {
					e.printStackTrace();
				}
				finish = true;
			}
		};
		new Thread(run).start();
	}

	public String getPlace() {
		return provinceName + "-" + cityName;
	}

	public String getDate() {
		return year + "-" + month + "-" + dayOfMonth + " " + hour + ":"
				+ minute;
	}

	public Hashtable<String, LinkedList<String>> getWeather() {
		while (!finish) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return status;
	}

	static public String getContent(String fileName) {
		StringBuffer sb = new StringBuffer();
		FileReader reader = null;
		try {
			reader = new FileReader(new File(assertDir, fileName));
			char buf[] = new char[1024];
			int len = 0;
			try {
				while ((len = reader.read(buf, 0, 1024)) != -1)
					sb.append(buf, 0, len);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getContent(String fileName, String charset) {
		StringBuffer sb = new StringBuffer();
		InputStreamReader reader = null;
		try {
			try {
				reader = new InputStreamReader(new FileInputStream(new File(
						assertDir, fileName)), charset);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			char buf[] = new char[1024];
			int len = 0;
			try {
				while ((len = reader.read(buf, 0, 1024)) != -1)
					sb.append(buf, 0, len);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static LinkedList<LinkedList<String>> getCityNames(String text) {
		LinkedList<LinkedList<String>> citys = new LinkedList<LinkedList<String>>();
		Pattern pattern = Pattern.compile("Array\\([\\s\\S]+?\\)");
		Pattern patName = Pattern.compile("\"([\\s\\S]+?)\"");
		Matcher matcher = pattern.matcher(text);
		Matcher matcherName = patName.matcher("");
		while (matcher.find()) {
			matcherName.reset(matcher.group());
			LinkedList<String> names = new LinkedList<String>();
			while (matcherName.find()) {
				names.addLast(matcherName.group(1));
			}
			citys.addLast(names);
		}
		return citys;
	}

	protected static LinkedList<LinkedList<String>> getCityNumbers(String text) {
		return getCityNames(text);
	}

	protected static LinkedList<String> getProvince(String text) {
		LinkedList<String> provinces = new LinkedList<String>();
		Pattern pattern = Pattern
				.compile("<[\\s\\S]+?>([\\s\\S]+?)<[\\s\\S]+?>");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			provinces.addLast(matcher.group(1));
		}
		return provinces;
	}

	protected String getInternetContent(String cityID) {
		URL url = null;
		try {
			url = new URL(parURL + cityID + ".shtml");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStreamReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new InputStreamReader(url.openStream(), "utf-8");
			int len = 0;
			char buf[] = new char[1024];
			while ((len = reader.read(buf, 0, 1024)) != -1) {
				sb.append(buf, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void show() {
		System.out.println("Citys：" + citys.size());
		for (LinkedList<String> ele : citys) {
			System.out.print(ele.size() + " : ");
			for (String name : ele) {
				System.out.print(name + " ");
			}
			System.out.println();
		}
		System.out.println("City's ID：" + numbers.size());
		for (LinkedList<String> ele : numbers) {
			System.out.print(ele.size() + " : ");
			for (String name : ele) {
				System.out.print(name + " ");
			}
			System.out.println();
		}
		System.out.println("Province：" + provinces.size());
		for (String name : provinces) {
			System.out.println(name + " ");
		}
	}

	public String queryID(String provinceName, String cityName)
			throws Exception {
		int provinceIndex = provinces.indexOf(provinceName);
		if (provinceIndex == -1 || provinceIndex == 0) {
			throw new Exception("No this province,please check whether the "
					+ provinceName + " province is correct!");
		}
		int cityIndex = citys.get(provinceIndex).indexOf(cityName);
		if (cityIndex == -1 || cityIndex == 0) {
			throw new Exception("No such city in province " + provinceName
					+ ",please check whether " + cityName + " city is correct!");
		}
		return numbers.get(provinceIndex).get(cityIndex);
	}

	private void dealData(String text) throws Exception {
		Pattern pattern = Pattern
				.compile("<p\\s+?class=[\"']updataTime[\"']\\s*?>"
						+ "(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s{1,2}"
						+ "(\\d{1,2}):(\\d{1,2})[\\s\\S]*?</p>");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			year = matcher.group(1);
			month = matcher.group(2);
			dayOfMonth = matcher.group(3);
			hour = matcher.group(4);
			minute = matcher.group(5);
		} else
			throw new Exception("matcher updataTime failed!");
		int begin = matcher.end(), end = 0;
		pattern = Pattern
				.compile("<li[\\s\\S]*?data-dn=[\"']7d(\\d)[\"']\\s*?>"// rank
						+ "[\\s\\S]*?<h2>(\\d{1,2})[\\s\\S]+?</h2>"// date
						+ "[\\s\\S]*?<p[\\s\\S]*?>([\\s\\S]*?)</p>"// weather
						+ "[\\s\\S]*?<span>([\\d]{0,3}?)</span>[\\s\\S]*?<span>([\\d]{0,3}?)</span>"// temperature
						+ "[\\s\\S]*?<span[\\s\\S]*?\"([\\s\\S]*?)\"[\\s\\S]*?>"// windy1
						+ "[\\s\\S]*?[<span]{0,5}[\\s\\S]*?[\"']{0,1}([\\s\\S]*?)[\"']{0,1}[\\s\\S]*?[>]{0,1}"// windy2
						+ "[\\s\\S]*?<i>([\\s\\S]*?)</i>"// windy power
						+ "[\\s\\S]*?</li>");
		String text1 = text.substring(begin);
		matcher = pattern.matcher(text1);
		String curRank = null;
		System.out.println("weather and temperature...");
		int count = 0;
		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				if (i == 1) {
					curRank = matcher.group(1);
					status.put(curRank, new LinkedList<String>());
				} else
					status.get(curRank).addLast(matcher.group(i));
			}
			end = matcher.end();
			if (++count >= 7)
				break;
		}
		System.out.println("weather and tenperature finished!");
		text1 = text1.substring(end);
		pattern = Pattern.compile("<ul\\sclass=[\"']clearfix[\"']>");
		matcher = pattern.matcher(text1);
		if (matcher.find()) {
			begin = matcher.end();
			pattern = Pattern.compile("<li>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?<b>([\\s\\S]*?)</b>([\\s\\S]*?)</aside>"
					+ "[\\s\\S]*?</section>"
					+ "[\\s\\S]*?<a[\\s\\S]*?>([\\s\\S]*?)</a>[\\s\\S]*?</li>");
			matcher = pattern.matcher(text1.substring(begin));
			String item = null;
			System.out.println("quality...");
			count = 0;
			while (matcher.find()) {
				item = matcher.group(matcher.groupCount());
				for (int i = 1; i <= matcher.groupCount() / 2; i++) {
					status.get(rank[i - 1]).addLast(
							item + ":" + matcher.group(i * 2 - 1) + ":"
									+ matcher.group(i * 2));
				}
				if (++count >= 8)
					break;
			}
			System.out.println("quality finished!");
		}
	}

	public static void main(String[] Args) throws Exception {
		// CityWeather deal = new CityWeather("直辖市", "北京");
		// Hashtable<String, LinkedList<String>> ans = deal.getWeather();
		// for (String rank : ans.keySet()) {
		// System.out.print(rank + "  ");
		// for (String ele : ans.get(rank))
		// System.out.print(ele + "  ");
		// System.out.println();
		// }
		URL url = null;
		try {
			url = new URL(
					"http://api.map.baidu.com/geocoder/v2/?ak=MxmjAPGF18ssD5U5S91diWw8&"
					+ "callback=renderOption&output=json&address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStreamReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new InputStreamReader(url.openStream(), "utf-8");
			int len = 0;
			char buf[] = new char[1024];
			while ((len = reader.read(buf, 0, 1024)) != -1) {
				sb.append(buf, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sb.toString());

	}
}
