package com.bindungso.map;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by BinDungSo on 6/29/2017.
 */
public class MyAsyncTask extends AsyncTask<String, Address, Void> {
    public MyAsyncTask(MainActivity context) {
        this.context = context;
    }

    private MainActivity context;

    @Override
    protected Void doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                StringBuffer buffer = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                String stringJSON = buffer.toString();
                JSONObject rootJSONObject = new JSONObject(stringJSON);
                JSONArray results = rootJSONObject.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    Address address = parseJSON(stringJSON, i);
                    publishProgress(address);
                }


            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Address parseJSON(String stringJSON, int i) {

        Address address = null;
        try {
            JSONObject rootJSONObject = new JSONObject(stringJSON);
            JSONArray results = rootJSONObject.getJSONArray("results");
            JSONObject childObject = results.getJSONObject(i);
            String name = (String) childObject.get("name");
            JSONObject geoJSONObject = childObject.getJSONObject("geometry");
            JSONObject locaJSONObject = geoJSONObject.getJSONObject("location");
            String detailAddress = childObject.getString("vicinity");
            double lat = locaJSONObject.getDouble("lat");
            double lng = locaJSONObject.getDouble("lng");
            address = new Address(new LatLng(lat, lng),detailAddress, name);
            return address;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Address... values) {
        super.onProgressUpdate(values);
        context.setMarker(values[0]);
    }
}

