package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Η κλάση `ReportIncidentActivity` επιτρέπει στον χρήστη να αναφέρει ένα νέο περιστατικό.
 * Παρέχει μια φόρμα όπου ο χρήστης μπορεί να εισάγει μια περιγραφή του περιστατικού.
 * Η δραστηριότητα λαμβάνει επίσης την τρέχουσα τοποθεσία του χρήστη (γεωγραφικό πλάτος και μήκος).
 * Χρησιμοποιεί ένα λεξικό με λέξεις-κλειδιά και βάρη για να υπολογίσει ένα επίπεδο επικινδυνότητας
 * για το αναφερόμενο περιστατικό βάσει της περιγραφής.
 * Τα δεδομένα του περιστατικού (τοποθεσία, περιγραφή, βάρος επικινδυνότητας, χρονική σήμανση και email χρήστη)
 * αποθηκεύονται στην Firebase Realtime Database. Επιπλέον, εφαρμόζεται ένας μηχανισμός
 * ανίχνευσης διπλών αναφορών χρησιμοποιώντας έναν απλό αλγόριθμο Naive Bayes που συνδυάζει
 * ομοιότητα κειμένου, εγγύτητα τοποθεσίας και εγγύτητα χρόνου για να αποφευχθεί η δημιουργία
 * πολλαπλών πανομοιότυπων αναφορών.
 */

public class ReportIncidentActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView textViewLatitude;
    private TextView textViewLongitude;
    private EditText editTextDescription;
    private Button buttonSubmit;
    private TextView textViewWeight; // Για να εμφανίζει την επικινδυνότητα στο xml


    // Λεξικό με λέξεις έτσι ώστε να ελέγχουμε αν υπάρχουν στην αναφορά του χρήστη για να
    // ορίσουμε την επικινδυνότητα του περιστατικού
    private Map<String, Integer> keywordWeights;

    private String email;

    Random random ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        email = getIntent().getStringExtra("email");

        random=new Random();
        // Για το λεξικό
        keywordWeights = new HashMap<>();

// Χαμηλή επικινδυνότητα (1)
        keywordWeights.put("έκλεψ", 1);
        keywordWeights.put("ατυχημα", 1);
        keywordWeights.put("ακανονιστ", 1);
        keywordWeights.put("αναστατωσ", 1);
        keywordWeights.put("αναφορα", 1);
        keywordWeights.put("ανησυχι", 1);
        keywordWeights.put("αταξ", 1);
        keywordWeights.put("ατυχημ", 1);
        keywordWeights.put("γκαραζ", 1);
        keywordWeights.put("διαδηλ", 1);
        keywordWeights.put("διαμαρτυρ", 1);
        keywordWeights.put("διαπληκτ", 1);
        keywordWeights.put("διαρρο", 1);
        keywordWeights.put("διασπασ", 1);
        keywordWeights.put("ενοχλ", 1);
        keywordWeights.put("εντασ", 1);
        keywordWeights.put("εξαφανισ", 1);
        keywordWeights.put("θορυβ", 1);
        keywordWeights.put("καθυστερ", 1);
        keywordWeights.put("καυγ", 1);
        keywordWeights.put("κλεφ", 1);
        keywordWeights.put("κλοπ", 1);
        keywordWeights.put("μαλω", 1);
        keywordWeights.put("παραβια", 1);
        keywordWeights.put("παρανομ", 1);
        keywordWeights.put("παραπον", 1);
        keywordWeights.put("παρεξ", 1);
        keywordWeights.put("πλακωθ", 1);
        keywordWeights.put("πορει", 1);
        keywordWeights.put("προβλ", 1);
        keywordWeights.put("προστριβ", 1);
        keywordWeights.put("σταθμευ", 1);
        keywordWeights.put("συνωστισ", 1);
        keywordWeights.put("τροχ", 1);
        keywordWeights.put("τσακωμ", 1);
        keywordWeights.put("φασαρ", 1);
        keywordWeights.put("φων", 1);


// Μέτρια επικινδυνότητα (2)
        keywordWeights.put("χτυπ",2);
        keywordWeights.put("αναρχικ", 2);
        keywordWeights.put("αναταραχ", 2);
        keywordWeights.put("απεγκλωβ", 2);
        keywordWeights.put("απεργ", 2);
        keywordWeights.put("βανδαλ", 2);
        keywordWeights.put("βιασ", 2);
        keywordWeights.put("διεφυγ", 2);
        keywordWeights.put("δολιοφθορ", 2);
        keywordWeights.put("εκαψ", 2);
        keywordWeights.put("εκρηξ", 2);
        keywordWeights.put("εμπρησ", 2);
        keywordWeights.put("επιχειρ", 2);
        keywordWeights.put("εσπασ", 2);
        keywordWeights.put("ζημι", 2);
        keywordWeights.put("καπν", 2);
        keywordWeights.put("καταδιωξ", 2);
        keywordWeights.put("καταληψ", 2);
        keywordWeights.put("καταστολ", 2);
        keywordWeights.put("καταστροφ", 2);
        keywordWeights.put("κινδυν", 2);
        keywordWeights.put("κροτ", 2);
        keywordWeights.put("ληστ", 2);
        keywordWeights.put("μπουκαρ", 2);
        keywordWeights.put("πειρατ", 2);
        keywordWeights.put("πεταξ", 2);
        keywordWeights.put("πετροβολ", 2);
        keywordWeights.put("προσαγωγ", 2);
        keywordWeights.put("πυρκαγι", 2);
        keywordWeights.put("ρατσιστικ", 2);
        keywordWeights.put("σοβαρ", 2);
        keywordWeights.put("συγκρουσ", 2);
        keywordWeights.put("συρραξ", 2);
        keywordWeights.put("τρακαρ", 2);
        keywordWeights.put("τραυματ", 2);
        keywordWeights.put("τραυματιζ", 2);
        keywordWeights.put("τραυματιστ", 2);
        keywordWeights.put("φλεγ", 2);
        keywordWeights.put("φωτι", 2);

// Υψηλή επικινδυνότητα (3)
        keywordWeights.put("αιματοχυσ", 3);
        keywordWeights.put("ακροτητ", 3);
        keywordWeights.put("απειλ", 3);
        keywordWeights.put("αρπαγ", 3);
        keywordWeights.put("αρπαξ", 3);
        keywordWeights.put("βανδαλισ", 3);
        keywordWeights.put("βι", 3);
        keywordWeights.put("δολοφον", 3);
        keywordWeights.put("εγκλεισμ", 3);
        keywordWeights.put("εκβιασ", 3);
        keywordWeights.put("εκρηξ", 3);
        keywordWeights.put("εξαπελυσε", 3);
        keywordWeights.put("επιθεσ", 3);
        keywordWeights.put("επιθετικ", 3);
        keywordWeights.put("θρασυδειλ", 3);
        keywordWeights.put("κακοποι", 3);
        keywordWeights.put("καταδιωκ", 3);
        keywordWeights.put("καυγ", 3);
        keywordWeights.put("μαχ", 3);
        keywordWeights.put("μαχαιρ", 3);
        keywordWeights.put("ξυλο", 3);
        keywordWeights.put("ομηρ", 3);
        keywordWeights.put("οπλ", 3);
        keywordWeights.put("προπηλακ", 3);
        keywordWeights.put("πυγμ", 3);
        keywordWeights.put("συγκρουστ", 3);
        keywordWeights.put("συμπλοκ", 3);
        keywordWeights.put("τρομοκρατ", 3);
        keywordWeights.put("πλημμύρα", 3);
        keywordWeights.put("αρπα", 3);
        keywordWeights.put("διερ", 3);
        keywordWeights.put("διαρ", 3);
        keywordWeights.put("επιτε", 3);

// Πολύ υψηλή επικινδυνότητα (5)
        keywordWeights.put("απήγ",5);
        keywordWeights.put("αιματηρ", 5);
        keywordWeights.put("ανατιναχ", 5);
        keywordWeights.put("αναφλεξ", 5);
        keywordWeights.put("απαγωγ", 5);
        keywordWeights.put("αποκεφαλισμ", 5);
        keywordWeights.put("βομβ", 5);
        keywordWeights.put("δολοφον", 5);
        keywordWeights.put("εγκλημ", 5);
        keywordWeights.put("εκρηκτικ", 5);
        keywordWeights.put("εκτελεσ", 5);
        keywordWeights.put("εμπρηστ", 5);
        keywordWeights.put("εξαφανισ", 5);
        keywordWeights.put("εξεγερσ", 5);
        keywordWeights.put("επικινδυν", 5);
        keywordWeights.put("κατεστρεψ", 5);
        keywordWeights.put("μαζικ", 5);
        keywordWeights.put("μακελει", 5);
        keywordWeights.put("οπαδικ", 5);
        keywordWeights.put("πυροβολ", 5);
        keywordWeights.put("πυροδοτ", 5);
        keywordWeights.put("συνωμοτ", 5);
        keywordWeights.put("σφαγ", 5);
        keywordWeights.put("τρομοκρατ", 5);

        // Αρχικοποίηση του FusedLocationProviderClient για την λήψη της τοποθεσίας
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Σύνδεση μεταβλητών με τα TextViews και το EditText από το layout
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        editTextDescription = findViewById(R.id.editTextDescription); // Από εδώ παίρνουμε το input του χρήστη
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewWeight = findViewById(R.id.textViewWeight); // Αρχικοποίηση του TextView για την εμφάνιση της επικινδυνότητας

        // Λήψη της τρέχουσας τοποθεσίας
        getLocation();

        // HashMap που προορίζεται για την αποθήκευση της αναφοράς (δεν χρησιμοποιείται τελικά για άμεση αποθήκευση)
        HashMap<Integer,String> add_input_to_database =new HashMap<Integer,String>();


        // Ορισμός OnClickListener για το κουμπί υποβολής
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextDescription.getText().toString();
                // Έλεγχος αν η περιγραφή είναι κενή
                if(input.isEmpty()){
                    editTextDescription.setError("Η αναφορά δεν μπορεί να είναι κενή");
                }else{
                    // Προσθήκη της περιγραφής στο HashMap (δεν χρησιμοποιείται για άμεση αποθήκευση στη βάση)
                    add_input_to_database.put(random.nextInt(),input);
                }
                // Κλήση της μεθόδου για την υποβολή του περιστατικού στην Firebase
                submitIncident();
            }
        });

        // Λήψη αναφοράς στο κουμπί "πίσω" και ορισμός OnClickListener για την επιστροφή στην προηγούμενη δραστηριότητα
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Κλείνει την τρέχουσα δραστηριότητα και επιστρέφει στην προηγούμενη
            }
        });

        // Text watcer ώστε να εμγανίζει την επικινδυνότητα του περιστατικού σε πραγματικό χρόνο
        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Δεν χρειάζεται κάποια ενέργεια πριν την αλλαγή του κειμένου
            }


            // Καλείται κάθε φορά που αλλάζει το κείμενο έτσι ώστε να κάνει ξανά υπολογισμό της επικινδυνότητας
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String description = s.toString().toLowerCase(); // Μετατροπή του κειμένου σε πεζά για ευκολότερη αντιστοίχιση
                int weight = calculateWeight(description); // Υπολογισμός της επικινδυνότητας βάσει της περιγραφής
                textViewWeight.setText("Επικινδυνότητα = " + weight); // Ενημέρωση του TextView με την υπολογισμένη επικινδυνότητα
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Δεν χρειάζεται κάποια ενέργεια μετά την αλλαγή του κειμένου
            }
        });
    }

    // Υπολογισμός της επικινδυνότητας του περιστατικού βάσει των λέξεων-κλειδιών στην περιγραφή
    // Κάνουμε αναζήτηση με contains(keyword) αντί για exact match, για να βρίσκουμε παραλλαγές των λέξεων.
    // Για κάθε λέξη-κλειδί που βρίσκεται στην περιγραφή (έστω και ως υποσυμβολοσειρά), προσθέτουμε το βάρος της στην συνολική επικινδυνότητα.
    // Χρησιμοποιούμε ένα Set για να μετρήσουμε κάθε λέξη-κλειδί μόνο μία φορά ανά αναφορά, ακόμα κι αν εμφανίζεται πολλές

    private int calculateWeight(String description) {
        int totalWeight = 0;

        // Κανονικοποίηση περιγραφής: μετατροπή σε πεζά και αφαίρεση τόνων
        String normalizedDescription = removeAccents(description.toLowerCase());

        // Χώρισε την περιγραφή σε λέξεις χρησιμοποιώντας ως διαχωριστικό τα κενά
        String[] words = normalizedDescription.split("\\s+");

        // Ένα σύνολο (Set) για την αποθήκευση των λέξεων-κλειδιών που έχουν ήδη αντιστοιχιστεί,
        // ώστε κάθε λέξη-κλειδί να μετρηθεί μόνο μία φορά.
        Set<String> matchedKeywords = new HashSet<>();

        // Επανάληψη σε όλες τις καταχωρήσεις του λεξικού keywordWeights (λέξη-κλειδί και βάρος)
        for (Map.Entry<String, Integer> entry : keywordWeights.entrySet()) {
            // Κανονικοποίηση και της λέξης-κλειδιού για σύγκριση χωρίς τόνους
            String keyword = removeAccents(entry.getKey().toLowerCase());

            // Επανάληψη σε κάθε λέξη της περιγραφής
            for (String word : words) {
                // Έλεγχος αν η τρέχουσα λέξη της περιγραφής περιέχει τη λέξη-κλειδί
                // και αν η λέξη-κλειδί δεν έχει ήδη μετρηθεί
                if (word.contains(keyword) && !matchedKeywords.contains(keyword)) {
                    // Προσθήκη του βάρους της λέξης-κλειδιού στο συνολικό βάρος
                    totalWeight += entry.getValue();
                    // Προσθήκη της λέξης-κλειδιού στο σύνολο των αντιστοιχισμένων λέξεων-κλειδιών
                    matchedKeywords.add(keyword);
                    // Μόλις βρεθεί μια αντιστοίχιση για την τρέχουσα λέξη-κλειδί,
                    // δεν χρειάζεται να ελέγξουμε τις υπόλοιπες λέξεις της περιγραφής για την ίδια λέξη-κλειδί
                    break;
                }
            }
        }

        // Επιστροφή του συνολικού υπολογισμένου βάρους επικινδυνότητας
        return totalWeight;
    }


    /**
     * Αφαιρεί τους τόνους από μια συμβολοσειρά.
     * Χρησιμοποιεί την κλάση Normalizer για την αποσύνθεση των συνδυασμένων χαρακτήρων
     * και ένα regex pattern για την αφαίρεση των διακριτικών σημείων.
     *
     * @param input Η συμβολοσειρά από την οποία θα αφαιρεθούν οι τόνοι.
     * @return Μια νέα συμβολοσειρά χωρίς τόνους.
     */
    private String removeAccents(String input) {
        // Κανονικοποίηση της συμβολοσειράς στην κανονική μορφή συμβατότητας NFD
        // (Canonical Decomposition). Αυτό διαχωρίζει τους τονισμένους χαρακτήρες
        // σε βασικούς χαρακτήρες και διακριτικά σημάδια.
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Δημιουργία ενός pattern για την εύρεση όλων των διακριτικών σημείων Unicode.
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        // Αντικατάσταση όλων των διακριτικών σημείων με μια κενή συμβολοσειρά,
        // ουσιαστικά αφαιρώντας τα.
        return pattern.matcher(normalized).replaceAll("");
    }

    /**
     * Λαμβάνει την τρέχουσα τοποθεσία της συσκευής.
     * Ελέγχει αν έχουν δοθεί οι απαραίτητες άδειες τοποθεσίας και, αν όχι, τις ζητά από τον χρήστη.
     * Αν οι άδειες έχουν δοθεί, λαμβάνει την τελευταία γνωστή τοποθεσία και ενημερώνει τα αντίστοιχα TextViews.
     */
    private void getLocation() {
        // Έλεγχος αν η εφαρμογή έχει την άδεια για πρόσβαση στην ακριβή τοποθεσία
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Αν δεν έχουν δοθεί οι άδειες, ζητήστε τις από τον χρήστη.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return; // Σταματήστε την εκτέλεση της μεθόδου μέχρι να απαντήσει ο χρήστης στο αίτημα αδειών.
        }

        // Αν οι άδειες έχουν δοθεί, λάβετε την τελευταία γνωστή τοποθεσία
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Αν η τοποθεσία δεν είναι null, ενημερώστε τα TextViews με το γεωγραφικό πλάτος και μήκος.
                        if (location != null) {
                            textViewLatitude.setText("Γεωγραφικό Πλάτος: " + location.getLatitude());
                            textViewLongitude.setText("Γεωγραφικό Μήκος: " + location.getLongitude());
                        } else {
                            // Αν η τοποθεσία είναι null, εμφανίστε μηνύματα σφάλματος στα TextViews.
                            textViewLatitude.setText("Αδυναμία λήψης γεωγραφικού πλάτους");
                            textViewLongitude.setText("Αδυναμία λήψης γεωγραφικού μήκους");
                        }
                    }
                });
    }

    /**
     * Υποβάλλει την αναφορά περιστατικού στην Firebase Realtime Database.
     * Λαμβάνει την τρέχουσα τοποθεσία, την περιγραφή από τον χρήστη,
     * υπολογίζει το βάρος επικινδυνότητας και αποθηκεύει όλα αυτά τα δεδομένα
     * μαζί με τη χρονική σήμανση και το email του χρήστη στη βάση δεδομένων.
     * Πριν την αποθήκευση, ελέγχει αν υπάρχει παρόμοια αναφορά για να αποφευχθούν διπλότυπα.
     */
    private void submitIncident() {
        // Έλεγχος αν η εφαρμογή έχει τις απαραίτητες άδειες τοποθεσίας
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Αν δεν έχουν δοθεί, ζητήστε τις.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return; // Σταματήστε μέχρι να απαντήσει ο χρήστης.
        }

        // Λήψη instance της Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Δημιουργία αναφοράς στον κόμβο "Αναφορές"
        DatabaseReference ref = database.getReference("Αναφορές");

        // Λήψη της τελευταίας γνωστής τοποθεσίας
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Αν η τοποθεσία δεν είναι null
                        if (location != null) {
                            // Λήψη γεωγραφικού πλάτους και μήκους ως strings
                            String latitude = String.valueOf(location.getLatitude());
                            String longitude = String.valueOf(location.getLongitude());
                            // Λήψη της περιγραφής του περιστατικού και μετατροπή σε πεζά
                            String description = editTextDescription.getText().toString().toLowerCase();
                            // Υπολογισμός του βάρους επικινδυνότητας για την περιγραφή
                            int weight = calculateWeight(description);

                            // Λήψη της τρέχουσας ημερομηνίας και ώρας σε συγκεκριμένη μορφή
                            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                            // Ενημέρωση του TextView με την υπολογισμένη επικινδυνότητα
                            textViewWeight.setText("Επικινδυνότητα = " + weight);

                            // Ανάγνωση των υπαρχουσών αναφορών από τη Firebase για έλεγχο διπλότυπων
                            ref.get().addOnSuccessListener(dataSnapshot -> {
                                // Δημιουργία μιας λίστας για την αποθήκευση των υπαρχουσών αναφορών
                                List<Map<String, Object>> existingIncidents = new ArrayList<>();
                                // Επανάληψη σε όλα τα παιδιά του κόμβου "Αναφορές"
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // Προσθήκη κάθε αναφοράς (ως Map) στη λίστα
                                    existingIncidents.add((Map<String, Object>) snapshot.getValue());
                                }

                                // Έλεγχος αν η νέα αναφορά είναι διπλότυπη με βάση την περιγραφή, την τοποθεσία και τον χρόνο
                                if (isDuplicateBayes(description, location.getLatitude(), location.getLongitude(), currentDateTime, existingIncidents)) {
                                    // Εμφάνιση μηνύματος ότι υπάρχει παρόμοια αναφορά
                                    Toast.makeText(ReportIncidentActivity.this, "Παρόμοια αναφορά υπάρχει ήδη.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Δημιουργία ενός Map για τα δεδομένα της νέας αναφοράς
                                    Map<String, Object> incidentData = new HashMap<>();
                                    incidentData.put("latitude", latitude);
                                    incidentData.put("longitude", longitude);
                                    incidentData.put("description", description);
                                    incidentData.put("weight", weight);
                                    incidentData.put("timestamp", currentDateTime);
                                    incidentData.put("email", email);

                                    // Δημιουργία ενός νέου μοναδικού κλειδιού για την αναφορά
                                    String incidentId = ref.push().getKey();
                                    // Αποθήκευση των δεδομένων της αναφοράς κάτω από αυτό το κλειδί
                                    ref.child(incidentId).setValue(incidentData)
                                            .addOnSuccessListener(aVoid -> Toast.makeText(ReportIncidentActivity.this, "Το περιστατικό αναφέρθηκε!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(ReportIncidentActivity.this, "Σφάλμα αποθήκευσης", Toast.LENGTH_SHORT).show());

                                    // Δημιουργία και αποστολή ενός broadcast intent για να ενημερωθούν άλλες δραστηριότητες (αν υπάρχουν)
                                    Intent intent = new Intent("INCIDENT_REPORTED");
                                    sendBroadcast(intent);
                                    // Δημιουργία ενός result intent για να ενημερωθεί η δραστηριότητα που κάλεσε αυτήν (αν υπάρχει)
                                    Intent resultIntent = new Intent();
                                    setResult(RESULT_OK, resultIntent);
                                    // Ολοκλήρωση αυτής της δραστηριότητας και επιστροφή στην προηγούμενη
                                    finish();
                                }
                            });

                        } else {
                            // Εμφάνιση μηνύματος αν δεν μπόρεσε να ληφθεί η τοποθεσία
                            Toast.makeText(ReportIncidentActivity.this, "Αδυναμία λήψης τοποθεσίας", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * Ελέγχει αν μια νέα αναφορά περιστατικού είναι πιθανό διπλότυπο μιας υπάρχουσας,
     * χρησιμοποιώντας έναν απλό αλγόριθμο Naive Bayes που συνδυάζει την ομοιότητα
     * της περιγραφής, την εγγύτητα της τοποθεσίας και την εγγύτητα του χρόνου.
     *
     * @param desc      Η περιγραφή της νέας αναφοράς.
     * @param lat       Το γεωγραφικό πλάτος της νέας αναφοράς.
     * @param lon       Το γεωγραφικό μήκος της νέας αναφοράς.
     * @param timestamp Η χρονική σήμανση της νέας αναφοράς.
     * @param incidents Μια λίστα με τις υπάρχουσες αναφορές περιστατικών.
     * @return true αν η νέα αναφορά είναι πιθανό διπλότυπο, false διαφορετικά.
     */

    private boolean isDuplicateBayes(String desc, double lat, double lon, String timestamp, List<Map<String, Object>> incidents) {
        for (Map<String, Object> incident : incidents) {
            String existingDesc = (String) incident.get("description");
            double existingLat = Double.parseDouble(incident.get("latitude").toString());
            double existingLon = Double.parseDouble(incident.get("longitude").toString());
            String existingTime = (String) incident.get("timestamp");

            // Υπολογισμός της ομοιότητας της περιγραφής ανάμεσα στην τρέχουσα και την υπάρχουσα αναφορά
            // Η συνάρτηση textSimilarity επιστρέφει μία τιμή από 0 έως 1, όπου το 1 σημαίνει απόλυτη ομοιότητα
            double descSim = textSimilarity(desc, existingDesc); // [0,1]

            // Υπολογισμός της απόστασης μεταξύ των τοποθεσιών των δύο αναφορών σε μέτρα
            // Η συνάρτηση calculateDistance επιστρέφει την απόσταση σε μέτρα μεταξύ των δύο γεωγραφικών συντεταγμένων
            double locDist = calculateDistance(lat, lon, existingLat, existingLon); // σε μέτρα

            // Υπολογισμός της ομοιότητας της τοποθεσίας
            // Αν η απόσταση είναι μικρότερη ή ίση με 5 μέτρα, θεωρούμε ότι είναι 100% ίδια τοποθεσία (1.0)
            // Αν η απόσταση είναι μεγαλύτερη ή ίση με 100 μέτρα, θεωρούμε ότι είναι εντελώς διαφορετική (0.0)
            // Αν η απόσταση είναι μεταξύ 5 και 100 μέτρων, χρησιμοποιούμε γραμμική συνάρτηση για να υπολογίσουμε την ομοιότητα
            double locSim = locDist <= 5 ? 1.0 : locDist >= 100 ? 0.0 : 1.0 - ((locDist - 5) / 95.0);


            // Υπολογισμός της διαφοράς χρόνου μεταξύ των δύο αναφορών σε λεπτά
            // Η συνάρτηση getTimeDifferenceInMinutes επιστρέφει την διαφορά σε λεπτά μεταξύ των χρονικών στιγμών
            int timeDiff = getTimeDifferenceInMinutes(timestamp, existingTime);

            // Υπολογισμός της ομοιότητας του χρόνου
            // Αν η διαφορά χρόνου είναι μικρότερη ή ίση με 10 λεπτά, θεωρούμε ότι είναι 100% ίδιος χρόνος (1.0)
            // Αν η διαφορά είναι μεγαλύτερη ή ίση με 60 λεπτά, θεωρούμε ότι είναι εντελώς διαφορετικός χρόνος (0.0)
            // Αν η διαφορά είναι μεταξύ 10 και 60 λεπτών, χρησιμοποιούμε γραμμική συνάρτηση για να υπολογίσουμε την ομοιότητα
            double timeSim = timeDiff <= 10 ? 1.0 : timeDiff >= 60 ? 0.0 : 1.0 - ((timeDiff - 10) / 50.0);

            // Naive Bayes - θεώρηση ανεξαρτησίας
            double probabilityDuplicate = descSim * locSim * timeSim;

            // threshold πιθανότητας
            if (probabilityDuplicate >= 0.30) return true; // Αν η πιθανότητα υπερβεί το 30%, θεωρείται διπλή
        }

        // Αν δεν βρεθεί καμία υπάρχουσα αναφορά που να πληροί
        return false;
    }

    /**
     * Υπολογίζει την ομοιότητα μεταξύ δύο συμβολοσειρών κειμένου χρησιμοποιώντας την απόσταση Levenshtein.
     * Η ομοιότητα επιστρέφεται ως μια τιμή μεταξύ 0 και 1, όπου το 1 σημαίνει απόλυτη ομοιότητα.
     *
     * @param s1 Η πρώτη συμβολοσειρά.
     * @param s2 Η δεύτερη συμβολοσειρά.
     * @return Η ομοιότητα μεταξύ των δύο συμβολοσειρών.
     */
    private double textSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;

        int dist = levenshteinDistance(s1, s2);
        return 1.0 - (double) dist / maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(
                            dp[i - 1][j] + 1,    // διαγραφή
                            dp[i][j - 1] + 1     // εισαγωγή
                    ), dp[i - 1][j - 1] + cost); // αντικατάσταση
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }


    /**
     * Υπολογίζει τη γεωγραφική απόσταση μεταξύ δύο συντεταγμένων (γεωγραφικό πλάτος και μήκος).
     * Χρησιμοποιεί τη μέθοδο `distanceBetween` της κλάσης `Location` για τον υπολογισμό.
     *
     * @param lat1 Το γεωγραφικό πλάτος της πρώτης τοποθεσίας.
     * @param lon1 Το γεωγραφικό μήκος της πρώτης τοποθεσίας.
     * @param lat2 Το γεωγραφικό πλάτος της δεύτερης τοποθεσίας.
     * @param lon2 Το γεωγραφικό μήκος της δεύτερης τοποθεσίας.
     * @return Η απόσταση μεταξύ των δύο τοποθεσιών σε μέτρα.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }


    /**
     * Υπολογίζει τη διαφορά σε λεπτά μεταξύ δύο χρονοσήμων (timestamps) σε μορφή "yyyy-MM-dd HH:mm:ss".
     * Χρησιμοποιεί την κλάση `SimpleDateFormat` για την ανάλυση των συμβολοσειρών ημερομηνίας
     * και την κλάση `Date` για τον υπολογισμό της διαφοράς σε milliseconds, η οποία στη συνέχεια
     * μετατρέπεται σε λεπτά.
     *
     * @param t1 Η πρώτη χρονική σήμανση.
     * @param t2 Η δεύτερη χρονική σήμανση.
     * @return Η απόλυτη διαφορά μεταξύ των δύο χρονοσήμων σε λεπτά.
     * Επιστρέφει `Integer.MAX_VALUE` σε περίπτωση σφάλματος κατά την ανάλυση της ημερομηνίας.
     */
    private int getTimeDifferenceInMinutes(String t1, String t2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date1 = sdf.parse(t1);
            Date date2 = sdf.parse(t2);
            long diff = Math.abs(date1.getTime() - date2.getTime());
            return (int) (diff / (1000 * 60));
        } catch (ParseException e) {
            return Integer.MAX_VALUE;
        }
    }



    /**
     * Καλείται όταν ο χρήστης απαντά στο αίτημα για άδειες (π.χ., άδεια τοποθεσίας).
     * Ελέγχει αν η άδεια που ζητήθηκε (requestCode == 100 για την άδεια τοποθεσίας)
     * έχει δοθεί. Αν ναι, καλεί τη μέθοδο `getLocation()` για να λάβει την τοποθεσία.
     * Αν όχι, εμφανίζει ένα μήνυμα Toast ενημερώνοντας τον χρήστη ότι η άδεια δεν δόθηκε.
     *
     * @param requestCode Ο κωδικός αιτήματος που πέρασε στην `requestPermissions`.
     * @param permissions Ο πίνακας των ζητούμενων αδειών.
     * @param grantResults Ο πίνακας των αποτελεσμάτων για κάθε αντίστοιχη άδεια.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Η άδεια τοποθεσίας δεν δόθηκε", Toast.LENGTH_SHORT).show();
            }
        }
    }
}