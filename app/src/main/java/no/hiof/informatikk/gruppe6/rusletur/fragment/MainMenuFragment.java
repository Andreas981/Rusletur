package no.hiof.informatikk.gruppe6.rusletur.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import no.hiof.informatikk.gruppe6.rusletur.MainActivity;
import no.hiof.informatikk.gruppe6.rusletur.MainScreen;
import no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips.MapsActivity;
import no.hiof.informatikk.gruppe6.rusletur.MapsAndTrips.TripTracker;
import no.hiof.informatikk.gruppe6.rusletur.R;

public class MainMenuFragment extends Fragment {

    String userEmail;
    Button signOut;
    ConstraintLayout layout;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public static final String TAG = "RusleTur";
    private int LOCATION_PERMISSION_CODE = 1;
    private ArrayList<String> mItem = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<LatLng> savedTrip;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.fragment_mainscreen, container, false);
        Log.d("TESTER", "UTENFOR ONCLICK");





        view.findViewById(R.id.recordTripButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), TripTracker.class));
            }
        });

        view.findViewById(R.id.stopRecordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                getActivity().stopService(new Intent(getActivity(), TripTracker.class));

                //new AlertDialog.Builder(getActivity())



            }
        });


        return view;

    }




}
