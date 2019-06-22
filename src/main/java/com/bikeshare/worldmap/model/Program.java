package com.bikeshare.worldmap.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Program {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String city;
    private String country;
    private String continent;
    private String url;
    private Integer status;
    private Date startDate;
    private Date endDate;
    private Float latitude;
    private Float longitude;
}
