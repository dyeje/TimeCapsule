/** FilterActivity.java */
package com.gvsu.socnet.map;

import java.text.SimpleDateFormat;
import java.util.Date;

import soc.net.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
	    "MM-dd-yyyy");
	private Long minDate = 0L;
	private Long maxDate = 0L;

	public static final String START_RANGE = "date_range_start";
	public static final String END_RANGE = "date_range_end";
	public static final String MIN_RATING = "min_capsule_rating";

	/****************************************************************
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * @param savedInstanceState
	 ***************************************************************/
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.map_filter);

		Date minDate = new Date(1293858000000L); // Jan 1 2011
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
		seekBar.setSelectedMinValue(this.minDate);
		seekBar.setSelectedMaxValue(this.maxDate);

		// add RangeSeekBar to pre-defined layout
		LinearLayout layout = (LinearLayout) findViewById(R.id.seek_bar_layout);
		layout.addView(seekBar);

		onRangeSeekBarValuesChanged(seekBar, minDate.getTime(),
		    maxDate.getTime());
		seekBar.setNotifyWhileDragging(true);

		super.onCreate(savedInstanceState);
	}

	/****************************************************************
	 * @see android.app.Activity#onDestroy()
	 ***************************************************************/
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/****************************************************************
	 * @see android.app.Activity#onPause()
	 ***************************************************************/
	protected void onPause() {
		SharedPreferences.Editor edit = PreferenceManager
		    .getDefaultSharedPreferences(getApplicationContext())
		    .edit();
		edit.putLong("date_range_start", minDate);
		edit.putLong("date_range_end", maxDate);
		RatingBar bar = (RatingBar) findViewById(R.id.rating_bar);
		float rating = bar.getRating();
		edit.putFloat("min_capsule_rating", rating);
		edit.commit();
		Log.d("debug", "saved stuff:: startDate: " + minDate
		    + " endDate: " + maxDate + " minRating: " + rating);
		super.onPause();
	}

	/****************************************************************
	 * @see android.app.Activity#onRestart()
	 ***************************************************************/
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/****************************************************************
	 * @see android.app.Activity#onResume()
	 ***************************************************************/
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/****************************************************************
	 * @see android.app.Activity#onStart()
	 ***************************************************************/
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/****************************************************************
	 * @see android.app.Activity#onStop()
	 ***************************************************************/
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/****************************************************************
	 * @see com.gvsu.socnet.views.RangeSeekBar.OnRangeSeekBarChangeListener#onRangeSeekBarValuesChanged(com.gvsu.socnet.views.RangeSeekBar, java.lang.Object, java.lang.Object)
	 * @param bar
	 * @param minValue
	 * @param maxValue
	 ***************************************************************/
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
	    Long minValue, Long maxValue) {
		// handle changed range values
		minDate = minValue;
		maxDate = maxValue;

		TextView from = (TextView) findViewById(R.id.text_from);
		from.setText(dateFormat.format(new Date(minValue)));
		TextView to = (TextView) findViewById(R.id.text_to);
		to.setText(dateFormat.format(new Date(maxValue)));
		Log.i("debug", "User selected new date range: MIN="
		    + new Date(minValue) + ", MAX=" + new Date(maxValue));

	}
}
