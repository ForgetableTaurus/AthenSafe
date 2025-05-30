package com.example.myapplication;

/**
 * Η κλάση `Message` αντιπροσωπεύει ένα απλό μήνυμα που μπορεί να σταλεί ή να ληφθεί
 * σε μια συνομιλία. Κάθε μήνυμα περιέχει το κείμενο του μηνύματος και μια boolean τιμή
 * που υποδεικνύει αν το μήνυμα στάλθηκε από τον χρήστη ή από κάποια άλλη πηγή (π.χ., ένα bot).
 */
public class Message {
    private String text; // Το κείμενο του μηνύματος
    private boolean isUser; // Μια σημαία που υποδεικνύει αν το μήνυμα στάλθηκε από τον χρήστη (true) ή όχι (false)

    /**
     * Κατασκευαστής για την κλάση `Message`.
     *
     * @param text   Το κείμενο του μηνύματος.
     * @param isUser true αν το μήνυμα στάλθηκε από τον χρήστη, false διαφορετικά.
     */
    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    /**
     * Επιστρέφει το κείμενο του μηνύματος.
     *
     * @return Η συμβολοσειρά που περιέχει το κείμενο του μηνύματος.
     */
    public String getText() {
        return text;
    }

    /**
     * Επιστρέφει true αν το μήνυμα στάλθηκε από τον χρήστη, false διαφορετικά.
     *
     * @return true αν το μήνυμα είναι από τον χρήστη, false αν όχι.
     */
    public boolean isUser() {
        return isUser;
    }
}