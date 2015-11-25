package com.example.temp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	Button submitBt;
	Button resetBt;
	Button signInBt;
	EditText username;
	EditText password;
	CheckBox saveUserCb;
	private SharedPreferences mSettings = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
		submitBt = (Button)findViewById(R.id.submit);
		saveUserCb = (CheckBox)findViewById(R.id.save_cookie_cb);
		signInBt = (Button)findViewById(R.id.sign_in_Bt);
		
		mSettings = getSharedPreferences("data_save", Activity.MODE_PRIVATE);
		String usernameStr = mSettings.getString("username", "");
		String passwordStr = mSettings.getString("password", "");
		if (usernameStr.compareTo("") != 0 || passwordStr.compareTo("") != 0) {
			username.setText(usernameStr);
			password.setText(passwordStr);
			check();
		}
		
		submitBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				check();
			}
			
		});
		
		resetBt = (Button)findViewById(R.id.reset);
		resetBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				username.setText("");
				password.setText("");
				username.setHint("username");
				password.setHint("password");
				System.out.println("reset button is clicked!");
			}
			
		});
		signInBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(Login.this, SignInActivity.class);
				startActivity(newIntent);
			}
			
		});
	}
	
	public void check() {
		System.out.println(username.getText().toString() + "?");
		System.out.println(password.getText().toString() + "?");
		System.out.println(getString(R.string.server_ip) + "?");
		// 不允许在主线程金星网络访问，只能将网络访问操作单独放到一个线程中
		new HttpThread(username.getText().toString(), password.getText().toString(), getString(R.string.server_ip)).start();
		action();
	}
	
	public void startIntent() {
		// 跳转到group date的list页面
		Intent listIntent = new Intent(this, MainActivity.class);
		startActivity(listIntent);
	}

	public void action() {
		while (true) {
			if (flagLog == 0) {
				// 输出错误信息
				Toast errorToast = Toast.makeText(this, "错误的用户名或密码", Toast.LENGTH_LONG);
				errorToast.show();
				break;
			}
			else if (flagLog == 1) {
				if (saveUserCb.isChecked()) {
					SharedPreferences.Editor editor = mSettings.edit();
					editor.putString("username", username.getText().toString());
					editor.putString("password", password.getText().toString());
					editor.commit();
				}
				
				startIntent();
				break;
			}
		}
	}
	
	public static int flagLog = -1;
	public static void receiveFlag(int flag) {
		flagLog = flag;
	}
}
