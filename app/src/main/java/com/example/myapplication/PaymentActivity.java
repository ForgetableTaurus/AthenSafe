package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Η δραστηριότητα `PaymentActivity` προσομοιώνει μια διαδικασία πληρωμής για την αναβάθμιση
 * ενός χρήστη σε Premium. Όταν ο χρήστης πατήσει το κουμπί πληρωμής, εμφανίζεται ένα μήνυμα
 * επιτυχίας και μετά από μια μικρή καθυστέρηση, ο χρήστης ανακατευθύνεται στην `MapsActivity`
 * ως Premium χρήστης. Τα στοιχεία του Premium χρήστη αποθηκεύονται στη Firebase Realtime Database.
 */
public class PaymentActivity extends AppCompatActivity {

    private TextView welcomeText; // TextView για την εμφάνιση ενός μηνύματος καλωσορίσματος μετά την πληρωμή
    private MaterialButton paymentButton; // Κουμπί για την προσομοίωση της διαδικασίας πληρωμής

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

        // Ορίζουμε το layout της δραστηριότητας
        setContentView(R.layout.activity_payment); // Βεβαιωθείτε ότι το αρχείο XML ονομάζεται activity_payment.xml

        // Αντιστοιχίζουμε τις μεταβλητές με τα στοιχεία του layout
        welcomeText = findViewById(R.id.welcomeText);
        paymentButton = findViewById(R.id.paymentButton);

        // Αρχικά κάνουμε το TextView αόρατο, θα εμφανιστεί μετά την "πληρωμή"
        welcomeText.setVisibility(View.GONE);

        // Προσθήκη listener για το κουμπί "Proceed to Payment"
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Εμφάνιση του TextView με το μήνυμα καλωσορίσματος όταν πατηθεί το κουμπί
                welcomeText.setVisibility(View.VISIBLE);

                // Χρονοκαθυστέρηση 3 δευτερολέπτων πριν την ανακατεύθυνση στην MapsActivity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Έλεγχος αν το email του χρήστη είναι διαθέσιμο
                        if (email != null && !email.isEmpty()) {
                            // Δημιουργία αναφοράς στον κόμβο "Premium Χρήστες" της Firebase
                            DatabaseReference premiumRef = FirebaseDatabase.getInstance().getReference("Premium Χρήστες");
                            // Ασφαλής αποθήκευση του email ως κλειδί, αντικαθιστώντας τις τελείες με κόμματα
                            String escapedEmail = email.replace(".", ",");
                            // Αποθήκευση μιας τιμής (true) κάτω από το email του χρήστη στον κόμβο Premium
                            premiumRef.child(escapedEmail).setValue(true)
                                    .addOnSuccessListener(aVoid -> {
                                        // Εμφάνιση μηνύματος επιτυχίας αν η αποθήκευση είναι επιτυχής
                                        Toast.makeText(getApplicationContext(), "Επιτυχής ολοκλήρωση.", Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Εμφάνιση μηνύματος αποτυχίας αν η αποθήκευση αποτύχει
                                        Toast.makeText(getApplicationContext(), "Μη επιτυχής ολοκλήρωση.", Toast.LENGTH_LONG).show();
                                    });
                        }

                        // Δημιουργία Intent για μετάβαση στην MapsActivity
                        Intent intent = new Intent(PaymentActivity.this, MapsActivity.class);
                        // Περνάμε ένα extra για να ενημερώσουμε την MapsActivity ότι ο χρήστης είναι Premium
                        intent.putExtra("isPremium", 1); // 1 υποδηλώνει Premium χρήστη
                        intent.putExtra("emailnopassword", emailnopassword);
                        intent.putExtra("username", username);
                        intent.putExtra("firstname", firstname);
                        intent.putExtra("lastname", lastname);
                        intent.putExtra("email", email);
                        // Εκκίνηση της MapsActivity
                        startActivity(intent);
                        // Κλείσιμο της τρέχουσας PaymentActivity
                        finish();
                    }
                }, 3000); // 3000 milliseconds = 3 δευτερόλεπτα καθυστέρηση
            }
        });
    }
}