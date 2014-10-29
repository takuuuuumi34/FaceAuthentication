package com.example.takumi.faceauthentication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;


public class MyActivity extends Activity {
  public static int port = 60000;
  public static String address = "192.168.110.95";
  Socket echoSocket = null;
  FileInputStream fis = null;
  BufferedInputStream bin = null;
  BufferedOutputStream bos = null;
  DataOutputStream dos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

  public void onClick(View v){
    switch (v.getId()){
      case R.id.button:
        Intent intent = new Intent(this, PictureActivity.class);
        startActivity(intent);
        break;
    }
  }

  public void onClickAuthentication(View v){
    Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/camera_test.bmp");
    ImageView image1 = (ImageView)findViewById(R.id.imageView);
    image1.setImageBitmap(bmp);
    Runnable sender = new Runnable() {
      @Override
      public void run() {
        try {
          echoSocket = new Socket(address, port);
          System.out.println("接続完了！");
        }catch(Exception e){
          e.printStackTrace();
        }

        try{
          //send file
          byte[] data = new byte[1024];
          fis = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/camera_test.bmp");
          bin = new BufferedInputStream(fis);
          bos = new BufferedOutputStream(echoSocket.getOutputStream());
          dos = new DataOutputStream(bos);
          int len;
          while ((len = bin.read(data)) != -1) {
            dos.write(data, 0, len);
            dos.flush();
          }
          System.out.println("sent file");

          dos.close();
          bos.close();
          bin.close();
          fis.close();

          if(echoSocket != null){
            try{
              echoSocket.close();
            }catch(Exception e){
              e.printStackTrace();
            }
          }
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    };
    Thread th = new Thread(sender);
    th.start();
  }
}
