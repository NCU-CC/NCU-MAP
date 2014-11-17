package tw.edu.ncu.cc.ncumap;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.edu.ncu.cc.location.client.tool.config.LocationConfig;
import tw.edu.ncu.cc.location.client.tool.config.NCULocationConfig;
import tw.edu.ncu.cc.location.client.tool.response.ResponseListener;
import tw.edu.ncu.cc.location.client.volley.NCUAsyncLocationClient;
import tw.edu.ncu.cc.location.data.keyword.Word;
import tw.edu.ncu.cc.location.data.keyword.WordType;
import tw.edu.ncu.cc.location.data.person.Person;
import tw.edu.ncu.cc.location.data.place.Place;
import tw.edu.ncu.cc.location.data.place.PlaceType;
import tw.edu.ncu.cc.location.data.unit.Unit;

/**
 * Created by Tatsujin on 2014/10/6.
 */
public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private NCUAsyncLocationClient locationClient;
    private Map<String, NCUMarker> markers;

    private final LatLng ncuLocation = new LatLng(24.968297, 121.192151);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        markers = new HashMap<String, NCUMarker>();
        locationClient = MyActivity.locationClient;


        setUpMapIfNeeded();

        if (MyActivity.word != null)
            setWord(MyActivity.word);
        for (final QueryData queryData : MyActivity.selectedQueryOptions) {
            Log.w("PlaceType", queryData.getPlaceType().value());
            locationClient.getPlaces(queryData.getPlaceType(), new ResponseListener<Place>() {
                @Override
                public void onResponse(List<Place> places) {
                    setPlaces(places, queryData.getNum());
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);
        moveToLocation(location);
        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                moveToLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }, null);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                NCUMarker ncuMarker = markers.get(marker.getId());
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle(marker.getTitle() + " " + marker.getSnippet());
                TextView message = (TextView) getLayoutInflater().inflate(R.layout.dialog_content, null);
                message.setText("目前沒有可以顯示的資訊");
                switch (ncuMarker.getWordType()) {
                    case PERSON:
                        Person person = (Person) ncuMarker.getObject();
                        break;
                    case PLACE:
                        Place place = (Place) ncuMarker.getObject();
                        break;
                    case UNIT:
                        Unit unit = (Unit) ncuMarker.getObject();
                        break;
                }
                builder.setView(message);
                builder.setNegativeButton(R.string.close, null);
                builder.show();
            }
        });
    }

    private void moveToLocation(Location location) {
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        float[] result = new float[3];
        Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, ncuLocation.latitude, ncuLocation.longitude, result);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result[0] > 20000 ? ncuLocation : myLatLng, 17));
        Log.w("Distance", String.valueOf(result[0]));
    }

    private void setWord(Word word) {
        WordType type = word.getType();
        switch (type) {
            case PERSON:
                locationClient.getPeople(word.getWord(), new ResponseListener<Person>() {
                    @Override
                    public void onResponse(List<Person> people) {
                        setPeople(people);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
                break;
            case UNIT:
                locationClient.getUnits(word.getWord(), new ResponseListener<Unit>() {
                    @Override
                    public void onResponse(List<Unit> units) {
                        setUnits(units);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
                break;
            case PLACE:
                locationClient.getPlaces(word.getWord(), new ResponseListener<Place>() {
                    @Override
                    public void onResponse(List<Place> places) {
                        setPlaces(places, 16);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
        }
    }

    private void setPlaces(List<Place> places, float iconColor) {
        for (Place place : places) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLocation().getLat(), place.getLocation().getLng()))
                    .title(place.getChineseName())
                    .snippet(place.getEnglishName())
                    .icon(BitmapDescriptorFactory.defaultMarker(iconColor * 19)));
            markers.put(marker.getId(), new NCUMarker(WordType.PLACE, place));
        }
    }

    private void setUnits(List<Unit> units) {
        for (Unit unit : units) {
            if (unit.getLocation() == null)
                continue;
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(unit.getLocation().getLat(), unit.getLocation().getLng()))
                    .title(unit.getFullName())
                    .snippet(unit.getEnglishName())
                    .icon(BitmapDescriptorFactory.defaultMarker(323)));
            markers.put(marker.getId(), new NCUMarker(WordType.UNIT, unit));
        }
    }

    private void setPeople(List<Person> people) {
        for (Person person : people) {
            if (person.getPrimaryUnit().getLocation() == null)
                continue;
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(person.getPrimaryUnit().getLocation().getLat(), person.getPrimaryUnit().getLocation().getLng()))
                    .title(person.getChineseName())
                    .snippet(person.getEnglishName())
                    .icon(BitmapDescriptorFactory.defaultMarker(342)));
            markers.put(marker.getId(), new NCUMarker(WordType.PERSON, person));
        }
    }
}

class NCUMarker {
    private WordType wordType;
    private Object object;

    public NCUMarker (WordType wordType, Object object) {
        this.wordType = wordType;
        this.object = object;
    }

    public WordType getWordType() {
        return wordType;
    }

    public Object getObject() {
        return object;
    }
}
