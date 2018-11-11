package no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

import no.hiof.informatikk.gruppe6.rusletur.Model.LocalStorage;
import no.hiof.informatikk.gruppe6.rusletur.R;
import no.hiof.informatikk.gruppe6.rusletur.RecyclerView.MainTripRecyclerViewAdapter;

import static no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips.Trip.trips;

/**
 * Class for displaying stored Trip objects
 * SQLite
 * When the user either wants to store or load objects, the static methods are called upon
 */
public class LocalStorageTrips extends AppCompatActivity {
    Button btnBack;
    private MainTripRecyclerViewAdapter mainTripAdapter;

    public static ArrayList<String> rowIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_storage_trips);
        btnBack = findViewById(R.id.localStorage_goBack_button);

        //TODO Make prepared statements


        // Initialize recyclerview and set adapter
        //LocalStorage localStorage = LocalStorage.getInstance(this);

        
        RecyclerView recyclerView = findViewById(R.id.local_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LocalStorage localStorage = LocalStorage.getInstance(this);



        mainTripAdapter = new MainTripRecyclerViewAdapter(this, localStorage.getAllTrips());
        recyclerView.setAdapter(mainTripAdapter);


    }




    /**
     * Used for retriving objects based on search criteria ( Fylke and Kommune) when looking
     * up a avalible trip in "Finn en tur"
     *
     * @param context The context the request is comeing from
     * @param aFylke
     * @param aKommune
     * @return
     */
    public static ArrayList<Trip> retriveItemsFromStorage(Context context,String aFylke, String aKommune){
        ArrayList<Trip> availableTrips = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("TripsLocal.db", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS trips(id TEXT,navn TEXT," +
                "tag TEXT,gradering TEXT,tilbyder TEXT,fylke TEXT,kommune TEXT,beskrivelse TEXT," +
                "lisens TEXT, url TEXT, tidsbruk TEXT, latLng TEXT);");

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM trips " +
                "WHERE fylke = '"+aFylke+"'  AND kommune =  '"+aKommune+"';", null);
        if (cursor.moveToFirst()) {
            do {

                String latLng = cursor.getString(11);
                //Fetch the string that contains the compressed Array
                //Remove the [ ] from the recovered string
                String arrRemoved = latLng.substring(1, latLng.length() - 1);
                //Make a new Array by splitting the latLng points by ,
                String[] latLngArray = arrRemoved.split(", ");
                ArrayList<LatLng> arrayListLatLng = new ArrayList<>();
                //Split simple array into Lat[0} and Long[1]
                for (int i = 0; i < latLngArray.length; i++) {
                    String[] latLngSplits = latLngArray[0].split(" - ");
                    Double lat = Double.parseDouble(latLngSplits[0]);
                    Double longt = Double.parseDouble(latLngSplits[1]);
                    arrayListLatLng.add(new LatLng(lat, longt));
                }

                //Build the retrived trip object from storage, add it to the array
                availableTrips.add(new Trip(cursor.getString(1),
                        cursor.getString(2),cursor.getString(3),
                        cursor.getString(4),cursor.getString(5),
                        cursor.getString(6),cursor.getString(7),
                        cursor.getString(8),cursor.getString(9),
                        cursor.getString(10),
                        arrayListLatLng,cursor.getString(11)));
                Log.i("SQLQ","Matches: " + availableTrips.size());

            } while ((cursor.moveToNext()));

        }
        cursor.close();
        sqLiteDatabase.close();

        return availableTrips;
    }

    /**
     * Method for showing all the stored objects to the user.
     * Adds the row ids to the array
     * @ Gives back a array of available objects
     */


    public static String getRowId(int position){
        return rowIds.get(position);
    }

    /**
     * Method for deleting objects.
     * @param context Context from the activity that wants to delete the object
     * @param rowId The row index that the object is listed on
     * @return Gives back a message when the object is deleted.
     */
    public static String deleteTripFromTable(Context context,String rowId){
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("TripsLocal.db", MODE_PRIVATE, null);

        sqLiteDatabase.delete("trips" , "ROWID"
                    + " = " + rowId, null);
        sqLiteDatabase.close();

        return "Slettet";
    }

    public void goBack(View view){
        super.onBackPressed();
    }
}



