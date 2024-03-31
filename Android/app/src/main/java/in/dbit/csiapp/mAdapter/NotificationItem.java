package in.dbit.csiapp.mAdapter;

// NotificationItem.java

public class NotificationItem {
    private String title;
    private String heading;
    private String imageUrl;

    public NotificationItem(String title, String heading, String imageUrl) {
        this.title = title;
        this.heading = heading;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getHeading() {
        return heading;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
