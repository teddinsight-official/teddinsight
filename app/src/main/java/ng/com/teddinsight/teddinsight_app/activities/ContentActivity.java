package ng.com.teddinsight.teddinsight_app.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ng.com.teddinsight.teddinsight_app.R;

public class ContentActivity extends AppCompatActivity {

    @BindView(R.id.photo_edit)
    PhotoEditorView photoEditorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);

        photoEditorView.getSource().setImageResource(R.drawable.ic_tedd);
        Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.montserrat);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        PhotoEditor mPhotoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                //      .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build();

        mPhotoEditor.addText("Yo", Color.BLACK);
    }

}
