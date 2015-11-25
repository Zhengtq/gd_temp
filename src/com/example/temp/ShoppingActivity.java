package com.example.temp;

import com.gd.chat.ChatActivity;
import com.gd.model.YQClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import javabean.UserChat;

public class ShoppingActivity extends Activity implements OnRefreshListener {

	ImageView ourLogoView, partnersLogoView;
	TextView ourDescription, partnersDescription;
	ImageButton doChat;
	RefreshListView shopInfoListView;
	private static ShopInfoListAdapter sila;
	SearchView searchShopSv;
	int currentPosition = -1;
	final UserChat user = new UserChat();
	private Context context;
	private int position = -1;

	public static final int LOGIN_BY_GET_MSG = 100;
	private boolean flag = false;
	
	//b用来判断是否成功在客户端与服务器建立thread
	public Handler mHandler1 = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOGIN_BY_GET_MSG:
				flag = (Boolean)msg.obj;
				Toast.makeText(context, "flag: " + flag, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			
			if (flag) {
				Intent intent = new Intent(ShoppingActivity.this, ChatActivity.class);
				intent.putExtra("sentName", HttpThread.userInfo.username);
				intent.putExtra("receiveName", InfoListAdapter.infoList.get(position).partner.groupmembers);			    
				startActivity(intent) ;
			}
		};
	};
	
	public static Handler shopHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				sila.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping);
		context = this;

		Bundle bundle = getIntent().getExtras();
		position = bundle.getInt("position");
		if (currentPosition != position) {
			currentPosition = position;
			ShopInfoListAdapter.shopInfoList.clear();
			ShopInfoListAdapter.shopLogo.clear();
			if (sila != null) {
				sila.refreshData();
			}
		}

		System.out.println("position: " + position);
		ourLogoView = (ImageView) findViewById(R.id.iv_ours_logo);
		partnersLogoView = (ImageView) findViewById(R.id.iv_partners_logo);
		ourLogoView.setImageBitmap(MyGroupListAdapter.img.get(InfoListAdapter.infoList.get(position).myMapId));
		partnersLogoView.setImageBitmap(InfoListAdapter.img.get(position));

		ourDescription = (TextView) findViewById(R.id.tv_ours_messages);
		partnersDescription = (TextView) findViewById(R.id.tv_partners_messages);
		ourDescription.setText(InfoListAdapter.infoList.get(position).myGroupName);
		partnersDescription.setText(InfoListAdapter.infoList.get(position).partner.groupName);

		shopInfoListView = (RefreshListView) findViewById(R.id.rlv_shop_info);
		sila = new ShopInfoListAdapter(this, getString(R.string.server_ip));
		shopInfoListView.setAdapter(sila);
		shopInfoListView.setOnRefreshListener(this);
		
		searchShopSv = (SearchView) findViewById(R.id.search_shop_sv);

		doChat = (ImageButton) findViewById(R.id.communicate_bt);

		doChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setThread(HttpThread.userInfo.username, HttpThread.userInfo.password);
				System.out.println(flag + "???");					
			}

		});

		shopInfoListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}

		});

		searchShopSv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(ShoppingActivity.this, SearchShopActivity.class);
				startActivity(newIntent);
			}
			
		});
	}

	void setThread(final String a, final String p)
	{	
		user.setUsername(a);
		user.setPassword(p);
		user.setOperation("chat");	
		final YQClient tmp = new YQClient(this, getString(R.string.server_ip_addr)){};

		new Thread() 
		{
			public void run()
			{	
				boolean b = tmp.sendLoginInfo(user);
				Message msg1 = new Message();
				msg1.what = LOGIN_BY_GET_MSG;
				msg1.obj = b;
				mHandler1.sendMessage(msg1);		
			};
		}.start();		
	}

	@Override
	public void onDownPullRefresh() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);
				sila.refreshData();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				sila.notifyDataSetChanged();
				shopInfoListView.hideHeaderView();
			}

		}.execute(new Void[] {});
	}

	@Override
	public void onLoadingMore() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				SystemClock.sleep(1000);	
				sila.loadMoreData();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				sila.notifyDataSetChanged();
				shopInfoListView.hideFooterView();
			}

		}.execute(new Void[] {});
	}
}
