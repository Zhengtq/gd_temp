package com.example.temp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

class InfoData {
	public int myGroupId;
	public int myMapId;
	public int messageNum;
	public String message;
	public String myGroupName;
	public int pGroupId;
	GroupData partner;
	public boolean isDelSign = false;
}

public class InfoListAdapter extends BaseAdapter {

	public static List<InfoData> infoList = new ArrayList<InfoData>();
	public static List<Bitmap> img = new ArrayList<Bitmap>();
	private Context context;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;
	public static boolean finishedLoadingFlag = false;
	private float scale = 1;
	private String url;

	public InfoListAdapter(Context context, String url, float scale) {
		this.context = context;
		this.url = url;
		this.scale = scale;
		layoutInflater = (LayoutInflater) this.context.getSystemService(inflater);

		if (infoList.size() == 0) {
			new LoadInfoListThreads(url, HttpThread.userInfo.groups).start();
//			while (!finishedLoadingFlag)
//				;
//			finishedLoadingFlag = false;
		}
	}

	public void refreshData() {
		infoList.clear();
		img.clear();
		if (infoList.size() == 0) {
			new LoadInfoListThreads(url, HttpThread.userInfo.groups).start();
//			while (!finishedLoadingFlag)
//				;
//			finishedLoadingFlag = false;
		}
	}

	@Override
	public int getCount() {
		return infoList.size();
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
		RelativeLayout relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.info_list_item, null);
		if (position < infoList.size() && infoList.get(position) != null) {
			ImageView ivLogo = (ImageView) relativeLayout.findViewById(R.id.info_parternerLogo);
			TextView tvInfo = (TextView) relativeLayout.findViewById(R.id.infoText);
			
			for (int i = 0; i < MyGroupListAdapter.groupDataList.size(); i++) {
				if (MyGroupListAdapter.groupDataList.get(i).id == infoList.get(position).myGroupId) {
					infoList.get(position).myGroupName = MyGroupListAdapter.groupDataList.get(i).groupName;
					infoList.get(position).myMapId = i;
					break;
				}
			}
			tvInfo.setText(infoList.get(position).myGroupName + "\n" + infoList.get(position).message);
			ImageButton ibDel = (ImageButton) relativeLayout.findViewById(R.id.info_item_delete_button);

			if (position < img.size() && img.get(position) != null)
				ivLogo.setImageBitmap(MyGroupListAdapter.img.get(infoList.get(position).myMapId));
			else
				ivLogo.setImageResource(R.drawable.empty_photo);
			
			if (infoList.get(position).isDelSign) {
				relativeLayout.scrollBy((int) (65 * scale + 0.5f), 0);
				ibDel.setFocusable(true);
				ibDel.setClickable(true);
				ibDel.setFocusableInTouchMode(true);
			} else {
				relativeLayout.scrollTo(0, 0);
			}
			
			
			ibDel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					System.out.println("image button clicked!");
					remove(position);
				}
				
			});
		}
		
		
		relativeLayout.setOnClickListener(new OnClickListener() {			
		
			@Override
			public void onClick(View v) {
				System.out.println(position + " is clicked!");
				Intent newIntent = new Intent(context, ShoppingActivity.class);
				newIntent.putExtra("position", position);
				newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(newIntent);
			}});
		return relativeLayout;
	}
	
	public void remove(int position) {
		infoList.remove(position);
		this.notifyDataSetChanged();
	}

}
