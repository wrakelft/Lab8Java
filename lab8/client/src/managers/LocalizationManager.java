package managers;


import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {
    private ResourceBundle rb;

    public LocalizationManager(Locale locale) {
        setLocale(locale);
    }

    public void setLocale(Locale locale) {
        rb = ResourceBundle.getBundle("MessageBundle", locale);
    }

    public String getString(String key) {
        return rb.getString(key);
    }


}
