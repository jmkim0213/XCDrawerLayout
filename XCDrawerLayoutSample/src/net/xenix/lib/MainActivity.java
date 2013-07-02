package net.xenix.lib;

import net.xenix.lib.fragment.Fragment1;
import net.xenix.lib.fragment.Fragment2;
import net.xenix.lib.fragment.Fragment3;
import net.xenix.lib.fragment.Fragment4;
import net.xenix.lib.view.XCDrawerLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity {
	private XCDrawerLayout mDrawerLayout;
	private Fragment mCurrentFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.activity_main_Button_menu1).setOnClickListener(mClickListener);
		findViewById(R.id.activity_main_Button_menu2).setOnClickListener(mClickListener);
		findViewById(R.id.activity_main_Button_menu3).setOnClickListener(mClickListener);
		findViewById(R.id.activity_main_Button_menu4).setOnClickListener(mClickListener);
		
		mDrawerLayout = (XCDrawerLayout)findViewById(R.id.activity_main_XCDrawerLayout);
		mDrawerLayout.setDrawerViewInChild(R.id.activity_main_FrameLayout_content);
		mDrawerLayout.setDimMaxValue(0.8F);
		mDrawerLayout.setTouchFullRangeMode(true);
		
		
		showFragment(R.id.activity_main_FrameLayout_content, new Fragment1());
	}
	
	private void showFragment(int contentId, Fragment newFragment) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		if ( mCurrentFragment != null && mCurrentFragment.isAdded() ) {
			ft.remove(mCurrentFragment);
		}
		
		ft.add(contentId, newFragment, "Content");
		mCurrentFragment = newFragment;
		
		ft.commitAllowingStateLoss();
		fm.executePendingTransactions();
	}
	
	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			

			switch ( v.getId() ) {
			case R.id.activity_main_Button_menu1:
				showFragment(R.id.activity_main_FrameLayout_content, new Fragment1());
				break;
				
			case R.id.activity_main_Button_menu2:
				showFragment(R.id.activity_main_FrameLayout_content, new Fragment2());
				break;
				
			case R.id.activity_main_Button_menu3:
				showFragment(R.id.activity_main_FrameLayout_content, new Fragment3());
				break;
				
			case R.id.activity_main_Button_menu4:
				showFragment(R.id.activity_main_FrameLayout_content, new Fragment4());
				break;
			}
			
			
			mDrawerLayout.closeDrawerView();	
		}
	};


}
