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

public class SearchMemberListAdapter extends BaseAdapter {

	public static List<User> memberDataList = new ArrayList<User>();
	public static List<Bitmap> img = new ArrayList<Bitmap>();
	private Context context;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;
	
	public SearchMemberListAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return memberDataList.size();
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
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.member_list_detail, null);
		ImageView ivLogo = (ImageView) linearLayout.findViewById(R.id.memberLogo);
		TextView tvInfo = (TextView) linearLayout.findViewById(R.id.memberInfo);
		if (memberDataList.size() > position) {
			tvInfo.setText(memberDataList.get(position).username + "\n" + memberDataList.get(position).school + "\n"
					+ memberDataList.get(position).interest);
			System.out.println("" + position);
			if (position < img.size() && img.get(position) != null)
				ivLogo.setImageBitmap(img.get(position));
		}		

		return linearLayout;
	}

}
