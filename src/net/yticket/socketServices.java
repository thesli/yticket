package net.yticket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.ReconnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 8/31/13.
 */
public class socketServices extends Service {
    SocketIOClient socket;
    String DEBUGTAG = "socketServicesDEBUGTAG";
    private ConnectCallback connectCb;
    private Handler h;
    private IBinder mbinder =new MyBinder();

    @Override
    public void onCreate() {
        connectCb = new ConnectCallback() {

            @Override
            public void onConnectCompleted(Exception e, SocketIOClient client) {
                if (e != null) {
                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    v.vibrate(1000);
                    l("connect failure");
                    Toast.makeText(getApplicationContext(), "THE SERVER NOT FOUND,NOW STOP THE SERVICES", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(), socketServices.class);
                    stopService(i);
                    e.printStackTrace();
                }
                socket.emit("here");
                DisconnectCallback disconnectCb = new DisconnectCallback() {
                    @Override
                    public void onDisconnect(Exception e) {
                        l("onDisconnected");
                    }
                };
                /*write the callback*/
                ErrorCallback errorCb = new
                        ErrorCallback() {
                            @Override
                            public void onError(String s) {
                                l("onError");
                            }
                        };
                JSONCallback jsonCb = new JSONCallback() {
                    @Override
                    public void onJSON(JSONObject jsonObject, Acknowledge acknowledge) {
                        l(jsonObject.toString() + acknowledge.toString());
                    }
                };
                ReconnectCallback reconnectCb = new
                        ReconnectCallback() {
                            @Override
                            public void onReconnect() {
                                l("onReconnected");
                            }
                        };
                StringCallback stringCb = new
                        StringCallback() {
                            @Override
                            public void onString(String s, Acknowledge acknowledge) {
                                l(s + acknowledge.toString());
                            }
                        };
                EventCallback evtCb = new
                        EventCallback() {
                            @Override
                            public void onEvent(String s, JSONArray jsonArray, Acknowledge acknowledge) {
                                l(s + acknowledge.toString());
                            }
                        };

                /*set the stuff*/
                client.setReconnectCallback(reconnectCb);
                client.setErrorCallback(errorCb);
                client.setStringCallback(stringCb);
                client.setJSONCallback(jsonCb);
                client.setDisconnectCallback(disconnectCb);
                client.addListener("event", evtCb);
            }
        };
        h = new Handler();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        socket.connect("http://192.168.1.111:3040", connectCb, h);
        Toast.makeText(getApplicationContext(), "the services started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

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

    public void l(String s) {
        Log.d(DEBUGTAG, s);
    }

    public class MyBinder extends Binder {
        socketServices getService(){
            return socketServices.this;
        };
    }
    public void sendMsg(String s){
        socket.emit(s);
    }
}
