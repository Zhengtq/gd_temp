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
	private String chatContent;//��Ϣ����
	ListView chatListView;
	public List<ChatEntity> chatEntityList=new ArrayList<ChatEntity>();//������������
	private String chatNick;
	public static int[] avatar = new int[]{R.drawable.avatar_default, R.drawable.h001, R.drawable.h002, R.drawable.h003,
			R.drawable.h004, R.drawable.h005, R.drawable.h006};

	MyBroadcastReceiver br;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_chat);
		//����top�����Ϣ,�������ϸ�intent��ֵ������������Ϊ�˷���ĳ�Ĭ��ֵ

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

					//ͨ��account�ҵ����̣߳��Ӷ��õ�OutputStream
					oos = new ObjectOutputStream(ManageClientConServer.getClientConServerThread(chatUsername).getS().getOutputStream());
					//�õ����������
					chatContent=et_input.getText().toString();
					//���EditText
					et_input.setText("");
					//������Ϣ					
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
					//������������
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

		//ע��㲥
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

	//�㲥������
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] mes = intent.getStringArrayExtra("message");

			//�����������ݣ�2��3��4�ֱ���ͼƬ�����ݣ�ʱ��
			updateChatView(new ChatEntity(
					Integer.parseInt(mes[2]),
					mes[3],
					mes[4],
					true));
		}
	}

	public void updateChatView(ChatEntity chatEntity){
		//Ϊ����ʾ��ʷ������������ʱ��Ҫ��list�����ݽṹ
		chatEntityList.add(chatEntity);
		//���listview�Ŀؼ���ID
		chatListView=(ListView) findViewById(R.id.lv_chat);
		//���������listview���������ݱ�����chatEntityList��
		chatListView.setAdapter(new ChatAdapter(this,chatEntityList));
	}

}
