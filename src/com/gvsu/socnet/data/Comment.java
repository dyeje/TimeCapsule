/** Comment.java */
package com.gvsu.socnet.data;

import java.util.Calendar;

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
	private String leftTime;

	public Comment(/* ImageView pic, */String user, String body, String dateStamp) {
		// this.pic = pic;
		this.user = user;
		this.body = body;
		this.leftOn = makeSenseOf(dateStamp);
		this.leftTime = dateStamp;
	}

	/****************************************************************
	 * @param dateStamp string representation of date item was left
	 * @return Calendar object representation of date item was left
	 ***************************************************************/
	private Calendar makeSenseOf(String dateStamp) {
		String[] ds = dateStamp.split(" ");
		String date = ds[0];
		String time = ds[1];

		Calendar c = Calendar.getInstance();
		String[] dateArray = date.split("-");
		int year = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]);
		int day = Integer.parseInt(dateArray[2]);
		c.set(year, month, day);

		String[] timeArray = time.split(":");
		int hour = Integer.parseInt(timeArray[0]);
		int minute = Integer.parseInt(timeArray[1]);
		int second = Integer.parseInt(timeArray[2]);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);

		return c;
	}

	@Override
	public String toString() {
		String timeLeft = "";
		timeLeft += user + ":\t\t  ";
//		timeLeft += leftTime;
		timeLeft += leftOn.get(Calendar.MONTH) + "/" + leftOn.get(Calendar.DAY_OF_MONTH) + " " + leftOn.get(Calendar.HOUR) + ":" + leftOn.get(Calendar.MINUTE) + " "
		    + (leftOn.get(Calendar.AM_PM) == 0 ? "am" : "pm");
		timeLeft += "\n\t" + body;
		return timeLeft;
	}

}
