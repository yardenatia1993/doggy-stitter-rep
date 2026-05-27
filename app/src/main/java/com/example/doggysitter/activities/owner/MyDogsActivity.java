package com.example.doggysitter.activities.owner;

import com.example.doggysitter.R;
import com.example.doggysitter.adapters.DogAdapter;
import com.example.doggysitter.models.Dog;
import com.example.doggysitter.repositories.DogRepository;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyDogsActivity extends AppCompatActivity implements DogAdapter.Listener {
    private LinearLayout emptyLayout;
    private RecyclerView dogsRecyclerView;
    private ProgressBar progressBar;
    private DogRepository dogRepository;
    private DogAdapter dogAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dogs);

        dogRepository = new DogRepository();
        emptyLayout = findViewById(R.id.layout_empty);
        dogsRecyclerView = findViewById(R.id.recycler_dogs);
        progressBar = findViewById(R.id.progress_bar);
        Button addDogButton = findViewById(R.id.button_add_dog);

        dogAdapter = new DogAdapter(this);
        dogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogsRecyclerView.setAdapter(dogAdapter);

        addDogButton.setOnClickListener(view -> startActivity(new Intent(this, AddDogActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDogs();
    }

    private void loadDogs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.error_login_required, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        dogRepository.getDogsForOwner(currentUser.getUid())
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<Dog> dogs = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        Dog dog = documentSnapshot.toObject(Dog.class);
                        if (dog != null) {
                            if (dog.getId() == null || dog.getId().trim().isEmpty()) {
                                dog.setId(documentSnapshot.getId());
                            }
                            dogs.add(dog);
                        }
                    }

                    dogAdapter.submitList(dogs);
                    emptyLayout.setVisibility(dogs.isEmpty() ? View.VISIBLE : View.GONE);
                    dogsRecyclerView.setVisibility(dogs.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_load_dogs, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditDog(Dog dog) {
        Intent intent = new Intent(this, AddDogActivity.class);
        intent.putExtra(AddDogActivity.EXTRA_DOG_ID, dog.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteDog(Dog dog) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_dog_title)
                .setMessage(R.string.delete_dog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteDog(dog))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteDog(Dog dog) {
        progressBar.setVisibility(View.VISIBLE);
        dogRepository.deleteDog(dog.getId())
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.dog_deleted, Toast.LENGTH_SHORT).show();
                    loadDogs();
                })
                .addOnFailureListener(error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_delete_dog, Toast.LENGTH_SHORT).show();
                });
    }
}
