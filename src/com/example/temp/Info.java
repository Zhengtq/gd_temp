package com.example.temp;

import android.app.Fragment;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class Info extends Fragment implements OnFlipperDeleteListener{
	private FlipperListView messageListView;
	private static InfoListAdapter ila;
	int moveX = 0, moveY;
	float scale = 1; 
	
	public static Handler infoHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				ila.notifyDataSetChanged();
				break;
			case 2:
			}				
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_info, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		scale = this.getActivity().getResources().getDisplayMetrics().density;

		messageListView = (FlipperListView) this.getActivity().findViewById(R.id.messageListView);
		ila = new InfoListAdapter(this.getActivity(), getString(R.string.server_ip), scale);
		messageListView.setAdapter(ila);
		messageListView.setOnFlipperDeleteListener(this);

		messageListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				System.out.println(position + " is clicked!");
				onMessageItemClicked(position - 1);
			}

		});
	}

	public void onMessageItemClicked(int position) {
		System.out.println(position + " is clicked!");
		Intent newIntent = new Intent(this.getActivity(), ShoppingActivity.class);
		newIntent.putExtra("positon", position);
		startActivity(newIntent);
	}

	@Override
	public void onDownPullRefresh() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);
				ila.refreshData();				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				ila.notifyDataSetChanged();
				messageListView.hideHeaderView();
			}

		}.execute(new Void[] {});
	}

	@Override
	public void onLoadingMore() {
		messageListView.hideFooterView();
	}

	@Override
	public void getMoveY(float moveX, float moveY) {
		this.moveX = (int) moveX;
		this.moveY = (int) moveY;
	}

	@Override
	public void onFlipping(float xPosition, float yPosition, float apartX, float apartY) {
		int index = messageListView.pointToPosition((int) xPosition, moveY);
		if (index >= 0) {
			int firstVisible = messageListView.getFirstVisiblePosition();
			View v = messageListView.getChildAt(index - firstVisible);
			RelativeLayout myLayoutItem = (RelativeLayout) v.findViewById(R.id.list_item);
			int temp = (int)apartX;
			if (myLayoutItem != null) {
				moveX = moveX + temp; // 计算移动距离
				if (Math.abs(moveX) < (35 * scale + 0.5f)
						&& InfoListAdapter.infoList.get(index - 1).isDelSign == false) {
					myLayoutItem.scrollBy(-temp, 0);
				}
			}
		}
	}

	@Override
	public void restoreView(float x, float y, boolean tag) {
		// 获取需要移动的listview项
		int index = messageListView.pointToPosition((int)x, moveY);
		if (index >= 0) {
			int firstVisible = messageListView.getFirstVisiblePosition();
			View v = messageListView.getChildAt(index- firstVisible);
			for (int i = 0; i < InfoListAdapter.infoList.size(); i++) {
				InfoListAdapter.infoList.get(i).isDelSign = false;
			}
			RelativeLayout myLayoutItem = (RelativeLayout) v.findViewById(R.id.list_item);
			if (tag) {
				System.out.println("restore true");
				InfoListAdapter.infoList.get(index - 1).isDelSign = true;
				int listview_num = messageListView.getChildCount();
				for (int i = 0; i < listview_num; i++) {
					View viewi = messageListView.getChildAt(i);
					RelativeLayout myLayoutItemi = (RelativeLayout) viewi.findViewById(R.id.list_item);
					if (myLayoutItemi != null) {
						if ((index - 1) != i) {
							myLayoutItemi.scrollBy(0, 0);
						} else {
							myLayoutItem.scrollBy((int) (35 * scale + 0.5f), 0); // 露出删除按钮35 * scale + 0.5f
							ImageButton ib = (ImageButton) myLayoutItem.findViewById(R.id.info_item_delete_button);
							ib.setFocusable(true);
							ib.setClickable(true);
							ib.setFocusableInTouchMode(true);
						}
					}
				}				
			} else {
				System.out.println("restore true");
				InfoListAdapter.infoList.get(index - 1).isDelSign = false;
				if (myLayoutItem != null) {
					myLayoutItem.scrollTo(0, 0);
				}
			}
		}
		moveY = 0;
	}


}
