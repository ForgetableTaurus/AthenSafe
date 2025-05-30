package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

/**
 * Η δραστηριότητα `PremiumActivity` παρουσιάζει στον χρήστη τις διαφορές μεταξύ ενός
 * Premium και ενός Standard λογαριασμού. Επιτρέπει στον χρήστη να επιλέξει να γίνει
 * Premium χρήστης ή να συνεχίσει ως Standard χρήστης. Ανάλογα με την επιλογή,
 * αποθηκεύει τα στοιχεία του χρήστη στη Firebase Realtime Database και τον
 * κατευθύνει στην αντίστοιχη δραστηριότητα (PaymentActivity για Premium, MapsActivity για Standard).
 */
public class PremiumActivity extends AppCompatActivity {

    private TextView premiumUserDescription; // TextView για την εμφάνιση της περιγραφής των πλεονεκτημάτων Premium/Standard
    private TextView whatIsPremiumUserLink; // TextView που λειτουργεί ως σύνδεσμος για εμφάνιση/απόκρυψη της περιγραφής
    private boolean isDescriptionVisible = false; // Μεταβλητή για την παρακολούθηση της ορατότητας της περιγραφής
    private String email; // Το πλήρες email του χρήστη

    private String emailnopassword; // Το email του χρήστη χωρίς τον κωδικό

    private String firstname; // Το όνομα του χρήστη

    private String lastname; // Το επώνυμο του χρήστη

    private String username; // Το όνομα χρήστη (username)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Λαμβάνουμε τα δεδομένα του χρήστη που πέρασαν από την προηγούμενη δραστηριότητα
        email = getIntent().getStringExtra("email");
        emailnopassword=getIntent().getStringExtra("emailnopassword");
        firstname=getIntent().getStringExtra("firstname");
        lastname=getIntent().getStringExtra("lastname");
        username=getIntent().getStringExtra("username");
        // Δημιουργούμε μια αναφορά στον κόμβο "Προφίλ Χρηστών" της Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Προφίλ Χρηστών");

        // Ορίζουμε το layout της δραστηριότητας
        setContentView(R.layout.activity_premium); // Βεβαιωθείτε ότι το αρχείο XML ονομάζεται activity_premium.xml

        // Αντιστοιχίζουμε τις μεταβλητές με τα στοιχεία του layout
        premiumUserDescription = findViewById(R.id.premiumUserDescription);
        whatIsPremiumUserLink = findViewById(R.id.whatIsPremiumUserLink);

        // Δημιουργούμε ένα κείμενο με HTML για να μορφοποιήσουμε τις διαφορές Premium/Standard
        String descriptionText = "<b>As a Premium User</b><br>• Make unlimited incident reports<br>• Search unlimited destinations<br><br><b>As a Regular User</b><br>• One report per day<br>• Search up to 3 destinations";
        // Εμφανίζουμε το μορφοποιημένο κείμενο στο TextView
        premiumUserDescription.setText(Html.fromHtml(descriptionText));

        // Ορίζουμε έναν OnClickListener για το "What is a premium user?" σύνδεσμο
        whatIsPremiumUserLink.setOnClickListener(v -> {
            // Αν η περιγραφή είναι ορατή, την κρύβουμε, αλλιώς την εμφανίζουμε
            if (isDescriptionVisible) {
                premiumUserDescription.setVisibility(View.GONE);
            } else {
                premiumUserDescription.setVisibility(View.VISIBLE);
            }
            // Αντιστρέφουμε την κατάσταση ορατότητας
            isDescriptionVisible = !isDescriptionVisible;
        });

        // Συνδέουμε τα κουμπιά "Become Premium User" και "Continue as Standard User"
        Button premiumUserButton = findViewById(R.id.premiumUserButton);
        Button standardUserButton = findViewById(R.id.officialUserButton);

        // Ορίζουμε έναν OnClickListener για το κουμπί "Become Premium User"
        premiumUserButton.setOnClickListener(v -> {
            // Δημιουργούμε ένα μοναδικό ID για τον χρήστη αντικαθιστώντας τις τελείες στο email
            String userId = emailnopassword.replace(".", "_");

            // Δημιουργούμε ένα HashMap για να αποθηκεύσουμε τα στοιχεία του χρήστη
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("firstname", firstname);
            userData.put("lastname", lastname);
            userData.put("email", email);
            userData.put("emailnopassword", emailnopassword);

            // Γράφουμε τα στοιχεία του χρήστη στον κόμβο "Προφίλ Χρηστών" της Firebase
            databaseReference.child(userId).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Εάν η εγγραφή είναι επιτυχής, μεταβαίνουμε στην PaymentActivity
                        Intent intent = new Intent(PremiumActivity.this, PaymentActivity.class);
                        // Περνάμε τα στοιχεία του χρήστη στην PaymentActivity
                        intent.putExtra("email", email);
                        intent.putExtra("emailnopassword", emailnopassword);
                        intent.putExtra("username", username);
                        intent.putExtra("firstname", firstname);
                        intent.putExtra("lastname", lastname);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Εάν η εγγραφή αποτύχει, εκτυπώνουμε το σφάλμα
                        e.printStackTrace();
                    });
        });

        // Ορίζουμε έναν OnClickListener για το κουμπί "Continue as Standard User"
        standardUserButton.setOnClickListener(v -> {
            // Δημιουργούμε ένα μοναδικό ID για τον χρήστη αντικαθιστώντας τις τελείες στο email
            String userId = emailnopassword.replace(".", "_");

            // Δημιουργούμε ένα HashMap για να αποθηκεύσουμε τα στοιχεία του χρήστη
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("firstname", firstname);
            userData.put("lastname", lastname);
            userData.put("email", email);
            userData.put("emailnopassword", emailnopassword);

            // Γράφουμε τα στοιχεία του χρήστη στον κόμβο "Προφίλ Χρηστών" της Firebase
            databaseReference.child(userId).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Εάν η εγγραφή είναι επιτυχής, μεταβαίνουμε στην MapsActivity
                        Intent intent = new Intent(PremiumActivity.this, MapsActivity.class);
                        // Περνάμε τα στοιχεία του χρήστη στην MapsActivity
                        intent.putExtra("email", email);
                        intent.putExtra("emailnopassword", emailnopassword);
                        intent.putExtra("username", username);
                        intent.putExtra("firstname", firstname);
                        intent.putExtra("lastname", lastname);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Εάν η εγγραφή αποτύχει, εκτυπώνουμε το σφάλμα
                        e.printStackTrace();
                    });
        });
    }
}