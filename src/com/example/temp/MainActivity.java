package com.example.temp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewFlipper;

public class MainActivity extends Activity { // Fragment

	private ViewFlipper allFlipper;
	private Fragment[] mFragments;
	private RadioGroup bottomRg;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private RadioButton rbHome, rbMyGroups, rbMyself, rbInfo, rbShare;
	public static int id = 0;
	Drawable imgHome, imgHome_red, imgMyGroups, imgMyGroups_red, imgMyself, 
	imgMyself_red, imgInfo, imgInfo_red, imgShare, imgShare_red;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				allFlipper.setDisplayedChild(1); // 切换到主界面
			}
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);
		
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				handler.sendEmptyMessage(1);
			}
			
		}, 2000); // 启动等待两秒
		
		bottomRg = (RadioGroup) findViewById(R.id.bottomRg);
		rbHome = (RadioButton) findViewById(R.id.rbHome);
		rbMyGroups = (RadioButton) findViewById(R.id.rbCreateGroup);
		rbMyself = (RadioButton) findViewById(R.id.rbMySelf);
		rbInfo = (RadioButton) findViewById(R.id.rbInfo);
		rbShare = (RadioButton) findViewById(R.id.rbShare);
		
		Resources res = getResources();
		imgHome = res.getDrawable(R.drawable.my_groups);
		imgHome_red = res.getDrawable(R.drawable.my_groups_red);
		imgMyGroups = res.getDrawable(R.drawable.create_group);
		imgMyGroups_red = res.getDrawable(R.drawable.create_group_red);
		imgMyself = res.getDrawable(R.drawable.myself);
		imgMyself_red = res.getDrawable(R.drawable.myself_red);
		imgInfo = res.getDrawable(R.drawable.info);
		imgInfo_red = res.getDrawable(R.drawable.info_red);
		imgShare = res.getDrawable(R.drawable.share);
		imgShare_red = res.getDrawable(R.drawable.share_red);
		imgHome.setBounds(0, 0, imgHome.getMinimumWidth(), imgHome.getMinimumHeight());
		imgHome_red.setBounds(0, 0, imgHome_red.getMinimumWidth(), imgHome_red.getMinimumHeight());
		imgMyGroups.setBounds(0, 0, imgMyGroups.getMinimumWidth(), imgMyGroups.getMinimumHeight());
		imgMyGroups_red.setBounds(0, 0, imgMyGroups_red.getMinimumWidth(), imgMyGroups_red.getMinimumHeight());
		imgMyself.setBounds(0, 0, imgMyself.getMinimumWidth(), imgMyself.getMinimumHeight());
		imgMyself_red.setBounds(0, 0, imgMyself_red.getMinimumWidth(), imgMyself_red.getMinimumHeight());
		imgInfo.setBounds(0, 0, imgInfo.getMinimumWidth(), imgInfo.getMinimumHeight());
		imgInfo_red.setBounds(0, 0, imgInfo_red.getMinimumWidth(), imgInfo_red.getMinimumHeight());
		imgShare.setBounds(0, 0, imgShare.getMinimumWidth(), imgShare.getMinimumHeight());
		imgShare_red.setBounds(0, 0, imgShare_red.getMinimumWidth(), imgShare_red.getMinimumHeight());
		System.out.println(imgShare.getIntrinsicWidth() + " " + imgShare.getIntrinsicHeight());
		
		mFragments = new Fragment[5];
		fragmentManager = getFragmentManager(); // getSupportFragmentManager();
		mFragments[0] = fragmentManager.findFragmentById(R.id.fragment_home);
		mFragments[1] = fragmentManager.findFragmentById(R.id.fragment_mygroups);
		mFragments[2] = fragmentManager.findFragmentById(R.id.fragment_myself);
		mFragments[3] = fragmentManager.findFragmentById(R.id.fragment_info);
		mFragments[4] = fragmentManager.findFragmentById(R.id.fragment_share);
		fragmentTransaction = fragmentManager.beginTransaction().hide(mFragments[0]).hide(mFragments[1])
				.hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
			id = bundle.getInt("id");

		
		switch (id) {
		case 0:
			rbHome.setCompoundDrawables(null, imgHome_red, null, null);
			rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
			rbMyself.setCompoundDrawables(null, imgMyself, null, null);
			rbInfo.setCompoundDrawables(null, imgInfo, null, null);
			rbShare.setCompoundDrawables(null, imgShare, null, null);
			fragmentTransaction.show(mFragments[0]).commit();
			break;
		case 1:
			rbHome.setCompoundDrawables(null, imgHome, null, null);
			rbMyGroups.setCompoundDrawables(null, imgMyGroups_red, null, null);
			rbMyself.setCompoundDrawables(null, imgMyself, null, null);
			rbInfo.setCompoundDrawables(null, imgInfo, null, null);
			rbShare.setCompoundDrawables(null, imgShare, null, null);
			fragmentTransaction.show(mFragments[1]).commit();
			break;
		case 2:
			rbHome.setCompoundDrawables(null, imgHome, null, null);
			rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
			rbMyself.setCompoundDrawables(null, imgMyself_red, null, null);
			rbInfo.setCompoundDrawables(null, imgInfo, null, null);
			rbShare.setCompoundDrawables(null, imgShare, null, null);
			fragmentTransaction.show(mFragments[2]).commit();
			break;
		case 3:
			rbHome.setCompoundDrawables(null, imgHome, null, null);
			rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
			rbMyself.setCompoundDrawables(null, imgMyself, null, null);
			rbInfo.setCompoundDrawables(null, imgInfo_red, null, null);
			rbShare.setCompoundDrawables(null, imgShare, null, null);
			fragmentTransaction.show(mFragments[3]).commit();
			break;
		case 4:
			rbHome.setCompoundDrawables(null, imgHome, null, null);
			rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
			rbMyself.setCompoundDrawables(null, imgMyself, null, null);
			rbInfo.setCompoundDrawables(null, imgInfo, null, null);
			rbShare.setCompoundDrawables(null, imgShare_red, null, null);
			fragmentTransaction.show(mFragments[4]).commit();
			break;
		}		

		bottomRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.i("tag", "home1");
				fragmentTransaction = fragmentManager.beginTransaction().hide(mFragments[0]).hide(mFragments[1])
						.hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]);
				switch (checkedId) {
				case R.id.rbHome:
					rbHome.setCompoundDrawables(null, imgHome_red, null, null);
					rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
					rbMyself.setCompoundDrawables(null, imgMyself, null, null);
					rbInfo.setCompoundDrawables(null, imgInfo, null, null);
					rbShare.setCompoundDrawables(null, imgShare, null, null);
					fragmentTransaction.show(mFragments[0]).commit();
					break;
				case R.id.rbCreateGroup:
					rbHome.setCompoundDrawables(null, imgHome, null, null);
					rbMyGroups.setCompoundDrawables(null, imgMyGroups_red, null, null);
					rbMyself.setCompoundDrawables(null, imgMyself, null, null);
					rbInfo.setCompoundDrawables(null, imgInfo, null, null);
					rbShare.setCompoundDrawables(null, imgShare, null, null);
					fragmentTransaction.show(mFragments[1]).commit();
					break;
				case R.id.rbMySelf:
					rbHome.setCompoundDrawables(null, imgHome, null, null);
					rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
					rbMyself.setCompoundDrawables(null, imgMyself_red, null, null);
					rbInfo.setCompoundDrawables(null, imgInfo, null, null);
					rbShare.setCompoundDrawables(null, imgShare, null, null);
					fragmentTransaction.show(mFragments[2]).commit();
					break;
				case R.id.rbInfo:
					rbHome.setCompoundDrawables(null, imgHome, null, null);
					rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
					rbMyself.setCompoundDrawables(null, imgMyself, null, null);
					rbInfo.setCompoundDrawables(null, imgInfo_red, null, null);
					rbShare.setCompoundDrawables(null, imgShare, null, null);
					fragmentTransaction.show(mFragments[3]).commit();
					break;
				case R.id.rbShare:
					rbHome.setCompoundDrawables(null, imgHome, null, null);
					rbMyGroups.setCompoundDrawables(null, imgMyGroups, null, null);
					rbMyself.setCompoundDrawables(null, imgMyself, null, null);
					rbInfo.setCompoundDrawables(null, imgInfo, null, null);
					rbShare.setCompoundDrawables(null, imgShare_red, null, null);
					fragmentTransaction.show(mFragments[4]).commit();
					break;
				default:
					break;
				}
			}

		});

	}

}
