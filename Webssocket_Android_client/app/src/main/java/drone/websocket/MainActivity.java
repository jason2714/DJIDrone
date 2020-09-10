package drone.websocket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "ServerLog";
    private Socket client;
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_IP = "140.121.198.99";
    private TextView tv_show;
    private String tmp;
    private BufferedReader br;
    private DataInputStream ids;
    private Button mBtnOpenSocket,mBtnCloseSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_show = findViewById(R.id.tv_show);
        mBtnOpenSocket = findViewById(R.id.btn_openSocket);
        mBtnCloseSocket = findViewById(R.id.btn_closeSocket);
        connect();
    }

    public class Onclick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){

            }
        }
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket(SERVER_IP, SERVER_PORT);
                    br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    ids = new DataInputStream(client.getInputStream());
                    if (client.isConnected()) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                tv_show.setText("Connected");
                            }
                        });
                    }
                    Log.d(TAG, "Done Connecting");
//                    while(true){
//                        Log.d(TAG, "check data");
//                        tmp = br.readLine();
////                        tmp = ids.readUTF();
//                        Log.d(TAG, tmp);
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                //Do your UI operations like dialog opening or Toast here
//                                tv_show.setText(tmp);
//                            }
//                        });
//                    }
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                tmp = br.readLine();
                                Log.d(TAG, "check data");
                                Log.d(TAG, tmp);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        //Do your UI operations like dialog opening or Toast here
                                        tv_show.setText(tmp);
                                    }
                                });
                                PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
                                printwriter.write("receive data success"); // write the message to output stream
                                printwriter.flush();
//                                printwriter.close();
                            } catch (IOException e) {
                                Log.d(TAG, "read file error");
                                Log.d(TAG, e.toString());
                                e.printStackTrace();
                            }
                        }
                    }, 0, 100);
//                    Log.d(TAG, "out of timer");
                } catch (UnknownHostException e1) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Unknown host please make sure IP address", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e2) {
                    Log.d(TAG, "IOException");
                    Log.d(TAG, e2.toString());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}