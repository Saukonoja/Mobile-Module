package fi.jamk.kaademo;

import android.content.Context;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;



public class MainActivity extends AppCompatActivity {

    private KaaClient kaaClient;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        kaaStart();
        sendLog();
        attachUser();

    }

    public void kaaStart(){
        kaaClient = Kaa.newClient(new AndroidKaaPlatformContext(mContext), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));
            }

            @Override
            public void onStopped() {

            }
        });
        kaaClient.start();
    }

    public void sendLog(){
        kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(1));

    }

    public void attachUser(){
        kaaClient.attachUser("User", "user123", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                Toast.makeText(getBaseContext(), "User attached to server", Toast.LENGTH_LONG).show();
                if (response.getResult() == SyncResponseResultType.SUCCESS){
                    //onUserAttached();
                }
                else{
                    kaaClient.stop();
                    Toast.makeText(getBaseContext(), "kaaClient stopped something wwnt wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
