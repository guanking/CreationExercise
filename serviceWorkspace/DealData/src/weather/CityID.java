package weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CityID {
	public static class Detail {
		public String region;
		public String ID;

		public Detail(String region, String ID) {
			this.ID = ID;
			this.region = region;
		}

		public String toString() {
			return region +"\",\"" + ID;
		}
	}

	public static void setFilePath(String path) {
		CityID.path = path;
	}

	private static String path = null;
	private static Hashtable<String, Hashtable<String, LinkedList<Detail>>> IDS = new Hashtable<String, Hashtable<String, LinkedList<Detail>>>();

	private static void init() throws Exception {
		if (path == null) {
			throw new Exception("未设置数据文件路径");
		}
		String text = getContent(new File(path));
		Pattern pattern = Pattern.compile("<tr[\\s\\S]*?"
				+ "<td>([A-Z]{2}[\\d]+?)</td>" + "<td>[\\s\\S]+?</td>"
				+ "<td>([\\s\\S]+?)</td>" + "<td>([\\s\\S]+?)</td>"
				+ "<td>([\\s\\S]+?)</td>" + "[\\s\\S]*?</tr>");
		Matcher matcher = pattern.matcher(text);
		int begin = 0;
		Detail t = null;
		String provinceName = null, cityName = null;
		while (matcher.find(begin)) {
			t = new Detail(matcher.group(2), matcher.group(1));
			provinceName = matcher.group(4);
			cityName = matcher.group(3);
			if (!IDS.keySet().contains(provinceName)) {
				Hashtable<String, LinkedList<Detail>> temp = new Hashtable<String, LinkedList<Detail>>();
				LinkedList<Detail> tid = new LinkedList<CityID.Detail>();
				tid.add(t);
				temp.put(cityName, tid);
				IDS.put(provinceName, temp);
			} else {
				Hashtable<String, LinkedList<Detail>> temp = IDS
						.get(provinceName);
				if (temp.keySet().contains(cityName)) {
					temp.get(cityName).add(t);
				} else {
					LinkedList<Detail> tid = new LinkedList<CityID.Detail>();
					tid.add(t);
					temp.put(cityName, tid);
				}
			}
			begin = matcher.end();
		}

	}

	public static void show() {
		for (String cityName : IDS.keySet()) {
			for (String origon : IDS.get(cityName).keySet()) {
				for (Detail detail : IDS.get(cityName).get(origon)) {
					System.out.println("{\""+cityName + "\",\"" + origon + "\",\""
							+ detail.toString()+"\"},");
				}
			}
		}
	}

	public static String query(String provinceName, String cityName,
			String sightName) {
		if (provinceName == null || cityName == null) {
			return null;
		}
		provinceName = provinceName.replace("省", "");
		cityName = cityName.replace("市", "");
		if (sightName == null) {
			return IDS.get(provinceName).get(cityName).get(0).toString();
		} else {
			for (Detail det : IDS.get(provinceName).get(cityName)) {
				if (det.region.contains(sightName)) {
					return det.ID;
				}
			}
		}
		if (IDS.get(provinceName).get(cityName) != null) {
			return IDS.get(provinceName).get(cityName).toString();
		}
		return null;
	}

	public static String getContent(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		FileReader reader = new FileReader(file);
		int len = 0;
		char buf[] = new char[1024];
		while ((len = reader.read(buf, 0, 1024)) != -1) {
			sb.append(buf, 0, len);
		}
		reader.close();
		return sb.toString();
	}

	public static String request(String cityID) {
		BufferedReader reader = null;
		String httpUrl = "http://apis.baidu.com/heweather/weather/free";
		String result = null;
		StringBuffer sbf = new StringBuffer();
		httpUrl = httpUrl + "?" + "cityid=CN101210101";
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("apikey",
					"1c603d4075226f05cce567371339c5dc");
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
			}
			reader.close();
			return sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class Weather {
		public String province, city, county;
		private String detail;
		private String latitude, logitude;

		public Weather(String province, String city, String county) {
			this.province = province;
			this.city = city;
			this.county = county;
			detail = request(query(province, city, county));
		}

		public double[] getLocation() {
			return new double[] { Double.parseDouble(logitude),
					Double.parseDouble(latitude) };
		}

		public Calendar getUpdateTime() {
			Calendar cal = Calendar.getInstance();
			return cal;
		}

		public Weather(String province, String city) {
			this.province = province;
			this.city = city;
			detail = request(query(province, city, null));
		}

		public void show() {
			JSONObject obj = JSONObject.fromObject(detail);
			showJsonObject(obj, 0);
			System.out.println(detail);
			obj=(JSONObject) obj.getJSONArray("HeWeather data service 3.0").get(0);
			System.out.println(obj.getJSONObject("basic").getDouble("lat")+" "+obj.getJSONObject("basic").getDouble("lon"));
			if(obj.getJSONObject("aqi")!=null){
				System.out.println(obj.getJSONObject("aqi").getJSONObject("city").getInt("pm25"));
			}
		}

		void showJsonObject(Object obj, int spaceCount) {
			if (obj instanceof JSONArray) {
				JSONArray a = (JSONArray) obj;
				for (Object ele : a) {
					showJsonObject((JSONObject) ele, spaceCount + 1);
				}
			} else {
				JSONObject jsonObj = (JSONObject) obj;
				for (Object key : jsonObj.keySet()) {
					int count = spaceCount;
					while (count-- > 0) {
						System.out.print("      ");
					}
					System.out.print(key + " : ");
					if (jsonObj.get(key) instanceof JSONObject
							|| jsonObj.get(key) instanceof JSONArray) {
						System.out.println();
						showJsonObject(jsonObj.get(key), spaceCount + 1);
						continue;
					}
					System.out.println(jsonObj.get(key));
				}
			}
		}
	}

	public static void main(String[] Args) throws Exception {
		setFilePath("F:\\CreationExercises\\serviceWorkspace\\DealData\\src\\weather\\cityID.txt");
		init();
		Weather w = new Weather("浙江", "绍兴", "上虞");
		w.show();
	}
}
