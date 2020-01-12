package rekkursion.manager;

public class PreferenceManager {
    // static instance
    private static PreferenceManager instance = null;

    // private constructor
    private PreferenceManager() {}

    // get the instance
    public static PreferenceManager getInstance() {
        if (instance == null)
            instance = new PreferenceManager();
        return instance;
    }
}
