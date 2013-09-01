package net.yticket;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class MainActivity extends Activity {
    private Button scanButton;
    private Bitmap mBitmap;
    private ImageView mImageView;
    private Button btn;
    Intent servicesIntent;
    private socketServices s;
    Messenger msger;
    Message msg;
    Handler h;
    Handler ha;
    ImageScanner scanner;
    android.hardware.Camera.PreviewCallback previewCb;
    private Camera mCamera;
    private cameraPreview mPreview;
    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private TextView scanText;

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        init();
        initCamera();
        setOnClick();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            for (int camId = 0; camId < Camera.getNumberOfCameras(); camId++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(camId, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    c = Camera.open(camId);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void setOnClick(){
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(barcodeScanned){
                    barcodeScanned = false;
                    scanText.setText("scanning.....");
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                }
            }
        });
    }

    public void initCamera(){
        scanButton = (Button)findViewById(R.id.scanbtn);
        scanText = (TextView)findViewById(R.id.scantext);
        mCamera = getCameraInstance();
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        ha = new Handler(getApplicationContext().getMainLooper());
        mPreview = new cameraPreview(this, mCamera, previewCb, null);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraframe);
        preview.addView(mPreview);
        previewCb = new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);

                if (result != 0) {
                    previewing = false;
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();

                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) {
                        scanText.setText("barcode result " + sym.getData());
                        barcodeScanned = true;
                    }
                }
            }
        };
    }

    private void init() {
        mImageView = (ImageView) findViewById(R.id.qrimg);
        btn = (Button) findViewById(R.id.btn);
        h = new Handler(getApplicationContext().getMainLooper());
        msger = new Messenger(h);
        servicesIntent = new Intent(MainActivity.this,socketServices.class);
        startService(servicesIntent);
        bindService(servicesIntent, mConnection, BIND_AUTO_CREATE);
        generateQRCode("hello World");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateQRCode("fuck the world then");
                if(s!=null){
                    s.sendMsg("fuck you shit head");
                }
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            s = ((socketServices.MyBinder) iBinder).getService();
            Toast.makeText(getApplicationContext(),"socketServices Connected",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            s = null;
        }
    };

    public void generateQRCode(String data) {
        Writer writer = new QRCodeWriter();
        String finaldata = Uri.encode(data, "ISO-8559-1");
        try {
            BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 100, 100);
            mBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if(mBitmap != null){
            mImageView.setImageBitmap(mBitmap);
        }
    }


    /*DEBUG STUFF*/
    String DEBUGTAG = "MainActivityDEBUGTAG";

    public void l(String s) {
        Log.d(DEBUGTAG, s);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void l(String s, String t) {
        if (t.equals("d")) {
            Log.d(DEBUGTAG, s);
        } else if (t.equals("e")) {
            Log.d(DEBUGTAG, s);
        } else if (t.equals("v")) {
            Log.v(DEBUGTAG, s);
        } else if (t.equals("i")) {
            Log.i(DEBUGTAG, s);
        } else if (t.equals("wtf")) {
            Log.wtf(DEBUGTAG, s);
        } else if (t.equals("w")) {
            Log.w(DEBUGTAG, s);
        } else {
            l(s);
        }
    }
}
