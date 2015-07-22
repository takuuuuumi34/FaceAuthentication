package com.example.takumi.faceauthentication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;


public class MyActivity extends Activity {
  public static int port = 60000;
  public static String address = "192.168.110.129";
  //public static String address = "192.168.2.112";
  Socket echoSocket = null;
  Socket sendSocket = null;
  FileInputStream fis = null;
  BufferedInputStream bin = null;
  DataInputStream din = null;
  Reader reader = null;
  BufferedOutputStream bos = null;
  DataOutputStream dos = null;
  String name = "";
  CharSequence nameCS;
  Handler mHandler = new Handler();

  CascadeClassifier mJavaDetector;

  private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      switch (status) {
        case LoaderCallbackInterface.SUCCESS:
        {
          System.out.println("OpenCV loaded successfully");
        }
        break;
        default:
        {
          super.onManagerConnected(status);
        }
        break;
      }
    }};

  @Override
  public void onResume() {
    super.onResume();
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
  }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        System.out.println(Environment.getExternalStorageDirectory().getPath());
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
        int requestCode = 1001;
        startActivityForResult(intent, requestCode);
        break;
    }
  }

  public void onActivityResult( int requestCode, int resultCode, Intent intent ){
    if( requestCode == 1001 ){
      if( resultCode == Activity.RESULT_OK ){
        Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/camera_test.bmp").copy(Bitmap.Config.ARGB_8888, true);
/*
        try {
          // load cascade file from application resources
          InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
          File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
          File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
          FileOutputStream os = new FileOutputStream(mCascadeFile);

          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
          }
          is.close();
          os.close();

          mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
          if (mJavaDetector.empty()) {
            mJavaDetector = null;
          }

          cascadeDir.delete();

        } catch (IOException e) {
          e.printStackTrace();
        }

        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);

        MatOfRect faceDetections = new MatOfRect();
        mJavaDetector.detectMultiScale(img, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        for(Rect rect : faceDetections.toArray()) {
          Core.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
            new Scalar(0, 255, 0), 5);
        }
        

        Utils.matToBitmap(img, bmp);*/
        ImageView image1 = (ImageView)findViewById(R.id.imageView);
        image1.setImageBitmap(bmp);

      }
    }
  }

  public void onClickAuthentication(View v){
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

        try {
          sendSocket = new Socket(address, port);
          System.out.println("受信スレッド接続完了！");
        }catch(Exception e){
          e.printStackTrace();
        }

        try{
          din = new DataInputStream(sendSocket.getInputStream());
          bin = new BufferedInputStream(din);
          reader = new InputStreamReader(bin, "Shift_JIS");
          name = "";
          for(;;) {
            final int readChar = reader.read();
            //Streamの終わりに達して読み込むデータがない場合
            if ((char)readChar == '\0') break;
            // charとして画面出力
            name = name + (char)readChar;
          }
          nameCS = name;
          //thread内からUI操作はできないのでhandlerにpostする。
          mHandler.post(new Runnable() {
            public void run() {
              TextView textView = (TextView) findViewById(R.id.name);
              textView.setText(nameCS);
            }
          });
          din.close();
          bin.close();

          if(sendSocket != null){
            try{
              sendSocket.close();
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
