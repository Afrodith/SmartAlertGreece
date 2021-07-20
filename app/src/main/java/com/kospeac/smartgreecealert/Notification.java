package com.kospeac.smartgreecealert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.os.Handler;

/* Notification κλαση
    Ειδοποιει αλλους χρηστες με την αποστολη sms
*  */


public class Notification {
    static Activity mainActivity;
    String[] contacts;
    SmsManager smsManager;
    private Speech tts;

    public Notification(Activity activity) {
        mainActivity = activity;
        getContacts();
        smsManager = SmsManager.getDefault();
    }

    /* getMessage
     *   Αποστολη SMS
     *  */
    public void sendNotification(String notificationType,String lat,String lon, String time){
        String msg = getMessage(notificationType,lat,lon,time);

        if(contacts != null && contacts.length >0) {
            for (String contact : contacts) {
                System.out.println("SENT");
                smsManager.sendTextMessage(contact, null, msg, null, null);
            }
        }
        Toast.makeText(mainActivity, msg, Toast.LENGTH_LONG).show();
    }


    private String getMessage(String notificationType,String lat,String lon, String time){
        String type ="";
        if(notificationType == "SOS"){
            textToSpeech(notificationType);
            type = mainActivity.getResources().getString(R.string.typeSOS);
        }else if(notificationType == "FIRE"){
            textToSpeech(notificationType);
            type = mainActivity.getResources().getString(R.string.typeFire);
            return type + ". " + mainActivity.getResources().getString(R.string.helpMsgFire,lat,lon,time);
        }else if(notificationType == "fallDetection"){
            type = mainActivity.getResources().getString(R.string.typeFall);
        }else if(notificationType == "AbortSOS"){
            type = mainActivity.getResources().getString(R.string.typeAbort);
            return type + mainActivity.getResources().getString(R.string.abortMsg,lat,lon,time);
        }

        return type + ". " + mainActivity.getResources().getString(R.string.helpMsg,lat,lon,time);

    }



    private void getContacts(){
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        Set<String> numberList = new HashSet<String>();
        numberList.add("6981173500");
        numberList.add("6979016979");
        Set<String> contacts = sharedPref.getStringSet("ContactNumbers", numberList);
        if(contacts != null && !contacts.isEmpty())
        this.contacts = contacts.toArray(new String[contacts.size()]);

    }



    // textToSpeech

    private void textToSpeech(final String notificationType){
        final Handler handler = new Handler();
        tts = new Speech(mainActivity.getApplicationContext());
        for(int i=1; i<=3; i++){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(notificationType ==  "SOS") {
                        tts.speak("Help me, i'm in danger!");
                    }
                    else if(notificationType == "FIRE")
                    {
                        tts.speak("Help me,there is a fire near me!");
                    }
                }
            }, 2000 * i);
        }
    }

}
