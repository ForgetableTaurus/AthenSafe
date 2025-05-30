package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Ο `MessageAdapter` είναι ένας προσαρμογέας (Adapter) για το `RecyclerView` που χρησιμοποιείται
 * για την εμφάνιση λιστών μηνυμάτων. Υποστηρίζει δύο διαφορετικούς τύπους προβολών:
 * μηνύματα από τον χρήστη και μηνύματα από το bot.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Σταθερές για τον καθορισμό του τύπου προβολής για τα μηνύματα του χρήστη και του bot
    private static final int USER_VIEW_TYPE = 1;
    private static final int BOT_VIEW_TYPE = 2;

    // Η λίστα των μηνυμάτων που θα εμφανιστούν στο RecyclerView
    private final List<Message> messageList;

    /**
     * Κατασκευαστής για τον `MessageAdapter`.
     *
     * @param messageList Η λίστα των μηνυμάτων που θα διαχειριστεί ο προσαρμογέας.
     */
    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    /**
     * Επιστρέφει τον τύπο της προβολής (view type) για το στοιχείο στην καθορισμένη θέση.
     * Χρησιμοποιεί την ιδιότητα `isUser()` του αντικειμένου `Message` για να καθορίσει
     * αν το μήνυμα είναι από τον χρήστη ή από το bot.
     *
     * @param position Η θέση του στοιχείου στη λίστα δεδομένων.
     * @return `USER_VIEW_TYPE` αν το μήνυμα είναι από τον χρήστη, `BOT_VIEW_TYPE` αν είναι από το bot.
     */
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? USER_VIEW_TYPE : BOT_VIEW_TYPE;
    }

    /**
     * Δημιουργεί ένα νέο `ViewHolder` ανάλογα με τον τύπο της προβολής.
     * Εμφανίζει το κατάλληλο layout (item_user_message ή item_bot_message)
     * και επιστρέφει μια νέα παρουσία του αντίστοιχου `ViewHolder`.
     *
     * @param parent   Το `ViewGroup` στο οποίο θα προστεθεί η νέα προβολή.
     * @param viewType Ο τύπος της προβολής που θα δημιουργηθεί.
     * @return Ένα νέο `ViewHolder` που περιέχει την προβολή για τον τύπο προβολής.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == USER_VIEW_TYPE) {
            // Φούσκωμα του layout για τα μηνύματα του χρήστη
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            // Φούσκωμα του layout για τα μηνύματα του bot
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    /**
     * Συνδέει τα δεδομένα του μηνύματος στην προβολή που κατέχεται από το `ViewHolder` στην καθορισμένη θέση.
     * Ανάλογα με τον τύπο του `ViewHolder` (UserMessageViewHolder ή BotMessageViewHolder),
     * καλεί την κατάλληλη μέθοδο `bind` για να εμφανίσει το μήνυμα.
     *
     * @param holder   Το `ViewHolder` που θα ενημερωθεί για να αντιπροσωπεύσει το στοιχείο στην καθορισμένη θέση.
     * @param position Η θέση του στοιχείου στη λίστα δεδομένων.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof UserMessageViewHolder) { // Αν από χρήστη
            // Σύνδεση δεδομένων για μήνυμα χρήστη
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) { // Αν από bot
            // Σύνδεση δεδομένων για μήνυμα bot
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    /**
     * Επιστρέφει τον συνολικό αριθμό των στοιχείων στη λίστα δεδομένων που διαχειρίζεται ο προσαρμογέας.
     *
     * @return Ο αριθμός των μηνυμάτων στη λίστα.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Μια εσωτερική στατική κλάση που επεκτείνει το `RecyclerView.ViewHolder` για την αναπαράσταση
     * των μηνυμάτων του χρήστη στην προβολή. Περιέχει ένα `TextView` για το κείμενο του μηνύματος
     * και ένα `ImageView` για την εικόνα του χρήστη.
     */
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        /**
         * Κατασκευαστής για το `UserMessageViewHolder`.
         *
         * @param itemView Η προβολή ενός στοιχείου μηνύματος χρήστη.
         */
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textUser);
            imageView = itemView.findViewById(R.id.imageUser); // Αντιστοίχιση του ImageView από το layout
        }

        /**
         * Συνδέει ένα αντικείμενο `Message` (που υποτίθεται ότι είναι από τον χρήστη)
         * στην προβολή. Θέτει το κείμενο του μηνύματος στο `TextView` και την εικόνα του χρήστη στο `ImageView`.
         *
         * @param message Το αντικείμενο `Message` που θα εμφανιστεί.
         */
        public void bind(Message message) {
            textView.setText(message.getText());
            imageView.setImageResource(R.drawable.user);
        }
    }

    /**
     * Μια εσωτερική στατική κλάση που επεκτείνει το `RecyclerView.ViewHolder` για την αναπαράσταση
     * των μηνυμάτων του bot στην προβολή. Περιέχει ένα `TextView` για το κείμενο του μηνύματος
     * και ένα `ImageView` για την εικόνα του bot.
     */
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        /**
         * Κατασκευαστής για το `BotMessageViewHolder`.
         *
         * @param itemView Η προβολή ενός στοιχείου μηνύματος bot.
         */
        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textBot);
            imageView = itemView.findViewById(R.id.imageBot); // Αντιστοίχιση του ImageView από το layout
        }

        /**
         * Συνδέει ένα αντικείμενο `Message` (που υποτίθεται ότι είναι από το bot)
         * στην προβολή. Θέτει το κείμενο του μηνύματος στο `TextView` και την εικόνα του bot στο `ImageView`.
         *
         * @param message Το αντικείμενο `Message` που θα εμφανιστεί.
         */
        public void bind(Message message) {
            textView.setText(message.getText());
            imageView.setImageResource(R.drawable.ic_bot); // Φόρτωση της εικόνας bot (βεβαιωθείτε ότι υπάρχει αυτό το drawable)
        }
    }
}