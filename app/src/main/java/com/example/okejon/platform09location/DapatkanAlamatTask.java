package com.example.okejon.platform09location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//AsyncTask agar dapat melakukan  reverse geocoding secara asynchronous
public class DapatkanAlamatTask extends AsyncTask<Location,Void,String>{

    @Override
    //agar method berjalan di background aplikasi
    protected String doInBackground(Location... locations) {
        //object geocoder untuk menerima input latitude dan longitude,
        Geocoder geocoder=new Geocoder(mContext, Locale.getDefault());
        //array 0 untuk mendapatkan objek pertama
        Location location=locations[0];
        List<Address> alamat=null;
        //menyimpan hasil resultmessage
        String resultMessage="";
        try{
            alamat=geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        }

        catch (IOException ioException){
            resultMessage="Servis tidak ada";
            Log.e("Lokasi Error",resultMessage,ioException);
        }

        catch (IllegalArgumentException illegalArgumentException ){
            //tangkap error input kordinat invalid
            resultMessage="Koordinat error bos";
            Log.e("Lokasi error",resultMessage+". " +
            "Latitude= "+location.getLatitude()+ ", Longitude=" +location.getLongitude(), illegalArgumentException);
        }

        //jika alamat tidak ditemukan maka eror
        if (alamat == null || alamat.size() == 0){
            if(resultMessage.isEmpty()){
                resultMessage = "Address Not Found";
                Log.e("Error Location", resultMessage);
            }
        } else {
            //jika alamat ditemukan, proses dan masukkan ke resultMessage
            Address myAddress = alamat.get(0);
            ArrayList<String> addressLine = new ArrayList<>();

            //get baris alamat using fungsi getAddressLine lalu gabungkan
            for (int i=0; i<=myAddress.getMaxAddressLineIndex(); i++){
                addressLine.add(myAddress.getAddressLine(i));
            }
            //Gabung line address di baris baru
            resultMessage = TextUtils.join("\n", addressLine);
        }
        //kembali ke resultMessage
        return resultMessage;
    }

//interface yang menjalankan ontaskcompleted
    interface onTaskSelesai{
        void onTaskCompleted(String result);
    }

//method untuk mendapatkan result data alamat berupa String.
    @Override
    protected void onPostExecute(String alamat) {
        mListener.onTaskCompleted(alamat);
        super.onPostExecute(alamat);
    }

    private Context mContext;
    private onTaskSelesai mListener;


    DapatkanAlamatTask(Context applicationContext , onTaskSelesai listener){
        mContext=applicationContext;
        mListener=listener;
    }
}
