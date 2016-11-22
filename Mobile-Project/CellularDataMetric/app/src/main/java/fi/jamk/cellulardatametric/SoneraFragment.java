package fi.jamk.cellulardatametric;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SoneraFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray golfCourses;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_sonera, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.sonera);
        //textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FetchDataTask task = new FetchDataTask();

        task.execute("http://ptm.fi/jamk/android/golf_courses.json");
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // store map object to member variable
        mMap = googleMap;
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return json;
        }

        protected void onPostExecute(JSONObject json) {
            try {
                BitmapDescriptor bitmapDescriptor = null;
                golfCourses = json.getJSONArray("kentat");
                for (int i = 0; i < golfCourses.length(); i++) {
                    JSONObject golfJson = golfCourses.getJSONObject(i);
                    LatLng latlng = new LatLng(golfJson.getDouble("lat"), golfJson.getDouble("lng"));


                    //Toast.makeText(getApplicationContext(), golfJson.getString("Tyyppi"), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }

}
