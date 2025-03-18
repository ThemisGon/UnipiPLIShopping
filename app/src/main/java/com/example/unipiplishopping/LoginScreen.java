package com.example.unipiplishopping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    EditText email,password;
    Button loginButton;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Διαδρομή όπου είναι αποθηκευμένοι οι χρήστες στη Firebase
        prefs = getSharedPreferences("userPrefs", MODE_PRIVATE); // SharedPreferences για αποθήκευση του fullName


        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        //Βάζουμε την ενέργεια του κουμπιού με listener αντί να την βάλουμε απο το xml αρχείο απευθείας στο κουμπί
        loginButton.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(LoginScreen.this, "Fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            fetchAndSaveUserName(userId); // Καλούμε τη μέθοδο που αποθηκεύει το fullName
                        }
                    } else {
                        Toast.makeText(LoginScreen.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fetchAndSaveUserName(String userId) {
        usersRef.child(userId).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.getValue(String.class);

                    // Αποθήκευση του fullName στα SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("fullName", fullName);
                    editor.apply();

                    // Εμφάνιση μηνύματος επιτυχίας
                    Toast.makeText(LoginScreen.this, "Welcome " + fullName + "!", Toast.LENGTH_SHORT).show();

                    // Μετάβαση στην οθόνη προϊόντων
                    startActivity(new Intent(LoginScreen.this, Products.class));
                    finish();
                } else {
                    Toast.makeText(LoginScreen.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginScreen.this, "Error fetching user data!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}