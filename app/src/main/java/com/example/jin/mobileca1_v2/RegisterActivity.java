package com.example.jin.mobileca1_v2;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mFirstNameEditText;

    private EditText mPasswordEditText;
    private EditText mRetypePasswordEditText;
    private EditText mEmailEditText;
    private Button mRegisterBtn;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        mFirstNameEditText = (EditText) findViewById(R.id.et_name);
        mNameEditText = (EditText) findViewById(R.id.et_name2);
        mPasswordEditText = (EditText) findViewById(R.id.et_password);
        mRetypePasswordEditText = (EditText) findViewById(R.id.et_repassword);
        mEmailEditText = (EditText) findViewById(R.id.et_email);
        mRegisterBtn = (Button) findViewById(R.id.btn_register);
        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Log.e("1",mNameEditText.getText().toString());
                Log.e("2",mPasswordEditText.getText().toString());
                Log.e("3",mEmailEditText.getText().toString());
                addDataToDatabase();
            }
        });

    }
    private void addDataToDatabase(){
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", mFirstNameEditText.getText().toString());
        user.put("lastName", mNameEditText.getText().toString());
        user.put("email", mEmailEditText.getText().toString());
        user.put("password", mPasswordEditText.getText().toString());
//
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("Tage 1", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("Tag 2", "Error adding document", e);
//                    }
//                });
        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });

    }

    public static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }
}
