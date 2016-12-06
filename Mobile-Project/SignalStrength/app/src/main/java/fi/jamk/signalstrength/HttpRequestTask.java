package fi.jamk.signalstrength;

import android.os.AsyncTask;
import android.util.Log;
import static java.util.UUID.randomUUID;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;
import java.util.zip.DataFormatException;

// This class is responsible for sending POST requests to rest
public class HttpRequestTask extends AsyncTask<DataLocation, Void, DataLocation> {
    DataLocation dl;
    String uri;

    public HttpRequestTask(DataLocation dl, String uri){
        this.dl = dl;
        this.uri = uri;
    }

    @Override
    protected DataLocation doInBackground(DataLocation... params ) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            DataLocation dl1 = restTemplate.postForObject(uri, dl, DataLocation.class);
            return dl1;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(DataLocation dl) {

    }

}

