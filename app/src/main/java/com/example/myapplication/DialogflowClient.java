package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Πελάτης για επικοινωνία με την υπηρεσία Dialogflow χρησιμοποιώντας Retrofit και εξουσιοδότηση OAuth2.
 */
public class DialogflowClient {
    private static DialogflowService instance; // Singleton Retrofit service
    private static String accessToken; // Το access token που χρησιμοποιείται για την εξουσιοδότηση
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Εκτελεστής για async εργασίες
    private static Future<String> accessTokenFuture; // Χρησιμοποιείται για την ανάκτηση του token ασύγχρονα

    /**
     * Επιστρέφει singleton instance του DialogflowService με εξουσιοδότηση μέσω Interceptor.
     */
    public static DialogflowService getInstance(Context context) {
        if (instance == null) {
            Log.d("DialogflowClient", "Δημιουργία νέου Retrofit instance.");

            // Ορισμός Interceptor για προσθήκη header Authorization με το token
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        // Λήψη του token συγχρονισμένα
                        String token = getAccessTokenSynchronously(context);
                        Log.d("DialogflowClient", "Interceptor: Λήφθηκε token (synchronous): " + token);

                        if (token != null) {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + token)
                                    .build();

                            Log.d("DialogflowClient", "Interceptor: Προσθήκη Authorization header με token: " +
                                    token.substring(0, Math.min(token.length(), 20)) + "...");
                            return chain.proceed(request);
                        } else {
                            Log.e("DialogflowClient", "Interceptor: Το token είναι null, η αυθεντικοποίηση απέτυχε!");
                            throw new IllegalStateException("Authentication failed");
                        }
                    })
                    .build();

            // Ρύθμιση Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("") // Βάση URL για τις Dialogflow API κλήσεις
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(DialogflowService.class);
            Log.d("DialogflowClient", "Retrofit instance δημιουργήθηκε: " + instance);
        } else {
            Log.d("DialogflowClient", "Επιστροφή υπάρχοντος Retrofit instance: " + instance);
        }
        return instance;
    }

    /**
     * Συγχρονική μέθοδος για λήψη access token από το JSON key.
     * Χρησιμοποιείται κυρίως από το Interceptor.
     */
    private static String getAccessTokenSynchronously(Context context) {
        try {
            if (accessToken != null) {
                Log.d("DialogflowClient", "getAccessTokenSynchronously: Επιστροφή υπάρχοντος token: " +
                        accessToken.substring(0, Math.min(accessToken.length(), 20)) + "...");
                return accessToken;
            }

            // Άνοιγμα του JSON αρχείου των credentials
            InputStream stream = context.getAssets().open("");
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Collections.singleton("")); // Scope για Dialogflow

            credentials.refreshIfExpired();
            accessToken = credentials.getAccessToken().getTokenValue();

            Log.d("DialogflowClient", "getAccessTokenSynchronously: Λήφθηκε νέο token: " +
                    accessToken.substring(0, Math.min(accessToken.length(), 20)) + "...");
            return accessToken;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ασύγχρονη λήψη token
     * Αν το token υπάρχει, επιστρέφεται άμεσα. Διαφορετικά, δημιουργείται για ανάκτηση.
     */
    public static String getAccessToken(Context context) {
        if (accessToken == null && accessTokenFuture == null) {
            Log.d("DialogflowClient", "getAccessToken: Δεν υπάρχει token, υποβολή εργασίας για λήψη.");
            accessTokenFuture = executorService.submit(() -> {
                try {
                    InputStream stream = context.getAssets().open("");
                    GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                            .createScoped(Collections.singleton(""));

                    credentials.refreshIfExpired();
                    String token = credentials.getAccessToken().getTokenValue();
                    Log.d("DialogflowClient", "getAccessToken (ExecutorService): Λήφθηκε token: " +
                            token.substring(0, Math.min(token.length(), 20)) + "...");
                    return token;
                } catch (Exception e) {
                    return null;
                }
            });
        } else if (accessToken != null) {
            Log.d("DialogflowClient", "getAccessToken: Επιστροφή υπάρχοντος token: " +
                    accessToken.substring(0, Math.min(accessToken.length(), 20)) + "...");
            return accessToken;
        } else {
            Log.d("DialogflowClient", "getAccessToken: Το token είναι σε διαδικασία λήψης.");
        }

        try {
            if (accessTokenFuture != null && !accessTokenFuture.isDone()) {
                // Περιμένει την ολοκλήρωση του Future (προσοχή: blocking call)
                accessToken = accessTokenFuture.get();
                Log.d("DialogflowClient", "getAccessToken: Λήφθηκε token από Future: " +
                        accessToken.substring(0, Math.min(accessToken.length(), 20)) + "...");
            }
        } catch (Exception e) {
            return null;
        }

        return accessToken;
    }
}