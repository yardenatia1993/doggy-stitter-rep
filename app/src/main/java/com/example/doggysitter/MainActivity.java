package com.example.doggysitter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button btnLogout,btn_test;
    TextView welcome;
    EditText name,age,breed,owner_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitViews();
        HandleLogout();

        Intent intent = getIntent();


        ChangeText();

        
    }
    private void InitViews() {
        btnLogout = findViewById(R.id.btn_logout);
        btn_test=findViewById(R.id.btn_test);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        breed = findViewById(R.id.breed);
        owner_id = findViewById(R.id.owner_id);
        welcome = findViewById(R.id.welcome);
    }

    private void HandleLogout() {
        btnLogout.setOnClickListener(v -> {
            CreateDialog();
        });
    }

    private void CreateDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want to logout?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
        alert.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        alert.show();
    }

    private void ChangeText(){
        welcome.setText(String.format("Welcome %s %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
    }




}