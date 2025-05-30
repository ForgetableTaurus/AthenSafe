package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

/**
 * Αρχική δραστηριότητα της εφαρμογής.
 * Εμφανίζει εισαγωγικές εικόνες/κειμενικά στοιχεία με animation και παρέχει πρόσβαση σε εγγραφή ή σύνδεση.
 */
public class MainActivity extends AppCompatActivity {

    private Button signInButton, createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Φορτώνει το layout

        // Σύνδεση κουμπιών από το layout
        signInButton = findViewById(R.id.signInButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // IDs των εικόνων για την αρχική παρουσίαση
        int[] iconIds = {
                R.id.chatbot,      // εικόνα για chatbot
                R.id.sos,          // εικόνα για sos
                R.id.report,       // εικόνα για αναφορά
                R.id.find_route    // εικόνα για εύρεση διαδρομής
        };

        // IDs αντίστοιχων επεξηγηματικών κειμένων
        int[] textIds = {
                R.id.chatbotText,
                R.id.sosText,
                R.id.reportText,
                R.id.routeText
        };

        // Φόρτωση animation τύπου fade-in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        int delayBetweenMarkers = 1600; // καθυστέρηση μεταξύ των εμφανίσεων σε milliseconds

        // Εμφάνιση πρώτης εικόνας & κειμένου άμεσα
        ImageView firstIcon = findViewById(iconIds[0]);
        TextView firstText = findViewById(textIds[0]);
        firstIcon.setAlpha(0f);
        firstText.setAlpha(0f);
        firstIcon.startAnimation(fadeIn);
        firstText.startAnimation(fadeIn);
        firstIcon.setAlpha(1f);
        firstText.setAlpha(1f);

        // Εμφάνιση υπολοίπων με χρονική καθυστέρηση
        for (int i = 1; i < iconIds.length; i++) {
            ImageView marker = findViewById(iconIds[i]);
            TextView text = findViewById(textIds[i]);

            marker.setAlpha(0f);
            text.setAlpha(0f);

            int delay = i * delayBetweenMarkers;

            // Χρήση Handler για delayed animation
            new Handler().postDelayed(() -> {
                marker.startAnimation(fadeIn);
                marker.setAlpha(1f);
                text.startAnimation(fadeIn);
                text.setAlpha(1f);
            }, delay);
        }

        // Μεταφορά στην οθόνη σύνδεσης
        signInButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        });

        // Μεταφορά στην οθόνη δημιουργίας λογαριασμού
        createAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));
        });
    }
}