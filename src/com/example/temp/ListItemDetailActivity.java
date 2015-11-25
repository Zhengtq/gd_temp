package com.example.temp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ListItemDetailActivity extends Activity {

	private ImageView imgView;
	private TextView text;
	private Button returnBt;
	private Button addPartnerBt;
	private GroupData datai;
	public static String result = "waiting";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_item_detail);
		
		imgView = (ImageView)findViewById(R.id.groupLogoi);
		text = (TextView)findViewById(R.id.groupInfoi);
		Bundle data = getIntent().getExtras();
		int position = data.getInt("position");
		datai = (GroupData) data.getSerializable("simple");
		

		Log.i("tag", position + ":" + datai.groupName);
		imgView.setImageBitmap(GroupListAdapter.img.get(position));
		text.setText(datai.groupName + "\n" + datai.location + "\n" + datai.freeTime + "\n" + datai.activity);

		returnBt = (Button)findViewById(R.id.returnBt);
		addPartnerBt = (Button)findViewById(R.id.addPartner);
		
		returnBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onReturnBtClick();
			}
			
		});
		
		addPartnerBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onAddPartnerBtClick(MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition).id, datai.id);
			}
			
		});
	}
	
	private void onAddPartnerBtClick(int mid, int pid) {		
		new AddPartnerThreads(getString(R.string.server_ip), mid, pid).start();
		
		while (result.equals("waiting"));
		
		if (result.equals("success")) {
			AlertDialog ad = new AlertDialog.Builder(this).setTitle("消息").create();
			ad.setMessage(result + "!\n你的队伍：" + MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition).groupName + 
					"已经成功与" + datai.groupName + "确定联谊，请去消息页面与对方联系活动细节...");
			ad.show();
		} else {
			AlertDialog ad = new AlertDialog.Builder(this).setTitle("消息").create();
			ad.setMessage("Unfortunately!\n你的队伍：" + MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition).groupName + 
					"没能与" + datai.groupName + "确定联谊，请重新选择伙伴...");
			ad.show();
		}
	} 
	
	private void onReturnBtClick() {
		Intent returnIntent = new Intent(this, MainActivity.class);
		startActivity(returnIntent);
	}
}
