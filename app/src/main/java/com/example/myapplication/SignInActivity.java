package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Η κλάση `SignInActivity` είναι υπεύθυνη για την οθόνη σύνδεσης (Sign In) της εφαρμογής.
 * Παρέχει ένα απλό περιβάλλον όπου ο χρήστης μπορεί να εισάγει το email και τον κωδικό πρόσβασής του.
 * Όταν ο χρήστης πατήσει το κουμπί "Sign In", η κλάση ελέγχει αν έχουν συμπληρωθεί και τα δύο πεδία.
 * Αν ναι, τότε δημιουργεί ένα Intent για να μεταβεί στην `MapsActivity`, περνώντας κάποια δεδομένα (email και συνδυασμό email-password) σε αυτήν.
 * Τέλος, καλεί την `startActivity` για να εμφανίσει την `MapsActivity` και την `finish()` για να κλείσει την `SignInActivity`,
 * ώστε ο χρήστης να μην μπορεί να επιστρέψει σε αυτήν πατώντας το κουμπί "πίσω".
 */
public class SignInActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton signInButton;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.signInButton);

        usersRef = FirebaseDatabase.getInstance().getReference("Προφίλ Χρηστών");

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = editTextEmail.getText().toString().trim();
                String passwordInput = editTextPassword.getText().toString().trim();

                if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Παρακαλώ συμπληρώστε όλα τα πεδία", Toast.LENGTH_SHORT).show();
                } else {
                    String fullCredential = emailInput + passwordInput;
                    checkIfUserExists(fullCredential, emailInput);
                }
            }
        });
    }

    private void checkIfUserExists(final String fullCredential, final String emailOnly) {
        usersRef.orderByChild("email").equalTo(fullCredential).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Είσοδος επιτυχής
                    Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
                    intent.putExtra("email", fullCredential);
                    intent.putExtra("emailnopassword", emailOnly);
                    startActivity(intent);
                    finish();
                } else {
                    // Το email+password δεν ταιριάζει με κάποιον χρήστη
                    Toast.makeText(SignInActivity.this, "Αυτός ο λογαριασμός δεν υπάρχει. Παρακαλώ δημιουργήστε πρώτα λογαριασμό.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignInActivity.this, "Σφάλμα σύνδεσης με τη βάση", Toast.LENGTH_SHORT).show();
            }
        });
    }
}