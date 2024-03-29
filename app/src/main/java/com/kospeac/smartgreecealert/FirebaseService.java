package com.kospeac.smartgreecealert;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


interface FirebaseListener
{
    public void onStatusChanged(String newStatus);
}
public class FirebaseService {
   private static FirebaseService instance = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference().child("users");

    private String FCMToken;
    private String KEY_USERS = "users";

    private FirebaseListener listener;
    public List<EventModel> eventsList;
    public List<EventModel> eventsUserList;

    private FirebaseService() { //singleton class
    }


    public static FirebaseService getInstance(){
        if(instance == null){
            instance = new FirebaseService();
        }
        return instance;
    }


    //getFCMToken

    public void getFCMToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.mainActivity,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                FCMToken = instanceIdResult.getToken();
                System.out.println("FCM" + FCMToken);
            }
        });
    }

    //insertEvent

    public void insertEvent(EventModel event){
        usersRef.child(FCMToken).push().setValue(event);
    }

    // getEvents

    public void getEvents(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsList = deconstructData(dataSnapshot);
                setFirebaseStatus("allEvents");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // getUserEvents

    public void getUserEvents(){
        usersRef.child(FCMToken).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsUserList = deconstructUserData(dataSnapshot);
                setFirebaseStatus("userEvents");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private List<EventModel> deconstructUserData(DataSnapshot dataSnapshot) {
        List<EventModel> events = new ArrayList<>();
        for(DataSnapshot perEvent: dataSnapshot.getChildren() ){
            events.add(perEvent.getValue(EventModel.class));
        }
        return events;
    }

    private List<EventModel> deconstructData(DataSnapshot dataSnapshot){
        List<EventModel> events = new ArrayList<>();
        for(DataSnapshot perUser: dataSnapshot.getChildren() ){
            for(DataSnapshot perEvent: perUser.getChildren() ){
                events.add(perEvent.getValue(EventModel.class));
            }
        }
        return events;
    }

    // FirebaseStatus

    public void setFirebaseStatus(String onCompleteStatus){
        if(listener !=null){
            listener.onStatusChanged(onCompleteStatus);
        }
    }

    //setFirebaseListener
    
    public void setFirebaseListener(FirebaseListener listener){
        this.listener = listener;
    }

}
