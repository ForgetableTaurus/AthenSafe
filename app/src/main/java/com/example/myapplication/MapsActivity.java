package com.example.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Calendar;

import android.location.LocationManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.widget.SearchView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.heatmaps.WeightedLatLng;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.DirectionsLeg; // Προστέθηκε η εισαγωγή για DirectionsLeg

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.widget.Toast;
import androidx.annotation.NonNull;





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    public interface HeatmapDataCallback {
        void onHeatmapDataReceived(List<WeightedLatLng> heatmapData, List<LatLng> incidentLatLngsList);
    }

    private static boolean flag_start =false;

    private GoogleMap mMap;
    private TileOverlay tileOverlay; // Αρχικοποίηση εδώ

    private HeatmapTileProvider provider;
    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    private Marker userMarker;

    private Polyline routePolyline;

    private float previousDistance = -1; // Αρχικοποίηση με -1 για να είναι διαφορετική από την πρώτη απόσταση

    private Handler handler = new Handler();

    private boolean isNotificationActive = false;

    private Location incidentLocation;

    private ActivityMapsBinding binding;

    private boolean isNotificationShown = false;

    private boolean isSoundPlayed = false;

    private List<WeightedLatLng> heatmapData = new ArrayList<>(); // Initialize heatmapData
    List<LatLng> incidentLatLngs = new ArrayList<>();

    private boolean isLocationMessageShown = false; // Για να εμφανίζει στον χρήστη την τοποθεσία του όταν μπεί στην εφαρμογή

    private FloatingActionButton fabSos;

    private DatabaseReference databaseReference;
    private Map<String, Integer> incidentCounts = new HashMap<>();
    private boolean markersVisible = false;
    private List<Marker> areaMarkers = new ArrayList<>();
    private Map<String, Double> areaDangerPercentages = new HashMap<>(); // Για το νυπολογισμο ποσοστου

    private PlacesClient placesClient;
    private Map<LatLng, String> areaNames ;

    private Map<String, LatLng> areaCenters = new HashMap<>();
    private Geocoder geocoder ;

    private Map<String, Marker> sosMarkers = new HashMap<>();

    private Marker currentSosMarker;

    private Map<String, AreaInfo> athensAreas = new HashMap<>();

    private Map<String, Marker> activeSosMarkers = new HashMap<>(); // Για να αποθηκεύουμε τους SOS markers που εμφανίζονται

    private int isPremium;
    private String email;

    private String emailnopassword;

    private String firstname;

    private String lastname;

    private String username;

    private Map<String, DataSnapshot> nearbyIncidents = new HashMap<>();
    private String currentClosestAndLatestIncidentKey = null;
    private ValueEventListener incidentsEventListener;

    private String lastRouteAnalysis = "";
    private Marker destinationMarker;








    /*

     * Η μέθοδος onCreate καλείται όταν η δραστηριότητα δημιουργείται.

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Λαμβάνουμε τα στοιχεία του χρήστη που συμπλήρωσε στο Intent (οταν εκανε εγγραφή)
        isPremium = getIntent().getIntExtra("isPremium", 0);
        email = getIntent().getStringExtra("email");
        emailnopassword=getIntent().getStringExtra("emailnopassword");
        firstname=getIntent().getStringExtra("firstname");
        lastname=getIntent().getStringExtra("lastname");
        username=getIntent().getStringExtra("username");


        // Αρχικοποίηση του Places API client χρησιμοποιώντας το API key
        Places.initialize(getApplicationContext(), ""); // API key
        placesClient = Places.createClient(this);

        // Αρχικοποίηση του Firebase
        FirebaseApp.initializeApp(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Αρχικοποίηση του FusedLocationProviderClient για την λήψη της τρέχουσας τοποθεσίας
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // Λήψη του SupportMapFragment για τοποθέτηση του χαρτη στην εφαρμογή
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // Δημιουργία ενός LocationRequest για να ορίσουμε τις παραμέτρους των αιτημάτων τοποθεσίας
        createLocationRequest();
        // Δημιουργία ενός LocationCallback για να χειριστούμε τα αποτελέσματα των αιτημάτων τοποθεσίας
        createLocationCallback();



        //ΓΙΑ ΤΑ ΣΤΑΤΙΣΤΙΚΑ
        databaseReference = FirebaseDatabase.getInstance().getReference("Αναφορές");

        //-----------------ΛΙΣΤΑ ΜΕ ΚΑΠΟΙΕΣ ΠΕΡΙΟΧΕΣ ΤΗΣ ΑΘΗΝΑΣ ΓΙΑ ΑΥΤΕΣ ΠΟΥ ΔΕΝ ΕΧΟΥΝ HEATMAP
        athensAreas.put("Ομόνοια", new AreaInfo(new LatLng(37.9840, 23.7285), "Επικίνδυνη περιοχή"));
        athensAreas.put("Μεταξουργείο", new AreaInfo(new LatLng(37.9845, 23.7196), "Επικίνδυνη περιοχή"));
        athensAreas.put("Κεραμεικός", new AreaInfo(new LatLng(37.9786, 23.7107), "Επικίνδυνη περιοχή"));
        athensAreas.put("Πλατεία Βάθη", new AreaInfo(new LatLng(37.9900, 23.7250), "Επικίνδυνη περιοχή"));
        athensAreas.put("Άγιος Παντελεήμονας", new AreaInfo(new LatLng(38.0020, 23.7210), "Επικίνδυνη περιοχή"));
        athensAreas.put("Πλατεία Αμερικής", new AreaInfo(new LatLng(38.0050, 23.7280), "Επικίνδυνη περιοχή"));
        athensAreas.put("Κυψέλη", new AreaInfo(new LatLng(38.0000, 23.7350), "Μέτρια ασφαλής περιοχή"));
        athensAreas.put("Πατήσια", new AreaInfo(new LatLng(38.0150, 23.7350), "Μέτρια ασφαλής περιοχή"));
        athensAreas.put("Πλατεία Αττικής", new AreaInfo(new LatLng(38.0050, 23.7250), "Επικίνδυνη περιοχή"));
        athensAreas.put("Αχαρνές (Μενίδι)", new AreaInfo(new LatLng(38.0833, 23.7333), "Επικίνδυνη περιοχή"));
        athensAreas.put("Καματερό", new AreaInfo(new LatLng(38.0333, 23.7000), "Μέτρια ασφαλής περιοχή"));
        athensAreas.put("Ζεφύρι", new AreaInfo(new LatLng(38.0833, 23.7000), "Επικίνδυνη περιοχή"));
        athensAreas.put("Άνω Λιόσια", new AreaInfo(new LatLng(38.0833, 23.7000), "Επικίνδυνη περιοχή"));
        athensAreas.put("Ασπρόπυργος", new AreaInfo(new LatLng(38.0667, 23.5833), "Επικίνδυνη περιοχή"));
        athensAreas.put("Κολωνάκι", new AreaInfo(new LatLng(37.9761, 23.7430), "Ασφαλής περιοχή"));
        athensAreas.put("Ψυχικό", new AreaInfo(new LatLng(38.0167, 23.7667), "Ασφαλής περιοχή"));
        athensAreas.put("Παλαιό Φάληρο", new AreaInfo(new LatLng(37.9333, 23.7000), "Ασφαλής περιοχή"));
        athensAreas.put("Βούλα", new AreaInfo(new LatLng(37.8333, 23.7833), "Ασφαλής περιοχή"));
        athensAreas.put("Γλυφάδα", new AreaInfo(new LatLng(37.8667, 23.7500), "Ασφαλής περιοχή"));
        athensAreas.put("Φιλοθέη", new AreaInfo(new LatLng(38.0333, 23.7667), "Ασφαλής περιοχή"));
        athensAreas.put("Εκάλη", new AreaInfo(new LatLng(38.1000, 23.8167), "Ασφαλής περιοχή"));
        athensAreas.put("Πεύκη", new AreaInfo(new LatLng(38.0667, 23.7833), "Ασφαλής περιοχή"));
        athensAreas.put("Κουκάκι", new AreaInfo(new LatLng(37.9667, 23.7333), "Ασφαλής περιοχή"));
        athensAreas.put("Κηφισιά", new AreaInfo(new LatLng(38.0667, 23.8000), "Ασφαλής περιοχή"));
        athensAreas.put("Γκάζι", new AreaInfo(new LatLng(37.9780, 23.7100), "Μέτρια ασφαλής περιοχή"));

        FirebaseAuth auth = FirebaseAuth.getInstance();

        /*
        *
        *
        * ΓΙΑ ΑΥΘΕΝΤΙΚΟΠΟΙΗΣΗ ΧΡΗΣΤΗ ΩΣΤΕ ΝΑ ΜΠΟΡΕΙ ΝΑ ΔΙΑΒΑΖΕΙ ΚΑΙ ΝΑ ΕΠΕΞΕΡΓΑΖΕΤΑΙ ΣΤΟΙΧΕΙΑ ΤΗς ΒΑΣΗΣ ΔΕΔΟΜΕΝΩΝ
        *
        *
        * */

/*

        auth.createUserWithEmailAndPassword("", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });

*/




        // Σύνδεση χρήστη με email και κωδικό
        auth.signInWithEmailAndPassword("", "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Ο χρήστης συνδέθηκε επιτυχώς
                            FirebaseUser user = auth.getCurrentUser();

                        } else {
                            // Σφάλμα κατά τη σύνδεση
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        //Button αναφοράς περιστατικού
        FloatingActionButton fabReportIncident = findViewById(R.id.fabReportIncident);
        // Ορισμός OnClickListener για το Floating Action Button αναφοράς περιστατικού
        fabReportIncident.setOnClickListener(v -> {
            // Έλεγχος αν ο χρήστης είναι premium (isPremium == 1)
            if (isPremium==1) {
                // Αν είναι premium, ανοίγει απευθείας την δραστηριότητα αναφοράς,δεν υπάρχει περιορισμός αριθμού αναφορών
                openReportActivity();
            } else {
                // Αν δεν είναι premium, ελέγχει την ημερομηνία της τελευταίας αναφοράς
                // και προχωρά ανάλογα (επιτρέποντας μία αναφορά ανά ημέρα για standard χρήστες)
                checkLastReportDateAndProceed();
            }
        });

        //infoButton
        FloatingActionButton infoButton = findViewById(R.id.fabRouteInfo);
        infoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Ανάλυση Διαδρομής")
                    .setMessage(lastRouteAnalysis != null && !lastRouteAnalysis.isEmpty() ? lastRouteAnalysis : "Δεν υπάρχει διαθέσιμη ανάλυση.")
                    .setPositiveButton("OK", null)
                    .show();
        });


        //ImageButton του προφίλ χρήστη
        ImageButton userProfileButton = findViewById(R.id.userProfileButton);

        // Ορισμός OnClickListener για το κουμπί του προφίλ χρήστη
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Δημιουργία Intent για την μετάβαση στην ProfileInfoActivity
                Intent intent = new Intent(MapsActivity.this, ProfileInfoActivity.class);
                // Πέρασμα των στοιχείων του χρήστη στην ProfileInfoActivity,τα στοιχεία με τα οποία έκανε εγγραφή κατα την είσοδο στην εφαρμογή
                intent.putExtra("emailnopassword", emailnopassword);
                intent.putExtra("username", username);
                intent.putExtra("firstname", firstname);
                intent.putExtra("lastname", lastname);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        //Floating Action Button του Chat Bot
        FloatingActionButton fabChatBot = findViewById(R.id.fabChatBot);
        // Ορισμός OnClickListener για το Floating Action Button του Chat Bo
        fabChatBot.setOnClickListener(v -> {
            // Δημιουργία Intent για την μετάβαση στην ChatActivity
            Intent intent = new Intent(MapsActivity.this, ChatActivity.class);
            // Έναρξη της ChatActivity
            startActivity(intent);
        });

        // Φόρτωση των ονομάτων των περιοχών (για την εμφάνιση ετικετών ή για αναζήτηση)
        loadAreaNames();

        //TextView εκκαθάρισης αναζήτησης (clearSearch)
        TextView clearSearch = findViewById(R.id.clearSearch);
        clearSearch.setOnClickListener(v -> {
            // Έλεγχος αν υπάρχει ενεργή διαδρομή στην οθόνη
            if (routePolyline != null) {
                // Αφαίρεση της διαδρομής από το χάρτη
                routePolyline.remove();
                routePolyline = null;
            }

            if (destinationMarker != null) {
                destinationMarker.remove();
                destinationMarker = null;
            }

            // Εύρεση του SearchView και εκκαθάρισμα του κειμένου αναζήτησης
            SearchView searchView = findViewById(R.id.searchView);
            searchView.setQuery("", false);  // Εκκαθάριση του κειμένου αναζήτησης χωρίς υποβολή
            searchView.clearFocus();  // Αφαίρεση της εστίασης για να κλείσει το πληκτρολόγιο

            // Επαναφορά του lastRouteAnalysis με μήνυμα ότι δεν υπάρχει διαθέσιμη ανάλυση
            lastRouteAnalysis = "Δεν υπάρχει διαθέσιμη ανάλυση.";

        });

        // Λήψη αναφοράς στο RecyclerView για την εμφάνιση των αυτόματων συμπληρώσεων
        RecyclerView recyclerView = findViewById(R.id.autocomplete_recycler_view);
        // Ορισμός του LayoutManager για το RecyclerView (γραμμική διάταξη)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Δημιουργία ενός AutocompleteAdapter με μια αρχικά κενή λίστα
        AutocompleteAdapter adapter = new AutocompleteAdapter(new ArrayList<>());
        // Ορισμός του Adapter για το RecyclerView
        recyclerView.setAdapter(adapter);

        // Δημιουργία token για την αυτόματη συμπλήρωση
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        SearchView searchView = findViewById(R.id.searchView);
        // Ορισμός OnQueryTextListener για το SearchView για την διαχείριση των υποβολών και αλλαγών κειμένου
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Καλείται όταν ο χρήστης υποβάλλει ένα αίτημα αναζήτησης


                // Έλεγχος αν το email είναι null, αν είναι, επιστρέφει false
                if (email == null) return false;

                // Δημιουργία αναφοράς στον κόμβο "Premium Χρήστες" της Firebase
                DatabaseReference premiumRef = FirebaseDatabase.getInstance().getReference("Premium Χρήστες");
                // Ανάγνωση των δεδομένων του κόμβου για τον συγκεκριμένο χρήστη (μέσω του email)
                premiumRef.child(email.replace(".", ",")).get().addOnSuccessListener(premiumSnapshot -> {
                    // Έλεγχος αν υπάρχει εγγραφή για τον χρήστη στον κόμβο Premium Χρήστες
                    boolean isPremium = premiumSnapshot.exists();

                    // Αν ο χρήστης είναι premium
                    if (isPremium) {
                        // Κλήση της μεθόδου για γεωκωδικοποίηση του ερωτήματος και εμφάνιση της διαδρομής
                        geocodeAndShowRoute(query);
                        // Αποθήκευση της αναζήτησης στο ιστορικό αναζητήσεων
                        saveSearch(email, query);
                    } else {
                        // Αν ο χρήστης δεν είναι premium, δημιουργία αναφοράς στον κόμβο "Αναζητήσεις"
                        DatabaseReference searchesRef = FirebaseDatabase.getInstance().getReference("Αναζητήσεις");
                        // Ανάγνωση όλων των αναζητήσεων
                        searchesRef.get().addOnSuccessListener(snapshot -> {
                            int countToday = 0;
                            // Λήψη της σημερινής ημερομηνίας σε μορφή yyyy-MM-dd
                            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            // Επανάληψη σε όλες τις αναζητήσεις
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                // Ανάκτηση του email και του timestamp κάθε αναζήτησης
                                String e = snap.child("email").getValue(String.class);
                                String timestamp = snap.child("timestamp").getValue(String.class);
                                // Έλεγχος αν το email ταιριάζει με του τρέχοντος χρήστη και αν το timestamp είναι σημερινό
                                if (e != null && timestamp != null && e.equals(email) && timestamp.startsWith(today)) {
                                    // Αύξηση του μετρητή σημερινών αναζητήσεων
                                    countToday++;
                                }
                            }

                            // Έλεγχος αν ο αριθμός των σημερινών αναζητήσεων είναι μικρότερος από 3
                            if (countToday < 3) {
                                // Αν είναι, γεωκωδικοποίηση και εμφάνιση διαδρομής και αποθήκευση αναζήτησης
                                geocodeAndShowRoute(query);
                                saveSearch(email, query);
                            } else {
                                // Αν έχει φτάσει το όριο, εμφάνιση μηνύματος στον χρήστη
                                Toast.makeText(getApplicationContext(),
                                        "Έχετε φτάσει το όριο των 3 αναζητήσεων για σήμερα. Αποκτήστε την Premium έκδοση για απεριόριστη χρήση.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                // Καλείται κάθε φορά που αλλάζει το κείμενο στο SearchView
                // Δημιουργία αιτήματος αυτόματης συμπλήρωσης προς το Places API
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(newText)
                        .setTypeFilter(TypeFilter.ADDRESS) // Μπορείτε να αλλάξετε το φίλτρο
                        .build();

                // Εκτέλεση του αιτήματος αυτόματης συμπλήρωσης και ορισμός listeners για την επιτυχία και την αποτυχία
                placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                    // Λήψη της λίστας των προβλέψεων από την απάντηση
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    // Ενημέρωση του Adapter του RecyclerView με τις νέες προβλέψεις
                    adapter.setPredictions(predictions);
                }).addOnFailureListener((exception) -> {
                    // Χειρισμός σφαλμάτων
                });
                return false;
            }
        });

        // Χειρισμός κλικ σε πρόταση αυτόματης συμπλήρωσης
        adapter.setOnItemClickListener(new AutocompleteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AutocompletePrediction prediction) {
                // Δημιουργία λίστας με τα πεδία που θέλουμε να ανακτήσουμε για την επιλεγμένη πρόταση (μόνο γεωγραφικές συντεταγμένες)
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                // Δημιουργία αιτήματος για την ανάκτηση λεπτομερειών της τοποθεσίας βάσει του Place ID
                placesClient.fetchPlace(FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields))
                        .addOnSuccessListener((response) -> {
                            // Λήψη του αντικειμένου Place από την απάντηση
                            Place place = response.getPlace();
                            // Λήψη των γεωγραφικών συντεταγμένων της επιλεγμένης τοποθεσίας
                            LatLng destination = place.getLatLng();
                            // Κλήση της μεθόδου για την εμφάνιση της ασφαλούς διαδρομής προς τον προορισμό
                            showSafeRoute(destination);
                        })
                        .addOnFailureListener((exception) -> {
                            // Εμφάνιση μηνύματος σφάλματος αν αποτύχει η λήψη της τοποθεσίας
                            Toast.makeText(MapsActivity.this, "Σφάλμα λήψης τοποθεσίας", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        //κουμπί "stats" (εμφάνιση στατιστικών περιοχών)
        findViewById(R.id.stats).setOnClickListener(v -> showAreaStatistics());

        //κώδικα για το κουμπί SOS
        ImageButton fabSos = findViewById(R.id.fabSos);
        fabSos.setOnClickListener(view -> {
            // Έλεγχος αν έχει δοθεί η άδεια για πρόσβαση στην ακριβή τοποθεσία
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Λήψη της τελευταίας γνωστής τοποθεσίας
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    // Έλεγχος αν η τοποθεσία δεν είναι null
                    if (location != null) {
                        // Δημιουργία ενός LatLng αντικειμένου με τις συντεταγμένες της τρέχουσας τοποθεσίας
                        LatLng sosLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Δημιουργία αναφοράς για ένα νέο παιδί κάτω από τον κόμβο "SOS Αναφορές"
                        DatabaseReference sosRef = FirebaseDatabase.getInstance().getReference("SOS Αναφορές").push();

                        // Δημιουργία ενός HashMap για την αποθήκευση των δεδομένων της αναφοράς SOS
                        HashMap<String, Object> sosData = new HashMap<>();
                        sosData.put("latitude", String.valueOf(sosLocation.latitude));
                        sosData.put("longitude", String.valueOf(sosLocation.longitude));
                        // Δημιουργία ενός timestamp για την αναφορά SOS
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String timestamp = sdf.format(new Date());
                        sosData.put("timestamp", timestamp);

                        // Αποστολή των δεδομένων στην βάση δεδομένων
                        sosRef.setValue(sosData).addOnCompleteListener(task -> {
                            // Έλεγχος αν η αποστολή ήταν επιτυχής
                            if (task.isSuccessful()) {
                                // Εμφάνιση μηνύματος επιτυχίας
                                Toast.makeText(MapsActivity.this, "Αίτημα SOS εστάλη!", Toast.LENGTH_SHORT).show();
                                // Δεν χρειάζεται να εμφανίσουμε marker και μήνυμα εδώ για όλους,
                                // η παρακολούθηση της Firebase σε πραγματικό χρόνο θα το κάνει για τους άλλους.
                                // Εμφανίζουμε έναν τοπικό marker για επιβεβαίωση στον χρήστη που έστειλε το SOS.
                                // Δημιουργία ενός εικονιδίου SOS
                                Drawable sosIconDrawable = ContextCompat.getDrawable(MapsActivity.this, R.drawable.sos_icon);
                                int iconSize = 100;
                                Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                sosIconDrawable.setBounds(0, 0, iconSize, iconSize);
                                sosIconDrawable.draw(canvas);
                                Paint paint = new Paint();
                                paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                                canvas.drawBitmap(bitmap, 0, 0, paint);

                                // Δημιουργία MarkerOptions για τον SOS marker
                                MarkerOptions sosMarkerOptions = new MarkerOptions()
                                        .position(sosLocation)
                                        .title("SOS")
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                // Προσθήκη του marker στο χάρτη
                                currentSosMarker = mMap.addMarker(sosMarkerOptions);
                                // Αποθήκευση του marker στη λίστα των SOS markers
                                sosMarkers.put(sosRef.getKey(), currentSosMarker);

                                // Ξεκινάμε έναν timer για την αυτόματη αφαίρεση του τοπικού marker μετά από 10 λεπτά
                                Handler sosHandler = new Handler();
                                Runnable sosRunnable = () -> {
                                    if (currentSosMarker != null) {
                                        currentSosMarker.remove();
                                        sosMarkers.remove(sosRef.getKey());
                                        currentSosMarker = null;
                                    }
                                };
                                sosHandler.postDelayed(sosRunnable, 600000); // 10 λεπτά σε milliseconds
                            } else {
                                // Εμφάνιση μηνύματος σφάλματος αν η αποστολή απέτυχε
                                Toast.makeText(MapsActivity.this, "Σφάλμα κατά την αποστολή του SOS!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Εμφάνιση μηνύματος αν δεν μπόρεσε να ληφθεί η τοποθεσία
                        Toast.makeText(MapsActivity.this, "Δεν μπόρεσε να ληφθεί η τοποθεσία.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Αν δεν έχει δοθεί η άδεια τοποθεσίας, ζητάμε την από τον χρήστη
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
            // Άνοιγμα της εφαρμογής κλήσεων με τον αριθμό 100 (αστυνομία)
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:100"));
            startActivity(dialIntent);
        });
    }

    /**
     * Αποθηκεύει μια αναζήτηση του χρήστη στη Firebase Realtime Database.
     *
     * @param email    Το email του χρήστη που πραγματοποίησε την αναζήτηση.
     * @param location Η τοποθεσία που αναζητήθηκε.
     */
    private void saveSearch(String email, String location) {
        // Δημιουργία αναφοράς στον κόμβο "Αναζητήσεις"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναζητήσεις");

        // Δημιουργία ενός HashMap για τα δεδομένα της αναζήτησης
        Map<String, Object> searchData = new HashMap<>();
        searchData.put("email", email);
        searchData.put("location", location);
        // Προσθήκη ενός timestamp για την αναζήτηση
        searchData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        // Αποστολή των δεδομένων κάτω από ένα νέο αυτόματα δημιουργημένο κλειδί
        ref.push().setValue(searchData);
    }

    /**
     * Ελέγχει την ημερομηνία της τελευταίας αναφοράς του χρήστη και προχωρά στην
     * δραστηριότητα αναφοράς περιστατικού αν ο χρήστης είναι premium ή αν δεν έχει
     * κάνει αναφορά σήμερα.
     */
    private void checkLastReportDateAndProceed() {
        // Αναφορά στον κόμβο "Αναφορές"
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Αναφορές");

        // Αναφορά στον κόμβο "Premium Χρηστες"
        DatabaseReference premiumRef = FirebaseDatabase.getInstance().getReference("Premium Χρήστες");

        // Ανάγνωση των δεδομένων από τον κόμβο "Αναφορές" μία φορά
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Λήψη της σημερινής ημερομηνίας
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // Έλεγχος αν το email του χρήστη υπάρχει στους "Premium Χρηστες"
                premiumRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean canReport = true;
                        // Έλεγχος αν το email υπάρχει στον κόμβο Premium Χρήστες
                        boolean isPremiumUser = dataSnapshot.exists();

                        // Αν ο χρήστης είναι premium, μπορεί πάντα να κάνει αναφορά
                        if (isPremiumUser) {
                            canReport = true;
                        } else {
                            // Αν δεν είναι premium, ελέγχουμε αν έχει ήδη κάνει αναφορά σήμερα
                            for (DataSnapshot dataSnapshotReport : snapshot.getChildren()) {
                                String timestamp = dataSnapshotReport.child("timestamp").getValue(String.class);
                                String email2 = dataSnapshotReport.child("email").getValue(String.class);

                                // Έλεγχος αν η αναφορά είναι του τρέχοντος χρήστη και αν η ημερομηνία είναι η σημερινή
                                if (email != null && email2 != null && email2.equals(email) && timestamp != null && timestamp.length() >= 10) {
                                    String reportDate = timestamp.substring(0, 10);
                                    if (reportDate.equals(today)) {
                                        canReport = false; // Δεν επιτρέπεται άλλη αναφορά για σήμερα
                                        break;
                                    }
                                }
                            }
                        }

                        // Αν ο χρήστης μπορεί να κάνει αναφορά, ανοίγει την δραστηριότητα αναφοράς
                        if (canReport) {
                            openReportActivity();
                        } else {
                            // Εμφάνιση μηνύματος αν έχει υπερβεί το ημερήσιο όριο αναφορών
                            Toast.makeText(MapsActivity.this,
                                    "Υπερβήκατε τον αριθμό αναφορών που μπορείτε να κάνετε ημερησίως. Για παραπάνω αναφορές μεταβείτε στην Premium έκδοση.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Εμφάνιση μηνύματος σφάλματος αν υπάρξει πρόβλημα κατά την ανάγνωση των δεδομένων
                        Toast.makeText(MapsActivity.this, "Σφάλμα κατά την ανάγνωση των δεδομένων.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Εμφάνιση μηνύματος σφάλματος αν υπάρξει πρόβλημα κατά την ανάγνωση των δεδομένων
                Toast.makeText(MapsActivity.this, "Σφάλμα κατά την ανάγνωση των δεδομένων.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Ανοίγει την δραστηριότητα για την αναφορά ενός νέου περιστατικού.
     */
    private void openReportActivity() {
        Intent intent = new Intent(MapsActivity.this, ReportIncidentActivity.class);
        intent.putExtra("email", email);
        startActivityForResult(intent, 1);
    }

    /**
     * Φορτώνει τα ονόματα των περιοχών από τη βάση δεδομένων (συντεταγμένες και προσπάθεια
     * ανάκτησης ονόματος μέσω Geocoder).
     */
    private void loadAreaNames() {
        areaNames=new HashMap<>();
        //DatabaseReference heatmapsRef = FirebaseDatabase.getInstance().getReference("heatmaps");

        // Ανάγνωση των δεδομένων από την βάση δεδομένων μία φορά
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                areaNames.clear();
                areaCenters.clear();

                // Επανάληψη σε κάθε παιδί του κόμβου
                for (DataSnapshot areaSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους και μήκους ως strings
                    String latitude = areaSnapshot.child("latitude").getValue(String.class);
                    String longitude = areaSnapshot.child("longitude").getValue(String.class);
                    // Δημιουργία ενός LatLng αντικειμένου από τις συντεταγμένες
                    LatLng location = new LatLng(Double.parseDouble(latitude),Double.parseDouble( longitude));

                    // Αρχικοποίηση του Geocoder
                    geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());
                    String area ;
                    try {
                        // Ανάκτηση μιας λίστας διευθύνσεων για την δεδομένη τοποθεσία (παίρνουμε μόνο την πρώτη)
                        area = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1).toString();

                    } catch (IOException e) {
                        // Χειρισμός πιθανών εξαιρέσεων IOException
                        throw new RuntimeException(e);
                    }

                    // Αν το όνομα της περιοχής ανακτήθηκε επιτυχώς
                    if (area != null) {
                        areaNames.put(location, area); // Αποθήκευση της τοποθεσίας και του ονόματος της περιοχής
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Χειρισμός σφαλμάτων κατά την ανάγνωση των δεδομένων
            }
        });
    }

    /**
     * Εμφανίζει στατιστικά στοιχεία για τις περιοχές με αναφορές περιστατικών.
     * Υπολογίζει τον αριθμό των περιστατικών και το ποσοστό επικινδυνότητας για κάθε περιοχή.
     */
    private void showAreaStatistics() {
        // Φόρτωση των ονομάτων των περιοχών (για να συσχετίσουμε τις αναφορές με τις περιοχές)
        loadAreaNames();

        // Δημιουργία αναφοράς στον κόμβο "Αναφορές" της Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");

        // Ανάγνωση των δεδομένων από τον κόμβο "Αναφορές" μία φορά
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Εκκαθάριση των προηγούμενων μετρήσεων και ποσοστών
                incidentCounts.clear();
                areaDangerPercentages.clear();
                double totalWeight = 0;
                int totalIncidents = 0;

                // Επανάληψη σε κάθε αναφορά περιστατικού
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους, μήκους και βάρους αναφοράς περιστατικού
                    String latitude = dataSnapshot.child("latitude").getValue(String.class);
                    String longitude = dataSnapshot.child("longitude").getValue(String.class);
                    Integer weight = dataSnapshot.child("weight").getValue(Integer.class);

                    // Έλεγχος αν τα δεδομένα δεν είναι null
                    if (latitude != null && longitude != null && weight != null) {
                        try {
                            Double latitude2 = Double.parseDouble(latitude);
                            Double longitude2 = Double.parseDouble(longitude);

                            // Ανάκτηση της περιοχής για τις δεδομένες συντεταγμένες
                            String area = getArea(latitude2, longitude2);

                            // Αν η περιοχή ανακτήθηκε επιτυχώς
                            if (area != null) {
                                // Αύξηση του μετρητή περιστατικών για την συγκεκριμένη περιοχή
                                incidentCounts.put(area, incidentCounts.getOrDefault(area, 0) + 1);
                                // Προσθήκη του βάρους στο συνολικό βάρος
                                totalWeight += weight;
                                // Αύξηση του συνολικού αριθμού περιστατικών
                                totalIncidents++;
                            }
                        } catch (NumberFormatException e) {
                            Log.e("MapsActivity", "Invalid latitude or longitude format in Firebase: " + e.getMessage());
                        }
                    } else {
                        Log.e("MapsActivity", "Latitude, longitude, or weight data is null in Firebase.");
                    }
                }



                // Υπολογισμός ποσοστού επικινδυνότητας περιοχής
                /*
                * Ο αριθμός περιστατικών δεν επηρεάζει άμεσα το ποσοστό.
                * Το βάρος των περιστατικών (π.χ., σοβαρότητα) επηρεάζει.
                * Άρα, περιοχή με λιγότερα αλλά βαριά περιστατικά μπορεί να έχει μεγαλύτερο ποσοστό.
                *
                * */
                if (totalIncidents > 0) {
                    for (Map.Entry<String, Integer> entry : incidentCounts.entrySet()) {
                        String area = entry.getKey();
                        double areaWeight = calculateAreaWeight(snapshot, area);
                        double dangerPercentage = (areaWeight / totalWeight) * 100;
                        areaDangerPercentages.put(area, dangerPercentage); //Ποσοστο για κάθε περιοχή
                    }
                }
                toggleMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Σφάλμα ανάκτησης δεδομένων", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Υπολογίζει το συνολικό βάρος επικινδυνότητας για μια συγκεκριμένη περιοχή
     * με βάση τις αναφορές περιστατικών που υπάρχουν στο snapshot.
     *
     * @param snapshot Το snapshot των δεδομένων από τον κόμβο "Αναφορές".
     * @param area     Το όνομα της περιοχής για την οποία θέλουμε να υπολογίσουμε το βάρος.
     * @return Το συνολικό βάρος επικινδυνότητας για την περιοχή.
     */
            private double calculateAreaWeight(DataSnapshot snapshot, String area) {
                double areaWeight = 0;
                // Επανάληψη σε κάθε αναφορά περιστατικού στο snapshot
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους, μήκους και βάρους επικινδυνότητας
                    String latitude = dataSnapshot.child("latitude").getValue(String.class);
                    String longitude = dataSnapshot.child("longitude").getValue(String.class);
                    Integer weight = dataSnapshot.child("weight").getValue(Integer.class);

                    // Έλεγχος αν τα δεδομένα δεν είναι null
                    if (latitude != null && longitude != null && weight != null) {
                        // Ανάκτηση της περιοχής για τις συντεταγμένες του περιστατικού
                        String incidentArea = getArea(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        // Αν η περιοχή του περιστατικού ταιριάζει με την περιοχή που ψάχνουμε
                        if (area.equals(incidentArea)) {
                            areaWeight += weight; // Προσθήκη του βάρους στο συνολικό βάρος της περιοχής
                        }
                    }
                }
                return areaWeight;
            }

    /**
     * Επιστρέφει το όνομα της περιοχής για μια δεδομένη γεωγραφική τοποθεσία.
     * Χρησιμοποιεί την αποθηκευμένη αντιστοίχιση από τη μέθοδο `loadAreaNames()`.
     *
     * @param latitude  Το γεωγραφικό πλάτος της τοποθεσίας.
     * @param longitude Το γεωγραφικό μήκος της τοποθεσίας.
     * @return Το όνομα της περιοχής ή null αν δεν βρεθεί.
     */
    private String getArea(double latitude, double longitude) {
        // Δημιουργία ενός LatLng αντικειμένου για την δεδομένη τοποθεσία
        LatLng location = new LatLng(latitude, longitude);

        // Επανάληψη στην αντιστοίχιση τοποθεσίας-ονόματος περιοχής
        for (Map.Entry<LatLng, String> entry : areaNames.entrySet()) {
            // Έλεγχος αν η τρέχουσα τοποθεσία ταιριάζει με την τοποθεσία του entry
            if (entry.getKey().equals(location)) {
                return entry.getValue(); // Επιστροφή του ονόματος της περιοχής
            }
        }
        return null; // Επιστροφή null αν δεν βρεθεί η τοποθεσία
    }

    /**
     * Εναλλάσσει την ορατότητα των markers που υποδεικνύουν στατιστικά στοιχεία περιοχών.
     * Αν οι markers είναι ορατοί, τους αφαιρεί. Αν δεν είναι, δημιουργεί νέους markers
     * με βάση τα υπολογισμένα στατιστικά (αριθμός περιστατικών, ποσοστό επικινδυνότητας).
     * Επίσης, ομαδοποιεί τους κοντινούς markers για καλύτερη οπτικοποίηση.
     */
    private void toggleMarkers() {
        String area_backup="";
        String area="";
        // Αν οι markers είναι ήδη ορατοί
        if (markersVisible) {
            // Αφαίρεση όλων των markers περιοχών από το χάρτη
            for (Marker marker : areaMarkers) {
                marker.remove();
            }
            areaMarkers.clear(); // Εκκαθάριση της λίστας των markers
            markersVisible = false; // Ορισμός της ορατότητας σε false
        } else {
            areaMarkers.clear(); // Εκκαθάριση της λίστας των markers πριν την προσθήκη νέων

            List<MarkerOptions> markerOptionsList = new ArrayList<>();
            List<MarkerOptions> markerOptionsList2 = new ArrayList<>();
            Map<String, MarkerOptions> areaMarkerOptionsMap = new HashMap<>();
            Set<String> areasWithIncidents = new HashSet<>(); // Σύνολο για την αποθήκευση των περιοχών με περιστατικά


            // Επανάληψη σε κάθε καταχωρημένη περιοχή με περιστατικά
            for (Map.Entry<String, Integer> entry : incidentCounts.entrySet()) {
                area = entry.getKey();
                areasWithIncidents.add(area); // Προσθήκη της περιοχής στο σύνολο

                int count = entry.getValue(); // Λήψη του αριθμού των περιστατικών
                LatLng location = getAreaCenter(area); // Λήψη του κέντρου της περιοχής
                Double dangerPercentage = areaDangerPercentages.get(area); // Λήψη του ποσοστού επικινδυνότητας

                // Αν υπάρχουν συντεταγμένες και ποσοστό επικινδυνότητας για την περιοχή
                if (location != null && dangerPercentage != null) {
                    // Δημιουργία ενός custom εικονιδίου για τον marker
                    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.info_icon);
                    int iconSize = 100;
                    Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, iconSize, iconSize);
                    drawable.draw(canvas);
                    Paint paint = new Paint();
                    paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(bitmap, 0, 0, paint);

                    String area_name="";
                    boolean flag=true;
                    // Προσπάθεια εξαγωγής ενός πιο σύντομου ονόματος περιοχής από την πλήρη διεύθυνση
                    for (int i = 0; i < area.length(); i++) {
                        char currentChar = area.charAt(i);
                        if(i>=25 &&flag ){
                            if(currentChar==']'){
                                flag=false;
                                break;
                            }
                            area_name+=currentChar;
                        }
                    }
                    area_backup=area;
                    area=area_name;

                    // Δημιουργία MarkerOptions για την εμφάνιση των στατιστικών της περιοχής
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location)
                            .title(area)
                            .snippet("Περιστατικά: " + count + " Επικινδυνότητα: " + String.format("%.2f", dangerPercentage) + "%")
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    markerOptionsList.add(markerOptions);

                    MarkerOptions markerOptions2 = new MarkerOptions()
                            .position(location)
                            .title(area_backup)
                            .snippet("Περιστατικά: " + count + " Επικινδυνότητα: " + String.format("%.2f", dangerPercentage) + "%")
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    markerOptionsList2.add(markerOptions2.title(area_backup));
                }
            }

            // Δημιουργία ενός διαφορετικού εικονιδίου για περιοχές χωρίς καταγεγραμμένα περιστατικά
            Drawable drawable2 = ContextCompat.getDrawable(this, R.drawable.info_icon);
            int iconSize2 = 100;
            Bitmap bitmap2 = Bitmap.createBitmap(iconSize2, iconSize2, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(bitmap2);
            drawable2.setBounds(0, 0, iconSize2, iconSize2);
            drawable2.draw(canvas2);
            Paint paint2 = new Paint();
            paint2.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
            canvas2.drawBitmap(bitmap2, 0, 0, paint2);

            // Προσθήκη markers για περιοχές της Αθήνας χωρίς καταγεγραμμένα περιστατικά
            for (Map.Entry<String, AreaInfo> entry : athensAreas.entrySet()) {
                String areaName = entry.getKey();
                AreaInfo areaInfo = entry.getValue();

                // Αν η περιοχή δεν περιλαμβάνεται στις περιοχές με περιστατικά
                if (!areasWithIncidents.contains(areaName)) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(areaInfo.center)
                            .title(areaName)
                            .snippet(areaInfo.safetyLevel)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap2));
                    Marker marker = mMap.addMarker(markerOptions);
                    areaMarkers.add(marker);
                }
            }

            // Ομαδοποίηση των markers που είναι κοντά μεταξύ τους
            List<List<MarkerOptions>> groupedMarkers = groupMarkersByDistance(markerOptionsList, 100);
            List<List<MarkerOptions>> groupedMarkers2 = groupMarkersByDistance(markerOptionsList2, 100);

            // Δημιουργία markers για κάθε ομάδα
            int count=0;
            for (List<MarkerOptions> group : groupedMarkers) {
                // Αν η ομάδα περιέχει μόνο έναν marker, τον προσθέτουμε απευθείας
                if (group.size() == 1) {
                    Marker marker = mMap.addMarker(group.get(0));
                    marker.setTag(groupedMarkers2.get(count).get(0).getTitle()); // Ορισμός tag με το πλήρες όνομα περιοχής
                    areaMarkers.add(marker);
                } else {
                    // Αν η ομάδα περιέχει πολλούς markers, υπολογίζουμε το κέντρο της ομάδας και συνοψίζουμε τα στοιχεία
                    LatLng groupLocation = calculateGroupCenter(group);
                    int totalCount = 0;
                    double totalDangerPercentage = 0;
                    List<Integer> allHours = new ArrayList<>();

                    StringBuilder titleBuilder = new StringBuilder();
                    int count2=0;
                    for (MarkerOptions markerOptions : group) {
                        totalCount += incidentCounts.get(groupedMarkers2.get(count).get(count2).getTitle());
                        totalDangerPercentage += areaDangerPercentages.get(groupedMarkers2.get(count).get(count2).getTitle());
                        titleBuilder.append(groupedMarkers.get(count).get(count2).getTitle()).append(", ");
                        allHours.addAll(getIncidentHours(groupedMarkers2.get(count).get(count2).getTitle()));
                        count2++;
                    }
                    double averageDangerPercentage = totalDangerPercentage / group.size();
                    titleBuilder.delete(titleBuilder.length() - 2, titleBuilder.length()); // Αφαίρεση του τελευταίου ", "

                    int mostDangerousHour = calculateMostFrequentHour(allHours); // Υπολογισμός της πιο συχνής ώρας περιστατικών

                    // Δημιουργία ενός marker για την ομάδα
                    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.info_icon);
                    int iconSize = 100;
                    Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, iconSize, iconSize);
                    drawable.draw(canvas);
                    Paint paint = new Paint();
                    paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(bitmap, 0, 0, paint);

                    String group_name ="";
                    if (!group.isEmpty() && group.get(0).getTitle() != null) {
                        String fullAddress = group.get(0).getTitle();
                        String[] parts = fullAddress.split(", ");
                        if (parts.length > 1) {
                            group_name = parts[1];
                        }
                    }
                    if (group_name.length() > 6) {
                        group_name = group_name.substring(0, group_name.length() - 6);
                    }

                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(groupLocation)
                            .title(group_name)
                            .snippet("Περιστατικά:" + totalCount + " Επικινδυνότητα: " + String.format("%.2f", averageDangerPercentage) + "%\nΠιο επικίνδυνη ώρα: " + mostDangerousHour + ":00")
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    marker.setTag(groupedMarkers2.get(count)); // Ορισμός tag με τη λίστα των αρχικών markers
                    areaMarkers.add(marker);
                }
                count++;
            }
            markersVisible = true; // Ορισμός της ορατότητας σε true

            // Ορισμός OnInfoWindowClickListener για τους markers
            mMap.setOnInfoWindowClickListener(marker -> {
                Object tag = marker.getTag();
                // Αν το tag είναι ένα String, εμφανίζουμε λεπτομέρειες για μια μεμονωμένη περιοχή
                if (tag instanceof String) {
                    showIncidentDetails((String) tag);
                } else if (tag instanceof List) {
                    // Αν το tag είναι μια λίστα, εμφανίζουμε λεπτομέρειες για ομαδοποιημένους markers
                    List<MarkerOptions> group = (List<MarkerOptions>) tag;
                    showGroupedIncidentDetails(group);
                }
            });
        }
    }

    /**
     * Ανακτά τις ώρες των περιστατικών για μια συγκεκριμένη περιοχή από τη Firebase.
     *
     * @param area Το όνομα της περιοχής για την οποία θέλουμε τις ώρες των περιστατικών.
     * @return Μια λίστα με τις ώρες (0-23) των περιστατικών στην περιοχή.
     */
    private List<Integer> getIncidentHours(String area) {
        List<Integer> hours = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές"); // Δημιουργία νέας μεταβλητής

        // Ανάγνωση των δεδομένων από τον κόμβο "Αναφορές" μία φορά
        ref.addListenerForSingleValueEvent(new ValueEventListener()  {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // Επανάληψη σε κάθε αναφορά περιστατικού
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους, μήκους και timestamp
                    String latitude = dataSnapshot.child("latitude").getValue(String.class);
                    String longitude = dataSnapshot.child("longitude").getValue(String.class);
                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class);

                    // Έλεγχος αν τα δεδομένα δεν είναι null
                    if (latitude != null && longitude != null && timestamp != null) {
                        // Ανάκτηση της περιοχής του περιστατικού
                        String incidentArea = getArea(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        // Αν η περιοχή του περιστατικού ταιριάζει με την περιοχή που ψάχνουμε
                        if (area.equals(incidentArea)) {
                            try {
                                // Παρσάρισμα του timestamp σε Date
                                Date date = dateFormat.parse(timestamp);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                // Λήψη της ώρας της ημέρας (0-23)
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                hours.add(hour); // Προσθήκη της ώρας στη λίστα
                            } catch (ParseException e) {
                                // Καταγραφή σφάλματος αν το timestamp δεν μπορεί να παρσαριστεί
                                Log.e("MapsActivity", "Σφάλμα ανάλυσης ημερομηνίας: " + timestamp, e);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Καταγραφή σφάλματος αν η ανάγνωση των δεδομένων αποτύχει
                Log.e("MapsActivity", "Σφάλμα ανάκτησης δεδομένων", error.toException());
            }
        });
        return hours;
    }

    /**
     * Υπολογίζει την ώρα με τα περισσότερα περιστατικά από μια λίστα ωρών.
     *
     * @param hours Μια λίστα με τις ώρες (0-23) των περιστατικών.
     * @return Η ώρα με τα περισσότερα περιστατικά ή -1 αν η λίστα είναι άδεια.
     */

    private int calculateMostFrequentHour(List<Integer> hours) {
        if (hours.isEmpty()) {
            return -1; // Δεν υπάρχουν ώρες
        }

        Map<Integer, Integer> hourCount = new HashMap<>();
        for (int hour : hours) {
            hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
        }
        return Collections.max(hourCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }



    /**
     * Ομαδοποιεί μια λίστα από MarkerOptions με βάση την απόστασή τους.
     * Δημιουργεί ομάδες markers που βρίσκονται εντός ενός καθορισμένου
     * ορίου απόστασης μεταξύ τους.
     *
     * @param markerOptionsList Η λίστα των MarkerOptions που θα ομαδοποιηθούν.
     * @param distanceThreshold Η μέγιστη απόσταση (σε μέτρα) μεταξύ δύο markers για να θεωρηθούν στην ίδια ομάδα.
     * @return Μια λίστα από λίστες MarkerOptions, όπου κάθε εσωτερική λίστα αντιπροσωπεύει μια ομάδα.
     */
    private List<List<MarkerOptions>> groupMarkersByDistance(List<MarkerOptions> markerOptionsList, double distanceThreshold) {
        List<List<MarkerOptions>> groupedMarkers = new ArrayList<>();
        // Δημιουργία μιας αντιγραφής της αρχικής λίστας για να παρακολουθούμε τους markers που δεν έχουν ομαδοποιηθεί
        List<MarkerOptions> remainingMarkers = new ArrayList<>(markerOptionsList);

        // Επανάληψη όσο υπάρχουν markers που δεν έχουν ομαδοποιηθεί
        while (!remainingMarkers.isEmpty()) {
            // Δημιουργία μιας νέας ομάδας
            List<MarkerOptions> group = new ArrayList<>();
            // Επιλογή του πρώτου marker από τους υπόλοιπους ως αρχικό για τη νέα ομάδα
            MarkerOptions firstMarker = remainingMarkers.remove(0);
            group.add(firstMarker);

            // Δημιουργία μιας λίστας για την αποθήκευση των markers που θα αφαιρεθούν από τους υπόλοιπους
            List<MarkerOptions> markersToRemove = new ArrayList<>();
            // Επανάληψη στους υπόλοιπους markers
            for (MarkerOptions otherMarker : remainingMarkers) {
                // Υπολογισμός της απόστασης μεταξύ του αρχικού marker της ομάδας και του τρέχοντος marker
                if (Distance(firstMarker.getPosition(), otherMarker.getPosition()) <= distanceThreshold) {
                    // Αν η απόσταση είναι εντός του ορίου, προσθέτουμε τον marker στην τρέχουσα ομάδα
                    group.add(otherMarker);
                    markersToRemove.add(otherMarker); // Σημειώνουμε τον marker για αφαίρεση από τους υπόλοιπους
                }
            }

            // Αφαίρεση των markers που προστέθηκαν στην τρέχουσα ομάδα από τους υπόλοιπους
            remainingMarkers.removeAll(markersToRemove);
            // Προσθήκη της τρέχουσας ομάδας στην λίστα των ομαδοποιημένων markers
            groupedMarkers.add(group);
        }

        return groupedMarkers;
    }

    /**
     * Υπολογίζει την απόσταση (σε μέτρα) μεταξύ δύο γεωγραφικών συντεταγμένων (LatLng).
     *
     * @param latLng1 Οι συντεταγμένες του πρώτου σημείου.
     * @param latLng2 Οι συντεταγμένες του δεύτερου σημείου.
     * @return Η απόσταση σε μέτρα μεταξύ των δύο σημείων.
     */
    private double Distance(LatLng latLng1, LatLng latLng2) {
        float[] results = new float[1];
        // Χρήση της Location.distanceBetween για τον υπολογισμό της απόστασης
        Location.distanceBetween(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, results);
        return results[0];
    }

    /**
     * Υπολογίζει το κεντρικό σημείο (γεωγραφικές συντεταγμένες) μιας ομάδας MarkerOptions.
     * Χρησιμοποιείται για την τοποθέτηση ενός marker που αντιπροσωπεύει μια ομάδα κοντινών markers.
     *
     * @param group Η λίστα των MarkerOptions που αποτελούν την ομάδα.
     * @return Ένα LatLng αντικείμενο που αντιπροσωπεύει το κεντρικό σημείο της ομάδας.
     */
    private LatLng calculateGroupCenter(List<MarkerOptions> group) {
        double latSum = 0;
        double lngSum = 0;
        // Άθροιση των γεωγραφικών πλατών και μηκών όλων των markers στην ομάδα
        for (MarkerOptions markerOptions : group) {
            latSum += markerOptions.getPosition().latitude;
            lngSum += markerOptions.getPosition().longitude;
        }
        // Υπολογισμός του μέσου όρου των πλατών και μηκών για να βρούμε το κέντρο
        return new LatLng(latSum / group.size(), lngSum / group.size());
    }

    /**
     * Εμφανίζει λεπτομέρειες για τα περιστατικά σε μια ομαδοποιημένη περιοχή.
     * Δημιουργεί ένα AlertDialog που περιέχει μια λίστα με τις περιγραφές και
     * τους χρόνους των περιστατικών στην ομάδα των markers.
     *
     * @param group Η λίστα των MarkerOptions που αποτελούν την ομάδα. Χρησιμοποιείται
     * για την ανάκτηση των ονομάτων των περιοχών που περιλαμβάνονται στην ομάδα.
     */
    private void showGroupedIncidentDetails(List<MarkerOptions> group) {
        String area = null;
        // Προσπάθεια ανάκτησης ενός αντιπροσωπευτικού ονόματος περιοχής από την ομάδα
        if (!group.isEmpty() && group.get(0).getTitle() != null) {
            String fullAddress = group.get(0).getTitle();
            String[] parts = fullAddress.split(", "); // Διαχωρίζουμε την διεύθυνση από τα κόμματα
            if (parts.length > 1) {
                area = parts[1]; // Η περιοχή είναι το δεύτερο στοιχείο μετά την διεύθυνση
            }
        }

        // Κοντύνουμε το όνομα της περιοχής αν είναι πολύ μεγάλο
        if (area != null && area.length() > 6) {
            area = area.substring(0, area.length() - 6);
        }

        // Δημιουργία ενός AlertDialog για την εμφάνιση των λεπτομερειών
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Περιστατικά στην περιοχή " + (area != null ? area : ""));
        // Δημιουργία ενός LinearLayout για την τοποθέτηση των περιεχομένων
        LinearLayout layout = new LinearLayout(MapsActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Λίστα για την αποθήκευση όλων των λεπτομερειών των περιστατικών στην ομάδα
        List<IncidentDetails> allIncidents = new ArrayList<>();
        // Map για την καταμέτρηση των περιστατικών ανά ώρα
        Map<Integer, Integer> hourCount = new HashMap<>();
        // Δημιουργία ενός SimpleDateFormat για την μορφοποίηση του timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Εκτέλεση ανάκτησης δεδομένων για κάθε περιοχή που αντιπροσωπεύεται από την ομάδα
        retrieveDataForGroup(group, 0, allIncidents, hourCount, dateFormat, builder, layout);
    }

    /**
     * Ανακτά τα δεδομένα των περιστατικών για μια ομάδα περιοχών (markers) από τη Firebase.
     * Καλείται αναδρομικά για κάθε marker στην ομάδα.
     *
     * @param group         Η λίστα των MarkerOptions που αποτελούν την ομάδα.
     * @param index         Ο τρέχων δείκτης του marker που εξετάζεται στην ομάδα.
     * @param allIncidents  Η λίστα στην οποία προστίθενται οι λεπτομέρειες των περιστατικών.
     * @param hourCount     Το map στο οποίο καταγράφεται η συχνότητα των περιστατικών ανά ώρα.
     * @param dateFormat    Το SimpleDateFormat για την ανάλυση του timestamp.
     * @param builder       Ο AlertDialog.Builder για την δημιουργία του dialog.
     * @param layout        Το LinearLayout στο οποίο θα προστεθούν οι πληροφορίες των περιστατικών.
     */
    private void retrieveDataForGroup(List<MarkerOptions> group, int index, List<IncidentDetails> allIncidents, Map<Integer, Integer> hourCount, SimpleDateFormat dateFormat, AlertDialog.Builder builder, LinearLayout layout) {
        // Αν έχουν εξεταστεί όλοι οι markers στην ομάδα, καλούμε την μέθοδο για την εμφάνιση των αποτελεσμάτων
        if (index >= group.size()) {
            showGroupedIncidents(allIncidents, hourCount, builder, layout);
            return;
        }

        // Λήψη του τρέχοντος marker από την ομάδα
        MarkerOptions markerOptions = group.get(index);
        // Δημιουργία αναφοράς στον κόμβο "Αναφορές" της Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");

        // Ανάγνωση των δεδομένων από τον κόμβο "Αναφορές" μία φορά
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Επανάληψη σε κάθε αναφορά περιστατικού
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους, μήκους, περιγραφής, βάρους και timestamp
                    String latitude = dataSnapshot.child("latitude").getValue(String.class);
                    String longitude = dataSnapshot.child("longitude").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    Integer weight = dataSnapshot.child("weight").getValue(Integer.class);
                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class);

                    // Έλεγχος αν όλα τα απαραίτητα δεδομένα υπάρχουν
                    if (latitude != null && longitude != null && description != null && weight != null && timestamp != null) {
                        // Ανάκτηση της περιοχής του περιστατικού
                        String incidentArea = getArea(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        // Έλεγχος αν η περιοχή του περιστατικού ταιριάζει με τον τίτλο του τρέχοντος marker
                        if (markerOptions.getTitle().equals(incidentArea)) {
                            try {
                                // Παρσάρισμα του timestamp σε Date
                                Date date = dateFormat.parse(timestamp);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                // Λήψη της ώρας της ημέρας
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                // Καταγραφή της συχνότητας της ώρας
                                hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
                                // Δημιουργία ενός νέου αντικειμένου IncidentDetails και προσθήκη του στη λίστα
                                allIncidents.add(new IncidentDetails(description, weight, timestamp, date));
                            } catch (ParseException e) {
                                // Καταγραφή σφάλματος αν η ανάλυση του timestamp αποτύχει
                                Log.e("showGroupedIncidentDetails", "Σφάλμα ανάλυσης ημερομηνίας: " + timestamp, e);
                            }
                        }
                    }
                }

                // Συνέχιση στην επόμενη περιοχή (marker)
                retrieveDataForGroup(group, index + 1, allIncidents, hourCount, dateFormat, builder, layout);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Εμφάνιση μηνύματος σφάλματος αν η ανάγνωση των δεδομένων αποτύχει
                Toast.makeText(MapsActivity.this, "Σφάλμα ανάκτησης δεδομένων", Toast.LENGTH_SHORT).show();
                // Συνέχιση στην επόμενη περιοχή (marker)
                retrieveDataForGroup(group, index + 1, allIncidents, hourCount, dateFormat, builder, layout);
            }
        });
    }

    /**
     * Εμφανίζει τις λεπτομέρειες των περιστατικών για μια ομάδα περιοχών σε ένα AlertDialog.
     * Περιλαμβάνει τη λίστα των περιστατικών και την πιο επικίνδυνη ώρα.
     *
     * @param allIncidents Η λίστα με τις λεπτομέρειες όλων των περιστατικών στην ομάδα.
     * @param hourCount    Το map με τη συχνότητα των περιστατικών ανά ώρα.
     * @param builder      Ο AlertDialog.Builder για την δημιουργία του dialog.
     * @param layout       Το LinearLayout στο οποίο θα προστεθούν οι πληροφορίες.
     */
    private void showGroupedIncidents(List<IncidentDetails> allIncidents, Map<Integer, Integer> hourCount, AlertDialog.Builder builder, LinearLayout layout) {
        // Ταξινόμηση των περιστατικών κατά ημερομηνία και ώρα (νεότερα πρώτα)
        Collections.sort(allIncidents, (o1, o2) -> o2.date.compareTo(o1.date));

        int mostDangerousHour = -1;
        // Εύρεση της ώρας με τα περισσότερα περιστατικά
        if (!hourCount.isEmpty()) {
            mostDangerousHour = Collections.max(hourCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        // Δημιουργία ενός StringBuilder για την κατασκευή του κειμένου των περιστατικών
        StringBuilder incidentsText = new StringBuilder();
        int incidentNumber = 1;
        // Προσθήκη κάθε περιστατικού στο κείμενο με αρίθμηση
        for (IncidentDetails details : allIncidents) {
            incidentsText.append(String.format("%d. %s (%s)\n", incidentNumber, details.description, details.timestamp));
            incidentNumber++;
        }

        // Δημιουργία ενός ScrollView για να χωρέσει μεγάλο αριθμό περιστατικών
        ScrollView scrollView = new ScrollView(MapsActivity.this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Δημιουργία ενός LinearLayout για την τοποθέτηση του κειμένου των περιστατικών
        LinearLayout incidentsLayout = new LinearLayout(MapsActivity.this);
        incidentsLayout.setOrientation(LinearLayout.VERTICAL);

        // Δημιουργία ενός TextView για την εμφάνιση του κειμένου των περιστατικών
        TextView incidentsTextView = new TextView(MapsActivity.this);
        incidentsTextView.setText(incidentsText.toString());
        incidentsTextView.setPadding(10, 10, 10, 10);
        incidentsLayout.addView(incidentsTextView);

        scrollView.addView(incidentsLayout);

        // Αφαίρεση τυχόν προηγούμενων views από το layout
        layout.removeAllViews();

        // Προσθήκη του ScrollView στο layout
        layout.addView(scrollView);

        // Δημιουργία και προσθήκη ενός TextView για την εμφάνιση της πιο επικίνδυνης ώρας (αν υπάρχει)
        if (mostDangerousHour != -1) {
            TextView dangerousHourText = new TextView(MapsActivity.this);
            String fullText = "Πιο επικίνδυνη ώρα: " + mostDangerousHour + ":00";
            SpannableString spannableString = new SpannableString(fullText);
            spannableString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Πιο επικίνδυνη ώρα: ".length(), 0);
            dangerousHourText.setText(spannableString);
            dangerousHourText.setPadding(10, 10, 10, 10);
            layout.addView(dangerousHourText);
        }

        // Ορισμός του view του AlertDialog
        builder.setView(layout);
        // Ορισμός ενός θετικού κουμπιού για το κλείσιμο του dialog
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        // Εμφάνιση του AlertDialog
        builder.show();
    }





    /**
     * Εμφανίζει τις λεπτομέρειες των περιστατικών για μια συγκεκριμένη περιοχή σε ένα AlertDialog.
     * Περιλαμβάνει τη λίστα των περιστατικών και την πιο επικίνδυνη ώρα.
     *
     * @param area Το όνομα της περιοχής για την οποία θέλουμε να εμφανίσουμε τις λεπτομέρειες.
     */
    private void showIncidentDetails(String area) {
        // Δημιουργία αναφοράς στον κόμβο "Αναφορές" της Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");

        // Ανάγνωση των δεδομένων από τον κόμβο "Αναφορές" μία φορά
        ref.addListenerForSingleValueEvent(new ValueEventListener()  {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Λίστα για την αποθήκευση των λεπτομερειών των περιστατικών
                List<IncidentDetails> incidentDetailsList = new ArrayList<>();
                // Map για την καταμέτρηση των περιστατικών ανά ώρα
                Map<Integer, Integer> hourCount = new HashMap<>();
                // Δημιουργία ενός SimpleDateFormat για την μορφοποίηση του timestamp
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // Επανάληψη σε κάθε αναφορά περιστατικού
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Ανάκτηση γεωγραφικού πλάτους, μήκους, περιγραφής, βάρους και timestamp
                    String latitude = dataSnapshot.child("latitude").getValue(String.class);
                    String longitude = dataSnapshot.child("longitude").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    Integer weight = dataSnapshot.child("weight").getValue(Integer.class);
                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class);

                    // Έλεγχος αν όλα τα απαραίτητα δεδομένα υπάρχουν
                    if (latitude != null && longitude != null && description != null && weight != null && timestamp != null) {
                        // Ανάκτηση της περιοχής του περιστατικού
                        String incidentArea = getArea(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        // Έλεγχος αν η περιοχή του περιστατικού ταιριάζει με την περιοχή που ζητήθηκε
                        if (area.equals(incidentArea)) {
                            try {
                                // Παρσάρισμα του timestamp σε Date
                                Date date = dateFormat.parse(timestamp);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                // Λήψη της ώρας της ημέρας
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                // Καταγραφή της συχνότητας της ώρας
                                hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
                                // Δημιουργία ενός νέου αντικειμένου IncidentDetails και προσθήκη του στη λίστα
                                incidentDetailsList.add(new IncidentDetails(description, weight, timestamp, date));
                            } catch (ParseException e) {
                                // Καταγραφή σφάλματος αν η ανάλυση του timestamp αποτύχει
                                Log.e("showIncidentDetails", "Σφάλμα ανάλυσης ημερομηνίας: " + timestamp, e);
                            }
                        }
                    }
                }

                int mostDangerousHour = -1;
                // Εύρεση της ώρας με τα περισσότερα περιστατικά
                if (!hourCount.isEmpty()) {
                    mostDangerousHour = Collections.max(hourCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                }

                // Ταξινόμηση των περιστατικών κατά ημερομηνία (νεότερα πρώτα)
                Collections.sort(incidentDetailsList, (o1, o2) -> o2.date.compareTo(o1.date));

                // Δημιουργία ενός AlertDialog για την εμφάνιση των λεπτομερειών
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                // Προσπάθεια εξαγωγής ενός πιο σύντομου ονόματος περιοχής από την πλήρη διεύθυνση
                String area_backup=area.substring(26);
                String area_name="";
                int count_characters=0;
                for(int i=0; i<area_backup.length();i++){
                    area_name+=area_backup.charAt(i);
                    if(area_backup.charAt(i)>='0' &&area_backup.charAt(i)<='9' ){
                        count_characters++;
                        if(count_characters>=6 ){
                            if(area_backup.charAt(i+1)>='0' &&area_backup.charAt(i+1)<='9'){
                                continue;
                            }else{
                                break;
                            }
                        }
                    }
                }

                builder.setTitle("Περιστατικά στην περιοχή "+area_name);
                // Δημιουργία ενός LinearLayout για την τοποθέτηση των περιεχομένων
                LinearLayout layout = new LinearLayout(MapsActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Δημιουργία ενός ScrollView για να χωρέσει μεγάλο αριθμό περιστατικών
                ScrollView scrollView = new ScrollView(MapsActivity.this);
                scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Δημιουργία ενός LinearLayout για την τοποθέτηση των περιστατικών
                LinearLayout incidentsLayout = new LinearLayout(MapsActivity.this);
                incidentsLayout.setOrientation(LinearLayout.VERTICAL);

                int incidentNumber = 1;
                // Προσθήκη κάθε περιστατικού στο layout
                for (IncidentDetails details : incidentDetailsList) {
                    LinearLayout incidentLayout = new LinearLayout(MapsActivity.this);
                    incidentLayout.setOrientation(LinearLayout.VERTICAL);

                    TextView descriptionTextView = new TextView(MapsActivity.this);
                    descriptionTextView.setText(String.format("%d. %s", incidentNumber, details.description));
                    descriptionTextView.setPadding(10, 10, 10, 0);

                    TextView timestampTextView = new TextView(MapsActivity.this);
                    timestampTextView.setText(String.format("Ημερομηνία: %s", details.timestamp));
                    timestampTextView.setPadding(10, 0, 10, 10);

                    incidentLayout.addView(descriptionTextView);
                    incidentLayout.addView(timestampTextView);

                    incidentsLayout.addView(incidentLayout);
                    incidentNumber++;
                }

                scrollView.addView(incidentsLayout);

                // Αφαίρεση τυχόν προηγούμενων views από το layout
                layout.removeAllViews();

                // Προσθήκη του ScrollView στο layout
                layout.addView(scrollView);

                // Δημιουργία και προσθήκη ενός TextView για την εμφάνιση της πιο επικίνδυνης ώρας (αν υπάρχει)
                if (mostDangerousHour != -1) {
                    TextView dangerousHourText = new TextView(MapsActivity.this);
                    String fullText = "Πιο επικίνδυνη ώρα: " + mostDangerousHour + ":00";
                    SpannableString spannableString = new SpannableString(fullText);
                    spannableString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Πιο επικίνδυνη ώρα: ".length(), 0);
                    dangerousHourText.setText(spannableString);
                    dangerousHourText.setPadding(10, 10, 10, 10);
                    layout.addView(dangerousHourText);
                }

                // Ορισμός του view του AlertDialog
                builder.setView(layout);
                // Ορισμός ενός θετικού κουμπιού για το κλείσιμο του dialog
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                // Εμφάνιση του AlertDialog
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Εμφάνιση μηνύματος σφάλματος αν η ανάγνωση των δεδομένων αποτύχει
                Toast.makeText(MapsActivity.this, "Σφάλμα ανάκτησης δεδομένων", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Εσωτερική στατική κλάση για την αναπαράσταση των λεπτομερειών ενός περιστατικού.
     */
    private static class IncidentDetails {
        String description;
        int weight;
        String timestamp;
        Date date; // Προσθέτουμε το πεδίο date για την ταξινόμηση

        IncidentDetails(String description, int weight, String timestamp, Date date) {
            this.description = description;
            this.weight = weight;
            this.timestamp = timestamp;
            this.date = date;
        }
    }

    /**
     * Επιστρέφει τις γεωγραφικές συντεταγμένες (LatLng) του κέντρου μιας περιοχής
     * με βάση το όνομά της. Αναζητά στην αποθηκευμένη αντιστοίχιση ονόματος-συντεταγμένων.
     *
     * @param area Το όνομα της περιοχής για την οποία αναζητούμε το κέντρο.
     * @return Ένα LatLng αντικείμενο που αντιπροσωπεύει το κέντρο της περιοχής ή null αν δεν βρεθεί.
     */
    private LatLng getAreaCenter(String area) {
        for(LatLng cordinates : areaNames.keySet()){
            if(areaNames.get(cordinates).equals(area)){
                return cordinates;
            }
        }
        return null;
    }

    /**
     * Εκτελεί γεωκωδικοποίηση μιας διεύθυνσης και στη συνέχεια καλεί τη μέθοδο
     * για την εμφάνιση της ασφαλούς διαδρομής προς την τοποθεσία που βρέθηκε.
     * Λαμβάνει επίσης τα δεδομένα heatmap για πιθανή χρήση στην εμφάνιση της διαδρομής.
     *
     * @param address
     */
    private void geocodeAndShowRoute(String address) {
        // Λήψη των δεδομένων heatmap μέσω ενός callback
        getHeatmapData(new HeatmapDataCallback() {
            @Override
            public void onHeatmapDataReceived(List<WeightedLatLng> heatmapData2, List<LatLng> incidentLatLngsList) {
                // Έλεγχος αν τα δεδομένα heatmap και οι συντεταγμένες περιστατικών δεν είναι null
                if (heatmapData2 != null && incidentLatLngsList!=null) {
                    heatmapData=heatmapData2;
                    incidentLatLngs=incidentLatLngsList;

                    // Λήψη του LocationManager για την πρόσβαση στις υπηρεσίες τοποθεσίας
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location currentLocation = null;

                    try {
                        // Προσπάθεια λήψης της τελευταίας γνωστής τοποθεσίας από τον GPS provider
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        // Αν δεν υπάρχει τοποθεσία από GPS, προσπαθούμε από τον Network provider
                        if (currentLocation == null) {
                            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                        // Αν δεν μπόρεσε να ληφθεί καμία τοποθεσία
                        if (currentLocation == null) {
                            Toast.makeText(MapsActivity.this, "Δεν μπόρεσε να ληφθεί η τρέχουσα τοποθεσία", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Δημιουργία Geocoder για την μετατροπή διεύθυνσης σε γεωγραφικές συντεταγμένες
                        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                        // Λήψη έως 5 πιθανών διευθύνσεων για το ερώτημα
                        List<Address> addresses = geocoder.getFromLocationName(address, 5);

                        // Έλεγχος αν βρέθηκαν διευθύνσεις
                        if (addresses != null && !addresses.isEmpty()) {
                            Address closestAddress = null;
                            float closestDistance = Float.MAX_VALUE;

                            // &Lambda;&#x3AE;&psi;&eta; &tau;&eta;&sigmaf; &chi;&#x3CE;&rho;&alpha;&sigmaf; &tau;&eta;&sigmaf; &tau;&rho;&#x3AD;&chi;&omicron;&upsilon;&sigma;&alpha;&sigmaf; &tau;&omicron;&pi;&omicron;&theta;&epsilon;&sigma;&#x3AF;&alpha;&sigmaf; &gamma;&iota;&alpha; &phi;&iota;&lambda;&tau;&rho;&#x3AC;&rho;&iota;&sigma;&mu;&alpha;
                            List<Address> currentAddresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                            String currentCountry = null;
                            if (currentAddresses != null && !currentAddresses.isEmpty()) {
                                currentCountry = currentAddresses.get(0).getCountryName();
                            }

                            int k = 0;
                            // Επανάληψη στις βρεθείσες διευθύνσεις
                            for (Address location : addresses) {
                                Location addressLocation = new Location("");
                                addressLocation.setLatitude(location.getLatitude());
                                addressLocation.setLongitude(location.getLongitude());

                                // Υπολογισμός της απόστασης μεταξύ της τρέχουσας τοποθεσίας και της βρεθείσας διεύθυνσης
                                float distance = currentLocation.distanceTo(addressLocation);

                                // Έλεγχος αν η βρεθείσα διεύθυνση είναι στην ίδια χώρα με την τρέχουσα τοποθεσία
                                if (currentCountry != null && currentCountry.equals(location.getCountryName())) {
                                    // Εύρεση της πλησιέστερης διεύθυνσης
                                    if (distance < closestDistance) {
                                        closestDistance = distance;
                                        closestAddress = location;
                                    }
                                }
                                k++;
                            }

                            // Αν βρέθηκε η πλησιέστερη διεύθυνση στην ίδια χώρα
                            if (closestAddress != null) {
                                LatLng destination = new LatLng(closestAddress.getLatitude(), closestAddress.getLongitude());
                                showSafeRoute(destination); // Εμφάνιση της ασφαλούς διαδρομής προς τον προορισμό
                            } else {
                                Toast.makeText(MapsActivity.this, "Δεν βρέθηκε κοντινή τοποθεσία στην περιοχή σας", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "Δεν βρέθηκε τοποθεσία", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Toast.makeText(MapsActivity.this, "Σφάλμα αναζήτησης", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        Toast.makeText(MapsActivity.this, "Δεν υπάρχουν δικαιώματα τοποθεσίας", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "KENH LISTA" + heatmapData.size(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Ανακτά τα δεδομένα για το heatmap (γεωγραφικές συντεταγμένες και βάρος)
     * από τη Firebase Realtime Database και καλεί ένα callback όταν τα δεδομένα ληφθούν.
     *
     * @param callback Ένα interface HeatmapDataCallback που θα ειδοποιηθεί όταν τα δεδομένα είναι διαθέσιμα.
     * @return null (τα δεδομένα επιστρέφονται μέσω του callback).
     */
    private List<WeightedLatLng> getHeatmapData(HeatmapDataCallback callback) {
        // Δημιουργία αναφοράς στον κόμβο "Αναφορές" της Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Αναφορές");

        // Ανάγνωση των δεδομένων από τον κόμβο μία φορά
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WeightedLatLng> heatmapDataList = new ArrayList<>();
                List<LatLng> incidentLatLngsList = new ArrayList<>();
                // Επανάληψη σε κάθε παιδί του κόμβου "Αναφορές"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Ανάκτηση των τιμών γεωγραφικού πλάτους, μήκους και βάρους ως Strings και Integer
                        String latitudeStr = snapshot.child("latitude").getValue(String.class);
                        String longitudeStr = snapshot.child("longitude").getValue(String.class);
                        Integer weight = snapshot.child("weight").getValue(Integer.class);

                        // Μετατροπή των Strings σε Doubles
                        Double latitude = Double.parseDouble(latitudeStr);
                        Double longitude = Double.parseDouble(longitudeStr);
                        double intensity = Double.valueOf(weight);

                        // Δημιουργία ενός LatLng αντικειμένου
                        LatLng latLng = new LatLng(latitude, longitude);
                        // Δημιουργία ενός WeightedLatLng αντικειμένου για το heatmap
                        WeightedLatLng weightedLatLng = new WeightedLatLng(latLng, intensity);
                        heatmapDataList.add(weightedLatLng);
                        incidentLatLngsList.add(latLng);
                    } catch (NumberFormatException e) {
                        Log.e("MapsActivity", "Σφάλμα μετατροπής String σε Double", e);
                    } catch (Exception e) {
                        Log.e("MapsActivity", "Σφάλμα κατά την ανάγνωση δεδομένων heatmap", e);
                    }
                }

                // Κλήση του callback με τα δεδομένα
                callback.onHeatmapDataReceived(heatmapDataList,incidentLatLngsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MapsActivity", "Σφάλμα βάσης δεδομένων", databaseError.toException());
                callback.onHeatmapDataReceived(null, null); // Κλήση του callback με null σε περίπτωση σφάλματος
            }
        });
        return null; // Δεν χρειάζεται να επιστρέψετε κάτι εδώ
    }



    /**
     *
     * Αν ο προορισμός είναι κοντά σε περιστατικό, εμφανίζεται ένα προειδοποιητικό μήνυμα.
     *
     * @param destination Οι γεωγραφικές συντεταγμένες του προορισμού.
     */
    private void showSafeRoute(LatLng destination) {


        // Έλεγχος αν ο marker του χρήστη έχει τοποθετηθεί στο χάρτη
        if (userMarker != null) {
            // Λήψη των συντεταγμένων της τρέχουσας θέσης του χρήστη
            LatLng origin = userMarker.getPosition();
            // Έλεγχος αν ο προορισμός είναι κοντά σε κάποιο καταγεγραμμένο περιστατικό
            if (isDestinationNearIncident(destination)) {
                // Δημιουργία ενός AlertDialog για να προειδοποιήσει τον χρήστη
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Ο προορισμός σας βρίσκεται κοντά σε περιστατικό. Θέλετε να συνεχίσετε;");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Αν ο χρήστης επιλέξει να συνεχίσει, ζητείται η ασφαλής διαδρομή
                        getSafeDirections(origin, destination);
                    }
                });
                builder.setNegativeButton("Ακύρωση", null);
                builder.show();
            } else {
                // Αν ο προορισμός δεν είναι κοντά σε περιστατικό, ζητείται απευθείας η ασφαλής διαδρομή
                getSafeDirections(origin, destination);
            }
        } else {
            // Εμφάνιση μηνύματος στον χρήστη αν η τρέχουσα τοποθεσία δεν έχει φορτωθεί ακόμα
            runOnUiThread(() -> {
                Toast.makeText(this, "Περιμένετε να φορτώσει η τοποθεσία σας", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Ελέγχει αν ένας δεδομένος προορισμός (γεωγραφικές συντεταγμένες) βρίσκεται σε κοντινή απόσταση
     * από κάποιο καταγεγραμμένο περιστατικό.
     *
     * @param destination Οι γεωγραφικές συντεταγμένες του προορισμού.
     * @return true αν ο προορισμός είναι κοντά σε περιστατικό, false διαφορετικά.
     */
    private boolean isDestinationNearIncident(LatLng destination) {
        // Έλεγχος αν η λίστα των δεδομένων heatmap είναι null ή άδεια
        if (heatmapData == null || heatmapData.isEmpty()) {
            return false; // Αν δεν υπάρχουν περιστατικά, ο προορισμός δεν μπορεί να είναι κοντά
        }

        // Επανάληψη σε όλες τις καταγεγραμμένες τοποθεσίες περιστατικών
        for (LatLng incidentLatLng : incidentLatLngs) {
            // Υπολογισμός της απόστασης μεταξύ του προορισμού και της τοποθεσίας του περιστατικού
            float[] results = new float[1];
            Location.distanceBetween(destination.latitude, destination.longitude,
                    incidentLatLng.latitude, incidentLatLng.longitude,
                    results);

            // Έλεγχος αν η απόσταση είναι μικρότερη ή ίση με 50 μέτρα
            if (results[0] <= 50) {
                return true; // Ο προορισμός είναι κοντά σε περιστατικό
            }
        }
        return false; // Ο προορισμός δεν είναι κοντά σε κανένα περιστατικό
    }

    /**
     * Ζητά οδηγίες ασφαλούς διαδρομής μεταξύ δύο σημείων (origin και destination)
     * χρησιμοποιώντας το Google Directions API και λαμβάνοντας υπόψη τα περιστατικά.
     *
     * @param origin      Οι γεωγραφικές συντεταγμένες της αφετηρίας.
     * @param destination Οι γεωγραφικές συντεταγμένες του προορισμού.
     */


    private void getSafeDirections(LatLng origin, LatLng destination) {
        Log.d("SafeDirections", "Η μέθοδος getSafeDirections κλήθηκε");
        Log.d("SafeDirections", "Αρχή: " + origin.latitude + ", " + origin.longitude);
        Log.d("SafeDirections", "Προορισμός: " + destination.latitude + ", " + destination.longitude);

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey()
                .build();

        DirectionsApiRequest req = DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .avoid(DirectionsApi.RouteRestriction.FERRIES)
                .alternatives(true);

        new Thread(() -> {
            try {
                Log.d("SafeDirections", "Αιτούμαστε οδηγίες...");
                DirectionsResult result = req.await();
                Log.d("SafeDirections", "Λήφθηκε το αποτέλεσμα των οδηγιών");

                List<LatLng> safeRoutePoints = findSafeRoute(result, origin);

                runOnUiThread(() -> {
                    if (safeRoutePoints != null && !safeRoutePoints.isEmpty()) {
                        Log.d("SafeDirections", "Βρέθηκε ασφαλής διαδρομή, σχεδιάζουμε την polyline.");

                        if (routePolyline != null) {
                            routePolyline.remove();
                        }

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(safeRoutePoints)
                                .width(5)
                                .color(0xFF0000FF);

                        routePolyline = mMap.addPolyline(polylineOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));

                        // Προσθήκη νέου marker για τον προορισμό
                        if (destinationMarker != null) {
                            destinationMarker.remove();
                        }

                        destinationMarker = mMap.addMarker(new MarkerOptions()
                                .position(destination)
                                .title("Προορισμός"));

                        // ✅ Εμφάνιση διαλόγου για άνοιγμα Google Maps
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("Άνοιγμα στο Google Maps")
                                .setMessage("Θέλετε να ανοίξετε την ασφαλή διαδρομή στο Google Maps;")
                                .setPositiveButton("Ναι", (dialog, which) -> openRouteInGoogleMaps(safeRoutePoints))
                                .setNegativeButton("Όχι", null)
                                .show();

                    } else {
                        Log.d("SafeDirections", "Δεν βρέθηκε ασφαλής διαδρομή.");
                        Toast.makeText(MapsActivity.this, "Δεν βρέθηκε ασφαλής διαδρομή", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SafeDirections", "Σφάλμα στη δρομολόγηση", e);
                runOnUiThread(() ->
                        Toast.makeText(MapsActivity.this, "Σφάλμα δρομολόγησης", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



    // Τροποποιημένη μέθοδος findSafeRoute()
    private List<LatLng> findSafeRoute(DirectionsResult result, LatLng originPoint) {
        StringBuilder routeAnalysis = new StringBuilder();

        Log.d("SafeRoute", "Η μέθοδος findSafeRoute κλήθηκε");
        routeAnalysis.append("🔍 Αναλύοντας διαδρομές...\n\n");

        if (result == null || result.routes.length == 0) {
            String message = "❌ Δεν βρέθηκαν διαδρομές στα αποτελέσματα";
            Log.d("SafeRoute", message);
            routeAnalysis.append(message).append("\n");
            lastRouteAnalysis = routeAnalysis.toString(); // Αποθήκευση του μηνύματος για το AlertDialog
            return null;
        }

        List<LatLng> fallbackRoutePoints = null;

        for (int r = 0; r < result.routes.length; r++) {
            DirectionsRoute route = result.routes[r];
            String summary = "🛣️ Διαδρομή " + (r + 1) + ": " + route.summary;
            Log.d("SafeRoute", summary);
            routeAnalysis.append(summary).append("\n");

            List<LatLng> routePoints = new ArrayList<>();
            boolean routeIsSafe = true;
            double totalDistance = 0; // ✅ Για υπολογισμό απόστασης

            for (DirectionsLeg leg : route.legs) {
                for (DirectionsStep step : leg.steps) {
                    List<com.google.maps.model.LatLng> decodedPath = step.polyline.decodePath();

                    for (int i = 0; i < decodedPath.size() - 1; i++) {
                        LatLng start = new LatLng(decodedPath.get(i).lat, decodedPath.get(i).lng);
                        LatLng end = new LatLng(decodedPath.get(i + 1).lat, decodedPath.get(i + 1).lng);

                        // ✅ Πρόσθεση απόστασης στο άθροισμα
                        totalDistance += distanceBetween(start, end);

                        if (distanceBetween(originPoint, start) <= 20 && distanceBetween(originPoint, end) <= 20) {
                            routePoints.add(start);
                            continue;
                        }

                        if (isPointNearIncident(start, end, originPoint)) {
                            String dangerMsg = "❌ Επικίνδυνο τμήμα: " + start + " -> " + end;
                            Log.d("SafeRoute", dangerMsg);
                            routeAnalysis.append(dangerMsg).append("\n");
                            routeIsSafe = false;
                            break;
                        }

                        routePoints.add(start);
                    }

                    if (!decodedPath.isEmpty()) {
                        com.google.maps.model.LatLng last = decodedPath.get(decodedPath.size() - 1);
                        routePoints.add(new LatLng(last.lat, last.lng));
                    }

                    if (!routeIsSafe) break;
                }

                if (!routeIsSafe) break;
            }

            if (fallbackRoutePoints == null) {
                fallbackRoutePoints = routePoints;
            }

            if (routeIsSafe) {
                String successMsg = "✅ Επιλέχθηκε ασφαλής διαδρομή " + (r + 1);
                Log.d("SafeRoute", successMsg);
                routeAnalysis.append(successMsg).append("\n");

                // ✅ Εμφάνιση συνολικής απόστασης
                routeAnalysis.append(String.format(Locale.getDefault(), "📏 Συνολική απόσταση: %.0f μέτρα\n", totalDistance));

                lastRouteAnalysis = routeAnalysis.toString(); // Αποθήκευση της ανάλυσης
                return routePoints;
            }

            routeAnalysis.append("⚠️ Η διαδρομή " + (r + 1) + " απορρίφθηκε λόγω επικινδυνότητας\n\n");
        }

        // Αν δεν βρέθηκε καμία ασφαλής διαδρομή, το fallback μήνυμα εμφανίζεται
        String fallbackMsg = "⚠️ Καμία ασφαλής διαδρομή. Επιλέγεται η λιγότερο επικίνδυνη.";
        Log.d("SafeRoute", fallbackMsg);
        routeAnalysis.append(fallbackMsg).append("\n");

        lastRouteAnalysis = routeAnalysis.toString(); // Αποθήκευση της ανάλυσης
        return fallbackRoutePoints;
    }



    private float distanceBetween(LatLng p1, LatLng p2) {
        float[] results = new float[1];
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0]; // Απόσταση σε μέτρα
    }







    //Άνοιγμα Προσαρμοσμένης Διαδρομής στο Google Maps
    private void openRouteInGoogleMaps(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.size() < 2) {
            Toast.makeText(this, "Η διαδρομή δεν είναι έγκυρη", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = routePoints.get(0);
        LatLng destination = routePoints.get(routePoints.size() - 1);

        // Πάρε τα ενδιάμεσα σημεία (χωρίς το πρώτο και τελευταίο)
        List<String> waypointStrings = new ArrayList<>();
        for (int i = 1; i < routePoints.size() - 1; i += Math.max(1, routePoints.size() / 23)) {
            LatLng point = routePoints.get(i);
            waypointStrings.add(point.latitude + "," + point.longitude);
        }


        String waypoints = TextUtils.join("|", waypointStrings);

        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1"
                + "&origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&travelmode=walking" // ή driving/cycling
                + "&waypoints=" + waypoints);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }





    /**
     *
     * Συνοπτικά, τα βασικά μέρη του Α* είναι:
     Η χρήση ευριστικής συνάρτησης.
     Η χρήση των τιμών g και f.
     Η ουρά προτεραιότητας για την διαχείρηση των σημείων.
     */

    /**
     * Εύρεση του συντομότερου ασφαλούς μονοπατιού μεταξύ δύο σημείων, χρησιμοποιώντας μια παραλλαγή του αλγορίθμου A*.
     * Ο αλγόριθμος προσπαθεί να αποφύγει τμήματα της διαδρομής που είναι κοντά σε καταγεγραμμένα περιστατικά.
     *
     * @param start     Το σημείο εκκίνησης της διαδρομής.
     * @param end       Το σημείο προορισμού της διαδρομής.
     * @param allPoints Μια λίστα με όλα τα ενδιάμεσα σημεία που μπορούν να αποτελέσουν μέρος της διαδρομής.
     * @return Μια λίστα με γεωγραφικές συντεταγμένες που αποτελούν το συντομότερο ασφαλές μονοπάτι,
     * ή null αν δεν βρεθεί μονοπάτι.
     */
  /*  private List<LatLng> findShortestSafePath(LatLng start, LatLng end, List<LatLng> allPoints) {
        Log.d("ShortestPath", "Η μέθοδος findShortestSafePath κλήθηκε με σημείο εκκίνησης: " + start + " και προορισμό: " + end);

        // openSet: Μια ουρά προτεραιότητας που περιέχει κόμβους προς εξέταση, ταξινομημένους με βάση το fScore.
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // closedSet: Ένα σύνολο που περιέχει τους κόμβους που έχουν ήδη εξεταστεί.
        Set<Node> closedSet = new HashSet<>();
        // cameFrom: Ένας χάρτης που αποθηκεύει τον προηγούμενο κόμβο στο συντομότερο γνωστό μονοπάτι προς κάθε κόμβο.
        Map<LatLng, LatLng> cameFrom = new HashMap<>();
        // gScore: Ένας χάρτης που αποθηκεύει το κόστος της διαδρομής από την εκκίνηση μέχρι κάθε κόμβο.
        Map<LatLng, Double> gScore = new HashMap<>();

        // Αρχικοποίηση του gScore για το σημείο εκκίνησης
        gScore.put(start, 0.0);
        // Προσθήκη του αρχικού κόμβου στην ουρά προτεραιότητας
        openSet.add(new Node(start, 0.0));
        Log.d("ShortestPath", "Ο κόμβος εκκίνησης προστέθηκε στην openSet: " + start);

        // Επανάληψη όσο υπάρχουν κόμβοι στην ουρά προς εξέταση
        while (!openSet.isEmpty()) {
            // Επιλογή του κόμβου με το χαμηλότερο fScore από την ουρά
            Node current = openSet.poll();
            Log.d("ShortestPath", "Τρέχων κόμβος: " + current.latLng);

            // Αν ο τρέχων κόμβος είναι ο προορισμός, ανακατασκευάζουμε το μονοπάτι
            if (current.latLng.equals(end)) {
                Log.d("ShortestPath", "Ο προορισμός επιτεύχθηκε: " + current.latLng);
                return reconstructPath(cameFrom, end);
            }

            // Προσθήκη του τρέχοντος κόμβου στο σύνολο των εξετασμένων
            closedSet.add(current);
            Log.d("ShortestPath", "Προστέθηκε στο closedSet: " + current.latLng);

            // Εξέταση των γειτονικών κόμβων του τρέχοντος
            for (LatLng neighbor : getNeighbors(current.latLng, allPoints)) {
                Log.d("ShortestPath", "Ελέγχουμε τον γείτονα: " + neighbor);

                // Αν ο γείτονας έχει ήδη εξεταστεί, τον παραλείπουμε
                if (closedSet.contains(new Node(neighbor, 0))) {
                    Log.d("ShortestPath", "Ο γείτονας " + neighbor + " είναι στο closedSet, παραλείπεται.");
                    continue;
                }

                // Έλεγχος αν η διαδρομή μεταξύ του τρέχοντος κόμβου και του γείτονα περνάει κοντά από περιστατικό
                if (isPointNearIncident(current.latLng, neighbor)) {
                    Log.d("ShortestPath", "Ο γείτονας " + neighbor + " είναι κοντά σε περιστατικό, παραλείπεται.");
                    continue;
                }

                // Υπολογισμός του προσωρινού gScore για τον γείτονα
                double tentativeGScore = gScore.get(current.latLng) + calculateDistance(current.latLng, neighbor);
                Log.d("ShortestPath", "Προσωρινό gScore για τον γείτονα " + neighbor + ": " + tentativeGScore);

                // Αν δεν έχουμε βρει ακόμα μονοπάτι προς τον γείτονα ή αν το νέο μονοπάτι είναι καλύτερο
                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    Log.d("ShortestPath", "Βρέθηκε καλύτερο μονοπάτι προς τον γείτονα " + neighbor);

                    // Ενημέρωση του προηγούμενου κόμβου για τον γείτονα
                    cameFrom.put(neighbor, current.latLng);
                    // Ενημέρωση του gScore για τον γείτονα
                    gScore.put(neighbor, tentativeGScore);
                    // Προσθήκη του γείτονα στην ουρά προτεραιότητας με το νέο fScore
                    openSet.add(new Node(neighbor, tentativeGScore + calculateDistance(neighbor, end)));
                    Log.d("ShortestPath", "Προστέθηκε ο γείτονας στην openSet: " + neighbor);
                }
            }
        }

        // Αν η ουρά είναι άδεια και δεν έχουμε φτάσει στον προορισμό, δεν υπάρχει μονοπάτι
        Log.d("ShortestPath", "Δεν βρέθηκε μονοπάτι, επιστρέφουμε null.");
        return null;
    }
*/
    /**
     * Προσπαθεί να ανακατευθύνει μια υπάρχουσα διαδρομή γύρω από τμήματα που είναι κοντά σε περιστατικά.
     *
     * @param route Η αρχική διαδρομή ως λίστα γεωγραφικών συντεταγμένων.
     * @return Μια νέα λίστα γεωγραφικών συντεταγμένων που αντιπροσωπεύει την ανακατευθυνόμενη (ασφαλέστερη) διαδρομή.
     */
   /* private List<LatLng> rerouteAroundIncident(List<LatLng> route) {
        Log.d("Reroute", "Η μέθοδος rerouteAroundIncident κλήθηκε με μέγεθος διαδρομής: " + route.size());

        List<LatLng> safeRoute = new ArrayList<>();
        LatLng previousPoint = route.get(0);

        // Επανάληψη σε όλα τα τμήματα της διαδρομής
        for (int i = 1; i < route.size(); i++) {
            LatLng currentPoint = route.get(i);

            // Έλεγχος αν το τμήμα της διαδρομής μεταξύ του προηγούμενου και του τρέχοντος σημείου είναι επικίνδυνο
            if (isPointNearIncident(previousPoint, currentPoint)) {
                Log.d("Reroute", "Αναγνωρίστηκε κίνδυνος μεταξύ " + previousPoint + " και " + currentPoint);

                LatLng detourStart = previousPoint;  // Το σημείο πριν το επικίνδυνο τμήμα
                LatLng detourEnd = currentPoint;  // Το σημείο μετά το επικίνδυνο τμήμα

                // Εύρεση μιας εναλλακτικής διαδρομής γύρω από το περιστατικό
                List<LatLng> detourRoute = findDetourRoute(detourStart, detourEnd);

                // Αν βρεθεί εναλλακτική διαδρομή
                if (detourRoute != null && !detourRoute.isEmpty()) {
                    Log.d("Reroute", "Βρέθηκε εναλλακτική διαδρομή μεταξύ " + detourStart + " και " + detourEnd);
                    safeRoute.addAll(detourRoute);
                } else {
                    Log.d("Reroute", "Δεν βρέθηκε εναλλακτική διαδρομή, προσθήκη του αρχικού τμήματος.");
                    // Αν δεν βρεθεί εναλλακτική, επιστρέφουμε στο αρχικό τμήμα
                    safeRoute.add(previousPoint);
                    safeRoute.add(currentPoint);
                }
            } else {
                // Αν το τμήμα δεν είναι επικίνδυνο, το προσθέτουμε στην ασφαλή διαδρομή
                Log.d("Reroute", "Προσθήκη ασφαλούς τμήματος: " + previousPoint + " έως " + currentPoint);
                safeRoute.add(previousPoint);
            }

            previousPoint = currentPoint; // Ενημέρωση του προηγούμενου σημείου
        }

        Log.d("Reroute", "Η επαναδρομολόγηση ολοκληρώθηκε. Μέγεθος ασφαλούς διαδρομής: " + safeRoute.size());
        return safeRoute;
    }
*/

    /**
     * Βρίσκει μια εναλλακτική διαδρομή μεταξύ δύο σημείων, προσπαθώντας να αποφύγει περιοχές κοντά σε περιστατικά
     * χρησιμοποιώντας το Google Directions API.
     *
     * @param detourStart Το σημείο εκκίνησης της εναλλακτικής διαδρομής.
     * @param detourEnd   Το σημείο προορισμού της εναλλακτικής διαδρομής.
     * @return Μια λίστα με γεωγραφικές συντεταγμένες που αποτελούν την εναλλακτική διαδρομή,
     * ή null αν δεν βρεθεί διαδρομή.
     */
    private List<LatLng> findDetourRoute(LatLng detourStart, LatLng detourEnd) {
        Log.d("Detour", "Η μέθοδος findDetourRoute κλήθηκε με αρχή: " + detourStart + " και τέλος: " + detourEnd);

        // Δημιουργία του Context για το Directions API
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey()
                .build();

        // Δημιουργία αιτήματος για directions, ζητώντας εναλλακτικές διαδρομές και αποφεύγοντας τα φέρι-μπότ
        DirectionsApiRequest req = DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(detourStart.latitude, detourStart.longitude))
                .destination(new com.google.maps.model.LatLng(detourEnd.latitude, detourEnd.longitude))
                .alternatives(true)
                .avoid(DirectionsApi.RouteRestriction.FERRIES);

        try {
            // Λήψη των αποτελεσμάτων της διαδρομής
            DirectionsResult result = req.await();
            Log.d("Detour", "Λήψη αποτελεσμάτων διαδρομής, αριθμός διαδρομών: " + result.routes.length);

            // Αν δεν υπάρχουν αποτελέσματα, επιστρέφουμε null
            if (result.routes.length == 0) {
                Log.d("Detour", "Δεν βρέθηκαν διαδρομές, επιστροφή null");
                return null;
            }

            // Επιλέγουμε μια εναλλακτική διαδρομή (π.χ. τη δεύτερη, index 1). Μπορεί να χρειαστεί πιο σύνθετη λογική επιλογής.
            DirectionsRoute route = result.routes[1];
            Log.d("Detour", "Επιλέχθηκε η εναλλακτική διαδρομή με " + route.legs.length + " σκέλη");

            List<LatLng> detourRoute = new ArrayList<>();

            // Συλλογή των σημείων της επιλεγμένης εναλλακτικής διαδρομής
            for (DirectionsLeg leg : route.legs) {
                Log.d("Detour", "Επεξεργασία σκέλους με " + leg.steps.length + " βήματα");
                for (DirectionsStep step : leg.steps) {
                    EncodedPolyline points = step.polyline;
                    List<com.google.maps.model.LatLng> decodedPath = points.decodePath();
                    for (com.google.maps.model.LatLng point : decodedPath) {
                        detourRoute.add(new LatLng(point.lat, point.lng));
                    }
                }
            }

            Log.d("Detour", "Η εναλλακτική διαδρομή δημιουργήθηκε με " + detourRoute.size() + " σημεία");
            return detourRoute; // Επιστρέφουμε τη νέα εναλλακτική διαδρομή
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Detour", "Σφάλμα κατά τη δημιουργία της εναλλακτικής διαδρομής: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ανακατασκευάζει το μονοπάτι από τον προορισμό προς την εκκίνηση, χρησιμοποιώντας τον χάρτη cameFrom.
     *
     * @param cameFrom Ο χάρτης που αποθηκεύει τον προηγούμενο κόμβο στο συντομότερο γνωστό μονοπάτι.
     * @param current   Ο τρέχων κόμβος (ξεκινώντας από τον προορισμό).
     * @return Μια λίστα με γεωγραφικές συντεταγμένες που αποτελούν το ανακατασκευασμένο μονοπάτι (από την εκκίνηση προς τον προορισμό).
     */
    private List<LatLng> reconstructPath(Map<LatLng, LatLng> cameFrom, LatLng current) {
        List<LatLng> path = new ArrayList<>();

        Log.d("PathReconstruction", "Ξεκινά η ανασυγκρότηση του μονοπατιού από το σημείο: " + current);

        // Προσθήκη του τρέχοντος σημείου στο μονοπάτι
        path.add(current);

        // Ανιχνεύουμε προς τα πίσω μέχρι να φτάσουμε στο σημείο εκκίνησης
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current); // Προσθέτουμε τον προηγούμενο κόμβο στην αρχή της λίστας

            Log.d("PathReconstruction", "Προστέθηκε το σημείο: " + current);
        }

        Log.d("PathReconstruction", "Η ανασυγκρότηση του μονοπατιού ολοκληρώθηκε. Μονοπάτι: " + path);

        return path;
    }

    /**
     * Επιστρέφει μια λίστα με τους γειτονικούς κόμβους ενός δεδομένου κόμβου σε ένα γράφημα σημείων.
     * Οι γείτονες επιλέγονται εντός μιας καθορισμένης εμβέλειας και δεν είναι κοντά σε περιστατικά.
     *
     * @param latLng    Ο κόμβος για τον οποίο αναζητούμε γείτονες.
     * @param allPoints Η λίστα με όλα τα σημεία του γραφήματος.
     * @return Μια λίστα με τους γειτονικούς κόμβους που είναι ασφαλείς.
     */
    private List<LatLng> getNeighbors(LatLng latLng, List<LatLng> allPoints, LatLng originPoint) {
        List<LatLng> neighbors = new ArrayList<>();
        int index = allPoints.indexOf(latLng);
        int range = 5; // Ορίζουμε μια εμβέλεια για την αναζήτηση γειτόνων

        for (int i = Math.max(0, index - range); i <= Math.min(allPoints.size() - 1, index + range); i++) {
            if (i != index) {
                LatLng neighbor = allPoints.get(i);
                // Χρησιμοποιούμε την έκδοση που αγνοεί περιστατικά κοντά στο originPoint
                if (!isPointNearIncident(latLng, neighbor, originPoint)) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }


    /**
     * Υπολογίζει την απόσταση μεταξύ δύο γεωγραφικών συντεταγμένων και αυξάνει το "κόστος"
     * αν η διαδρομή μεταξύ τους περνάει κοντά από περιστατικό.
     *
     * @param from Το σημείο εκκίνησης του τμήματος.
     * @param to   Το σημείο προορισμού του τμήματος.
     * @return Η απόσταση μεταξύ των δύο σημείων, πιθανώς αυξημένη αν είναι κοντά σε περιστατικό.
     */
 /*  private double calculateDistance(LatLng from, LatLng to) {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);
        double distance = results[0];
        // Διπλασιασμός της απόστασης αν η διαδρομή περνάει κοντά από περιστατικό για να την αποθαρρύνουμε
        if (isPointNearIncident(from, to)) {
            distance *= 2; //Τιμωρεί  τις διαδρομές από επικίνδυνες περιοχές
        }
        return distance;
    }*/

    /**
     * Εσωτερική στατική κλάση για την αναπαράσταση ενός κόμβου στον αλγόριθμο A*.
     * Υλοποιεί το Comparable interface για χρήση στην ουρά προτεραιότητας.
     */
    private static class Node implements Comparable<Node> {
        LatLng latLng;
        double fScore; // fScore = gScore + hScore (μια εκτίμηση του συνολικού κόστους)

        Node(LatLng latLng, double fScore) {
            this.latLng = latLng;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return latLng.equals(node.latLng);
        }

        @Override
        public int hashCode() {
            return latLng.hashCode();
        }
    }

    /**
     *
     *
     * η μέθοδος isPointNearIncident θα ελέγχει αν η
     * διαδρομή μεταξύ δύο σημείων περνάει κοντά από
     * ένα περιστατικό, και θα επιστρέφει true αν συμβαίνει αυτό
     * */
    private boolean isPointNearIncident(LatLng start, LatLng end, LatLng originPoint) {
        if (incidentLatLngs == null || incidentLatLngs.isEmpty()) return false;

        for (LatLng incident : incidentLatLngs) {
            // Αν το περιστατικό είναι κοντά στο origin, αγνόησέ το
            if (distanceBetween(originPoint, incident) <= 30) continue;

            // Αν είναι κοντά στο start ή end, θεωρείται επικίνδυνο
            if (distanceBetween(start, incident) <= 50 || distanceBetween(end, incident) <= 50) {
                return true;
            }
        }
        return false;
    }


    /**
     * Υπολογίζει την ελάχιστη απόσταση ενός σημείου 'p' από το ευθύγραμμο τμήμα που ορίζεται από τα σημεία 'v' και 'w'.
     *
     * @param p Το σημείο για το οποίο υπολογίζεται η απόσταση.
     * @param v Το πρώτο σημείο του ευθύγραμμου τμήματος.
     * @param w Το δεύτερο σημείο του ευθύγραμμου τμήματος.
     * @return Η ελάχιστη απόσταση του σημείου 'p' από το ευθύγραμμο τμήμα 'vw' σε μέτρα.
     */
    private float distanceToSegment(LatLng p, LatLng v, LatLng w) {
        double lat1 = v.latitude;
        double lon1 = v.longitude;
        double lat2 = w.latitude;
        double lon2 = w.longitude;
        double lat3 = p.latitude;
        double lon3 = p.longitude;

        double px = lat2 - lat1;
        double py = lon2 - lon1;
        double norm = px * px + py * py; // Υπολογισμός του τετραγώνου του μήκους του τμήματος

        // Υπολογισμός της παραμετρικής θέσης 'u' του πλησιέστερου σημείου στο τμήμα 'vw' από το 'p'
        double u = ((lat3 - lat1) * px + (lon3 - lon1) * py) / norm;

        // Περιορισμός της τιμής του 'u' στο διάστημα [0, 1] για να βρίσκεται εντός του τμήματος
        u = Math.max(0, Math.min(1, u));

        // Υπολογισμός των συντεταγμένων του πλησιέστερου σημείου στο τμήμα
        double latClosest = lat1 + u * px;
        double lonClosest = lon1 + u * py;

        // Υπολογισμός της απόστασης μεταξύ του σημείου 'p' και του πλησιέστερου σημείου στο τμήμα
        float[] results = new float[1];
        Location.distanceBetween(lat3, lon3, latClosest, lonClosest, results);
        return results[0]; // Επιστροφή της απόστασης σε μέτρα
    }

    /**
     * Αναζητά μια εναλλακτική διαδρομή μεταξύ δύο σημείων χρησιμοποιώντας το Google Directions API.
     *
     * @param start Το σημείο εκκίνησης της διαδρομής.
     * @param end   Το σημείο προορισμού της διαδρομής.
     * @return Μια λίστα με γεωγραφικές συντεταγμένες που αποτελούν την εναλλακτική διαδρομή,
     * ή null αν δεν βρεθεί διαδρομή.
     */
    private List<LatLng> findDetour(LatLng start, LatLng end) {

        // Δημιουργία του Context για το Directions API
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey()
                .build();

        // Δημιουργία αιτήματος για directions, ζητώντας εναλλακτικές διαδρομές και αποφεύγοντας τα φέρι-μπότ
        DirectionsApiRequest req = DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(start.latitude, start.longitude))
                .destination(new com.google.maps.model.LatLng(end.latitude, end.longitude))
                .avoid(DirectionsApi.RouteRestriction.FERRIES) // Μπορείς να προσθέσεις για να αποφύγεις περιοχές ή δρόμους
                .alternatives(true); // Αναζητεί εναλλακτικές διαδρομές

        try {
            // Εκτέλεση του αιτήματος και λήψη του αποτελέσματος
            DirectionsResult result = req.await();

            // Έλεγχος αν υπάρχουν αποτελέσματα και αν υπάρχουν διαδρομές
            if (result != null && result.routes.length > 0) {
                List<LatLng> detourPoints = new ArrayList<>();
                // Επιλογή της πρώτης εναλλακτικής διαδρομής
                DirectionsRoute route = result.routes[0];
                // Επανάληψη στα σκέλη της διαδρομής
                for (DirectionsLeg leg : route.legs) {
                    // Επανάληψη στα βήματα κάθε σκέλους
                    for (DirectionsStep step : leg.steps) {
                        // Ανάκτηση της πολυγώνου γραμμής που αντιπροσωπεύει το βήμα
                        EncodedPolyline points = step.polyline;
                        // Αποκωδικοποίηση των σημείων της πολυγώνου γραμμής
                        List<com.google.maps.model.LatLng> decodedPath = points.decodePath();
                        // Μετατροπή των σημείων σε LatLng και προσθήκη στη λίστα
                        for (com.google.maps.model.LatLng point : decodedPath) {
                            LatLng latLng = new LatLng(point.lat, point.lng);
                            detourPoints.add(latLng);
                        }
                    }
                }
                return detourPoints; // Επιστροφή της λίστας των σημείων της εναλλακτικής διαδρομής
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Επιστροφή null αν υπάρξει σφάλμα ή δεν βρεθεί διαδρομή
    }

    // BroadcastReceiver για την λήψη ειδοποιήσεων για νέα περιστατικά
    private BroadcastReceiver incidentReportedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Λήψη της τοποθεσίας του χρήστη και κλήση checkIncidentsProximity
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location -> {
                    if (location != null) {
                        checkIncidentsProximity(location);
                    }
                });
            }
        }
    };

    /**
     * Ελέγχει αν ο χρήστης βρίσκεται κοντά σε περιστατικά-αναφορές.
     *
     * @param userLocation Η τρέχουσα τοποθεσία του χρήστη.
     */
    private void checkIncidentsProximity(Location userLocation) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");

        incidentsEventListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nearbyIncidents.clear();
                String latestKey = null;
                float minDistance = Float.MAX_VALUE;
                String closestKey = null;
                long latestTimestamp = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date now = new Date();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String latitudeStr = snapshot.child("latitude").getValue(String.class);
                    String longitudeStr = snapshot.child("longitude").getValue(String.class);
                    String timestampStr = snapshot.child("timestamp").getValue(String.class);

                    if (latitudeStr != null && longitudeStr != null && timestampStr != null) {
                        try {
                            double latitude = Double.parseDouble(latitudeStr);
                            double longitude = Double.parseDouble(longitudeStr);
                            Location incidentLocation = new Location("");
                            incidentLocation.setLatitude(latitude);
                            incidentLocation.setLongitude(longitude);
                            float distance = userLocation.distanceTo(incidentLocation);

                            if (distance <= 1000) {
                                nearbyIncidents.put(snapshot.getKey(), snapshot);

                                Date incidentDate = sdf.parse(timestampStr);
                                if (incidentDate != null) {
                                    long incidentTimeInMillis = incidentDate.getTime();
                                    if (incidentTimeInMillis > latestTimestamp) {
                                        latestTimestamp = incidentTimeInMillis;
                                        latestKey = snapshot.getKey();
                                    }
                                }

                                if (distance < minDistance) {
                                    minDistance = distance;
                                    closestKey = snapshot.getKey();
                                }
                            }
                        } catch (NumberFormatException | ParseException e) {
                            Log.e("MapsActivity", "Error parsing incident data: " + e.getMessage());
                        }
                    }
                }

                String closestAndLatestKey = null;
                float currentMinDistance = Float.MAX_VALUE;
                long currentLatestTimestamp = 0;

                for (Map.Entry<String, DataSnapshot> entry : nearbyIncidents.entrySet()) {
                    DataSnapshot incidentSnapshot = entry.getValue();
                    String latitudeStr = incidentSnapshot.child("latitude").getValue(String.class);
                    String longitudeStr = incidentSnapshot.child("longitude").getValue(String.class);
                    String timestampStr = incidentSnapshot.child("timestamp").getValue(String.class);

                    try {
                        double latitude = Double.parseDouble(latitudeStr);
                        double longitude = Double.parseDouble(longitudeStr);
                        Location incidentLocation = new Location("");
                        incidentLocation.setLatitude(latitude);
                        incidentLocation.setLongitude(longitude);
                        float distance = userLocation.distanceTo(incidentLocation);
                        Date incidentDate = sdf.parse(timestampStr);
                        long incidentTimeInMillis = incidentDate.getTime();

                        // Ελέγχουμε αν είναι η πιο πρόσφατη ΚΑΙ η πιο κοντινή (μπορείτε να προσαρμόσετε την προτεραιότητα)
                        if (incidentTimeInMillis >= latestTimestamp - 5000 && distance <= minDistance + 10) { // Ένα μικρό χρονικό και χωρικό περιθώριο
                            if (distance < currentMinDistance || incidentTimeInMillis > currentLatestTimestamp) {
                                currentMinDistance = distance;
                                currentLatestTimestamp = incidentTimeInMillis;
                                closestAndLatestKey = entry.getKey();
                            }
                        }
                    } catch (NumberFormatException | ParseException e) {
                        Log.e("MapsActivity", "Error parsing incident data for comparison: " + e.getMessage());
                    }
                }

                if (closestAndLatestKey != null && !closestAndLatestKey.equals(currentClosestAndLatestIncidentKey)) {
                    currentClosestAndLatestIncidentKey = closestAndLatestKey;
                    DataSnapshot closestLatestSnapshot = nearbyIncidents.get(closestAndLatestKey);
                    if (closestLatestSnapshot != null) {
                        String description = closestLatestSnapshot.child("description").getValue(String.class);
                        String latitudeStr = closestLatestSnapshot.child("latitude").getValue(String.class);
                        String longitudeStr = closestLatestSnapshot.child("longitude").getValue(String.class);
                        try {
                            double latitude = Double.parseDouble(latitudeStr);
                            double longitude = Double.parseDouble(longitudeStr);
                            Location incidentLocation = new Location("");
                            incidentLocation.setLatitude(latitude);
                            incidentLocation.setLongitude(longitude);
                            float distance = userLocation.distanceTo(incidentLocation);
                            showProximityNotification(distance, description);
                        } catch (NumberFormatException e) {
                            Log.e("MapsActivity", "Error parsing coordinates for notification: " + e.getMessage());
                        }
                    }
                } else if (nearbyIncidents.isEmpty() && isNotificationShown) {
                    cancelNotification();
                    isNotificationShown = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error reading incidents: " + databaseError.getMessage());
            }
        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        // Ξεκινάει την παρακολούθηση των κοντινών περιστατικών όταν η δραστηριότητα επανέρχεται
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, this::checkIncidentsProximity); // Κλήση με το επιθυμητό όνομα
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Σταματάει τον listener όταν η δραστηριότητα τίθεται σε παύση για εξοικονόμηση πόρων
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");
        if (incidentsEventListener != null) {
            ref.removeEventListener(incidentsEventListener);
        }
        stopLocationUpdates();
    }



    /**
     * Εμφανίζει μια ειδοποίηση περιστατικού αν ο χρήστης βρίσκεται σε απόσταση 1000 μέτρων.
     *
     * @param distance          Η απόσταση του χρήστη από το πλησιέστερο περιστατικό.
     * @param incidentDescription Η περιγραφή του περιστατικού.
     */
    private void showProximityNotification(float distance, String incidentDescription) {
        String channelId = "incident_proximity_channel";

        Log.d("MapsActivity", "showProximityNotification() called");

        // Δημιουργία του builder για την ειδοποίηση
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.dangerr) // Εικονίδιο της ειδοποίησης
                .setContentTitle("Προειδοποίηση!") // Τίτλος της ειδοποίησης
                .setStyle(new NotificationCompat.BigTextStyle() // Στυλ για εμφάνιση μεγάλου κειμένου
                        .bigText("Περιστατικό: " + incidentDescription + "\nΑπόσταση: " + (int) distance + " μέτρα.")) // Κείμενο ειδοποίησης
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Προτεραιότητα ειδοποίησης
                .setOngoing(true); // Η ειδοποίηση δεν μπορεί να απορριφθεί από τον χρήστη

        // Αναπαραγωγή ήχου μόνο την πρώτη φορά που εμφανίζεται η ειδοποίηση
        if (!isSoundPlayed) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            isSoundPlayed = true;
        }

        // Λήψη του NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Δημιουργία καναλιού ειδοποιήσεων για Android Oreo και νεότερες εκδόσεις
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Incident Proximity", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Εμφάνιση της ειδοποίησης
        notificationManager.notify(0, builder.build()); // Χρήση της ίδιας NOTIFICATION_ID για να αντικαθιστά προηγούμενες ειδοποιήσεις
    }

    /**
     * Ενημερώνει την ειδοποίηση με την τρέχουσα απόσταση από το περιστατικό.
     *
     * @param distance          Η τρέχουσα απόσταση του χρήστη από το πλησιέστερο περιστατικό.
     * @param incidentDescription Η περιγραφή του περιστατικού.
     */
    private void updateNotification(float distance, String incidentDescription) {
        // Ενημέρωση της ειδοποίησης μόνο αν η απόσταση έχει αλλάξει σημαντικά
        if (previousDistance == -1 || Math.abs(distance - previousDistance) >= 3) { // Αλλαγή σε >= 3 μέτρα διαφορά
            String channelId = "incident_proximity_channel";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.dangerr)
                    .setContentTitle("Προειδοποίηση!")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Περιστατικό: " + incidentDescription + "\nΑπόσταση: " + (int) distance + " μέτρα."))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());

            previousDistance = distance; // Αποθήκευση της τρέχουσας απόστασης για την επόμενη σύγκριση
        }
    }

    /**
     * Ακυρώνει την ειδοποίηση που εμφανίζεται.
     */
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0); // Ακύρωση της ειδοποίησης με ID 0
        isSoundPlayed = false; // Επαναφορά της σημαίας ήχου για την επόμενη ειδοποίηση
    }

    /**
     * Ξεκινά τις περιοδικές ενημερώσεις της ειδοποίησης περιστατικού.
     * Δεν την χρησιμοποιουμε
     *
     * @param userLocation      Η αρχική τοποθεσία του χρήστη κατά την εμφάνιση της ειδοποίησης.
     * @param incidentDescription Η περιγραφή του κοντινού περιστατικού.
     */
    private void startNotificationUpdates(Location userLocation, String incidentDescription) {
        // Έλεγχος για άδεια πρόσβασης στην ακριβή τοποθεσία
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Λήψη της τελευταίας γνωστής τοποθεσίας ασύγχρονα
            fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, location -> {
                if (location != null) {
                    previousDistance = userLocation.distanceTo(location); // Αρχικοποίηση της προηγούμενης απόστασης
                    // Δημιουργία ενός Handler για την εκτέλεση κώδικα μετά από καθυστέρηση
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (userLocation != null) {
                                // Λήψη της πιο πρόσφατης τοποθεσίας για την ενημέρωση της απόστασης
                                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, currentLocation -> {
                                        if (currentLocation != null) {
                                            // Υπολογισμός της νέας απόστασης από την αρχική τοποθεσία του περιστατικού
                                            float distance = userLocation.distanceTo(currentLocation);
                                            // Ενημέρωση της ειδοποίησης με τη νέα απόσταση και την περιγραφή
                                            updateNotification(distance, incidentDescription);
                                        }
                                    });
                                }
                            }
                            // Αν η ειδοποίηση είναι ακόμα ενεργή, προγραμματίζουμε την επόμενη ενημέρωση
                            if (isNotificationActive) {
                                handler.postDelayed(this, 15000); // Επανάληψη κάθε 15 δευτερόλεπτα
                            }
                        }
                    }, 15000); // Αρχική καθυστέρηση 15 δευτερόλεπτα
                }
            });
        }
    }

    /**
     * Καλείται όταν ο χάρτης Google είναι έτοιμος για χρήση.
     * Αυτή η μέθοδος αρχικοποιεί τον χάρτη, ελέγχει για δικαιώματα τοποθεσίας,
     * μετακινεί την κάμερα στην τοποθεσία του χρήστη (ή σε προεπιλεγμένη),
     * ενεργοποιεί τις λειτουργίες ζουμ και τα σκρολ, ξεκινά τις ενημερώσεις τοποθεσίας
     * και φορτώνει τα δεδομένα περιστατικών από το Firebase για την εμφάνιση του heatmap.
     * Επίσης, ρυθμίζει έναν listener για τις SOS αναφορές σε πραγματικό χρόνο.
     *
     * @param googleMap Το αντικείμενο GoogleMap.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Έλεγχος για άδεια πρόσβασης στην ακριβή τοποθεσία
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Λήψη της τελευταίας γνωστής τοποθεσίας ασύγχρονα
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15)); // Μετακίνηση της κάμερας στην τοποθεσία του χρήστη με επίπεδο ζουμ 15
                } else {
                    // Η τοποθεσία δεν είναι διαθέσιμη, μετακίνηση σε προεπιλεγμένη τοποθεσία (Αθήνα)
                    LatLng athens = new LatLng(37.983810, 23.727539);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(athens, 12)); // Μετακίνηση της κάμερας στην Αθήνα με επίπεδο ζουμ 12
                }
            });
        } else {
            // Δεν υπάρχουν δικαιώματα τοποθεσίας, μετακίνηση σε προεπιλεγμένη τοποθεσία (Αθήνα)
            LatLng athens = new LatLng(37.983810, 23.727539);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(athens, 12)); // Μετακίνηση της κάμερας στην Αθήνα με επίπεδο ζουμ 12
        }

        // Ενεργοποίηση των χειρονομιών ζουμ
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        // Ενεργοποίηση των χειριστηρίων ζουμ στην οθόνη
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Έναρξη των περιοδικών ενημερώσεων τοποθεσίας του χρήστη
        startLocationUpdates();

        // Δημιουργία αναφοράς στον κόμβο "Αναφορές" της Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Αναφορές");
        // Ανάγνωση των δεδομένων περιστατικών μία φορά για την αρχική δημιουργία του heatmap
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<WeightedLatLng> heatmapData = new ArrayList<>();
                List<LatLng> incidentLatLngs = new ArrayList<>();

                // Επανάληψη σε όλα τα παιδικά στοιχεία της αναφοράς "Αναφορές"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Ανάκτηση των συντεταγμένων γεωγραφικού πλάτους και μήκους και του βάρους (επικινδυνότητας)
                        String latitude = snapshot.child("latitude").getValue(String.class);
                        String longitude = snapshot.child("longitude").getValue(String.class);
                        Integer weight = snapshot.child("weight").getValue(Integer.class);

                        Double latitudeD = Double.parseDouble(latitude);
                        Double longitudeD = Double.parseDouble(longitude);

                        // Προσθήκη των δεδομένων για το heatmap
                        heatmapData.add(new WeightedLatLng(new LatLng(latitudeD, longitudeD), weight));
                        incidentLatLngs.add(new LatLng(latitudeD, longitudeD));
                    } catch (Exception e) {
                        Log.e("Firebase", "Σφάλμα μετατροπής δεδομένων: " + e.getMessage());
                    }
                }

                // Δημιουργία του HeatmapTileProvider με τα δεδομένα και προσαρμογή της ακτίνας
                provider = new HeatmapTileProvider.Builder()
                        .weightedData(heatmapData)
                        .radius(50) // Ορίζει την ακτίνα επιρροής κάθε σημείου
                        .build();

                // Προσθήκη (ή αντικατάσταση) του heatmap layer στον χάρτη
                if (tileOverlay != null) {
                    tileOverlay.remove(); // Αφαίρεση του προηγούμενου layer αν υπάρχει
                }
                tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Σφάλμα κατά την ανάκτηση δεδομένων", databaseError.toException());
            }
        });

        // Δημιουργία αναφοράς στον κόμβο "SOS Αναφορές" της Firebase και προσθήκη ChildEventListener
        // για να λαμβάνουμε ειδοποιήσεις σε πραγματικό χρόνο για νέες SOS αναφορές
        DatabaseReference sosRefListener = FirebaseDatabase.getInstance().getReference("SOS Αναφορές");
        sosRefListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    // Ανάκτηση των συντεταγμένων, του timestamp, του κλειδιού SOS και του User ID από το snapshot
                    Double latitude = Double.parseDouble(snapshot.child("latitude").getValue(String.class));
                    Double longitude = Double.parseDouble(snapshot.child("longitude").getValue(String.class));
                    String timestampStr = snapshot.child("timestamp").getValue(String.class);
                    String sosKey = snapshot.getKey();
                    String userId = snapshot.child("userId").getValue(String.class); //
                    // Ανάκτηση του User ID

                    // Λήψη του User ID του τρέχοντος χρήστη
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUserId = (currentUser != null) ? currentUser.getUid() : null;

                    // Έλεγχος αν το timestamp είναι έγκυρο
                    if (timestampStr != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date sosDate = sdf.parse(timestampStr);
                        Date now = new Date();
                        long differenceInMillis = now.getTime() - sosDate.getTime();
                        long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis);

                        if (differenceInMinutes <= 10) {
                            LatLng sosLocation = new LatLng(latitude, longitude);

                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, currentLocation -> {
                                    if (currentLocation != null) {
                                        float[] distance = new float[1];
                                        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), sosLocation.latitude, sosLocation.longitude, distance);

                                        // Δημιουργία και εμφάνιση marker για την SOS αναφορά
                                        Drawable sosIconDrawable = ContextCompat.getDrawable(MapsActivity.this, R.drawable.sos_icon);
                                        int iconSize = 100;
                                        Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bitmap);
                                        sosIconDrawable.setBounds(0, 0, iconSize, iconSize);
                                        sosIconDrawable.draw(canvas);
                                        Paint paint = new Paint();
                                        paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                                        canvas.drawBitmap(bitmap, 0, 0, paint);

                                        MarkerOptions sosMarkerOptions = new MarkerOptions()
                                                .position(sosLocation)
                                                .title("SOS")
                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                        Marker sosMarker = mMap.addMarker(sosMarkerOptions);
                                        activeSosMarkers.put(sosKey, sosMarker); // Αποθήκευση του marker για πιθανή αφαίρεση αργότερα

                                        // Μήνυμα για τον χρήστη που έκανε το SOS
                                        if (currentUserId != null && currentUserId.equals(userId)) {
                                            Log.d("SOS", "Showing 'Μην ανησυχείτε!' dialog");
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                            builder.setTitle("Μην ανησυχείτε!");
                                            builder.setMessage("Το μήνυμα βοήθειας στάλθηκε επιτυχώς.");
                                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                                            builder.create().show();
                                        }
                                        // Μήνυμα για τους άλλους χρήστες που βρίσκονται κοντά
                                        else if (distance[0] <= 100) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                            builder.setTitle("SOS!");
                                            builder.setMessage("Ένας άνθρωπος χρειάζεται βοήθεια σε " + (int) distance[0] + " μέτρα!");
                                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                                            builder.create().show();
                                        }

                                        // Ξεκινάμε timer για αφαίρεση του marker μετά από 10 λεπτά
                                        Handler sosHandler = new Handler();
                                        Runnable removeMarkerRunnable = () -> {
                                            if (activeSosMarkers.containsKey(sosKey)) {
                                                Marker markerToRemove = activeSosMarkers.get(sosKey);
                                                if (markerToRemove != null) {
                                                    markerToRemove.remove();
                                                }
                                                activeSosMarkers.remove(sosKey);
                                            }
                                        };
                                        sosHandler.postDelayed(removeMarkerRunnable, 600000); // 10 λεπτά σε milliseconds
                                    }
                                });
                            }
                        }
                    }
                } catch (NumberFormatException | ParseException e) {
                    Log.e("SOS Listener", "Σφάλμα στην ανάγνωση/μετατροπή δεδομένων SOS.", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Μέθοδος που καλείται όταν αλλάζει ένα παιδικό στοιχείο κάτω από τον κόμβο "SOS Αναφορές".
                // Μπορεί να χρησιμοποιηθεί για να ενημερώσετε την θέση ενός υπάρχοντος SOS marker αν αλλάξουν οι συντεταγμένες του.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Μέθοδος που καλείται όταν αφαιρείται ένα παιδικό στοιχείο από τον κόμβο "SOS Αναφορές".
                String removedKey = snapshot.getKey(); // Λαμβάνουμε το κλειδί του αφαιρεθέντος SOS
                // Ελέγχουμε αν υπάρχει marker για αυτό το SOS και το αφαιρούμε από τον χάρτη και την αποθηκευμένη λίστα
                if (activeSosMarkers.containsKey(removedKey)) {
                    Marker markerToRemove = activeSosMarkers.get(removedKey);
                    if (markerToRemove != null) {
                        markerToRemove.remove(); // Αφαίρεση του marker από τον χάρτη
                    }
                    activeSosMarkers.remove(removedKey); // Αφαίρεση του marker από τη λίστα activeSosMarkers
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Μέθοδος που καλείται όταν η σειρά των παιδικών στοιχείων αλλάζει.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Μέθοδος που καλείται αν υπάρξει σφάλμα κατά την ανάγνωση των δεδομένων SOS από το Firebase.
                Log.e("SOS Listener", "Failed to read value.", error.toException());
            }
        });

    }

    /**
     * Δημιουργεί ένα αίτημα τοποθεσίας (LocationRequest) με συγκεκριμένες παραμέτρους
     * για την συχνότητα και την ακρίβεια των ενημερώσεων τοποθεσίας.
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Ζητά ενημέρωση τοποθεσίας κάθε 10 δευτερόλεπτα (σε milliseconds)
        locationRequest.setFastestInterval(5000); // Ορίζει το μικρότερο διάστημα μεταξύ ενημερώσεων (αν είναι διαθέσιμη πιο γρήγορα)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Ορίζει την επιθυμητή ακρίβεια της τοποθεσίας (υψηλή ακρίβεια μέσω GPS)
    }

    /**
     * Δημιουργεί ένα callback (LocationCallback) που θα λαμβάνει τα αποτελέσματα των αιτημάτων τοποθεσίας.
     * Η μέθοδος onLocationResult καλείται όταν λαμβάνονται νέες τοποθεσίες.
     */



    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Έλεγχος αν το locationResult είναι null
                if (locationResult == null) {
                    return;
                }
                // Επανάληψη σε όλες τις τοποθεσίες που περιέχονται στο locationResult
                for (Location location : locationResult.getLocations()) {
                    // Έλεγχος αν η τοποθεσία δεν είναι null
                    if (location != null) {
                        // Δημιουργία ενός LatLng αντικειμένου από τις συντεταγμένες της τοποθεσίας
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        // Αν δεν υπάρχει ήδη marker για τον χρήστη, δημιουργούμε έναν
                        if (userMarker == null) {
                            userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Η τοποθεσία σας"));
                        } else {
                            // Αν υπάρχει ήδη marker, απλά ενημερώνουμε τη θέση του
                            userMarker.setPosition(userLatLng);
                        }
                        // Κλήση της μεθόδου για έλεγχο κοντινών περιστατικών με βάση την τρέχουσα τοποθεσία
                        checkIncidentsProximity(location);
                        // Εμφάνιση της διεύθυνσης του χρήστη με ένα Toast όταν μπαίνει στην εφαρμογή (μόνο μία φορά)
                        if (!isLocationMessageShown) {
                            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                            try {
                                // Ανάκτηση μιας λίστας διευθύνσεων με βάση τις συντεταγμένες
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                // Έλεγχος αν η λίστα διευθύνσεων δεν είναι null και δεν είναι άδεια
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String addressLine = address.getAddressLine(0); // Λαμβάνουμε την πρώτη γραμμή της διεύθυνσης
                                    String city = address.getLocality(); // Λαμβάνουμε την πόλη
                                    String postalCode = address.getPostalCode(); // Λαμβάνουμε τον ταχυδρομικό κώδικα
                                    String message = "Η τοποθεσία σας είναι: " + addressLine + " " + city + " " + postalCode;
                                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MapsActivity.this, "Δεν βρέθηκε διεύθυνση", Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MapsActivity.this, "Σφάλμα γεωκωδικοποίησης", Toast.LENGTH_LONG).show();
                            }
                            isLocationMessageShown = true; // Σημαία για να μην εμφανιστεί ξανά το μήνυμα
                        }
                    }
                }
            }
        };
    }

    /**
     * Ενημερώνει τη θέση του χρήστη στο χάρτη μετακινώντας τον marker και την κάμερα.
     * @param location Η τρέχουσα τοποθεσία του χρήστη.
     */
    private void updateUserLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("MapsActivity", "Updating user location: " + userLocation.latitude + ", " + userLocation.longitude);
        // Αν δεν υπάρχει marker για τον χρήστη, δημιουργούμε έναν
        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Η τοποθεσία μου"));
            Log.d("MapsActivity", "Marker added");
        } else {
            // Αν υπάρχει ήδη marker, ενημερώνουμε τη θέση του
            userMarker.setPosition(userLocation);
            Log.d("MapsActivity", "Marker updated");
        }
        // Κινούμε την κάμερα του χάρτη στην τρέχουσα τοποθεσία με επίπεδο ζουμ 15
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        // Μετατροπή των συντεταγμένων σε διεύθυνση για εμφάνιση
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Ανάκτηση λίστας διευθύνσεων με βάση τις συντεταγμένες (μόνο 1 αποτέλεσμα)
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            // Έλεγχος αν βρέθηκε διεύθυνση
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0); // Λαμβάνουμε την πλήρη διεύθυνση
                // Εμφάνιση της διεύθυνσης με ένα Toast μόνο την πρώτη φορά
                if (!isLocationMessageShown) {
                    Toast.makeText(this, "Η τοποθεσία σας: " + addressText, Toast.LENGTH_LONG).show();
                    isLocationMessageShown = true; // Σημαία για να μην εμφανιστεί ξανά το μήνυμα
                }
            } else {
                Toast.makeText(this, "Δεν βρέθηκε διεύθυνση", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Σφάλμα γεωκωδικοποίησης", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ξεκινά τις ενημερώσεις τοποθεσίας χρησιμοποιώντας το FusedLocationProviderClient.
     * Πριν ξεκινήσει τις ενημερώσεις, ελέγχει αν έχουν δοθεί οι απαραίτητες άδειες τοποθεσίας.
     * Αν δεν έχουν δοθεί, ζητάει τις άδειες από τον χρήστη.
     */
    private void startLocationUpdates() {
        // Έλεγχος αν δεν έχουν δοθεί οι άδειες για ακριβή και κατά προσέγγιση τοποθεσία
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Αν δεν έχουν δοθεί άδειες, ζητάμε την άδεια για ακριβή τοποθεσία
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return; // Σταματάμε την εκτέλεση της μεθόδου μέχρι να απαντήσει ο χρήστης στο αίτημα αδειών
        }
        // Αν έχουν δοθεί οι άδειες, ξεκινάμε τις ενημερώσεις τοποθεσίας
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Καλείται όταν η δραστηριότητα τίθεται σε παύση. Σταματάμε τις ενημερώσεις τοποθεσίας
     * για εξοικονόμηση μπαταρίας και πόρων.
     */


    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Καλείται μετά την απάντηση του χρήστη στο αίτημα για άδειες.
     * Ελέγχει αν η άδεια για την ακριβή τοποθεσία δόθηκε και αν ναι, ξεκινά τις ενημερώσεις τοποθεσίας.
     * @param requestCode Ο κωδικός αιτήματος που χρησιμοποιήθηκε για το αίτημα αδειών.
     * @param permissions Ο πίνακας των αδειών που ζητήθηκαν.
     * @param grantResults Ο πίνακας των αποτελεσμάτων για κάθε άδεια.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Έλεγχος αν ο κωδικός αιτήματος είναι ο κωδικός που χρησιμοποιήσαμε για την άδεια τοποθεσίας
        if (requestCode == 100) {
            // Έλεγχος αν υπάρχουν αποτελέσματα και αν η πρώτη άδεια (ACCESS_FINE_LOCATION) δόθηκε
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Αν η άδεια δόθηκε, ξεκινάμε τις ενημερώσεις τοποθεσίας
                startLocationUpdates();
            } else {
                // Αν η άδεια δεν δόθηκε, εμφανίζουμε ένα μήνυμα στον χρήστη
                Toast.makeText(this, "Η άδεια τοποθεσίας δεν δόθηκε", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Εσωτερική κλάση για την αναπαράσταση πληροφοριών μιας περιοχής,
     * όπως το κέντρο της και το επίπεδο ασφάλειας.
     *
     */
    class AreaInfo {
        LatLng center;
        String safetyLevel;

        public AreaInfo(LatLng center, String safetyLevel) {
            this.center = center;
            this.safetyLevel = safetyLevel;
        }
    }

    /**
     * Κλάση για την αναπαράσταση ενός περιστατικού.
     * Σημείωση: Θα διαγραφεί αργότερα καθώς τα δεδομένα λαμβάνονται από το Firebase.
     */
    private class Incident {
        public double latitude;
        public double longitude;

        public Incident(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
