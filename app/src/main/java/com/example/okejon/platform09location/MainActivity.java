package com.example.okejon.platform09location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{

    //button
    private Button mLocationButton;
    private Button mPickLocationButton;
    //textview
    private TextView mLocationTextView;
    //imageview
    private ImageView mAndroidImageView;
    //animasi
    private AnimatorSet mRotateAnim;
    //location
    private Location mLastLocation;
    //deklarasi variabel untuk fusedlocationproviderclient
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mTrackingLocation;
    //object location callback
    private LocationCallback mLocationCallback;
    //constant digunakan untuk mengidentifikasi req permission
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_PICK_PLACE = 0;
    //places class
    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;

    //variabel untuk savedinstance
    private static String name, address;
    private static  int gambar = -1;

    //savedinstance agar tampilan landscape dan portrait tetap
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("name",name);
        savedInstanceState.putString("address",address);
        savedInstanceState.putInt("gambar",gambar);
    }

    //restore data dari instancestate pada object yg diinginkan
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getString("name")=="") {
            mLocationTextView.setText("Tekan Button dibawah ini untuk mendapatkan lokasi anda");
        } else{

            mLocationTextView.setText(getString(R.string.alamat_text,savedInstanceState.getString("name"),savedInstanceState.getString("address"), System.currentTimeMillis()));
            mAndroidImageView.setImageResource(savedInstanceState.getInt("gambar"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inisiasi object placedetectionclient untuk mendapat informasi lokasi device
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //deklarasi variabel button, textview, imageview dengan id yang diambil dari activity main
        mLocationButton = (Button)findViewById(R.id.button_location);
        mLocationTextView = (TextView)findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView)findViewById(R.id.imageview_android);
        mPickLocationButton = (Button) findViewById(R.id.button_pilihlocation);
        //mengatur animasi pada imageview
        mRotateAnim = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        mPickLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //eksekusi placepicker
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), REQUEST_PICK_PLACE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        //onclick button yang sudah dideklarasi
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jika tracking false yang dijalankan fungsi trackinglocation()
                if (!mTrackingLocation){
                    trackingLocation();
                } else {
                    //jika tracking true yang dijalankan fungsi stoptrackinglocation()
                    stopTrackingLocation();
                }
            }
        });
        //meminta update lokasi
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (mTrackingLocation){
                    new DapatkanAlamatTask(MainActivity.this, MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }
    // getLocation() digunakan untuk mendapatkan alamat
    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);

        }else{
            //  Log.d("getpermission", "getLocation: permission granted");

            Log.d("getpermission", "getLocation: permission granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        mLastLocation = location;
                        mLocationTextView.setText(getString(R.string.location_text, mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getTime()));
                        //lakukan reserve geocode AsyncTask
                    } else{
                        //mLocationTextView.setText("Lokasi gak ada bro");
                        mLocationTextView.setText(getString(R.string.alamat_text, "Searching Address", System.currentTimeMillis()));
                    }
                }
            });
        }
    }

    //memulai track lokasi jika permission allow maka mencari alamat , button menjadi berhenti bos, animasi rotate berjalan
    private void trackingLocation(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , REQUEST_LOCATION_PERMISSION );

        } else {
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback,null );
            //argumen settext nama tempat, alamat, waktu
            mLocationTextView.setText(getString(R.string.alamat_text, "Searching Places", "Searching Address", System.currentTimeMillis()));
            mTrackingLocation = true;
            mLocationButton.setText("Stop Tracking");
            mRotateAnim.start();

        }
    }

    //stop trackinglocation
    private void stopTrackingLocation(){
        if (mTrackingLocation){
            mTrackingLocation = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationButton.setText("Start Tracking Location");
            mLocationTextView.setText("Tracking stop");
            mRotateAnim.end();
        }
    }

    //untuk request lokasi dengan interval berapa lama menentukan update lokasi baru
    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    //untuk request permission
    @Override
    public void onRequestPermissionsResult(int RequesCode, @NonNull String[] permissions, @NonNull int[] grantResult){
        //permission
        switch (RequesCode){
            case REQUEST_LOCATION_PERMISSION :
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    //dijalankan set text menampilkan value string dengan lokasi saat ini
    //menambahkan throws
    public void onTaskCompleted(final String result) throws SecurityException{
        //cek mtrackinglocation
        if(mTrackingLocation) {
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            //throw securityexception ditambahkan karena akan muncul tanda apabila permission lokasi tidak didapatkan
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful()){
                        //jika successful hasil akan tampil pada getresult dan dimasukkan dalam placelikehoodbufferresponse
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        float maxLikehood = 0;
                        Place currentPlace = null;

                        //cek apakah tempat yg dihasilakn paling mendekati (likelihood)
                        for (PlaceLikelihood placeLikelihood : likelyPlaces){
                            if (maxLikehood < placeLikelihood.getLikelihood()){
                                //jika ya maka update object maxlikelihood dengan currentplace
                                maxLikehood = placeLikelihood.getLikelihood();
                                currentPlace = placeLikelihood.getPlace();
                            }

                            //Tampil di UI
                            if (currentPlace!= null){
                                mLocationTextView.setText(getString(R.string.alamat_text, currentPlace.getName(), result, System.currentTimeMillis()));
                                setTipeLokasi(currentPlace);
                                //input data pada variabel di saveinstance
                                name = placeLikelihood.getPlace().getName().toString();
                                address = placeLikelihood.getPlace().getAddress().toString();
                                gambar = setTipeLokasi(currentPlace);
                                mAndroidImageView.setImageResource(gambar);
                            }
                        }
                        //hapus buffer
                        likelyPlaces.release();
                        //jika nama tempat tidak ditemukan
                    }else{
                        mLocationTextView.setText(getString(R.string.alamat_text, "Location Name Not Found", result, System.currentTimeMillis()));
                    }
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        //cek resultcode jika ok maka passing data tempat yang dipilih melalui intent
        if (resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this, data);
            mLocationTextView.setText(getString(R.string.alamat_text, place.getName(), place.getAddress(), System.currentTimeMillis()));
            name = place.getName().toString();
            address = place.getAddress().toString();
            gambar = setTipeLokasi(place);
            mAndroidImageView.setImageResource(gambar);
        } else {
            mLocationTextView.setText("Pick The Location First!");
        }

    }

    //daftar type lokasi berdasarkan seleksi kondisi
    private static int setTipeLokasi(Place currentPlace){
        int drawableID = -1;
        for (Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_GAS_STATION:
                    drawableID = R.drawable.gas;
                    break;
                case Place.TYPE_POLICE:
                    drawableID = R.drawable.police;
                    break;
                case Place.TYPE_HOSPITAL:
                    drawableID = R.drawable.hospital;
                    break;
                case Place.TYPE_LIBRARY:
                    drawableID = R.drawable.books;
                    break;
            }
        }
        if (drawableID < 0){
            drawableID = R.drawable.error;
        }
        return drawableID;
    }
}
