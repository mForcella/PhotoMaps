package edu.newpaltz.photomaps;

public class Photo {

    private String id, lat, lng, comment, photo, date, location, upload;

    public Photo(String id, String lat, String lng, String comment, String photo, String date, String location, String upload) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.comment = comment;
        this.photo = photo;
        this.date = date;
        this.location = location;
        this.upload = upload;
    }

    public Photo(String lat, String lng, String comment, String photo, String date, String location, String upload) {
        this.lat = lat;
        this.lng = lng;
        this.comment = comment;
        this.photo = photo;
        this.date = date;
        this.location = location;
        this.upload = upload;
    }

    public String getId() {
        return id;
    }

    public String getPhoto() {
        return photo;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {return location; }

    public String getUpload() {return upload;}
}