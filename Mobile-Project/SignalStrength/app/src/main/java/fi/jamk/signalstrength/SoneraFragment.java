package fi.jamk.signalstrength;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class SoneraFragment extends Fragment {
    MapView mMapView;
    private GoogleMap googleMap;
    private JSONArray soneraSignals;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sonera, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                // For showing a move to my location button
                //googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng finland = new LatLng(64.351108, 26.708667);
                //googleMap.addMarker(new MarkerOptions().position(finland).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(finland).zoom(5).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        FetchJSONTask task = new FetchJSONTask();

        task.execute("http://84.251.189.202:8080/sonera");
        //task.execute("http://student.labranet.jamk.fi/~H3298/json/testdata.json");
        return rootView;
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    //function to async fetch json data
    class FetchJSONTask extends AsyncTask<String, Void, JSONObject> {

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

        //on post execute we parse json points to display in map
        protected void onPostExecute(JSONObject json) {
            try {
                soneraSignals = json.getJSONArray("sonera");

                int height = 80;
                int width = 80;
                BitmapDrawable bitmapDrawable = null;

                for (int i = 0; i < soneraSignals.length(); i++) {
                    JSONObject signalJson = soneraSignals.getJSONObject(i);
                    LatLng latlng = new LatLng(signalJson.getDouble("lat"), signalJson.getDouble("lon"));

                    if (Integer.parseInt(signalJson.getString("gsm")) > -65) {
                        bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.greencircle);
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) > -99 && Integer.parseInt(signalJson.getString("gsm")) <= -66) {
                        bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.yellowcircle);
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) < -100) {
                        bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.redcircle);
                    }

                    Bitmap b = bitmapDrawable.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    googleMap.addMarker(new MarkerOptions()
                                    .position(latlng)
                                    .title("Signaalit")
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                    .snippet("Latitude: " + signalJson.getDouble("lat") + "\n" + "Longitude: " + signalJson.getDouble("lon") + "\n"
                                           +"Gsm: " + signalJson.getInt("gsm") + "\n" + "Cdma: " + signalJson.getInt("cdma") + "\n" + "Evdo: " + signalJson.getInt("evdo")));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));
                }

                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        Context context = getActivity(); //or getActivity(), YourActivity.this, etc.

                        LinearLayout info = new LinearLayout(context);
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(context);
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(context);
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }
}

