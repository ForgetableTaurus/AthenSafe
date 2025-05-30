package com.example.myapplication;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import java.util.List;

public class AutocompleteAdapter extends RecyclerView.Adapter<AutocompleteAdapter.AutocompleteViewHolder> {

    private List<AutocompletePrediction> predictions;
    private OnItemClickListener listener;

    public AutocompleteAdapter(List<AutocompletePrediction> predictions) {
        this.predictions = predictions;
    }

    @Override
    public AutocompleteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new AutocompleteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AutocompleteViewHolder holder, int position) {
        AutocompletePrediction prediction = predictions.get(position);
        holder.textView.setText(prediction.getFullText(null));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(prediction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public void setPredictions(List<AutocompletePrediction> predictions) {
        this.predictions = predictions;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(AutocompletePrediction prediction);
    }

    public static class AutocompleteViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public AutocompleteViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}