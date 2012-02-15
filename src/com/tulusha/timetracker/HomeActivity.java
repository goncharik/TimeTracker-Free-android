package com.tulusha.timetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import com.tulusha.timetracker.widgets.ActionBar;
import com.tulusha.timetracker.widgets.ScrollingTextView;

/**
 * Created by IntelliJ IDEA.
 * User: zhenya
 * Date: 22.10.11
 * Time: 21:44
 */
public class HomeActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("Home");

    }

    public void onTrackerClick(View v)
    {
        startActivity(new Intent(this, TrackerActivity.class));
    }

    public void onCalClick(View v)
    {
        AlertDialog builder;
        String aboutTitle = "Available in Full version";
        final TextView message = new TextView(this);
        message.setText("You can unlock this feature by purchasing TimeTracker Android app from Android Market");
        message.setPadding(10, 10, 10, 10);
        
        builder = new AlertDialog.Builder(this).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
                    	 this.getString(android.R.string.ok), null).setView(message).create();
        builder.show();
    }

    public void onChartsClick(View v)
    {
        AlertDialog builder;
        String aboutTitle = "Available in Full version";
        final TextView message = new TextView(this);
        message.setText("You can unlock this feature by purchasing TimeTracker Android app from Android Market");
        message.setPadding(10, 10, 10, 10);

        builder = new AlertDialog.Builder(this).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
                         this.getString(android.R.string.ok), null).setView(message).create();
        builder.show();
    }

    public void onAboutClick(View v)
    {
        AlertDialog builder;
        try {
        	builder = AboutDialogBuilder.create(this);
        	builder.show();
        } catch (PackageManager.NameNotFoundException e) {
        	e.printStackTrace();
        }

    }

    public static Intent createIntent(Context context)
    {
        Intent i = new Intent(context, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    public static class AboutDialogBuilder {
    	public static AlertDialog create( Context context ) throws PackageManager.NameNotFoundException {
            // Try to load the a package matching the name of our own package
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String versionInfo = pInfo.versionName;

            String aboutTitle = context.getString(R.string.about, context.getString(R.string.app_name));
            String versionString = context.getString(R.string.version, versionInfo);
            String aboutText = context.getString(R.string.about_text);

            // Set up the TextView
            final ScrollingTextView message = new ScrollingTextView(context);
            // We'll use a spannablestring to be able to make links clickable
            final SpannableString s = new SpannableString(aboutText);

            // Set some padding
            message.setPadding(5, 5, 5, 5);
            // Set up the final string
            message.setText(versionString + "\n\n" + s);
            // Now linkify the text
            Linkify.addLinks(message, Linkify.ALL);

            return new AlertDialog.Builder(context).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
            	 context.getString(android.R.string.ok), null).setView(message).create();

    	}
    }

}