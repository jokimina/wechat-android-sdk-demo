package net.sourceforge.simcpux;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.JumpToOfflinePay;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity{
	
	private Button gotoBtn, regBtn, launchBtn, scanBtn, subscribeMsgBtn,subscribeMiniProgramMsgBtn;
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		checkPermission();
    	api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);

    	regBtn = (Button) findViewById(R.id.reg_btn);
    	regBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    api.registerApp(Constants.APP_ID);
			}
		});
    	
        gotoBtn = (Button) findViewById(R.id.goto_send_btn);
        gotoBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        startActivity(new Intent(MainActivity.this, SendToWXActivity.class));
//		        finish();
			}
		});
        
        launchBtn = (Button) findViewById(R.id.launch_wx_btn);
        launchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "launch result = " + api.openWXApp(), Toast.LENGTH_LONG).show();
			}
		});
        
		subscribeMsgBtn = (Button) findViewById(R.id.goto_subscribe_message_btn);
		subscribeMsgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SubscribeMessageActivity.class));
//				finish();
			}
		});

        subscribeMiniProgramMsgBtn = (Button) findViewById(R.id.goto_subscribe_mini_program_msg_btn);
        subscribeMiniProgramMsgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubscribeMiniProgramMsgActivity.class));
			}
		});


		View jumpToOfflinePay = (Button) findViewById(R.id.jump_to_offline_pay);
		jumpToOfflinePay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//                int wxSdkVersion = api.getWXAppSupportAPI();
//                if (wxSdkVersion >= Build.OFFLINE_PAY_SDK_INT) {
//                    api.sendReq(new JumpToOfflinePay.Req());
//                }else {
//                    Toast.makeText(MainActivity.this, "not supported", Toast.LENGTH_LONG).show();
//                }
				try {
					prepareWechatOrder();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void prepareWechatOrder() throws IOException {
    	new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request createOrderRequest = new Request.Builder()
                    .url("http://wzt.ca327358d67984123910974f87e8b6b70.cn-hangzhou.alicontainer.com/payment/wechat/app/create")
                    .header("sign", "test")
                    .header("refid", "d6e1f469-5cfc-447a-b393-fc07d0d910d1")
                    .header("token", "38672ce86ea4a1f1ea8a706c4cd6e1f4")
                    .post(new FormBody.Builder().add("platform", "1").add("productKey", "vip_month").build())
                    .build();
			Response resp = null;
			try {
				resp = client.newCall(createOrderRequest).execute();
				assert resp.body() != null;
				String respBody = resp.body().string();
				Log.i("创建订单返回: ", respBody);
				JSONObject jsonObject = new JSONObject(respBody);
				JSONObject info = jsonObject.getJSONObject("data");

				PayReq request = new PayReq();
				request.appId = info.getString("appId");
				request.partnerId = info.getString("partnerId");
				request.prepayId= info.getString("prepayId");
				request.packageValue = info.getString("packageValue");
				request.nonceStr= info.getString("nonceStr");
				request.timeStamp= info.getString("timeStamp");
				request.sign= info.getString("sign");
				api.sendReq(request);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void checkPermission() {
		int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					Constants.PERMISSIONS_REQUEST_STORAGE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case Constants.PERMISSIONS_REQUEST_STORAGE: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				} else {
					Toast.makeText(MainActivity.this,"Please give me storage permission!",Toast.LENGTH_LONG).show();
				}
				return;
			}
		}
	}

}