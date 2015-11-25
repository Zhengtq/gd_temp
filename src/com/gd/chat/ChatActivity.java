package com.gd.chat;

import com.example.temp.R;
import com.gd.chat.ChatEntity;
import com.gd.common.MyTime;
import com.gd.common.YQMessage;
import com.gd.common.YQMessageType;
import com.gd.model.ManageClientConServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends Activity {
	EditText et_input;
	private String chatContent;//消息内容
	ListView chatListView;
	public List<ChatEntity> chatEntityList=new ArrayList<ChatEntity>();//所有聊天内容
	private String chatNick;
	public static int[] avatar = new int[]{R.drawable.avatar_default, R.drawable.h001, R.drawable.h002, R.drawable.h003,
			R.drawable.h004, R.drawable.h005, R.drawable.h006};

	MyBroadcastReceiver br;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);
		//设置top面板信息,本来是上个intent的值传过来，现在为了方便改成默认值

		final String chatUsername=getIntent().getStringExtra("sentName");
		final String receiveName=getIntent().getStringExtra("receiveName");

		final String receivers[]=receiveName.split(",");


		int receiveCount=1;
		for(int i = 0; i < receiveName.length(); i++){
			if(receiveName.charAt(i) == '|'){
				receiveCount++;
			}
		}
		final int recieversNumber = receiveCount;


		int chatAvatar = 1;
		chatNick= chatUsername;

		ImageView avatar_iv=(ImageView) findViewById(R.id.chat_top_avatar);
		avatar_iv.setImageResource(avatar[chatAvatar]);
		TextView nick_tv=(TextView) findViewById(R.id.chat_top_nick);
		nick_tv.setText(chatNick);


		et_input=(EditText) findViewById(R.id.et_input);
		findViewById(R.id.ib_send).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				ObjectOutputStream oos;
				try {

					//通过account找到该线程，从而得到OutputStream
					oos = new ObjectOutputStream(ManageClientConServer.getClientConServerThread(chatUsername).getS().getOutputStream());
					//得到输入的数据
					chatContent=et_input.getText().toString();
					//清空EditText
					et_input.setText("");
					//发送消息					
					YQMessage m=new YQMessage();
					m.setType(YQMessageType.COM_MES);
					m.setSender(chatUsername);
					m.setSenderNick("zhenima");
					m.setSenderAvatar(2);
					m.setReceiver(receiveName);
					m.setReceivers(receivers);
					m.setContent(chatContent);
					m.setSendTime(MyTime.geTimeNoS());
					m.setReceiversNumber(recieversNumber);
					oos.writeObject(m);
					//更新聊天内容
					updateChatView(new ChatEntity(
							1,
							chatContent,
							MyTime.geTime(),
							false));					

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		//注册广播
		IntentFilter myIntentFilter = new IntentFilter(); 		
		myIntentFilter.addAction("org.yhn.yq.mes");
		br=new MyBroadcastReceiver();
		registerReceiver(br, myIntentFilter);

	}
	@Override
	public void finish() {
		unregisterReceiver(br);
		super.finish();
	}

	//广播接收器
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] mes = intent.getStringArrayExtra("message");

			//更新聊天内容，2，3，4分别是图片，内容，时间
			updateChatView(new ChatEntity(
					Integer.parseInt(mes[2]),
					mes[3],
					mes[4],
					true));
		}
	}

	public void updateChatView(ChatEntity chatEntity){
		//为了显示历史聊天的情况，此时需要用list的数据结构
		chatEntityList.add(chatEntity);
		//获得listview的控件的ID
		chatListView=(ListView) findViewById(R.id.lv_chat);
		//用数据填充listview，其中数据保存在chatEntityList中
		chatListView.setAdapter(new ChatAdapter(this,chatEntityList));
	}

}
