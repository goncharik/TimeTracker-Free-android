package com.tulusha.timetracker;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import com.tulusha.timetracker.widgets.ActionBar;
import com.tulusha.timetracker.widgets.ActionBar.Action;

import java.util.Timer;
import java.util.TimerTask;

public class TrackerActivity extends Activity
{
	final static String WORKING_STATE_KEY = "WORKING_STATE_KEY";
	final static String RESTING_STATE_KEY = "RESTING_STATE_KEY";
	final static String RESTING_TIME_KEY = "RESTING_TIME_KEY";
	final static String WORKING_TIME_KEY = "WORKING_TIME_KEY";
    static final String LAST_TIME_KEY = "LAST_TIME_KEY";
    static final int NOTIFICATION_KEY = 0;
	
	Button workButton;
	Button restButton;
	Animation workMagnify;
	Animation workShrink;
	Animation restMagnify;
	Animation restShrink;
	TextView restTime;
	TextView workTime;
	
	Boolean resting;
	Boolean working;
	
	Timer timer;
	TimerTask counter;
	long workingTime;
	long restingTime;
    long lastTime;
	
	SharedPreferences.Editor statistic_editor;
	
	OnClickListener onRestPressed = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!resting)
			{
				restTime.startAnimation(restMagnify);
				resting = true;
			}
			else
			{
				restTime.startAnimation(restShrink);
				resting = false;
			}
			
			if (working)
			{
				workTime.startAnimation(workShrink);
				working = false;
			}

            workButton.setSelected(working);
			restButton.setSelected(resting);

            saveTimersState();
		}
	};
	
	/**
	 *  Executes on work button press;
	 */
	OnClickListener onWorkPressed = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!working)
			{
				workTime.startAnimation(workMagnify);
				working = true;
			}
			else
			{
				workTime.startAnimation(workShrink);
				working = false;
			}
			
			if (resting)
			{
				restTime.startAnimation(restShrink);
				resting = false;
			}
			
			workButton.setSelected(working);
			restButton.setSelected(resting);

            saveTimersState();
		}
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        
        statistic_editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_KEY);

        SharedPreferences saved_statistic = PreferenceManager.getDefaultSharedPreferences(this);
        resting = saved_statistic.getBoolean(RESTING_STATE_KEY, false);
        restingTime = saved_statistic.getLong(RESTING_TIME_KEY, 0);
        working = saved_statistic.getBoolean(WORKING_STATE_KEY, false);
        workingTime = saved_statistic.getLong(WORKING_TIME_KEY, 0);

        lastTime = (System.currentTimeMillis() - saved_statistic.getLong(LAST_TIME_KEY, System.currentTimeMillis())) / 1000;

        workButton = (Button)findViewById(R.id.work);
        workButton.setOnClickListener(onWorkPressed);
        
        restButton = (Button)findViewById(R.id.rest);
        restButton.setOnClickListener(onRestPressed);
        
        restTime = (TextView)findViewById(R.id.rest_time);
        workTime = (TextView)findViewById(R.id.work_time);
        
        restMagnify = AnimationUtils.loadAnimation(this, R.anim.magnify_rest);
        restShrink = AnimationUtils.loadAnimation(this, R.anim.shrink_rest);
        workMagnify = AnimationUtils.loadAnimation(this, R.anim.magnify_work);
        workShrink = AnimationUtils.loadAnimation(this, R.anim.shrink_work);
        
        counter = new TimerTask() {
			
			@Override
			public void run() {
				if (working)
				{
					workingTime++;
					updateWorkTimer();
				}
				else if (resting)
				{
					restingTime++;
					updateRestTimer();
				}
			}
		};
		
		timer = new Timer();
        timer.schedule(counter, 0, 1000);

        if (resting)
        {
            restTime.startAnimation(restMagnify);
            restingTime += lastTime;
        }
        else if (working)
        {
            workTime.startAnimation(workMagnify);
            workingTime += lastTime;
        }

        restTime.setText(getStringTimeFromSeconds(restingTime));
        workTime.setText(getStringTimeFromSeconds(workingTime));

        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("Tracker");

        actionBar.addAction(new Action()
        {
            @Override
            public void performAction(View view) {
                startActivity(createShareIntent());
            }
            @Override
            public int getDrawable() {
                return R.drawable.ic_title_share_default;
            }
        } );

        actionBar.setHomeAction(new ActionBar.IntentAction(this, HomeActivity.createIntent(this), R.drawable.ic_title_home_default));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Called when activity stops
     */
    @Override
    protected void onStop() {
        super.onStop();

        saveTimersState();

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Notification notification = null;
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, TrackerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        if (working)
        {
            notification = new Notification(android.R.drawable.ic_menu_info_details, getResources().getString(R.string.notification_work), System.currentTimeMillis());
            notification.setLatestEventInfo(this, getResources().getString(R.string.notification_work), getResources().getString(R.string.show_app), intent);
        }
        else if (resting)
        {
            notification = new Notification(android.R.drawable.ic_menu_info_details, getResources().getString(R.string.notification_rest), System.currentTimeMillis());
            notification.setLatestEventInfo(this, getResources().getString(R.string.notification_rest), getResources().getString(R.string.show_app), intent);
        }

        if (notification != null)
        {
            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
            manager.notify(NOTIFICATION_KEY, notification);
        }
    }

    /**
     * Called when activity returns visible
     */
    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_KEY);
    }

    private void saveTimersState() {
        statistic_editor.putLong(LAST_TIME_KEY, System.currentTimeMillis());
        statistic_editor.putBoolean(RESTING_STATE_KEY, resting);
        statistic_editor.putBoolean(WORKING_STATE_KEY, working);

        statistic_editor.putLong(RESTING_TIME_KEY, restingTime);
        statistic_editor.putLong(WORKING_TIME_KEY, workingTime);
        statistic_editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(R.string.clear_timers);
        item.setIcon(android.R.drawable.ic_menu_delete);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        lastTime = 0;
        
        if (working)
            workTime.startAnimation(workShrink);
        working = false;
        workingTime = 0;
        workTime.setText("00:00:00");
        workButton.setSelected(resting);

        if (resting)
            restTime.startAnimation(restShrink);
        resting = false;
        restingTime = 0;
        restTime.setText("00:00:00");
        restButton.setSelected(resting);

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     *  Updates rest timer TextView from timer thread
     */
    private void updateRestTimer()
    {
    	final String timeString = getStringTimeFromSeconds(restingTime);
    	
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				restTime.setText(timeString);	
			}
		});
    }
    
    /**
     *  Updates work timer TextView from timer thread
     */
    private void updateWorkTimer()
    {
    	final String timeString = getStringTimeFromSeconds(workingTime);
    	
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				workTime.setText(timeString);	
			}
		});
    }
    
    private String getStringTimeFromSeconds(long secondsTime)
    {
    	String result = "";
    	
    	int hours = (int)(secondsTime / 3600);
    	if (hours < 10)
    	{
    		result += "0" + String.valueOf(hours) + ":";
    	}
    	else
    	{
    		result += String.valueOf(hours) + ":";
    	}
    	
    	short minutes = (short)((secondsTime - (hours * 3600)) / 60);
    	if (minutes < 10)
    	{
    		result += "0" + String.valueOf(minutes) + ":";
    	}
    	else
    	{
    		result += String.valueOf(minutes) + ":";
    	}
    	
    	short seconds = (short)(secondsTime - (hours * 3600) - (minutes * 60));
    	if (seconds < 10)
    	{
    		result += "0" + String.valueOf(seconds);
    	}
    	else
    	{
    		result += String.valueOf(seconds);
    	}
    	
    	return result;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveTimersState();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        SharedPreferences saved_statistic = PreferenceManager.getDefaultSharedPreferences(this);
        resting = saved_statistic.getBoolean(RESTING_STATE_KEY, false);
        working = saved_statistic.getBoolean(WORKING_STATE_KEY, false);
        restingTime = saved_statistic.getLong(RESTING_TIME_KEY, 0);
        workingTime = saved_statistic.getLong(WORKING_TIME_KEY, 0);
        
        if (resting)
		{
        	long duration = restMagnify.getDuration();
        	restMagnify.setDuration(0);
			restTime.startAnimation(restMagnify);
			restMagnify.setDuration(duration);
		}
        else if (working)
		{
        	long duration = workMagnify.getDuration();
        	workMagnify.setDuration(0);
			workTime.startAnimation(workMagnify);
			workMagnify.setDuration(duration);
		}
		
		workButton.setSelected(working);
		restButton.setSelected(resting);
    }
    
    
    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        if (working)
        {
            intent.putExtra(Intent.EXTRA_TEXT, "I'm working now using TimeTracker.");
        }
        else if (resting)
        {
            intent.putExtra(Intent.EXTRA_TEXT, "I'm resting now using TimeTracker.");
        }
        else
        {
            intent.putExtra(Intent.EXTRA_TEXT, "I'm doing nothing right now using TimeTracker.");
        }
        return Intent.createChooser(intent, "Share");
    }
}
