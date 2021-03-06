package xr.loginfornetdemo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import xr.loginfornetdemo.utils.SharedPreferenceUtil;
import xr.loginfornetdemo.utils.StreamUtils;

public class MainActivity extends Activity implements android.view.View.OnClickListener {
	private Button loginButton;
	private CheckBox remeBox;
	private EditText userText, passText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		userText = (EditText) findViewById(R.id.userText);
		passText = (EditText) findViewById(R.id.passText);
		loginButton = (Button) findViewById(R.id.loginButton);
		remeBox = (CheckBox) findViewById(R.id.remeBox);

		// 绑定事件监听器
		loginButton.setOnClickListener(this);

		// 得到用户名和密码
		Map<String, String> map = SharedPreferenceUtil.SharedgetInfo(MainActivity.this);
		if (map != null) {
			String username = map.get("username");
			String password = map.get("password");
			userText.setText(username);// 设置用户名
			passText.setText(password);
			remeBox.setChecked(true);// 设置复选框选中状态
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			Login();
			break;
		default:
			break;
		}

	}

	public void Login() {
		// 得到用户名和密码 并去掉空格
		final String username = userText.getText().toString().trim();
		final String password = passText.getText().toString().trim();
		// 查看复选框是否被选中
		final boolean isrem = remeBox.isChecked();
		// 如果用户名和密码为空
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			Toast.makeText(MainActivity.this, "用户名密码不能为空", Toast.LENGTH_SHORT).show();
		}

		//开启子线程 访问网络是耗时操作
		new Thread(new Runnable() {

			@Override
			public void run() {
				//利用Get方式
				//final boolean loginResult = loginForGet(username, password);
				//利用Post方式
				final boolean loginResult = loginForPost(username, password);
				
				//在子线程中更新UI
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (loginResult) {
							// 不为空的时候 如果复选框被选中
							if (isrem) {
								boolean result = SharedPreferenceUtil.SharedsaveInfo(MainActivity.this, username,
										password);
								if (result) {
									Toast.makeText(MainActivity.this, "用户名密码保存成功", Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(MainActivity.this, "用户名密码保存失败", Toast.LENGTH_SHORT).show();
								}

							} else {
								Toast.makeText(MainActivity.this, "无需保存", Toast.LENGTH_SHORT).show();
							}
						} else
							Toast.makeText(MainActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).start();

	}

	
	/**
	* @Title: loginForGet
	* @Description: 利用Get方式获得登陆信息
	* @param @param username
	* @param @param password
	* @param @return
	* @return boolean
	* @throws
	*/
	private boolean loginForGet(String username, String password) {

		try {
			URL url = new URL("http://172.25.10.172:8080/Web_LoginForNetDemo/servlet/LoginServlet?username=root&pwd=123");
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setRequestMethod("GET");
			openConnection.setReadTimeout(5000);

			int responseCode = openConnection.getResponseCode();
			if (responseCode == 200) {
				InputStream inputStream = openConnection.getInputStream();
				String result = StreamUtils.streamToString(inputStream);
				System.out.println(result);
				//如果返回信息包含成功 则返回真
				if (result.contains("Success")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	* @Title: loginForPost
	* @Description:利用Post方式返回登录信息
	* @param @param username
	* @param @param password
	* @param @return
	* @return boolean
	* @throws
	*/
	private boolean loginForPost(String username, String password) {

		try {
			URL url = new URL("http://172.25.10.172:8080/Web_LoginForNetDemo/servlet/LoginServlet");
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setRequestMethod("POST");
			openConnection.setReadTimeout(5000);
			
			//与Get方式不同的部分  都可以去掉
//			openConnection.setRequestProperty("Cache-Control", "max-age=0");
//			openConnection.setRequestProperty("Content-Length", "21");
//			openConnection.setRequestProperty("Origin", "http://172.25.10.172:8080");
			
			//将数据以键值对的形式写入 
			openConnection.setDoOutput(true);
			openConnection.getOutputStream().write(("username="+username+"&pwd="+password).getBytes());

			int responseCode = openConnection.getResponseCode();
			if (responseCode == 200) {
				InputStream inputStream = openConnection.getInputStream();
				String result = StreamUtils.streamToString(inputStream);
				System.out.println(result);
				if (result.contains("Success")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
