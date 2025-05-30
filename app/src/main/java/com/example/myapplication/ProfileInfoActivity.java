package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Η activity `ProfileInfoActivity` εμφανίζει τις πληροφορίες του προφίλ του χρήστη.
 * Αυτές οι πληροφορίες περιλαμβάνουν το όνομα χρήστη, το όνομα, το επώνυμο, το email και τον τύπο του λογαριασμού (Standard ή Premium).
 * Η δραστηριότητα ανακτά τα δεδομένα του χρήστη είτε από τα Intent extras που της έχουν περαστεί
 * είτε από τη Firebase Realtime Database. Επιπλέον, παρέχει κουμπιά για την αναβάθμιση σε Premium
 * και για την αποσύνδεση του χρήστη.
 */
public class ProfileInfoActivity extends AppCompatActivity {

    private Button changeToPremiumButton; // Κουμπί για μετάβαση στην οθόνη πληρωμής για αναβάθμιση σε Premium
    private Button logOutButton; // Κουμπί για την αποσύνδεση του χρήστη

    private String email; // Το πλήρες email του χρήστη (χρησιμοποιείται για έλεγχο Premium)
    private String emailnopassword; // Το email του χρήστη χωρίς τον κωδικό (χρησιμοποιείται για αναζήτηση στο "Προφίλ Χρηστών")
    private String firstname; // Το όνομα του χρήστη
    private String lastname; // Το επώνυμο του χρήστη
    private String username; // Το όνομα χρήστη (username)

    private TextView userNameText; // TextView για την εμφάνιση του username
    private TextView firstNameLabel; // TextView για την εμφάνιση του ονόματος
    private TextView lastNameLabel; // TextView για την εμφάνιση του επωνύμου
    private TextView emailLabel; // TextView για την εμφάνιση του email
    private TextView typeLabel; // TextView για την εμφάνιση του τύπου λογαριασμού

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        // Παίρνουμε τα δεδομένα που πέρασαν στην δραστηριότητα μέσω του Intent
        emailnopassword = getIntent().getStringExtra("emailnopassword");
        firstname = getIntent().getStringExtra("firstname");
        lastname = getIntent().getStringExtra("lastname");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");

        // Συνδέουμε τις μεταβλητές με τα αντίστοιχα κουμπιά από το layout
        changeToPremiumButton = findViewById(R.id.changeToPremiumButton);
        logOutButton = findViewById(R.id.logOutButton);

        // Συνδέουμε τις μεταβλητές με τα αντίστοιχα TextViews από το layout
        userNameText = findViewById(R.id.userNameText);
        firstNameLabel = findViewById(R.id.firstNameLabel);
        lastNameLabel = findViewById(R.id.lastNameLabel);
        emailLabel = findViewById(R.id.emailLabel);
        typeLabel = findViewById(R.id.typeLabel);


        // Βρίσκουμε το κουμπί "πίσω" και ορίζουμε έναν OnClickListener για να επιστρέψει στην προηγούμενη οθόνη
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Ή μπορεί να χρησιμοποιηθεί finish();
            }
        });


        // -------- Έλεγχος τύπου χρήστη (Premium/Standard) από τη Firebase --------
        // Δημιουργούμε μια αναφορά στον κόμβο "Premium Χρήστες" της Firebase
        DatabaseReference premiumRef = FirebaseDatabase.getInstance().getReference("Premium Χρήστες");

        // Διαβάζουμε τα δεδομένα από τον κόμβο "Premium Χρήστες" μία φορά
        premiumRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean isPremium = false;

                // Επαναλαμβάνουμε σε όλα τα παιδιά του κόμβου "Premium Χρήστες"
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String fetchedEmail = userSnapshot.getKey();

                    // Έλεγχος αν το email του τρέχοντος χρήστη ταιριάζει με κάποιο email Premium χρήστη
                    if (fetchedEmail != null && fetchedEmail.equals(email)) {
                        isPremium = true;
                        break; // Αν βρεθεί, δεν χρειάζεται να συνεχίσουμε την αναζήτηση
                    }
                }

                // Ενημερώνουμε το TextView ανάλογα με το αν ο χρήστης είναι Premium ή Standard
                if (isPremium) {
                    typeLabel.setText("Type: Premium");
                } else {
                    typeLabel.setText("Type: Standard");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Σε περίπτωση σφάλματος κατά την ανάγνωση των δεδομένων, θεωρούμε τον χρήστη Standard
                typeLabel.setText("Type: Standard");
            }
        });

        // -------- Ανάκτηση στοιχείων χρήστη από τον κόμβο "Προφίλ Χρηστών" της Firebase --------
        // Δημιουργούμε μια αναφορά στον κόμβο "Προφίλ Χρηστών"
        DatabaseReference profileUsersRef = FirebaseDatabase.getInstance().getReference("Προφίλ Χρηστών");

        // Διαβάζουμε τα δεδομένα από τον κόμβο "Προφίλ Χρηστών" μία φορά
        profileUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;

                // Επαναλαμβάνουμε σε όλα τα παιδιά του κόμβου "Προφίλ Χρηστών"
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Παίρνουμε το email (χωρίς κωδικό) από τα δεδομένα του χρήστη
                    String fetchedEmail = userSnapshot.child("emailnopassword").getValue(String.class);

                    // Έλεγχος αν το email του τρέχοντος χρήστη ταιριάζει με κάποιο email στον κόμβο "Προφίλ Χρηστών"
                    if (fetchedEmail != null && fetchedEmail.equals(emailnopassword)) {
                        // Αν βρεθεί, ανακτούμε τα υπόλοιπα στοιχεία του χρήστη
                        String fetchedUsername = userSnapshot.child("username").getValue(String.class);
                        String fetchedFirstname = userSnapshot.child("firstname").getValue(String.class);
                        String fetchedLastname = userSnapshot.child("lastname").getValue(String.class);

                        // Ενημερώνουμε τα TextViews με τα ανακτηθέντα στοιχεία (αν δεν είναι null)
                        if (fetchedUsername != null) {
                            userNameText.setText(fetchedUsername);
                        }
                        if (fetchedFirstname != null) {
                            firstNameLabel.setText("First name: " + fetchedFirstname);
                        }
                        if (fetchedLastname != null) {
                            lastNameLabel.setText("Last name: " + fetchedLastname);
                        }
                        if (fetchedEmail != null) {
                            emailLabel.setText("Email: " + fetchedEmail);
                        }

                        found = true;
                        break; // Αν βρεθεί ο χρήστης, σταματάμε την αναζήτηση
                    }
                }

                // Αν ο χρήστης δεν βρεθεί στον κόμβο "Προφίλ Χρηστών", χρησιμοποιούμε τα δεδομένα που πέρασαν μέσω του Intent
                if (!found) {
                    if (username != null) {
                        userNameText.setText(username);
                    }
                    if (firstname != null) {
                        firstNameLabel.setText("First name: " + firstname);
                    }
                    if (lastname != null) {
                        lastNameLabel.setText("Last name: " + lastname);
                    }
                    if (emailnopassword != null) {
                        emailLabel.setText("Email: " + emailnopassword);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Σε περίπτωση σφάλματος κατά την ανάγνωση των δεδομένων, χρησιμοποιούμε τα δεδομένα του Intent ως fallback
                if (username != null) {
                    userNameText.setText(username);
                }
                if (firstname != null) {
                    firstNameLabel.setText("First name: " + firstname);
                }
                if (lastname != null) {
                    lastNameLabel.setText("Last name: " + lastname);
                }
                if (emailnopassword != null) {
                    emailLabel.setText("Email: " + emailnopassword);
                }
            }
        });

        // OnClickListener για το κουμπί "Go Premium"
        changeToPremiumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Δημιουργούμε ένα Intent για να μεταβούμε στην PaymentActivity και περνάμε τα στοιχεία του χρήστη
                Intent intent = new Intent(ProfileInfoActivity.this, PaymentActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("emailnopassword", emailnopassword);
                intent.putExtra("username", username);
                intent.putExtra("firstname", firstname);
                intent.putExtra("lastname", lastname);
                startActivity(intent);
            }
        });

        // OnClickListener για το κουμπί "Log out"
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Δημιουργούμε ένα Intent για να μεταβούμε στην MainActivity και καθαρίζουμε το ιστορικό των δραστηριοτήτων
                Intent intent = new Intent(ProfileInfoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Κλείνουμε την τρέχουσα δραστηριότητα
            }
        });
    }
}