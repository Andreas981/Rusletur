package no.hiof.informatikk.gruppe6.rusletur.ApiCalls;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import no.hiof.informatikk.gruppe6.rusletur.FindAtrip;
import no.hiof.informatikk.gruppe6.rusletur.Model.Trip;

import static no.hiof.informatikk.gruppe6.rusletur.fragment.RecordFragment.TAG;

/**

 * Class used for looking up objects located on Nasjonal Turbase server
 * Takes a ArrayList of id's as argument
 * Creating Trip objects and adding them to an ArrayList.
 * @author Andreas M.
 * @author Andreas N.
 * @version 1.0
 */
 public class ApiNasjonalturbase {
     public static int antall = 0;
     static RequestQueue mQueue;
     private static Trip trip;
     private static Context kont;

    /**
     * Method for retriving Trip objects
     * @param idForTrip The id that was passed from the register, is used to fetch the Trip object stored
     *                  on NasjonalTurbase Server
     * @param context   The context the method was accessed from.
     */
    public static void getTripInfo(String idForTrip, final Context context) {

             kont = context;
             mQueue = Volley.newRequestQueue(context);

             String url = "https://api.nasjonalturbase.no/turer/" + idForTrip;
             Log.d(TAG, "getTripInfo onResponse2: Id for tur: " + idForTrip);
             JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                 @Override
                 public void onResponse(JSONObject response) {
                     try{
                         String id = response.get("_id").toString();
                         String navn = response.get("navn").toString();

                         String tag = response.get("tags").toString();

                         String gradering = response.get("gradering").toString();
                         String tilbyder = response.get("tilbyder").toString();

                         JSONArray fylker = (JSONArray) response.get("fylker");
                         String fylke = fylker.get(0).toString();

                         JSONArray kommuner = (JSONArray) response.get("kommuner");
                         String kommume = kommuner.get(0).toString();

                         String beskrivelse = response.get("beskrivelse").toString();
                         String lisens = response.get("lisens").toString();
                         
                         String urlFraUrl = response.get("url").toString();

                         ArrayList<LatLng> latlng = new ArrayList<>();

                         JSONObject geojson = (JSONObject) response.get("geojson");
                         JSONArray coords = (JSONArray) geojson.get("coordinates");
                         for (int j = 0; j < coords.length(); j++) {
                             JSONArray coord = (JSONArray) coords.get(j);
                             for (int k = 0; k < coord.length(); k++) {
                                 latlng.add(new LatLng(coord.getDouble(1),
                                         coord.getDouble(0)));
                             }
                         }

                         //Setter sammen strengen for tidsbruk
                         JSONObject tidsbrukObj = (JSONObject) response.get("tidsbruk");
                         JSONObject normal = null;

                         if(tidsbrukObj.has("normal")){
                             normal = (JSONObject) tidsbrukObj.get("normal");
                         }else if(tidsbrukObj.has("min")){
                             normal = (JSONObject) tidsbrukObj.get("min");
                         }

                         String dager = null;
                         String timer = null;
                         String minutter = null;
                         String tidsbruk;

                         Log.d(TAG, "onResponse2: Normal: " + normal);
                         if(normal.has("dager")){
                             dager = normal.getString("dager");
                         }
                         if(normal.has("timer")){
                             timer = normal.getString("timer");
                         }
                         if(normal.has("minutter")){
                             minutter = normal.getString("minutter");
                         }

                         tidsbruk = ((dager != null) ? (dager + " dager, ") : "") + "" + ((timer != null) ? (timer + " timer, ") : "0 timer, ") + "" + ((minutter != null) ? (minutter + " minutter") : "0 minutter");

                         Trip trip = new Trip(id, navn, tag, gradering, tilbyder, fylke, kommume, beskrivelse, lisens, urlFraUrl, latlng, tidsbruk);

                         FindAtrip.turer.add(trip);

                     } catch (JSONException e) {
                         e.printStackTrace();
                     }

                 }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError error) {
                     error.printStackTrace();
                 }
             });

             mQueue.add(request);
         }


     }
