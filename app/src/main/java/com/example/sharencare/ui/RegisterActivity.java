package com.example.sharencare.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static android.text.TextUtils.isEmpty;
import static com.example.sharencare.utils.Check.doStringsMatch;

public class RegisterActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    //widgets
    private EditText mEmail, mPassword, mConfirmPassword, username, mobile_number, registration_number, vehicle_name;
    private ProgressBar mProgressBar;
    String driver_rider = "Rider";
    Switch aSwitch;

    //vars
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        mEmail = (EditText) findViewById(R.id.input_email);
        username = (EditText) findViewById(R.id.input_fullname);
        mPassword = (EditText) findViewById(R.id.input_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        mobile_number = (EditText) findViewById(R.id.mob_number);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        registration_number = findViewById(R.id.registration_number);
        vehicle_name = findViewById(R.id.vehicle_name);
        aSwitch = findViewById(R.id.driver_rider);
        aSwitch.setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        hideSoftKeyboard();
    }

    /**
     * Register a new email and password to Firebase Authentication
     *
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, String password) {

        showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            //insert some default data
                            User user = new User();
                            user.setEmail(email);
                            user.setVehicle_name(vehicle_name.getText().toString());
                            user.setUsername(username.getText().toString());
                            user.setUser_id(FirebaseAuth.getInstance().getUid());
                            user.setDriver_or_rider(driver_rider);
                            user.setMobile_number(mobile_number.getText().toString());
                            user.setRegistration_number(registration_number.getText().toString());
                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                            mDb.setFirestoreSettings(settings);
                            DocumentReference newUserRef = mDb
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid());
                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    hideDialog();
                                    if (task.isSuccessful()) {
                                        getToken();
                                        redirectDetailsScreen();
                                    } else {
                                        View parentLayout = findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                            hideDialog();
                        }

                        // ...
                    }
                });
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectDetailsScreen() {
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View view) {
        driver_rider = "Rider";
        switch (view.getId()) {
            case R.id.driver_rider: {
                if (aSwitch.isChecked()) {
                    driver_rider = "Driver";
                } else {
                    driver_rider = "Rider";
                }

                Log.d(TAG, "onClick: " + driver_rider);
                break;
            }
            case R.id.btn_register: {
                Log.d(TAG, "onClick: attempting to register.");

                //check for null valued EditText fields
                if (!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())
                        && !isEmpty(mobile_number.getText().toString())) {

                    //check if passwords match
                    if (doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                        //Initiate registration task
                        if (driver_rider.equals("Driver") && !isEmpty(registration_number.getText().toString()) || driver_rider.equals("Rider") && isEmpty(registration_number.getText().toString()))
                            registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
                        else {
                            Toast.makeText(this, "Please provide Registration Number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void getToken() {
        Log.d(TAG, "getToken: called");
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    Log.d(TAG, "onComplete: Token: " + token);
                    sendResgistrationTokenToServer(token);
                }
            }
        });
    }

    private void sendResgistrationTokenToServer(String token) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference reference = db.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.update("token", token).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Token Send to FireStore ");
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "sendResgistrationTokenToServer: Error " + e.getMessage());
        }
    }
}
