package com.kayac.slidingmenu.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kayac.slidingmenu.R;
import com.kayac.slidingmenu.ui.activities.NaviActivity;

public class CFragment extends Fragment{
	public static final String TAG=CFragment.class.getSimpleName();
	public static boolean DEBUG =false;

	public CFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.navi_sample_fragment, container,
				false);
		TextView tv=(TextView) v.findViewById(R.id.screenname_tv);
		tv.setText("C Fragment");
		v.setBackgroundColor(Color.MAGENTA);
		v.findViewById(R.id.menu_bt).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((NaviActivity) getActivity()).openMenu();
			}
		});
		return v;
	}
}
