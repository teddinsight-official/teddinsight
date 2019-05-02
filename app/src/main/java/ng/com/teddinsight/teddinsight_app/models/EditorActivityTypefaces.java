package ng.com.teddinsight.teddinsight_app.models;

import android.graphics.Typeface;

public class EditorActivityTypefaces {

    private String fontName;
    private Typeface typeface;

    public EditorActivityTypefaces(String fontName, Typeface typeface) {
        this.fontName = fontName;
        this.typeface = typeface;
    }

    public String getFontName() {
        return fontName;
    }

    public Typeface getTypeface() {
        return typeface;
    }
}
