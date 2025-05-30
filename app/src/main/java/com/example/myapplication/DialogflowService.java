package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface για την αποστολή μηνυμάτων στο Dialogflow API
 * χρησιμοποιώντας την μέθοδο detectIntent.
 */
public interface DialogflowService {

    // Ορίζουμε το content-type ως JSON
    @Headers("Content-Type: application/json")

    /**
     * Κάνει POST αίτημα στο Dialogflow detectIntent endpoint.
     *
     * @param authHeader Το header "Authorization", π.χ. "Bearer <access_token>"
     * @param projectId Το ID του Dialogflow project
     * @param sessionId Το ID της συνεδρίας (π.χ. τυχαίο UUID ανά χρήστη)
     * @param body Το σώμα της αίτησης σε μορφή DialogflowRequest
     * @return Ένα Call αντικείμενο που επιστρέφει DialogflowResponse
     */
    @POST("v2/projects/{projectId}/agent/sessions/{sessionId}:detectIntent")
    Call<DialogflowResponse> sendMessage(
            @Header("Authorization") String authHeader,
            @Path("projectId") String projectId,
            @Path("sessionId") String sessionId,
            @Body DialogflowRequest body
    );
}