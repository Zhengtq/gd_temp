package com.example.temp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyGroupListAdapter extends BaseAdapter {

	private Context context;
	private String rootUrl;
	private String imgUrl;
	private String groupsStr = HttpThread.userInfo.groups;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;
	public static List<GroupData> groupDataList = new ArrayList<GroupData>();
	public static List<Bitmap> img = new ArrayList<Bitmap>();
	public static boolean flag = false;

	MyGroupListAdapter(Context context, String url) {
		this.context = context;
		this.rootUrl = url + "/web/SecondServlet";
		this.imgUrl = url + "/web/logo_imgs/";
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);

		groupDataList.clear();
		new InputHttpThreads(rootUrl, groupsStr).start();
//		while (!flag)
//			;
//		flag = false;
		
	}
	
	public void loadImgs() {
		if (img.size() < groupDataList.size()) {
			for (int i = img.size(); i < groupDataList.size(); i++) {
				if (!groupDataList.get(i).url.equals("")) {
					new InputHttpThreads(imgUrl + groupDataList.get(i).url + ".png", i, "my").start();
//					while (!flag)
//						;
//					flag = false;
				}
				else {
					Bitmap tmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.who);
					img.add(tmp);
				}
			}
		}
	}

	@Override
	public int getCount() {
		return groupDataList.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.mygroups_list_item, null);
		ImageView groupLogo = (ImageView)linearLayout.findViewById(R.id.myGroupLogo);
		TextView groupInfo = (TextView)linearLayout.findViewById(R.id.myGroupInfo);
		Button setActiveBt = (Button)linearLayout.findViewById(R.id.setActiveBt);

		if (position < groupDataList.size() && groupDataList.get(position) != null) {
		groupInfo.setText(groupDataList.get(position).groupName + "\n" + groupDataList.get(position).groupmembers + "\n" + groupDataList.get(position).activity);
		} else {
			groupInfo.setText("loading...");
		}
		Log.i("TAG", imgUrl + groupDataList.get(position).url + ".png");
		try {
			if (position < img.size() && img.get(position) != null) {
				groupLogo.setImageBitmap(img.get(position));
				if (position == MyGroups.currentPosition) {
					MyGroups.myCurrentGroupLogo.setImageBitmap(img.get(position));
				}
				Log.i("TAG", position + imgUrl + groupDataList.get(position).url + ".png");
			} else {
				groupLogo.setImageResource(R.drawable.empty_photo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		setActiveBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyGroups.currentPosition = position;
				MyGroups.myCurrentGroupLogo.setImageBitmap(img.get(position));
				GroupData currentGroup = groupDataList.get(position);
				MyGroups.myCurrentGroupInfo.setText(currentGroup.groupName + "\n" + currentGroup.groupmembers + "\n" + currentGroup.activity);
				GroupListAdapter.refreshData();
			}

		});
		return linearLayout;
	}

}
