package com.example.temp;

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

public class SearchGroupAdapter extends BaseAdapter {

	public static List<GroupData> groupDataList = new ArrayList<GroupData>();
	public static List<Bitmap> img = new ArrayList<Bitmap>();
	private Context context;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;

	public SearchGroupAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return groupDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.group_list_item, null);
		ImageView ivLogo = (ImageView) linearLayout.findViewById(R.id.groupLogo);
		TextView tvInfo = (TextView) linearLayout.findViewById(R.id.groupInfo);
		if (groupDataList.size() > position) {
			tvInfo.setText(groupDataList.get(position).groupName + "\n" + groupDataList.get(position).location + "\n"
					+ groupDataList.get(position).freeTime);
			System.out.println("" + position);
			if (position < img.size() && img.get(position) != null)
				ivLogo.setImageBitmap(img.get(position));
		}

		return linearLayout;
	}

}
