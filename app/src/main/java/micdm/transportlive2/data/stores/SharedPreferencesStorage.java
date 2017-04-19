package micdm.transportlive2.data.stores;

import android.content.SharedPreferences;

class SharedPreferencesStorage implements BaseStore.Storage {

    private final SharedPreferences sharedPreferences;
    private final String key;

    public SharedPreferencesStorage(SharedPreferences sharedPreferences, String key) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
    }

    @Override
    public String read() {
        return sharedPreferences.getString(key, null);
    }

    @Override
    public void write(String value) {
        sharedPreferences
            .edit()
            .putString(key, value)
            .apply();
    }
}
