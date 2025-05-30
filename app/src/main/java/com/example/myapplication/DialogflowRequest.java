package com.example.myapplication;

/**
 * Αντιπροσωπεύει το σώμα του JSON αιτήματος προς το Dialogflow API.
 */
public class DialogflowRequest {

    private QueryInput queryInput;

    /**
     * Constructor που δημιουργεί ένα αίτημα με δεδομένο κείμενο και γλώσσα.
     *
     * @param text Το μήνυμα του χρήστη που θα σταλεί στο Dialogflow.
     * @param languageCode Η γλώσσα του μηνύματος (π.χ. "en" ή "el").
     */
    public DialogflowRequest(String text, String languageCode) {
        this.queryInput = new QueryInput(new TextInput(text, languageCode));
    }

    // Getter για το queryInput (χρήσιμο στο Retrofit)
    public QueryInput getQueryInput() {
        return queryInput;
    }

    // Setter για το queryInput -Αν αλλάξουμε μετά το input
    public void setQueryInput(QueryInput queryInput) {
        this.queryInput = queryInput;
    }

    /**
     * Εσωτερική κλάση που αναπαριστά το πεδίο "queryInput" στο JSON του Dialogflow.
     */
    public static class QueryInput {
        private TextInput text;

        public QueryInput(TextInput text) {
            this.text = text;
        }

        public TextInput getText() {
            return text;
        }

        public void setText(TextInput text) {
            this.text = text;
        }
    }

    /**
     * Εσωτερική κλάση που αναπαριστά το αντικείμενο "text" μέσα στο "queryInput".
     * Περιέχει το κείμενο του χρήστη και τον κωδικό γλώσσας.
     */
    public static class TextInput {
        private String text;
        private String languageCode;

        public TextInput(String text, String languageCode) {
            this.text = text;
            this.languageCode = languageCode;
        }

        public String getText() {
            return text;
        }

        public String getLanguageCode() {
            return languageCode;
        }
    }
}