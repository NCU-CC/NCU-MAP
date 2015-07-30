package tw.edu.ncu.cc.location.client.android;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tw.edu.ncu.cc.location.client.tool.AsynLocationClient;
import tw.edu.ncu.cc.location.client.tool.config.LocationConfig;
import tw.edu.ncu.cc.location.client.tool.response.ResponseListener;
import tw.edu.ncu.cc.location.data.keyword.Word;
import tw.edu.ncu.cc.location.data.person.Person;
import tw.edu.ncu.cc.location.data.place.Place;
import tw.edu.ncu.cc.location.data.place.PlaceType;
import tw.edu.ncu.cc.location.data.unit.Unit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationClient implements AsynLocationClient {

    private RequestQueue queue;
    private String baseURL;
    private String apiToken;

    public LocationClient( LocationConfig config, Context context ) {
        this.baseURL = config.getServerAddress();
        this.queue = Volley.newRequestQueue( context );
        this.apiToken = config.getApiToken();
    }

    public RequestQueue getQueue() {
        return queue;
    }

    @Override
    public void getPlaces( String placeName, ResponseListener< Place > responseListener ) {
        sendRequest(
                "/places?cname=" + Uri.encode( placeName ), responseListener, Place[].class
        );
    }

    @Override
    public void getPlaces( PlaceType placeType, ResponseListener< Place > responseListener ) {
        sendRequest(
                "/places?type=" + Uri.encode( placeType.value() ), responseListener, Place[].class
        );
    }

    @Override
    public void getPlaceUnits( String placeName, ResponseListener< Unit > responseListener ) {
        sendRequest(
                "/units?building_cname=" + Uri.encode( placeName ), responseListener, Unit[].class
        );
    }

    @Override
    public void getPeople( String peopleName, ResponseListener< Person > responseListener ) {
        sendRequest(
                "/faculties?cname=" + Uri.encode( peopleName ), responseListener, Person[].class
        );
    }

    @Override
    public void getUnits( String unitName, ResponseListener< Unit > responseListener ) {
        sendRequest(
                "/units?fname=" + Uri.encode( unitName ), responseListener, Unit[].class
        );
    }

    @Override
    public void getWords( String keyword, ResponseListener< Word > responseListener ) {
        sendRequest(
                "/search?q=" + Uri.encode( keyword ), responseListener, Word[].class
        );
    }

    private < T > void sendRequest( String path, final ResponseListener< T > responseListener, final Class< T[] > type ) {
        queue.add( new StringRequest( Request.Method.GET, baseURL + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String response ) {
                        Log.w("response", response);
                        T[] result = new Gson().fromJson( response, type );
                        responseListener.onResponse( Arrays.asList( result ) );
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        responseListener.onError( error );
                    }
                }
        ) {
            public Map< String, String > getHeaders() throws AuthFailureError {
                Map< String, String > headers = new HashMap<>();
                headers.put( "X-NCU-API-TOKEN", apiToken );
                return headers;
            }
        } );
    }
	
}
