package micdm.transportlive;

public class ComponentHolder {

    private static AppComponent appComponent;
    private static ActivityComponent activityComponent;

    public static AppComponent getAppComponent() {
        checkComponent(appComponent);
        return appComponent;
    }

    public static ActivityComponent getActivityComponent() {
        checkComponent(activityComponent);
        return activityComponent;
    }

    private static void checkComponent(Object component) {
        if (component == null) {
            throw new IllegalStateException("component isn't ready yet");
        }
    }

    static void setAppComponent(AppComponent value) {
        ComponentHolder.appComponent = value;
    }

    static void setActivityComponent(ActivityComponent value) {
        ComponentHolder.activityComponent = value;
    }

    static void resetActivityComponent() {
        ComponentHolder.activityComponent = null;
    }
}
