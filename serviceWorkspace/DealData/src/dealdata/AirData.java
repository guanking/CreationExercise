package dealdata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AirData {
	private final String cityName;
	private final String beginTime, endTime;
	private final Hashtable<String, LinkedList<String>> status;

	/**
	 * 
	 * @param cityName
	 *            cityName append "shi";
	 * @param beginTime
	 *            format:YYYY-MM-DD
	 * @param endTime
	 *            format:YYYY-MM-DD
	 * @throws Exception
	 */
	public AirData(String cityName, String beginTime, String endTime)
			throws Exception {
		Pattern pattern = Pattern.compile("^[0-9]{4}-[0-9]{1,2}-[0-9]{0,2}$");
		Matcher matcher = pattern.matcher(beginTime);
		if (!matcher.find())
			throw new Exception("wrong format : " + beginTime);
		matcher.reset(endTime);
		if (!matcher.find())
			throw new Exception("wrong format : " + endTime);
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.cityName = cityName;
		status = new Hashtable<String, LinkedList<String>>();
		dealURLContent(getContent());
	}

	protected String getContent() throws Exception {
		StringBuffer sb = new StringBuffer();
		URL url = null;
		try {
			url = new URL(
					"http://datacenter.mep.gov.cn/report/air_daily/air_dairy.jsp");
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);
			OutputStreamWriter writer = new OutputStreamWriter(
					connection.getOutputStream(), "utf-8");
			writer.write("city=" + URLEncoder.encode(cityName, "UTF-8")
					+ "&startdate=" + beginTime + "&enddate=" + endTime);
			writer.flush();
			writer.close();
			connection.connect();
			if (connection.getResponseCode() == 200) {
				InputStreamReader reader = new InputStreamReader(
						connection.getInputStream(), "utf-8");
				char[] buf = new char[1024];
				int len = 0;
				while ((len = reader.read(buf, 0, 1024)) != -1) {
					sb.append(buf, 0, len);
				}
				reader.close();
			} else {
				throw new Exception(connection.getResponseMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private void dealURLContent(String text) {
		Pattern pattern = Pattern
				.compile("<tr[\\s\\S]*?>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\d]+?)</td>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\s\\S]+?)</td>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\d]{4}-[\\d]{1,2}-[\\d]{1,2})</td>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\d]{1,10})</td>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\s\\S]*?)</td>"
						+ "[\\s\\S]*?<td[\\s\\S]*?>([\\s\\S]*?)</td>"
						+ "[\\s\\S]*?</tr>");
		Matcher matcher = pattern.matcher(text);
		LinkedList<String> temp = null;
		while (matcher.find()) {
			temp = new LinkedList<String>();
			status.put(matcher.group(3), temp);// time
			temp.addLast(matcher.group(4));// AQI
			temp.addLast(matcher.group(5));// air quality
			temp.addLast(matcher.group(6));// pollution
		}
	}

	public void show() {
		for (String time : status.keySet()) {
			System.out.print(time + " : ");
			for (String ele : status.get(time))
				System.out.print(ele + " ");
			System.out.println();
		}
	}

	/**
	 * @return Hashtable<key,value>
	 * @key the time
	 * @value a linkedList containing strings
	 * @first_value AQI
	 * @second_value Air Quality
	 * @third_value Main Pollution
	 */
	public Hashtable<String, LinkedList<String>> getResult() {
		return status;
	}
	public static class CityStatus{
		public String name;
		public String date;
		public String weather;
	}
	public static void main(String[] Args) throws Exception {
		AirData d = new AirData("ºªµ¦ÊÐ", "2015-08-27", "2015-09-29");
		d.show();
	}
}
