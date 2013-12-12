package com.kayac.slidingmenu.ui.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.kayac.slidingmenu.R;
import com.kayac.slidingmenu.ui.fragments.AFragment;
import com.kayac.slidingmenu.ui.fragments.BFragment;
import com.kayac.slidingmenu.ui.fragments.CFragment;
import com.kayac.slidingmenu.ui.views.DraggableLayout;
import com.kayac.slidingmenu.ui.views.DraggableLayout.SidingListenerInterface;

/**
 * This activity acts like a holder activity for side-panel enabled fragments
 * 
 * @author long-nguyen
 * 
 */
public class NaviActivity extends FragmentActivity implements OnClickListener {
	public static final boolean DEBUG = true;
	public static final String TAG = NaviActivity.class.getSimpleName();
	
	private DraggableLayout mDraggableLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navi_activity);
		
		mDraggableLayout = (DraggableLayout) findViewById(R.id.navi_draggable_layout);
		mDraggableLayout.setShadowBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.common_shadow_right));
		mDraggableLayout.setDimBackground(false);
		
		//Add the first fragment
		switchPanel(null);
		
		//Add menu listener 
		findViewById(R.id.nav_A_fragment).setOnClickListener(this);
		findViewById(R.id.nav_B_fragment).setOnClickListener(this);
		findViewById(R.id.nav_C_fragment).setOnClickListener(this);
		findViewById(R.id.nav_about_bt).setOnClickListener(this);
	}
	
	
	@Override
	public void onBackPressed() {
		if(!mDraggableLayout.isOpeningSlideMenu()){
			mDraggableLayout.invokeScrollAction(null);
		}else{ 
			finish();
		}
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_about_bt:
			startActivity(new Intent(this,AboutActivity.class));
			break;
		case R.id.nav_A_fragment:
			switchPanel(new AFragment());
			break;
		case R.id.nav_B_fragment:
			switchPanel(new BFragment());
			break;
		case R.id.nav_C_fragment:
			switchPanel(new CFragment());
			break;
		default:
			break;
		}
	}
	
	/**
	 * Switch current main fragment(which could be scrolled) . Auto add main Fragment if there is no fragment yet
	 * @param newFm
	 */
	private void switchPanel(final Fragment newFm) {
		if(getSupportFragmentManager().findFragmentById(R.id.navi_draggable_layout)==null){
			setContentFragment(new AFragment());
			return;
		}
		if(newFm==null) return;
		final String fragmentName=newFm.getClass().getSimpleName();
		final Fragment child=getSupportFragmentManager().findFragmentById(R.id.navi_draggable_layout);
		
		if(mDraggableLayout.isOpeningSlideMenu()){
			//Do scroll, afterthat, lets change fragment
			mDraggableLayout.invokeScrollAction(new SidingListenerInterface() {
				@Override
				public void onSlideFinished() {
					if (!fragmentName.equals(child.getClass().getSimpleName())) {	
						setContentFragment(newFm);
					}
				}
			});
		}else{
			if (!fragmentName.equals(child.getClass().getSimpleName())) {	
				setContentFragment(newFm);
			}
		}
	}
	
	private void setContentFragment(Fragment fm){
		if(fm==null) return;
		Fragment child=getSupportFragmentManager().findFragmentById(R.id.navi_draggable_layout);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(child!=null){
			ft.setCustomAnimations(R.anim.splashfadein, R.anim.splashfadeout);
			ft.replace(R.id.navi_draggable_layout, fm);
		}else{
			ft.add(R.id.navi_draggable_layout, fm);
		}
		ft.commit();
	}
	
	//Open menu, with no callback after menu fully visible
	public void openMenu(){
		mDraggableLayout.invokeScrollAction(null);
	}

	
}
