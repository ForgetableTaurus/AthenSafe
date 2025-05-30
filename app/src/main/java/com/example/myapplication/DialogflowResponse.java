package com.example.myapplication;

/**
 * Αντιπροσωπεύει την απάντηση που επιστρέφει το Dialogflow API.
 */
public class DialogflowResponse {

    // Το κύριο αντικείμενο που περιέχει τις πληροφορίες της απάντησης
    private QueryResult queryResult;

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(QueryResult queryResult) {
        this.queryResult = queryResult;
    }

    /**
     * Εσωτερική κλάση που αντιστοιχεί στο πεδίο "queryResult" της απάντησης.
     */
    public static class QueryResult {
        // Το ακριβές κείμενο που στάλθηκε από τον χρήστη
        private String queryText;

        // Η action που ταυτοποιήθηκε από το Dialogflow (αν έχει οριστεί στο intent)
        private String action;

        // Το κείμενο που επιστρέφεται από το Dialogflow για εμφάνιση στον χρήστη
        private String fulfillmentText;

        public String getQueryText() {
            return queryText;
        }

        public void setQueryText(String queryText) {
            this.queryText = queryText;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getFulfillmentText() {
            return fulfillmentText;
        }

        public void setFulfillmentText(String fulfillmentText) {
            this.fulfillmentText = fulfillmentText;
        }
    }
}