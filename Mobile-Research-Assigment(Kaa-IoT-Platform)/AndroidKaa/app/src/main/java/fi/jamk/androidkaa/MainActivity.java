package fi.jamk.androidkaa;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.ecf.AndroidDemoEcf;
import org.kaaproject.kaa.demo.ecf.TempAnswer;
import org.kaaproject.kaa.demo.ecf.TempRequest;

public class MainActivity extends AppCompatActivity {

    private KaaClient kaaClient;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
    }


    public void kaaStart(View v){
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


    public void attachUser(View v){
        kaaClient.attachUser("User", "user123", new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse response) {
                Toast.makeText(getBaseContext(), "User attached to server", Toast.LENGTH_LONG).show();
                if (response.getResult() == SyncResponseResultType.SUCCESS){
                    //onUserAttached();
                }
                else{
                    kaaClient.stop();

                    Toast.makeText(getBaseContext(), "kaaClient stopped something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void detachUser(View v){
        kaaClient.detachEndpoint(new EndpointKeyHash(kaaClient.getEndpointKeyHash()), new OnDetachEndpointOperationCallback() {
            @Override
            public void onDetach(SyncResponseResultType syncResponseResultType) {
                Toast.makeText(getBaseContext(), "User Detached from server", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void listenEvents(){
        final EventFamilyFactory eff = kaaClient.getEventFamilyFactory();
        final AndroidDemoEcf aecf = eff.getAndroidDemoEcf();
        aecf.addListener(new AndroidDemoEcf.Listener(){
            @Override
            public void onEvent(TempRequest event, String source){


            }

            @Override
            public void onEvent(TempAnswer tempAnswer, String s) {
                Toast.makeText(getBaseContext(), "asd", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendTempRequest(View v){
        final EventFamilyFactory eff = kaaClient.getEventFamilyFactory();
        final AndroidDemoEcf aecf = eff.getAndroidDemoEcf();
        aecf.sendEventToAll(new TempRequest(kaaClient.getEndpointKeyHash()));
    }

}



