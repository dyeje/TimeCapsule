/** FilterActivity.java */
package com.gvsu.socnet.map;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import soc.net.R;

import com.gvsu.socnet.views.RangeSeekBar;

/****************************************************************
 * com.gvsu.socnet.map.FilterActivity
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class FilterActivity extends Activity implements RangeSeekBar.OnRangeSeekBarChangeListener<Long> {

  private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private long minDate = 0L;
  private long maxDate = 0L;
  private long millisInDay = 86400000L;

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

    GregorianCalendar min = new GregorianCalendar(2012, Calendar.MARCH, 1);
    Calendar cal = Calendar.getInstance();
    GregorianCalendar max = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    this.minDate = prefs.getLong(START_RANGE, min.getTimeInMillis());
    this.maxDate = prefs.getLong(END_RANGE, max.getTimeInMillis());
    RatingBar bar = (RatingBar) findViewById(R.id.rating_bar);
    bar.setRating(prefs.getFloat(MIN_RATING, 1));

    final RangeSeekBar<Long> seekBar = new RangeSeekBar<Long>(min.getTimeInMillis(), max.getTimeInMillis(), getApplication());
    seekBar.setOnRangeSeekBarChangeListener(this);
    seekBar.setId(123456);
    // sets up initial seekbar values
    seekBar.setSelectedMinValue(this.minDate);
    seekBar.setSelectedMaxValue(this.maxDate);
    // notifies UI of initial values
    onRangeSeekBarValuesChanged(seekBar, this.minDate, this.maxDate);

    // add RangeSeekBar to pre-defined layout
    LinearLayout layout = (LinearLayout) findViewById(R.id.seek_bar_layout);
    layout.addView(seekBar);

    seekBar.setNotifyWhileDragging(true);

    // setup quick date picking buttons
    ((Button) findViewById(R.id.btn_filter_today)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        seekBar.setSelectedMinValue(seekBar.getAbsoluteMaxValue() - millisInDay);
        seekBar.setSelectedMaxValue(seekBar.getAbsoluteMaxValue());
        onRangeSeekBarValuesChanged(seekBar, seekBar.getAbsoluteMaxValue() - millisInDay, seekBar.getAbsoluteMaxValue());

      }
    });
    ((Button) findViewById(R.id.btn_filter_anytime)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        seekBar.setSelectedMinValue(seekBar.getAbsoluteMinValue());
        seekBar.setSelectedMaxValue(seekBar.getAbsoluteMaxValue());
        onRangeSeekBarValuesChanged(seekBar, seekBar.getAbsoluteMinValue(), seekBar.getAbsoluteMaxValue());

      }
    });
    ((Button) findViewById(R.id.btn_filter_week)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        long newMin = seekBar.getAbsoluteMaxValue() - millisInDay * 7;
        if (newMin < seekBar.getAbsoluteMinValue())
          newMin = seekBar.getAbsoluteMinValue();
        seekBar.setSelectedMinValue(newMin);
        seekBar.setSelectedMaxValue(seekBar.getAbsoluteMaxValue());
        onRangeSeekBarValuesChanged(seekBar, newMin, seekBar.getAbsoluteMaxValue());

      }
    });
    ((Button) findViewById(R.id.btn_filter_month)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        long newMin = seekBar.getAbsoluteMaxValue() - millisInDay * 7 * 4;
        if (newMin < seekBar.getAbsoluteMinValue())
          newMin = seekBar.getAbsoluteMinValue();
        seekBar.setSelectedMinValue(newMin);
        seekBar.setSelectedMaxValue(seekBar.getAbsoluteMaxValue());
        onRangeSeekBarValuesChanged(seekBar, newMin, seekBar.getAbsoluteMaxValue());

      }
    });
    ((Button) findViewById(R.id.btn_filter_year)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        long newMin = seekBar.getAbsoluteMaxValue() - millisInDay * 7 * 52;
        if (newMin < seekBar.getAbsoluteMinValue())
          newMin = seekBar.getAbsoluteMinValue();
        seekBar.setSelectedMinValue(newMin);
        seekBar.setSelectedMaxValue(seekBar.getAbsoluteMaxValue());
        onRangeSeekBarValuesChanged(seekBar, newMin, seekBar.getAbsoluteMaxValue());

      }
    });

    // setup quick rating buttons
    ((Button) findViewById(R.id.button_set_rating_0)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ((RatingBar) findViewById(R.id.rating_bar)).setRating(0);

      }
    });
    ((Button) findViewById(R.id.button_set_rating_3)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ((RatingBar) findViewById(R.id.rating_bar)).setRating(3);

      }
    });
    ((Button) findViewById(R.id.button_set_rating_5)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ((RatingBar) findViewById(R.id.rating_bar)).setRating(5);

      }
    });
    ((Button) findViewById(R.id.map_filter_filter_button)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

        // fixes special cases
        if (minDate == maxDate) {
          // server will return nothing if start and end date are
          // the same. this will make sure the server returns
          // capsules occurring on the date asked for
          GregorianCalendar max = new GregorianCalendar();
          max.setTimeInMillis(maxDate);
          max.add(Calendar.DAY_OF_YEAR, 1);
          maxDate = max.getTimeInMillis();
        } else if (Math.abs(minDate - maxDate) == millisInDay) {

        } else if (minDate == Calendar.getInstance().getTimeInMillis()) {
          minDate -= millisInDay;
        } else if (maxDate == Calendar.getInstance().getTimeInMillis()) {
          maxDate += millisInDay;
        }
        edit.putLong(START_RANGE, minDate);
        edit.putLong(END_RANGE, maxDate);
        RatingBar bar = (RatingBar) findViewById(R.id.rating_bar);
        float rating = bar.getRating();
        edit.putFloat(MIN_RATING, rating);
        edit.commit();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(minDate);
        String minDate = dateFormat.format(new Date(cal.getTimeInMillis()));
        cal.setTimeInMillis(maxDate);
        String maxDate = dateFormat.format(new Date(cal.getTimeInMillis()));
        // Log.d("debug", "saved stuff:: startDate: " + minDate + " endDate: " + maxDate +
        // " minRating: " + rating);
        finish();

      }
    });
    ((Button) findViewById(R.id.map_filter_cancel_button)).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();

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
    super.onPause();
  }

  // public void onBackPressed() {
  // return;
  // }

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
  public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
    minDate = minValue;
    maxDate = maxValue;
    Log.d("filter", "Earliest Date: " + minDate % millisInDay + " Latest Date: " + maxDate % millisInDay);

    TextView from = (TextView) findViewById(R.id.text_from);
    from.setText(dateFormat.format(new Date(minDate)));
    TextView to = (TextView) findViewById(R.id.text_to);
    to.setText(dateFormat.format(new Date(maxDate)));

  }
}
