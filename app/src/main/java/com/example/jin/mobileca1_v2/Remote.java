package com.example.jin.mobileca1_v2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

public class Remote extends Service {
    final Messenger message = new Messenger(new IncomingHandler(this));

    public Remote() {
    }

    class IncomingHandler extends Handler{
        private Context appContext;

        IncomingHandler(Context context){
            appContext = context.getApplicationContext();
        }

        public void handleMessage(Message msg){
            Bundle data = msg.getData();
            String dataString = data.getString("MyString");
            Toast.makeText(getApplicationContext(),dataString,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return message.getBinder();
    }
}
