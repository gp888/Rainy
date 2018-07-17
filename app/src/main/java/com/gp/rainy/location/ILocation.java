package com.gp.rainy.location;

public interface ILocation {
    void locationSuccess(String lat1, String long1, String location);

    void locationFailed();
}
