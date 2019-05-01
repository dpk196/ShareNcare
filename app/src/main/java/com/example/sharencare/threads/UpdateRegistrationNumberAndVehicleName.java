package com.example.sharencare.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sharencare.Interfaces.RegistrationAndVehicleNameInterFace;
import com.example.sharencare.Models.User;
import com.example.sharencare.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;

public class UpdateRegistrationNumberAndVehicleName extends AsyncTask<Void,Void, User> {
    private static final String TAG = "UpdateRegistrationNumbe";
    private boolean flag_vehicle=false;
    private boolean flag_register=false;
    private boolean flag_updated_user=false;
    private String userId;
    private String vehicleName;
    private  String registrationNumber;
    private  User user;
    private WeakReference<RegistrationAndVehicleNameInterFace> faceWeakReference;

    public UpdateRegistrationNumberAndVehicleName(String userId, String vehicleName, String registrationNumber, RegistrationAndVehicleNameInterFace reference) {
        this.userId = userId;
        this.vehicleName = vehicleName;
        this.registrationNumber = registrationNumber;
        faceWeakReference=new WeakReference<>(reference);
    }

    @Override
    protected User doInBackground(Void... voids) {
         update();
         return user;
    }
    private  void update(){
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection("Users").document(userId);
        try {
            reference.update("vehicle_name", vehicleName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Vehicle name updated ");

                    }
                flag_vehicle=true;
                }


            });
        }catch (Exception e){
            Log.d(TAG, "sendRegistrationTokenToServer: User is not signed in Token cannt be refreshed "+e.getMessage());
        }
        try{
            reference.update("registration_number",registrationNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: Registration Number Updated");
                    }
                    flag_register=true;
                }

            });

        }catch (Exception e){
            Log.d(TAG, "update: "+e.getMessage());
        }
        while (flag_register!=true&&flag_vehicle!=true){
            Log.d(TAG, "update: Waiting for update");
        };
       getUpdatedUserObject();

    }

    private void getUpdatedUserObject() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
        FirebaseFirestore mDb  =FirebaseFirestore.getInstance();
        mDb.setFirestoreSettings(settings);
        CollectionReference userCollectionReference = mDb.collection("Users");
        Query userQuery = userCollectionReference.whereEqualTo("user_id", userId);
        userQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot :task.getResult()){
                        user=snapshot.toObject(User.class);
                        Log.d(TAG, "onComplete: Updated User"+user.toString());
                    }

                }
                flag_updated_user=true;
            }
        });
        while(flag_updated_user!=true){
            Log.d(TAG, "getUpdatedUserObject: Waiting for updated user Object");
        };

    }

    @Override
    protected void onPostExecute(User user) {
        faceWeakReference.get().registrationNumberAndVehicleNameUpdated(user);
    }
}
