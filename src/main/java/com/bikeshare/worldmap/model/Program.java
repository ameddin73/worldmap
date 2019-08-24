package com.bikeshare.worldmap.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Program implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String city;
    private String continent;
    private String country;
    private Date endDate;
    private Float latitude;
    private Float longitude;
    private String name;
    private Date startDate;
    private Integer status;
    private String url;

    public Program(String city, String continent, String country, Date endDate, Float latitude, Float longitude, String name, Date startDate, Integer status, String url) {
        this.city = city;
        this.continent = continent;
        this.country = country;
        this.endDate = endDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.startDate = startDate;
        this.status = status;
        this.url = url;
    }

    protected Program() {
    }

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Program{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", continent='" + continent + '\'' +
                ", country='" + country + '\'' +
                ", endDate=" + endDate +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", status=" + status +
                ", url='" + url + '\'' +
                '}';
    }
}
