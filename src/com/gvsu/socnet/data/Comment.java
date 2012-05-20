/** Comment.java */
package com.gvsu.socnet.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import android.util.Log;

/****************************************************************
 * data.Comment
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class Comment {

	// private ImageView pic;
	private String user;
	private String body;
	private Calendar leftOn;

	// private String leftTime;

	public Comment(/* ImageView pic, */String user, String body, String dateStamp) {
		// this.pic = pic;
		this.user = user;
		this.body = body;
		this.leftOn = makeSenseOf(dateStamp);
		// this.leftTime = dateStamp;
	}

	/****************************************************************
	 * @param dateStamp string representation of date item was left
	 * @return Calendar object representation of date item was left
	 ***************************************************************/
	public static GregorianCalendar makeSenseOf(String dateStamp) {
		String[] ds = dateStamp.split(" ");
		String date = ds[0];
		String time = ds[1];

		String[] dateArray = date.split("-");
		int year = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]) - 1;// because Java Calendar is inconsistent
		int day = Integer.parseInt(dateArray[2]);
		// c.set(year, month, day);

		String[] timeArray = time.split(":");
		int hour = Integer.parseInt(timeArray[0]);
		int minute = Integer.parseInt(timeArray[1]);
		int second = Integer.parseInt(timeArray[2]);
		// c.set(Calendar.HOUR_OF_DAY, hour);
		// c.set(Calendar.MINUTE, minute);
		// c.set(Calendar.SECOND, second);

		// get the supported ids for GMT-08:00 (Pacific Standard Time)
		String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
		// if no ids were returned, something is wrong. get out.
		if (ids.length == 0)
			System.exit(0);

		// begin output
		System.out.println("Current Time");

		// create a Pacific Standard Time time zone
		SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);

		// set up rules for daylight savings time
		pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
		pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

		// create a GregorianCalendar with the Pacific Daylight time zone
		// and the current date and time
		GregorianCalendar c = new GregorianCalendar(pdt);
		Date trialTime = new Date();
		c.setTime(trialTime);
		c.set(year, month, day);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);

		// GregorianCalendar c = new GregorianCalendar(year, month, day, hour, minute, second);
		Log.i("comment", "dow=" + c.get(Calendar.DAY_OF_WEEK));
		Log.i("comment", "madeSenseOf " + dateStamp + ":=" + c.toString());
		return c;
	}

	@Override
	public String toString() {
		String timeLeft = "";
		timeLeft += user + ":\t\t  ";
		timeLeft += (leftOn.get(Calendar.MONTH) + 1);// because Calendar is inconsistent...
		timeLeft += "/" + leftOn.get(Calendar.DAY_OF_MONTH);
		int hour = leftOn.get(Calendar.HOUR);
		if (hour == 0)
			hour = 12;
		int minute = leftOn.get(Calendar.MINUTE);
		String sminute = "";
		if (minute < 10)
			sminute = "0" + minute;
		else
			sminute = "" + minute;
		timeLeft += " " + hour + ":" + sminute;
		timeLeft += (leftOn.get(Calendar.AM_PM) == 0 ? "am" : "pm");
		timeLeft += "\n\t" + body;
		return timeLeft;
	}

}
