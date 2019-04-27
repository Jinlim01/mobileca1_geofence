package com.example.jin.mobileca1_v2;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RemoteActivity extends AppCompatActivity {
    Messenger myService = null;
    boolean bounded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        Intent intent = new Intent(getApplicationContext(),Remote.class);

        bindService(intent,myConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection myConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    myService = new Messenger(service);
                    bounded = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    myService = null;
                    bounded = false;
                }
            };

    public void MessageSend(View view){
        if(!bounded) return;

        Message message = Message.obtain();

        Bundle bundle = new Bundle();
        bundle.putString("MyString","Received");

        message.setData(bundle);

        try{
            myService.send(message);
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }
}
