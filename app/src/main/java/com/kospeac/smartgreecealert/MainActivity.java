package com.kospeac.smartgreecealert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static Activity mainActivity;
    UsbService mUsbService = new UsbService();
    FirebaseService mFirebaseService;
    String type;
    Button sosBtn;
    Button fireBtn;
    Button abortBtn;
    public TextView mainTitle;
    public TextView sosTitle;
    CountDownTimer countDownTimer;
    CountDownTimer countDownSOS;
    boolean countDownTimerIsRunning = false;
    boolean sosStatus = false;
    private FallDetectionHandler falldetection;
    private SeismicDetectionHandler seismicdetection;

    private final static int REQUESTCODE = 325;
    LocationManager mLocationManager;
    Uri notification;
    Ringtone r;
    private LocationListener locationService;
    private Boolean prevStatus;
    Double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        mFirebaseService = FirebaseService.getInstance();
        mFirebaseService.getFCMToken(); //  generate FCM token - Firebase Messaging
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationService = new LocationService();

        sosBtn = findViewById(R.id.btn_sos);
        fireBtn = findViewById(R.id.btn_fire);
        abortBtn = findViewById(R.id.btn_abort);
        mainTitle = findViewById(R.id.main_title);
        sosTitle = findViewById(R.id.sos_text);
        sosBtn.setOnClickListener(this);
        fireBtn.setOnClickListener(this);
        abortBtn.setOnClickListener(this);
        checkPermissions();

        try {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.registerReceiver(mUsbService,new IntentFilter("android.hardware.usb.action.USB_STATE"));
        mUsbService.setOnUsbServiceStatusListener(new OnUsbServiseStatusListener() {
            @Override
            public void onStatusChanged(boolean newStatus) {
                if(newStatus){

                    if(prevStatus == null || prevStatus != newStatus ) {
                        prevStatus = newStatus;
                        type = "earthquakeEventDetected";
                        if (falldetection != null && FallDetectionHandler.getListenerStatus()) {
                            falldetection.unregisterListener();
                        }
                        mainTitle.setText(R.string.main_title2);
                        setupEarthquakeDetection(); // EarthquakeDetection
                    }
                }else {

                    if(prevStatus == null || prevStatus != newStatus ) {
                        prevStatus = newStatus;
                        type = "fallDetectionEvent";
                        if (seismicdetection != null && SeismicDetectionHandler.getListenerStatus()) {
                            System.out.println(seismicdetection);
                            seismicdetection.unregisterListener();
                        }
                        mainTitle.setText(R.string.main_title1);
                        setupFallDetection(); //  FallDetection
                    }
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() { // Κανουμε unregister τον broadcaster οταν φευγουμε απο το activity
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sos: //  SOS
                sosStatus = true;
                sosTitle.setText(R.string.sos_title);
                countDownSOS =  new CountDownTimer(20000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        sosTitle.setText("");
                        sosStatus = false;
                    }

                }.start();
                handleEvent("SOS");
                break;
            case R.id.btn_fire: // FIRE button
                sosTitle.setText(R.string.fire_title);
                countDownSOS =  new CountDownTimer(20000, 1000) {
                   public void onTick(long millisUntilFinished) {
                    }
                   public void onFinish() {
                       sosTitle.setText("");
                        sosStatus = false;
                    }

                 }.start();
                handleEvent("FIRE");
                break;
            case R.id.btn_abort: // κουμπι Abort
                if(type == "fallDetectionEvent" && countDownTimerIsRunning) {
                    cancelTimer();
                    Toast.makeText(this, "Aborted", Toast.LENGTH_LONG).show();
                    mainTitle.setText(R.string.main_title1);
                    falldetection.registerListener();
                } else if(sosStatus){ // αμα το sosStatus ειναι ενεργο δηλαδη εχει πατηθει το SOS button και δεν εχουν περασει τα 5 λεπτα που εχει ο χρηστης για να κανει ακυρωση
                    cancelSOSTimer();
                    handleEvent("AbortEvent");
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.topbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_statistics:
                Intent goToStatistics = new Intent(this,Statistics.class);
                startActivity(goToStatistics);  // Νεο acitvity Statistics
                return true;
            case R.id.menu_contacts:
                Intent goToContacts = new Intent(this,ContactsActivity.class);
                startActivity(goToContacts);  // Νεο acitvity Contacts
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void checkPermissions() {
        List<String> PERMISSIONS = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            System.out.println("GPS ENABLED");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                    locationService);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            PERMISSIONS.add(Manifest.permission.SEND_SMS);
        }

       if(!PERMISSIONS.isEmpty()){
           String[] array = PERMISSIONS.toArray(new String[PERMISSIONS.size()]);
           ActivityCompat.requestPermissions(this,
                   array,
                   REQUESTCODE);
       }

    }

//get location from the GPS service provider. Needs permission.
    protected void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = mLocationManager.getProviders(true);
            Location location = null;
            for(String provider : providers){
                Location l = mLocationManager.getLastKnownLocation(provider);
                location = l;
                if(location != null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    break;
                }
            }
            if (location == null) {
                latitude = -1.0;
                longitude = -1.0;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                            locationService);
                    getLocation();

                }

            }
        }
    }


    /* setupFallDetection
    *  Create object and  listener (from device accelerometer) when we are in fallDetection state.
    *  When status is true we have a user fall and we deactivate/unregister the listener
    *  and we enable a CountDownTimer of 30 secs in order to abort event.
    *    */
    private void setupFallDetection() {
        falldetection = new FallDetectionHandler(this);
        falldetection.setFallDetectionListener(new FallDetectionListener() {
            @Override
            public void onStatusChanged(boolean fallDetectionStatus) {
                if(fallDetectionStatus) {
                    falldetection.unregisterListener();
                    countDownTimerIsRunning = true;
                    countDownTimer =  new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) { // καθε δευτερολεπτο αλλαζουμε το UI για να εμφανιζεται η αντιστροφη μετρηση
                            r.play();
                            mainTitle.setText(Long.toString(millisUntilFinished / 1000));
                        }

                        public void onFinish() { // οταν τελειωσει ο timer ξανακανουμε register τον listener και γινεται διαχεριση του event
                            countDownTimerIsRunning = false;
                            r.stop();
                            mainTitle.setText(R.string.main_title1);
                            falldetection.registerListener();
                            handleEvent("fallDetectionEvent");
                        }

                    }.start();
                }
            }
        });
    }

    private void cancelTimer(){ //ακυρωση timer για το fall detection
        countDownTimer.cancel();
        r.stop();
    }

    private void cancelSOSTimer(){ //ακυρωση timer για το SOS button
        countDownSOS.onFinish();
        countDownSOS.cancel();
    }

    // setupEarthquakeDetection

    private void setupEarthquakeDetection() {
        seismicdetection = new SeismicDetectionHandler(this);
        seismicdetection.setSeismicDetectionListener(new SeismicDetectionListener() {
            @Override
            public void onStatusChanged(boolean seismicDetectionStatus) {
                if(seismicDetectionStatus) {
                    seismicdetection.unregisterListener(); // Κανουμε unregistrer τον listener μεχρι να γινει η καταγραφη στην βαση και να δουμε αν ειναι οντως σεισμος
                    handleEvent("earthquakeEventDetected"); //καταγραφουμε στην βαση με type earthquakeDetection ωστε να κανουμε αναζητηση και σε αλλους χρηστες με το ιδιο type
                }
            }
        });
    }

    // handleEvent

    private void handleEvent( String type){
        String eventType = type;
        final double latd,lond;
        //check current location from LocationChange, if that doesn't work get manually the current location from the GPS service provider
        if(LocationService.latitude !=0 & LocationService.longitude!=0) {
             latd = LocationService.latitude;
             lond = LocationService.latitude;
        }
        else
        {
            getLocation();
            latd = latitude;
            lond = longitude;

        }

        String lat = Double.toString(latd);
        String lon = Double.toString(lond);
        final long timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();

        mFirebaseService.insertEvent(new EventModel(eventType, latd,lond,timestamp,date)); // Εγγραφη στην Firebase Database
        if((eventType != "earthquakeEventDetected") && (eventType != "earthquakeTakingPlace")) { //Στελνουμε μηνυμα σε καθε περιπτωση εκτος απο την περιπτωση της ανιχνευσης σεισμου
            Notification notification = new Notification(mainActivity);
            notification.sendNotification(type, lat, lon, date); // αποστολη SMS
        }

        if(eventType == "earthquakeEventDetected"){ // Στην περιπτωση που εχουμε ανιχνευση σεισμου, γινεται ελεγχος της βασης για να βρεθει και αλλος χρηστης σε κοντινη αποσταση που ειχε ιδιο event
            mFirebaseService.getEvents();
            mFirebaseService.setFirebaseListener(new FirebaseListener() {
                @Override
                public void onStatusChanged(String newStatus) { // οταν η getEvents() ολοκληρωθει και εχει φερει ολα τα events τοτε το newStatus θα ειναι allEvents.
                    if(newStatus.equals("allEvents")){
                        List<EventModel> events = EventModel.filterEarthquakeDetectionEvents(mFirebaseService.eventsList); //φιλτρουμε απο ολα τα events μονο τα earthquakedetection
                        boolean seismicStatus = seismicdetection.seismicStatus(events, timestamp,latd,lond);
                        if(seismicStatus){
                            handleEvent("earthquakeTakingPlace"); // εγγραφη του event στην βαση
                            new AlertDialog.Builder(MainActivity.mainActivity) // ειδοποιση χρηστη και και ενεργοποιηση του listener otan πατησει το οκ
                                    .setTitle("Earthquake!")
                                    .setMessage("An Earthquake is taking place, please seek help!!")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if( FallDetectionHandler.getListenerStatus() == null || FallDetectionHandler.getListenerStatus() ==false ){
                                                seismicdetection.registerListener();
                                            }
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }else {
                            //αμα δεν υπαρχει αλλος κοντινος χρηστης τοτε δεν γινεται event earthquake
                            if(FallDetectionHandler.getListenerStatus() == null || FallDetectionHandler.getListenerStatus() ==false ){
                                seismicdetection.registerListener();
                            }

                        }
                    }
                }
            });
        }
    }
}
