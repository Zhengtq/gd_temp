package com.example.temp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyGroups extends Fragment {
	private ListView myGroupListView;
	private static MyGroupListAdapter mmgla = null;
	public static int currentPosition = 0;
	public static ImageView myCurrentGroupLogo;
	public static TextView myCurrentGroupInfo;
	Button createGroupBt;

	public static Handler myGroupHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				mmgla.notifyDataSetChanged();
				if (currentPosition < mmgla.groupDataList.size() && mmgla.groupDataList.get(currentPosition) != null) {
					GroupData currentGroup = mmgla.groupDataList.get(currentPosition);
					myCurrentGroupInfo
					.setText(currentGroup.groupName + "\n" + currentGroup.groupmembers + "\n" + currentGroup.activity);
				}
				mmgla.loadImgs();
				break;
			case 2:
				mmgla.notifyDataSetChanged();
				if (currentPosition < mmgla.img.size() && mmgla.img.get(currentPosition) != null)
					myCurrentGroupLogo.setImageBitmap(mmgla.img.get(currentPosition));
				break;
			}				
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_my_groups, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mmgla == null) {
			myGroupListView = (ListView) getActivity().findViewById(R.id.myGroupListView);
			mmgla = new MyGroupListAdapter(this.getActivity(), getString(R.string.server_ip));
			myGroupListView.setAdapter(mmgla);			

			myCurrentGroupLogo = (ImageView) getActivity().findViewById(R.id.myCurrentGroupLogo);
			myCurrentGroupInfo = (TextView) getActivity().findViewById(R.id.myCurrentGroupInfo);
			if (currentPosition < mmgla.img.size() && mmgla.img.get(currentPosition) != null)
				myCurrentGroupLogo.setImageBitmap(mmgla.img.get(currentPosition));
			else
				myCurrentGroupLogo.setImageResource(R.drawable.empty_photo);
			if (currentPosition < mmgla.groupDataList.size() && mmgla.groupDataList.get(currentPosition) != null) {
				GroupData currentGroup = mmgla.groupDataList.get(currentPosition);
				myCurrentGroupInfo
				.setText(currentGroup.groupName + "\n" + currentGroup.groupmembers + "\n" + currentGroup.activity);
			}
			else {
				myCurrentGroupInfo.setText("loading...");
			}
		}

		createGroupBt = (Button)getActivity().findViewById(R.id.createGroupBt);
		createGroupBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(getActivity(), CreateGroupActivity.class);
				startActivity(newIntent);
			}

		});


	}
}
