package fi.jamk.signalstrength;

import android.os.AsyncTask;
import android.util.Log;
import static java.util.UUID.randomUUID;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

public class HttpRequestTask extends AsyncTask<Void, Void, DataLocation> {

    @Override
    protected DataLocation doInBackground(Void... params) {
        try {
            final String uri = "http://84.251.189.202:8080/signals/pst";

            UUID uuid = randomUUID();
            Date date = new Date();
            DataLocation dl = new DataLocation(uuid, date, 62.341208, 25.858931, 89, 90, 91, 92);
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

