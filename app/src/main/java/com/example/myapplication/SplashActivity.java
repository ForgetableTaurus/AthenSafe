package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Η δραστηριότητα `SplashActivity` εμφανίζεται κατά την εκκίνηση της εφαρμογής.
 * Παρουσιάζει ένα λογότυπο και μια σειρά από εικονίδια τοποθεσίας με εφέ εμφάνισης
 * και μετά από μια καθορισμένη καθυστέρηση, μεταβαίνει στην `MainActivity`.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Κρύβει την γραμμή κατάστασης (status bar) για να εμφανίζεται η δραστηριότητα σε πλήρη οθόνη
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Ορίζει το layout που θα χρησιμοποιηθεί για αυτή την δραστηριότητα (activity_splash.xml)
        setContentView(R.layout.activity_splash);

        // Λήψη αναφοράς στο ImageView του λογοτύπου από το layout μέσω του ID "logo"
        ImageView logo = findViewById(R.id.logo);
        // Φόρτωση του animation "fade_in" από το αρχείο R.anim.fade_in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        // Έναρξη του animation "fade_in" για το ImageView του λογοτύπου, δημιουργώντας ένα οπτικό εφέ εμφάνισης
        logo.startAnimation(fadeIn);

        // Πίνακας με τα IDs των ImageViews που αναπαριστούν τους δείκτες τοποθεσίας
        int[] markerIds = {
                R.id.locationIcon2,
                R.id.locationIcon3,
                R.id.locationIcon4,
                R.id.locationIcon5,
                R.id.locationIcon6
        };

        // Καθυστέρηση σε milliseconds μεταξύ της εμφάνισης κάθε δείκτη
        int delayBetweenMarkers = 500; // ms (milliseconds)

        // Επανάληψη για κάθε ID δείκτη στον πίνακα markerIds
        for (int i = 0; i < markerIds.length; i++) {
            // Λήψη αναφοράς στο ImageView του τρέχοντος δείκτη χρησιμοποιώντας το ID από τον πίνακα
            ImageView marker = findViewById(markerIds[i]);
            // Αρχική απόκρυψη του δείκτη κάνοντας την διαφάνειά του 0 (πλήρως διαφανής)
            marker.setAlpha(0f); // Απόκρυψη αρχικά
            // Υπολογισμός της καθυστέρησης για την εμφάνιση του τρέχοντος δείκτη.
            // Κάθε δείκτης θα εμφανιστεί με μια αυξανόμενη καθυστέρηση.
            int delay = (i + 1) * delayBetweenMarkers;

            // Χρήση ενός Handler για να εκτελέσει κώδικα (την εμφάνιση του δείκτη) μετά από μια καθυστέρηση
            new Handler().postDelayed(() -> {
                // Φόρτωση του animation "fade_in" για τους δείκτες από το αρχείο R.anim.fade_in
                Animation fade = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
                // Έναρξη του animation "fade_in" για τον τρέχοντα δείκτη
                marker.startAnimation(fade);
                // Εμφάνιση του δείκτη κάνοντας την διαφάνειά του 1 (πλήρως αδιαφανής)
                marker.setAlpha(1f);
            }, delay);
        }

        // Καθυστέρηση για την μετάβαση στην MainActivity (4.5 δευτερόλεπτα)
        new Handler().postDelayed(() -> {
            // Δημιουργία ενός Intent για την εκκίνηση της MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            // Έναρξη της MainActivity
            startActivity(intent);
            // Ολοκλήρωση της SplashActivity για να μην μπορεί ο χρήστης να επιστρέψει σε αυτήν με το κουμπί "πίσω"
            finish();
        }, 4500); // 4500 milliseconds = 4.5 seconds
    }
}