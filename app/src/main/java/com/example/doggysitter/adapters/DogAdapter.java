package com.example.doggysitter.adapters;

import com.example.doggysitter.R;
import com.example.doggysitter.models.Dog;
import com.example.doggysitter.utils.DateTimeUtils;
import com.example.doggysitter.utils.ValidationUtils;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {
    public interface Listener {
        void onEditDog(Dog dog);

        void onDeleteDog(Dog dog);
    }

    private final Listener listener;
    private final List<Dog> dogs = new ArrayList<>();

    public DogAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Dog> newDogs) {
        dogs.clear();
        dogs.addAll(newDogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dog, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog dog = dogs.get(position);
        holder.nameTextView.setText(dog.getName());
        holder.breedTextView.setText(dog.getBreed());
        holder.ageTextView.setText(DateTimeUtils.formatDogAge(dog));

        String notes = ValidationUtils.normalizeSpaces(dog.getNotes());
        holder.notesTextView.setVisibility(TextUtils.isEmpty(notes) ? View.GONE : View.VISIBLE);
        holder.notesTextView.setText(notes);

        holder.editButton.setOnClickListener(view -> listener.onEditDog(dog));
        holder.deleteButton.setOnClickListener(view -> listener.onDeleteDog(dog));
    }

    @Override
    public int getItemCount() {
        return dogs.size();
    }

    static class DogViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView breedTextView;
        private final TextView ageTextView;
        private final TextView notesTextView;
        private final Button editButton;
        private final Button deleteButton;

        DogViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_dog_name);
            breedTextView = itemView.findViewById(R.id.text_dog_breed);
            ageTextView = itemView.findViewById(R.id.text_dog_age);
            notesTextView = itemView.findViewById(R.id.text_dog_notes);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
