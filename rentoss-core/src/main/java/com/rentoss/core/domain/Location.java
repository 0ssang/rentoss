package com.rentoss.core.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    private Double latitude;
    private Double longitude;
    private String address;

    private Location(Double latitude, Double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public static Location of(Double latitude, Double longitude, String address) {
        return new Location(latitude, longitude, address);
    }
}
