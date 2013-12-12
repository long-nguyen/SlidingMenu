package com.kayac.slidingmenu.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity{
	public static String TAG=AboutActivity.class.getSimpleName();

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		TextView v=new TextView(this);
		v.setText("About");
		setContentView(v);
	}
}
