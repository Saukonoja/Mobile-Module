package fi.jamk.signalstrength;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        protected void onPostExecute(JSONObject json) {
            try {
                BitmapDescriptor bitmapDescriptor = null;
                soneraSignals = json.getJSONArray("sonera");
                for (int i = 0; i < soneraSignals.length(); i++) {
                    JSONObject signalJson = soneraSignals.getJSONObject(i);
                    LatLng latlng = new LatLng(signalJson.getDouble("lat"), signalJson.getDouble("lon"));

                    int strokeColor = 0;
                    int fillColor = 0;

                    if (Integer.parseInt(signalJson.getString("gsm")) > -65) {
                        strokeColor = 0xFF00FF1A;
                        fillColor = 0x4D00FF1A;
                        // bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) >= -79 && Integer.parseInt(signalJson.getString("gsm")) <= -65) {
                        strokeColor = 0xFFF7FF00;
                        fillColor = 0x4DF7FF00;
                        //bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) >= -89 && Integer.parseInt(signalJson.getString("gsm")) <= -80) {
                        strokeColor = 0xFFFF8900;
                        fillColor = 0x4DFF8900;
                        // bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) >= -99 && Integer.parseInt(signalJson.getString("gsm")) <= -90) {
                        strokeColor = 0xFFFF00EF;
                        fillColor = 0x4DFF00EF;
                        //bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    }

                    if (Integer.parseInt(signalJson.getString("gsm")) >= -105 && Integer.parseInt(signalJson.getString("gsm")) <= -100) {
                        strokeColor = 0xFFFF0000;
                        fillColor = 0x4DFF0000;
                        //bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }

                    Circle circle = googleMap.addCircle(new CircleOptions()
                            .center(latlng)
                            .radius(100)
                            .strokeColor(strokeColor)
                            .strokeWidth(2f)
                            .fillColor(fillColor));


                    /*final Marker markers = googleMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title(signalJson.getString("gsm"))
                            .icon(bitmapDescriptor));*/
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));
                }
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }


        }
    }

}

