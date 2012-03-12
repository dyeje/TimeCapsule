/** FilterActivity.java */
package com.gvsu.socnet.map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import soc.net.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gvsu.socnet.views.RangeSeekBar;

/****************************************************************
 * com.gvsu.socnet.map.FilterActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class FilterActivity extends Activity implements
    RangeSeekBar.OnRangeSeekBarChangeListener<Long> {

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
	    "MM/dd/yyyy");
	private Long minDate = 0L;
	private Long maxDate = 0L;

	public static final String START_RANGE = "date_range_start";
	public static final String END_RANGE = "date_range_end";
	public static final String MIN_RATING = "min_capsule_rating";

	/****************************************************************
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * @param savedInstanceState
	 ***************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.map_filter);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Log.d("debug",
		    cal.getTimeInMillis() + " " + cal.get(Calendar.MONTH));
//		Date minDate = new Date(1333252800437L); // March 1 2012
		Date minDate = new Date(cal.getTimeInMillis()); // March 1 2012
		Log.d("debug",
		    cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH)
		        + "/" + cal.get(Calendar.DAY_OF_MONTH));
		

//		cal.setTimeInMillis(1333252800437L);
//		Log.d("debug",
//		    cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH)
//		        + "/" + cal.get(Calendar.DAY_OF_MONTH));

		Date maxDate = new Date();
		

		SharedPreferences prefs = PreferenceManager
		    .getDefaultSharedPreferences(getApplicationContext());
		this.minDate = prefs.getLong(START_RANGE, minDate.getTime());
		this.maxDate = prefs.getLong(END_RANGE, maxDate.getTime());
		RatingBar bar = (RatingBar) findViewById(R.id.rating_bar);
		bar.setRating(prefs.getFloat(MIN_RATING, 1));

		RangeSeekBar<Long> seekBar = new RangeSeekBar<Long>(
		    minDate.getTime(), maxDate.getTime(), getApplication());
		seekBar.setOnRangeSeekBarChangeListener(this);
		seekBar.setId(123456);
		//sets up initial seekbar values
		seekBar.setSelectedMinValue(this.minDate);
		seekBar.setSelectedMaxValue(this.maxDate);
		//notifies UI of initial values
		onRangeSeekBarValuesChanged(seekBar, this.minDate,
			this.maxDate);

		// add RangeSeekBar to pre-defined layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.seek_bar_layout);
		layout.addView(seekBar);

		seekBar.setNotifyWhileDragging(true);

		
		
		((Button)findViewById(R.id.button_set_rating_0)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				((RatingBar)findViewById(R.id.rating_bar)).setRating(0);
				
			}
		});
		
		super.onCreate(savedInstanceState);
	}

	/****************************************************************
	 * @see android.app.Activity#onDestroy()
	 ***************************************************************/
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/****************************************************************
	 * @see android.app.Activity#onPause()
	 ***************************************************************/
	@Override
	protected void onPause() {
		SharedPreferences.Editor edit = PreferenceManager
		    .getDefaultSharedPreferences(getApplicationContext())
		    .edit();
		edit.putLong(START_RANGE, minDate);
		edit.putLong(END_RANGE, maxDate);
		RatingBar bar = (RatingBar) findViewById(R.id.rating_bar);
		float rating = bar.getRating();
		edit.putFloat(MIN_RATING, rating);
		edit.commit();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(minDate);
		String minDate = dateFormat.format(new Date(cal.getTimeInMillis()));
		String maxDate = dateFormat.format(new Date(cal.getTimeInMillis()));
		Log.d("debug", "saved stuff:: startDate: " + minDate
		    + " endDate: " + maxDate + " minRating: " + rating);
		super.onPause();
	}

	/****************************************************************
	 * @see android.app.Activity#onRestart()
	 ***************************************************************/
	@Override
	protected void onRestart() {
		super.onRestart();
	}

	/****************************************************************
	 * @see android.app.Activity#onResume()
	 ***************************************************************/
	@Override
	protected void onResume() {
		super.onResume();
	}

	/****************************************************************
	 * @see android.app.Activity#onStart()
	 ***************************************************************/
	@Override
	protected void onStart() {
		super.onStart();
	}

	/****************************************************************
	 * @see android.app.Activity#onStop()
	 ***************************************************************/
	@Override
	protected void onStop() {
		super.onStop();
	}

	/****************************************************************
	 * @see com.gvsu.socnet.views.RangeSeekBar.OnRangeSeekBarChangeListener#onRangeSeekBarValuesChanged(com.gvsu.socnet.views.RangeSeekBar, java.lang.Object, java.lang.Object)
	 * @param bar
	 * @param minValue
	 * @param maxValue
	 ***************************************************************/
	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
	    Long minValue, Long maxValue) {
		// handle changed range values
		minDate = minValue;
		maxDate = maxValue;

		TextView from = (TextView) findViewById(R.id.text_from);
		from.setText(dateFormat.format(new Date(minValue)));
		TextView to = (TextView) findViewById(R.id.text_to);
		to.setText(dateFormat.format(new Date(maxValue)));
//		Log.i("debug", "User selected new date range: MIN="
//		    + new Date(minValue) + ", MAX=" + new Date(maxValue));

	}
}
