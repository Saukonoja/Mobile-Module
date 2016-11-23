package fi.jamk.signalstrength;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private JSONArray golfCourses;

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
                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(finland)
                        .radius(10000)
                        .strokeColor(0xFF00FFFF)
                        .fillColor(0x4D00FFFF));
                Circle circle2 = googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(62.351108,27.708667))
                        .radius(10000)
                        .strokeColor(0xFF00FFFF)
                        .fillColor(0x4D00FFFF));
                Circle circle3 = googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(61.351108,28.708667))
                        .radius(10000)
                        .strokeColor(0xFF00FFFF)
                        .fillColor(0x4D00FFFF));
            }
        });

        FetchDataTask task = new FetchDataTask();
        task.execute("http://ptm.fi/jamk/android/golfcourses/golf_courses.json");
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
                    if (golfJson.getString("Tyyppi").contains("?")) {
                        bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    }
                    if (golfJson.getString("Tyyppi").contains("Etu")) {
                        bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    if (golfJson.getString("Tyyppi").contains("Kulta")) {
                        bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    }
                    if (golfJson.getString("Tyyppi").contains("Kulta/Etu")) {
                        bitmapDescriptor = (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                    final Marker markers = googleMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title(golfJson.getString("Kentta"))
                            .snippet(golfJson.getString("Osoite") + "\n" + golfJson.getString("Puhelin") + "\n"
                                    + golfJson.getString("Sahkoposti") + "\n" + golfJson.getString("Webbi"))
                            .icon(bitmapDescriptor));
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5));


                    //Toast.makeText(getApplicationContext(), golfJson.getString("Tyyppi"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }
}
