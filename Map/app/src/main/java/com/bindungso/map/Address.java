package com.bindungso.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by BinDungSo on 6/29/2017.
 */
public class Address {
    private LatLng location;
    private String detail_address;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address(LatLng location, String detail_address, String name) {
        this.location = location;
        this.detail_address = detail_address;
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDetail_address() {
        return detail_address;
    }

    public void setDetail_address(String detail_address) {
        this.detail_address = detail_address;
    }
}
