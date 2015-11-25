package com.example.temp;


import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class GroupData implements Serializable {
	public int id;
	public String url;
	public String activity;
	public String groupmembers;
	public String groupName;
	public String location;
	public String school;
	public String freeTime;
	public String freeTimeStart;
	public String freeTimeStop;
	public int pid;
	public String messages;
}

public class GroupListAdapter extends BaseAdapter {
	private static Context context;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;
	private static String rootUrl;
	private String imgUrl;
	public static List<GroupData> groupDataList = new ArrayList<GroupData>();
	public static List<Bitmap> img = new ArrayList<Bitmap>();
	public static boolean flag = false;
	private static int minRow = 0;
	private static int maxRow = 2;
	private static int batchSize = 2;
	public static boolean addFinishedFlag = false;
	public static boolean refreshFinishedFlag = false;	

	public GroupListAdapter(Context context, String url) {
		rootUrl = url + "/web/SecondServlet";
		imgUrl = url + "/web/logo_imgs/";
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);
		try {
			if (groupDataList.size() == 0) {
				if (MyGroupListAdapter.groupDataList != null && MyGroupListAdapter.groupDataList.size() > 0 && 
						MyGroupListAdapter.groupDataList.get(0) != null)
					new InputHttpThreads(rootUrl, MyGroupListAdapter.groupDataList.get(0).id + "", minRow, maxRow).start();	
				else {
					new InputHttpThreads(rootUrl, "-1", minRow, maxRow).start();
				}
				while (!flag)
					;
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		flag = false;
	}

	public void loadImgs() {
		if (img.size() < groupDataList.size()) {
			for (int i = img.size(); i < groupDataList.size(); i++) {
				flag = false;
				if (!groupDataList.get(i).url.equals("") && !groupDataList.get(i).url.equals("null"))
					new InputHttpThreads(imgUrl + groupDataList.get(i).url + ".png", i).start();
//				Log.i("tag", i + " " + groupDataList.get(i).url + " " + img.size());
			}
		}
	}

	public static void refreshData() {
		minRow = 0;
		maxRow = batchSize;
		groupDataList.clear();
		img.clear();
		flag = false;
		if (MyGroupListAdapter.groupDataList != null && MyGroupListAdapter.groupDataList.size() > MyGroups.currentPosition && 
				MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition) != null)
			new InputHttpThreads(rootUrl, MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition).id + "", minRow, maxRow).start();
		else {
			new InputHttpThreads(rootUrl, "-1", minRow, maxRow).start();
		}
	}

	public void addABatch() {
		maxRow = batchSize;
		minRow += batchSize;
		flag = false;
		if (MyGroupListAdapter.groupDataList != null && MyGroupListAdapter.groupDataList.size() > MyGroups.currentPosition && 
				MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition) != null)
			new InputHttpThreads(rootUrl, MyGroupListAdapter.groupDataList.get(MyGroups.currentPosition).id + "", minRow, maxRow).start();
		else {
			new InputHttpThreads(rootUrl, "-1", minRow, maxRow).start();
		}
	}

	@Override
	public int getCount() {
		return Math.min(groupDataList.size(), img.size());
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//		Log.i("tag", position + "");
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.group_list_item, null);
		ImageView ivLogo = (ImageView) linearLayout.findViewById(R.id.groupLogo);
		TextView tvInfo = (TextView) linearLayout.findViewById(R.id.groupInfo);
		if (position < groupDataList.size() && groupDataList.get(position) != null)
			tvInfo.setText(groupDataList.get(position).groupName + "\n" + groupDataList.get(position).location + "\n"
					+ groupDataList.get(position).freeTime);
		else
			tvInfo.setText("loading...");

		if (position < img.size() && img.get(position) != null)
			ivLogo.setImageBitmap(img.get(position));
		else
			ivLogo.setImageResource(R.drawable.empty_photo);
		return linearLayout;
	}
}
