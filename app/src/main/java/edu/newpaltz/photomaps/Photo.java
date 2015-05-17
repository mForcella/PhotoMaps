package edu.newpaltz.photomaps;

public class Photo {

    private String lat, lng, comment, photo, date;

    public Photo(String lat, String lng, String comment, String photo, String date) {
        this.lat = lat;
        this.lng = lng;
        this.comment = comment;
        this.photo = photo;
        this.date = date;
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
}
