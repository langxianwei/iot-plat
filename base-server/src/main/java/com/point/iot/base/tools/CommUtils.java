package com.point.iot.base.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.point.iot.base.mysql.jdbc.DBConnectionManager;

public class CommUtils {
	static Logger logger = Logger.getLogger(CommUtils.class);

	public static final boolean DEPLOY_SERVER = !((System.getProperties().getProperty("os.name")).indexOf("Windows")>=0);
	/**
	 * int转byte
	 * @param value
	 * @return
	 */
	public static byte[] intToBytes(int value)   
	{   
	    byte[] byte_src = new byte[4];  
	    byte_src[3] = (byte) ((value & 0xFF000000)>>24);  
	    byte_src[2] = (byte) ((value & 0x00FF0000)>>16);  
	    byte_src[1] = (byte) ((value & 0x0000FF00)>>8);    
	    byte_src[0] = (byte) ((value & 0x000000FF));          
	    return byte_src;  
	}  
	/** 
     * 将short转成byte[2] 
     * @param a 
     * @param b 
     * @param offset b中的偏移量 
     */  
    public static byte[] short2Byte(short a){  
        byte[] b = new byte[2];  
          
        b[0] = (byte) (a >> 8);  
        b[1] = (byte) (a);  
          
        return b;  
    }
	/**
	 * 把低位在前的int转换为高位在前，低位在后 如ABCD 解析为DCBA
	 * @param a
	 * @return
	 */
	public static int highAndLowAddressSwap(int a){
		int b = (a & 0x000000ff)<<24;
		int c = (a & 0x0000ff00)<<8;
		int d = (a & 0x00ff0000)>>8;
		int e = (a & 0xff000000)>>24;
		return b + c + d + e;
	}

	public static String formatDouble(double i){
		String result = String .format("%.3f",i);
		return result;
	}
	public static void main(String args[]){//   
		int a = highAndLowAddressSwap(0x1C000000);
		System.out.println(a);
		
	}
	/**
	 * 替换参数{:key} 
	 * @param pattern
	 * @param arguments
	 * @return
	 */
	public static String format(String pattern, Map<String, Object> arguments) {
		String formatedStr = pattern;
		for (String key : arguments.keySet()) {
			String replacement = "\\{:" + key + "\\}";
			formatedStr = formatedStr.replaceAll(replacement, arguments.get(key)
					.toString());
			System.out.println(replacement + arguments.get(key)
					.toString());
		}
		return formatedStr;
	}
	
	public synchronized static String getTimeStamp() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = new Date();
		return formatter.format(date);
	}

	public static void printStacktrace(StackTraceElement[] stackTrackElements) {
		logger.error(Arrays.toString(stackTrackElements));
	}

	public static String getSessionAddress(IoSession session){
		if ( session == null ){
			return "" ;
		}
		if ( session.getRemoteAddress() == null || "".equals(session.getRemoteAddress())){
			return "" ;
		}
		String sessionAddress = session.getRemoteAddress().toString();
		sessionAddress = sessionAddress.substring(0, sessionAddress
				.indexOf(":"));
		return sessionAddress ;
	}
	/**
	 * 读取/proc/stat 获取cpu占用率 ; Linux
	 * 
	 * @return float efficiency
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static float getCpuInfo() {
		File file = new File("/proc/stat");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringTokenizer token = new StringTokenizer(br.readLine());
			token.nextToken();
			long user1 = Long.parseLong(token.nextToken());
			long nice1 = Long.parseLong(token.nextToken());
			long sys1 = Long.parseLong(token.nextToken());
			long idle1 = Long.parseLong(token.nextToken());

			Thread.sleep(1000);

			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			token = new StringTokenizer(br.readLine());
			token.nextToken();
			long user2 = Long.parseLong(token.nextToken());
			long nice2 = Long.parseLong(token.nextToken());
			long sys2 = Long.parseLong(token.nextToken());
			long idle2 = Long.parseLong(token.nextToken());

			return (float) ((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float) ((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(br != null)
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * 根据网卡取本机配置的IP 如果是双网卡的，则取出外网IP
	 * 
	 * @return
	 */
	public static String getLocalIp() {
		String localip = null;// 返回它
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			boolean finded = false;// 是否找到外网IP
			while (netInterfaces.hasMoreElements() && !finded) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();
				while (address.hasMoreElements()) {
					ip = address.nextElement();
					if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
						localip = ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return localip;
	}

	public static Date dateMulti(Date date, int milliseconds ){
		Date newdate = new Date(date.getTime() + milliseconds) ;
		return newdate ;
		
	}
	/**
	 * 根据日期获得星期
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeekOfDate(Date date) {
		int[] weekDaysCode = { 1, 2, 3, 4, 5, 6, 7 };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return weekDaysCode[intWeek];
	}

	/**
	 * 根据日期获得星期
	 * 
	 * @param date
	 * @return
	 */
	public static String getWeekNameOfDate(Date date) {
		String[] weekDaysName = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return weekDaysName[intWeek];
	}
	
	/**
	 * 获取下一个月的timestamp时间
	 * @param timestamp 时间戳
	 * @return
	 */
	public static long getNextDateOfMonth(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		Date date = new Date(timestamp);
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	public static long getLastDateOfMonth(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		Date date = new Date(timestamp);
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);
		return calendar.getTimeInMillis();
	}
	
	public static long getDateOfCurrentMonth(long timestamp) {
//		Calendar calendar = Calendar.getInstance();
//		Date date = new Date(timestamp);
//		calendar.setTime(date);
		String strDate = CommUtils.parseDate(timestamp);
		String strTime = strDate.substring(8);
		String strDate2 = CommUtils.parseDate(System.currentTimeMillis());
		String strTime2 = strDate2.substring(0, 8);
		
		return CommUtils.parseDate(strTime2 + strTime).getTime();
	}

	/**
	 * 获取本周周几的日期
	 * 
	 * @param date
	 * @param nDate
	 * @return
	 */
	public static long getDateOfWeek(long startTime, int nDate) {
		long lstartTime = getCurrTimestampFromHistoryTimestamp(startTime);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(lstartTime));
		c.set(Calendar.DAY_OF_WEEK, nDate);
		Date newDate = c.getTime();
		if (c.getTime().getTime() < System.currentTimeMillis()) {
			long l = c.getTime().getTime() + 7 * 24 * 60 * 60 * 1000;
			newDate = new Date(l);
		}
		return newDate.getTime();
	}

	/**
	 * 把毫秒数转化为*天*小时*分*秒
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return
	 */
	public static String formatMilliSecond(long startDateTime, long endDateTime) {
		long between = (endDateTime - startDateTime) / 1000;// 除以1000是为了转换成秒
		long day1 = between / (24 * 3600);
		long hour1 = between % (24 * 3600) / 3600;
		long minute1 = between % 3600 / 60;

		String msg = "";
		if (day1 > 0) {
			msg = msg + day1 + "天";
		}
		if (hour1 > 0) {
			msg = msg + hour1 + "小时";
		}
		if (minute1 > 0) {
			msg = msg + minute1 + "分钟";
		}
		return msg;
	}
	
	/**
	 * 	
	 * @param date 参照时间
	 * @param hour 固定小时
	 * @param minute 固定分
	 * @param second 固定秒
	 * @return
	 */
	public static long getNextDaySecond(Date date,int hour,int minute) {
		long between = 0;
		SimpleDateFormat formatter1 = new SimpleDateFormat("HH");//小时
		SimpleDateFormat formatter2 = new SimpleDateFormat("mm");//分钟
		int hh = Integer.parseInt(formatter1.format(date));
		int mm = Integer.parseInt(formatter2.format(date));
		between = (hour - hh) * 60 * 60 + (minute - mm) * 60 ;
		if(between <= 0) {//当前时间比下一个固定时间要晚，就计算下一天的当前时间
			between += 24 * 60 * 60;
		}
		return between;
	}
	
	/**
	 * 获得当前时间到下一个整点小时中间的秒数
	 * @param date
	 * @return
	 */
	public static long getNextHourSecond(Date date) {
		long second = 0;
		SimpleDateFormat formatter1 = new SimpleDateFormat("mm");//分钟
		SimpleDateFormat formatter2 = new SimpleDateFormat("ss");//秒
		int mm = Integer.parseInt(formatter1.format(date));
		int ss = Integer.parseInt(formatter2.format(date));
		second = 60 * 60 - mm*60 - ss;
		return second;
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		if (strDate == null || "".equals(strDate)) {
			return new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	
	/**
	 * 指定日期的后一天
	 * @param date
	 * @return
	 */
	public static String getSpecifiedDayAfter(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + 1);

		String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c
				.getTime());
		return dayBefore;
	}
	
	/**
	 * 指定日期的前或者后N天
	 * @param date
	 * @return
	 */
	public static Date getDateAddition(Date date, int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day + num);

		return c.getTime();
	}
	/**
	 * 指定日期的前一天
	 * @param date
	 * @return
	 */
	public static String getSpecifiedDayBefore(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - 1);

		String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c
				.getTime());
		return dayBefore;
	}
	/**
	 * 指定日期的前一天
	 * @param date
	 * @return
	 */
	public static String getYestoday(Date date,String formatter) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - 1);

		String dayBefore = new SimpleDateFormat(formatter).format(c
				.getTime());
		return dayBefore;
	}
	/**
	 * 用历史时间组合为 本日 yyyy-MM-dd 加历史的 HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static long getCurrTimestampFromHistoryTimestamp(long lDate) {
		long timestamp = 0;
		// 每日重启后开赛时间变为今天得日期+定点比赛的开始时间+开赛的循环周期
		if (lDate != 0) {
			Date date = new Date(lDate);
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 小写的mm表示的是分钟
			String sCurrentDate = format1.format(new Date());
			String sMatchStartDate = format2.format(date);

			try {
				date = sdf.parse(sCurrentDate + " " + sMatchStartDate);
				timestamp = date.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return timestamp;
	}

	/**
	 * 获取最近的比赛开始时间
	 * @param l1 比赛开始时间
	 * @param l2 比赛结束时间
	 * @param nCycle 小循环 30 分钟一场
	 * @param nBigCycle 大循环 比如：1天
	 * @return
	 */
	public static long getLatestGameStartTime(long l1, long l2, int nCycle, int nBigCycle) {
		long cur = System.currentTimeMillis();
		int n = (int) (cur - l1) / nCycle;
		int dist = nCycle - ((int) (cur - l1) % nCycle);
		long latest = l1;
		System.out.println(dist);
		if (cur > l2) {
			return l1 + nBigCycle;
		} else if (cur < l1) {
			return l1;
		}
		if (dist >= 2 * 60 * 1000) {
			if ((l2 - cur) > nCycle) {
				latest = l1 + (n + 1) * nCycle;
			} else {
				System.out.println(1);
				latest = l2;
			}
		} else {
			if ((l2 - cur) >= nCycle) {
				latest = l1 + (n + 2) * nCycle;
			} else if ((l2 - cur) > 0 && (l2 - cur) < nCycle) {
				latest = l1 + (n + 2) * nCycle;
			}
		}
		return latest;
	}

	/**
	 * 是否同一天。同一天返回true ，否则返回false
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static boolean isTheSameDay(long l1, long l2) {
		if (l1 != 0 && l2 != 0) {
			Date d1 = new Date(l1);
			Date d2 = new Date(l2);
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			String s1 = sf.format(d1);
			String s2 = sf.format(d2);
			return s1.equals(s2);
		}
		return false;
	}

	public static Date strToDate(String strDate, String format) {
		if (strDate == null || "".equals(strDate)) {
			return new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date parseDate(String strDate) {
		if (strDate == null || "".equals(strDate)) {
			return new Date();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}
	
	/**
	 * 返回时间
	 * @param strDate
	 * @param format
	 * @return
	 */
	public static Date parseDate(String strDate, String format) {
		if (strDate == null || "".equals(strDate)) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 按照formater的格式，格式化date
	 * 
	 * @param date
	 * @param formater
	 * @return
	 */
	public static String formatDate(Date date, String formater) {
		SimpleDateFormat formatter = new SimpleDateFormat(formater);
		return formatter.format(date);
	}

	public static String parseDate(long dateTime) {
		if (dateTime == 0) {
			return "";
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return formatter.format(new Date(dateTime));

	}

	/**
	 * byte Array 转换为Int
	 * 
	 * @param bytes
	 * @return
	 */
	public static int byteArrayToInt(byte[] bytes) {

		int nTemp, nResult;

		nResult = 0;

		nTemp = bytes[0] & 0xFF;

		nTemp = nTemp << 24;

		nResult = nResult | nTemp;

		nTemp = bytes[1] & 0xFF;

		nTemp = nTemp << 16;

		nResult = nResult | nTemp;

		nTemp = bytes[2] & 0xFF;

		nTemp = nTemp << 8;

		nResult = nResult | nTemp;

		nTemp = bytes[3] & 0xFF;

		nResult = nResult | nTemp;

		return nResult;

	}

	/**
	 * int to byte数组
	 * 
	 * @param integer
	 * @return
	 */
	public static byte[] intToByteArray(final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	/**
	 * 将短时间格式时间转换为字符串 yyyy-MM-dd
	 * 
	 * @param dateDate
	 * @param k
	 * @return
	 */
	public static String dateToStr(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	public static String timestampToStr(long n) {
		Date date = new Date(n);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	
	public static String timestampToStr2(long n) {
		Date date = new Date(n);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
		return formatter.format(date);
	}
	
	public static String timestampToStr3(long n) {
		Date date = new Date(n);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd ");
		return formatter.format(date);
	}

	public static int parseInt(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return (int) Double.parseDouble(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	public static double parseDouble(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return Double.parseDouble(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}
	public static float parseFloat(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return Float.parseFloat(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}	
	public static short parseShort(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return (short) Double.parseDouble(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	public static byte parseByte(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return (byte) Double.parseDouble(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	public static long parseLong(String s) {
		try {
			if (s != null && !"".equals(s)) {
				return (long) Double.parseDouble(s);
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	/**
	 * 获得概率
	 * 
	 * @param probability
	 *            想要得到的概率，例如 probability = 20，则表示取得的概率为20%
	 * @return true，false
	 *         如果为true，则表示取得了probability相对应的概率；如果为false，则表示得到了与probability相反的概率
	 *         例如： probability = 20时，如果返回true，表示取得的概率为20%,返回false表示取得的概率为80%
	 */
	public static boolean getProbability(int probability) {
		// 20%,25,40,50,60,70,80
		Random random = new Random();
		int ran = Math.abs((random.nextInt())) % 100;

		if (ran < probability) {
			return true;
		} else {
			return false;
		}
	}

	public static int getProbability(List<Integer> probability) {
		Random random = new Random();
		int ran = random.nextInt(100);
		int min = 0;
		int max = 0;
		for (Iterator iterator = probability.iterator(); iterator.hasNext();) {
			Integer integer = (Integer) iterator.next();
			max += integer;
			if (min <= ran && ran < max) {
				return integer;
			} else {
				min += max;
			}

		}
		return 0;
	}
	/**
	 * 取得区间随机数
	 * 
	 * @param minRange
	 *            区间最小数
	 * @param maxRange
	 *            区间最大数
	 * @return minRange与maxRange之间的一个随机数（闭区间）
	 */
	public static int getRangeRandom(int minRange, int maxRange) {
		Random random = new Random();
		int n = random.nextInt(maxRange - minRange + 1) + minRange;
		return n;
	}


	/**
	 * 父类赋值给子类
	 * 
	 * @param fatherObj
	 * @param childObj
	 * @return
	 */
	public static Object converce(Object fatherObj, Object childObj) {
		Field[] fatherFields = fatherObj.getClass().getFields();
		Field[] childFields = childObj.getClass().getFields();
		try {
			for (Field fatherField : fatherFields) {
				for (Field childField : childFields) {
					if (childField.getName().equalsIgnoreCase(fatherField.getName())) {
						childField.set(childObj, fatherField.get(fatherObj));
					}
				}
			}
		} catch (Exception e) {
			return childObj;
		}
		return childObj;
	}
	/**
	 * 数字转换成中文130010
	 * 
	 * @param num
	 * @return
	 */
	public static String conventNumber(int num) {
		String str = "";
		String strNum = String.valueOf(num);
		int strLen = strNum.length();
		String[] cn = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
		String[] dw = { "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿" };
		if (strLen == 1) {
			return cn[num];
		}
		int zeroCnt = 0;
		for (int i = 0; i < strLen; i++) {
			int n = Integer.parseInt(strNum.substring(i, i + 1));
			if (strLen - i > 0) {
				if (n == 0) {
					zeroCnt++;
				} else {
					if (zeroCnt > 0) {
						str += cn[0];
					}
					zeroCnt = 0;
					str += cn[n];
				}
				if (strLen - 2 - i >= 0 && n > 0) {
					str += dw[strLen - 2 - i];
				}
			}
		}
		return str;
	}

	/**
	 * 取得给定时间的下一天
	 * 
	 * @param strTime
	 * @return
	 */
	public static String getNextDay(String strTime) {
		Calendar cal = Calendar.getInstance();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = sdf.parse(strTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		cal.setTime(date);
		cal.add(Calendar.DATE, 1);
		strTime = sdf.format(cal.getTime());
		return strTime;
	}

	/**
	 * @param time
	 *            时间戳
	 * @param type
	 *            延迟类型 Calendar
	 * @param value
	 *            延迟时间
	 * @return
	 */
	public static long timeDelay(long time, int type, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.add(type, value);
		return calendar.getTimeInMillis();
	}

	public static String getVersionStr(int nVersionCode) {
		int nMainVer = (nVersionCode >> 24);
		int nSubVer = ((nVersionCode & 0x00FF0000) >> 16);
		return nMainVer + "." + nSubVer;
	}
	
	public static String getVersion(int nVersionCode) {
		int nMainVer = (nVersionCode >> 24);
		int nSubVer = ((nVersionCode & 0x00FF0000) >> 16);
		if ( nSubVer < 10 ){
			return nMainVer + "0" + nSubVer;
		}else{
			return nMainVer + "" + nSubVer;
		}
	}
	public static String getVersionWithPoint(int nVersionCode) {
		int nMainVer = (nVersionCode >> 24);
		int nSubVer = ((nVersionCode & 0x00FF0000) >> 16);
		if ( nSubVer < 10 ){
			return nMainVer + ".0" + nSubVer;
		}else{
			return nMainVer + "." + nSubVer;
		}
	}
	public static int[] byteArray2IntArray(byte[] ab) {
		int[] an = new int[ab.length];
		for (int i=0; i<an.length; i++) {
			an[i] = ab[i];
		}
		return an;
	}
	public static byte[] intArray2ByteArray(int[] an) {
		byte[] ab = new byte[an.length];
		for (int i=0; i<an.length; i++) {
			ab[i] = (byte)an[i];
		}
		return ab;
	}
	
	

	//本机时间误差
	private static long currentTimeMillisDiff;
	private static long lastRefreshMillisTime;
	/**
	 * 得到当前毫秒数
	 * @return
	 */
	public static long getCurrentTimeMillis() {
		if (System.currentTimeMillis() - lastRefreshMillisTime < 10 * 60000 && currentTimeMillisDiff != 0) {
			return System.currentTimeMillis() + currentTimeMillisDiff;
		}
		String sql = "select unix_timestamp() * 1000 as CurrentTimeMillis";
		DBConnectionManager ins = DBConnectionManager.getInstance() ;
		Connection conn = ins.getConnection("game") ;
		Statement st = null;
		long currTimeMillis = System.currentTimeMillis();
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if( rs.next() ){
				currTimeMillis = rs.getLong("CurrentTimeMillis");
			}
			rs.close() ;
		} catch (SQLException e) {
		}finally{
			if(st != null ){
				try {
					st.close() ;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ins.freeConnection("game", conn) ;
		}
		currentTimeMillisDiff = currTimeMillis - System.currentTimeMillis();
		lastRefreshMillisTime = System.currentTimeMillis();
		return currTimeMillis;
	}
	
    public static String post(String url,String xmlString){  
        int CONNECT_TIMEOUT = 10000;  
        int READ_TIMEOUT = 10000;  
      
    	HttpParams httpParams = new BasicHttpParams(); 
    	httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,  
                HttpVersion.HTTP_1_1);
    	httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
    	httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
    	httpParams.setParameter(CoreConnectionPNames.TCP_NODELAY, false);
    	HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost post = new HttpPost(url);
		byte[] reqBytes = xmlString.getBytes();
		HttpEntity entity = new ByteArrayEntity(reqBytes);
		post.setEntity(entity);
		InputStream in = null;
		try {
			HttpResponse response = httpClient.execute(post);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				throw new IllegalStateException("http response status:" + response.getStatusLine().getStatusCode());
			}
			HttpEntity responseEntity = response.getEntity();
			in = responseEntity.getContent();
			int len = (int) responseEntity.getContentLength();
			byte[] respBytes = new byte[len];
			byte[] tempBytes = new byte[256];
			int pos = 0;
			int readLen = 0;
			while((readLen = in.read(tempBytes, 0, tempBytes.length)) > 0){
				System.arraycopy(tempBytes, 0, respBytes, pos, readLen);
				pos += readLen;
			}
			String respStr = new String(respBytes);
			return respStr;
		} catch (Exception ex) {
			logger.error("HttpPost异常" + url + "\n" + xmlString, ex);
			return "";
		} finally{
			if(post != null){
				post.abort();
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	
    }
    
    /**
     * 版本号字符串转换为四字节数字型版本
     * 2.03 -> 0x02030000
     * 2.32.0.104 -> 0x02200068
     * @param versionStr
     * @return
     */
    public static int versionStr2VersionCode(String versionStr) {
    	if (versionStr == null || "".equals(versionStr)) {
    		return 0;
    	}
    	versionStr = versionStr.replace(".", "_");
    	String[] versionNumArray = versionStr.split("_");
    	int[] codeArr = new int[versionNumArray.length];
    	for (int i=0; i<versionNumArray.length; i++) {
    		codeArr[i] = Integer.parseInt(versionNumArray[i]);
    	}
    	
    	if (codeArr.length == 2) {
    		return (codeArr[0] << 24) + (codeArr[1] << 16);
    	}
    	if (codeArr.length == 4) {
    		return (codeArr[0] << 24) + (codeArr[1] << 16) + (codeArr[2] << 8) + (codeArr[3]);
    	}
    	return 0;
    }
    /**
     * Int型版本号转字符串
     * @param version
     * @return
     */
    public static String versionCode2VersionStr(int version) {
    	return ((version >> 24)&0xff) + "." + ((version >> 16)&0xff) + "." + ((version >> 8)&0xff) + "." + (version&0xff);
    }
    
	/**
	 * 得到本周一的时间戳
	 * @return
	 */
	public static long getCurrMondayTimeStamp() {
		Calendar clNow = Calendar.getInstance();
		int n = clNow.get(Calendar.WEEK_OF_YEAR);
		System.out.println(n);
		//
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(0);
		cl.set(Calendar.YEAR, clNow.get(Calendar.YEAR));
		cl.set(Calendar.MONTH, clNow.get(Calendar.MONTH));
		cl.set(Calendar.DATE , clNow.get(Calendar.DATE));
		cl.set(Calendar.HOUR, 0);
		//
		Calendar cl2 = Calendar.getInstance();
		cl2.setTimeInMillis(cl.getTimeInMillis());
		cl2.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cl2.getTimeInMillis();
	}
	
}