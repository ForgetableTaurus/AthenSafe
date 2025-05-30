package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editText;
    ImageButton sendBtn;
    MessageAdapter adapter;
    List<Message> messageList;
    Toolbar toolbar;

    // To ID του Dialogflow project
    // To ID του Dialogflow project
    String projectId = "";
    String sessionId; // Χρησιμοποιείται για τη μοναδική αναγνώριση της συνεδρίας

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Αρχικοποίηση και ρύθμιση του Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ρυθμίσεις ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Κουμπί επιστροφής
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Συνομιλία"); // Τίτλος της οθόνης
        }

        // Χειριστής για το κουμπί επιστροφής
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Συνδέουμε τα views με τα αντίστοιχα IDs στο layout
        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit_text);
        sendBtn = findViewById(R.id.send_btn);

        // Αρχικοποίηση της λίστας μηνυμάτων και του adapter
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Δημιουργία μοναδικής συνεδρίας
        sessionId = UUID.randomUUID().toString();
        Log.d("ChatActivity", "Δημιουργήθηκε νέα session ID: " + sessionId);
        // Προσθέτουμε το αρχικό μήνυμα από το chatbot
        String initialBotMessage = "Γεια! Είμαι ο προσωπικός σου βοηθός. Πώς μπορώ να σε βοηθήσω;";
        addMessage(initialBotMessage, false); // false για μήνυμα από το bot

        // Όταν ο χρήστης πατήσει αποστολή
        sendBtn.setOnClickListener(view -> {
            String userMsg = editText.getText().toString().trim();
            if (!userMsg.isEmpty()) {
                Log.d("ChatActivity", "Ο χρήστης έγραψε: " + userMsg);
                sendMessageToDialogflow(userMsg); // Στέλνουμε το μήνυμα στο Dialogflow
                addMessage(userMsg, true); // Προσθέτουμε το μήνυμα στο UI
                editText.setText(""); // Καθαρίζουμε το πεδίο κειμένου
            }
        });
    }

    // Μέθοδος αποστολής μηνύματος στο Dialogflow
    private void sendMessageToDialogflow(String message) {
        Log.d("ChatActivity", "Μέθοδος sendMessageToDialogflow κλήθηκε με μήνυμα: " + message);

        String token = DialogflowClient.getAccessToken(this); // Λήψη access token
        Log.d("ChatActivity", "Λήφθηκε token: " + token);

        DialogflowService service = DialogflowClient.getInstance(this); // Δημιουργία instance του service
        Log.d("ChatActivity", "Λήφθηκε instance του DialogflowService: " + service);

        // Δημιουργία του αιτήματος με γλώσσα τα Ελληνικά
        DialogflowRequest request = new DialogflowRequest(message, "el");

        // Logging για έλεγχο
        Gson gson = new Gson();
        String json = gson.toJson(request);
        Log.d("ChatActivity", "Project ID που χρησιμοποιείται: " + projectId);
        Log.d("ChatActivity", "Session ID που χρησιμοποιείται: " + sessionId);
        Log.d("Dialogflow REQUEST_HEADERS", "Authorization Header: Bearer " + token);

        // Δημιουργία και αποστολή του αιτήματος μέσω Retrofit
        Call<DialogflowResponse> call = service.sendMessage("Bearer " + token, projectId, sessionId, request);
        String requestUrl = call.request().url().toString();
        Log.d("RETROFIT_URL", "URL αιτήματος: " + requestUrl);

        // Callback για την επεξεργασία της απάντησης
        call.enqueue(new Callback<DialogflowResponse>() {
            @Override
            public void onResponse(Call<DialogflowResponse> call, Response<DialogflowResponse> response) {
                Log.d("ChatActivity", "Λήφθηκε απάντηση. Επιτυχής: " + response.isSuccessful() + ", Κωδικός: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getQueryResult().getFulfillmentText();
                    Log.d("DialogflowResponse", "Πλήρης απάντηση: " + new Gson().toJson(response.body()));

                    if (reply != null && !reply.isEmpty()) {
                        Log.d("DialogflowResponse", "Απάντηση: " + reply);
                        addMessage(reply, false); // Προσθέτουμε την απάντηση
                    } else {
                        Log.e("DialogflowResponse", "Κενό ή null κείμενο απάντησης.");
                        addMessage("Δεν λάβαμε καμία απάντηση από το Dialogflow.", false);
                    }
                } else {
                    // Χειρισμός σφαλμάτων
                    Log.e("Dialogflow", "Κωδικός σφάλματος: " + response.code());
                    Log.e("Dialogflow", "Μήνυμα: " + response.message());

                    if (response.code() == 401) {
                        addMessage("Αποτυχία αυθεντικοποίησης. Ελέγξτε το token.", false);
                    } else if (response.code() == 400) {
                        addMessage("Μη έγκυρο αίτημα. Ελέγξτε τη μορφή του αιτήματος.", false);
                    } else {
                        addMessage("Σφάλμα: " + response.message(), false);
                    }
                }
            }

            @Override
            public void onFailure(Call<DialogflowResponse> call, Throwable t) {
                Log.e("Dialogflow", "Αποτυχία σύνδεσης: " + t.getMessage(), t);
                addMessage("Αποτυχία σύνδεσης: " + t.getMessage(), false);
            }
        });
    }

    // Προσθέτει μήνυμα (χρήστη ή bot) στη λίστα και το εμφανίζει στο RecyclerView
    private void addMessage(String message, boolean isUser) {
        messageList.add(new Message(message, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
}