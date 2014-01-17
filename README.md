SlidingMenu
===========
This is a sample project of how to use SlidingMenu in Android application with just one customview. 
This SlidingMenu does not prevent using any other scrollableview like listview,scrollview or viewpager or any buttons.
 
How to do:

+ Step 1: Declare your xml layout for navigation activity.
Navigation activity has two views: 
	+ ScrollView: Contains list of navigation menus(you can put any kinds of menu here).
	+ DraggableLayout: Where you put your content fragments(or views). This fragment can be scrolled, replaced.
	See res/layout/navi_activity.xml
+ Step 2: Declare content fragments: 
	AFragment,BFragment and CFragment. These fragments are main contents to be scrolled and replaced when click to menu buttons. Each fragments should implement one button events to open menu.
	In my example I use fragments, but you can use views instead.
+ Step 3: Declare NaviActivity: This Activity will handle replacing fragments when user click to menu buttons.

<pre>
		mDraggableLayout = (DraggableLayout) findViewById(R.id.navi_draggable_layout);
		mDraggableLayout.setShadowBitmap(
				BitmapFactory.decodeResource(getResources(), R.drawable.common_shadow_right));
		mDraggableLayout.setDimBackground(false);
		
		//Add the first fragment
		switchPanel(null);
		
		//Add menu listener 
		findViewById(R.id.nav_A_fragment).setOnClickListener(this);
		findViewById(R.id.nav_B_fragment).setOnClickListener(this);
		findViewById(R.id.nav_C_fragment).setOnClickListener(this);
		findViewById(R.id.nav_about_bt).setOnClickListener(this);
</pre>

DraggableLayout is the custom view which reacts to scroll actions of user, it contains content fragments(which are AFragment,B,C..)

When menu items are clicked, just replace the content fragments:

<pre>

//Add or replace content. These contents could be fragments or views. 
private void switchPanel(final Fragment newFm) {
		if(getSupportFragmentManager().findFragmentById(R.id.navi_draggable_layout)==null){
			setContentFragment(new AFragment());
			return;
		}
		if(newFm==null) return;
		final String fragmentName=newFm.getClass().getSimpleName();
		final Fragment child=getSupportFragmentManager().findFragmentById(R.id.navi_draggable_layout);
		
		//If user are opening menu, the fragment content should be replaced after finishing scroll
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
	
</pre>

Enjoy it.

This is licensed under the Apache License, Version 2.0

Thank you

