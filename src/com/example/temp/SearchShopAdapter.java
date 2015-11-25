package com.example.temp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchShopAdapter extends BaseAdapter {
	public static List<ShopData> shopInfoList = new ArrayList<ShopData>();
	public static List<Bitmap> shopLogo = new ArrayList<Bitmap>();
	private Context context;
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;

	public SearchShopAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return shopInfoList.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.shop_info_list_item, null);
		if (position < shopInfoList.size() && shopInfoList.get(position) != null) {
			ImageView ivLogo = (ImageView) linearLayout.findViewById(R.id.shopImage);
			TextView tvInfo = (TextView) linearLayout.findViewById(R.id.ShopInfo);

			tvInfo.setText(shopInfoList.get(position).shopName + "\n" + shopInfoList.get(position).price + "\n" +
					shopInfoList.get(position).rating + "\n" + shopInfoList.get(position).location);
			ImageButton ibDel = (ImageButton) linearLayout.findViewById(R.id.info_item_delete_button);

			if (position < shopLogo.size() && shopLogo.get(position) != null)
				ivLogo.setImageBitmap(shopLogo.get(position));		
			else
				ivLogo.setImageResource(R.drawable.empty_photo);
		}

		linearLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (position < shopInfoList.size() && shopInfoList.get(position) != null && shopInfoList.get(position).httpUrl != null) {
					System.out.println(shopInfoList.get(position).httpUrl);
					Uri uri = Uri.parse(shopInfoList.get(position).httpUrl);
					System.out.println(uri.toString());
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					context.startActivity(intent);
				}
			}

		});

		return linearLayout;
	}

}
