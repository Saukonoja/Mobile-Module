package fi.jamk.kaademo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import demo.SignalData;


public class KaaActivity extends AppCompatActivity {

    private static KaaClient kaaClient;

    public void kaaStart(Context mContext){
        kaaClient = Kaa.newClient(new AndroidKaaPlatformContext(mContext), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
                attachUser();
            }

            @Override
            public void onStopped() {

            }
        });
        kaaClient.start();
    }


    public void attachUser(){

        kaaClient.attachUser("User", "user123", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                Toast.makeText(getBaseContext(), "User attached to server", Toast.LENGTH_LONG).show();
                if (response.getResult() == SyncResponseResultType.SUCCESS){
                    MainActivity.isAttached = true;
                }
                else{
                    kaaClient.stop();
                    MainActivity.isAttached = false;
                }
            }
        });
    }

    public void sendLog(double lan, double lon, int str){
        SignalData signalData = new SignalData(lan, lon, str);
        kaaClient.addLogRecord(signalData);
    }
}
