package no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import no.hiof.informatikk.gruppe6.rusletur.MainActivity;
import no.hiof.informatikk.gruppe6.rusletur.Model.LocalStorage;
import no.hiof.informatikk.gruppe6.rusletur.R;
import no.hiof.informatikk.gruppe6.rusletur.RecyclerView.MainTripRecyclerViewAdapter;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Class for displaying stored Trip objects
 * When the user either wants to store or load objects, the static methods are called upon
 * @author Andreas N
 * @version 1.1
 */
public class LocalStorageTrips extends AppCompatActivity {
    Button btnBack;
    Button btnImport;
    private MainTripRecyclerViewAdapter mainTripAdapter;
    private static final int READ_REQUEST_CODE = 42;
    private String TAG = "LocalStorageTrips";
    GPXParser mParser;
    private String[] neededPermissions = { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_storage_trips);
        btnBack = findViewById(R.id.localStorage_goBack_button);
        btnImport = findViewById(R.id.localStorageTrips_Import_button);

        RecyclerView recyclerView = findViewById(R.id.local_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LocalStorage localStorage = LocalStorage.getInstance(this);

        //Display the locally stored trips in a recycler view
        mainTripAdapter = new MainTripRecyclerViewAdapter(this, localStorage.getAllTrips());
        recyclerView.setAdapter(mainTripAdapter);

        checkPermissions();


    }

    /**
     * MEthod for checking if the user has granted permissions.
     * @return True or False
     */
    private boolean checkPermissions(){
        boolean isPermissionsGranted = false;

        if (EasyPermissions.hasPermissions(this,neededPermissions)){
            isPermissionsGranted = true;
        }else{
            startActivity(new Intent(this,MainActivity.class));

        }

        return isPermissionsGranted;
    }

    /**
     * Button for importing a GPX file that the user has on the unit.
     * @param view The button as a View
     */
    public void importAtrip(View view){
        Log.d(TAG, "importAtrip: ");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Only have files that have the .gpx extension selectable
        intent.setType("application/gpx+xml");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    //When a file is selected by the user:
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                mParser = new GPXParser();

                Gpx parsedGpx = null;
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    parsedGpx = mParser.parse(in);
                } catch (IOException | XmlPullParserException e) {
                    // do something with this exception
                    e.printStackTrace();
                }
                if (parsedGpx == null) {
                    Log.d(TAG, "onActivityResult: No coordinates found");
                    // error parsing track
                } else {
                    Log.d(TAG, "onActivityResult: File found and parsed");
                    ArrayList<LatLng> parsedLatLng = new ArrayList<>();
                    List<Track> tracks = parsedGpx.getTracks();
                    for (int i = 0; i < tracks.size(); i++) {
                        Track track = tracks.get(i);
                        Log.d(TAG, "track " + i + ":");
                        List<TrackSegment> segments = track.getTrackSegments();
                        for (int j = 0; j < segments.size(); j++) {
                            TrackSegment segment = segments.get(j);
                            Log.d(TAG, "  segment " + j + ":");
                            for (TrackPoint trackPoint : segment.getTrackPoints()) {
                                Log.d(TAG, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
                                parsedLatLng.add(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()));
                            }

                        }
                    }

                    Intent intent = new Intent(this,SaveTripActivity.class);
                    intent.putExtra("coordsArray", parsedLatLng);
                    intent.putExtra("sender",this.getClass().getSimpleName());
                    startActivity(intent);
                }
            }
        }
    }


    /**
     * Method for when the back button is pressed
     * @param view The button as a View
     */
    public void back(View view){
        onBackPressed();
    }

    /**
     * Method for when the back button is pressed
     * @param view The button as a View
     */
    public void goBack(View view){
        super.onBackPressed();
    }

}



