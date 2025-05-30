package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity για τη δημιουργία λογαριασμού χρήστη.
 */
public class CreateAccountActivity extends AppCompatActivity {

    // Ορισμός των στοιχείων του UI
    private MaterialButton signInButton;
    private TextInputEditText emailTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextUserName;
    private TextInputEditText editTextLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Σύνδεση του activity με το αντίστοιχο layout XML
        setContentView(R.layout.activity_create_account); // Βεβαιώσου ότι αυτό είναι το σωστό layout

        // Αντιστοίχιση των μεταβλητών με τα αντίστοιχα στοιχεία του UI
        signInButton = findViewById(R.id.signInButton);
        emailTextEmail = findViewById(R.id.emailTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFirstName = findViewById(R.id.editTextfirstname);
        editTextUserName = findViewById(R.id.usernameTextEmail);
        editTextLastName = findViewById(R.id.editTextlastname);

        // Ορισμός του τι θα συμβεί όταν πατηθεί το κουμπί "Δημιουργία λογαριασμού"
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Λήψη των τιμών που πληκτρολόγησε ο χρήστης
                String email = emailTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String username = editTextUserName.getText().toString().trim();
                String firstname = editTextFirstName.getText().toString().trim();
                String lastname = editTextLastName.getText().toString().trim();

                // Δημιουργία intent για μετάβαση στο PremiumActivity.
                Intent intent = new Intent(CreateAccountActivity.this, PremiumActivity.class);

                // Περνάμε τα δεδομένα μέσω του intent
                intent.putExtra("email", email + password);
                intent.putExtra("emailnopassword", email);
                intent.putExtra("username", username);
                intent.putExtra("firstname", firstname);
                intent.putExtra("lastname", lastname);

                // Ξεκινάμε το νέο activity
                startActivity(intent);

                // Τερματισμός του τρέχοντος activity ώστε να μην επιστρέφει ο χρήστης πίσω
                finish();
            }
        });
    }
}