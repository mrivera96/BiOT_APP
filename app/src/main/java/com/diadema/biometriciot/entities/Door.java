package com.diadema.biometriciot.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by César Andrade on 29/04/2019.
 */

public class Door {

    @SerializedName("Description")
    private String Description;

    @SerializedName("IP")
    private String IP;

    @SerializedName("Type")
    private String Type;

    @SerializedName("tiene_puerta")
    private String tiene_puerta;



    public Door(String Description, String IP, String Type, String tiene_puerta) {
        this.Description = Description;
        this.IP = IP;
        this.Type = Type;
        this.tiene_puerta = tiene_puerta;
    }


    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getHDoor(){
        return this.tiene_puerta;
    }
}