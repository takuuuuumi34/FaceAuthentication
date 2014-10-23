package com.example.takumi.faceauthentication;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileOutputStream;


public class PictureActivity extends Activity {
  private Camera myCamera;
  static private final int PREVIEW_WIDTH = 640;
  static private final int PREVIEW_HEIGHT = 480;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture);

    SurfaceView mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    SurfaceHolder holder = mySurfaceView.getHolder();
    holder.addCallback(mSurfaceListener);
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.picture, menu);
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

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (myCamera != null) {
        myCamera.takePicture(mShutterListener, null, mPictureListener);
      }
    }
    return true;
  }

  //Camera
  private SurfaceHolder.Callback mSurfaceListener =
    new SurfaceHolder.Callback() {
      public void surfaceCreated(SurfaceHolder holder) {
        myCamera = Camera.open();
        try {
          myCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      public void surfaceDestroyed(SurfaceHolder holder) {
        myCamera.release();
        myCamera = null;
      }

      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
      }
    };

  // シャッターが押されたとき
  private Camera.ShutterCallback mShutterListener =
    new Camera.ShutterCallback() {
      public void onShutter() {
      }
    };

  // jpegイメージ生成後
  private Camera.PictureCallback mPictureListener =
    new Camera.PictureCallback() {
      public void onPictureTaken(byte[] data, Camera camera) {
        // ローカルにJPEGデータを保存する
        if (data != null) {
          FileOutputStream myFOS = null;

          try {
            myFOS = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/camera_test.jpg");
            myFOS.write(data);
            myFOS.close();
          } catch (Exception e) {
            e.printStackTrace();
          }

          camera.startPreview();
          finish();
        }
      }
    };
}
