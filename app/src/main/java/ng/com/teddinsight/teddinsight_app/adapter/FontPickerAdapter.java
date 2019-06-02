package ng.com.teddinsight.teddinsight_app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.EditorActivityTypefaces;

public class FontPickerAdapter extends RecyclerView.Adapter<FontPickerAdapter.FontPickerViewHolder> {

    private Context context;
    private List<EditorActivityTypefaces> editorActivityTypefaces;
    private OnFontPickerClickListener onFontPickerClickListener;

    public FontPickerAdapter(Context context) {
        this.context = context;
        this.editorActivityTypefaces = this.getFonts();
    }

    public void setOnFontPickerClickListener(OnFontPickerClickListener onFontPickerClickListener) {
        this.onFontPickerClickListener = onFontPickerClickListener;
    }

    @NonNull
    @Override
    public FontPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FontPickerViewHolder(inflater.inflate(R.layout.font_picker_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FontPickerViewHolder holder, int position) {
        EditorActivityTypefaces activityTypefaces = editorActivityTypefaces.get(position);
        holder.textView.setText(activityTypefaces.getFontName());
        holder.textView.setTypeface(activityTypefaces.getTypeface());
    }

    @Override
    public int getItemCount() {
        return editorActivityTypefaces.size();
    }

    private List<EditorActivityTypefaces> getFonts() {
        int fonts[] = {R.font.montserrat, R.font.maven_pro_regular, R.font.open_sans_regular, R.font.roboto_regular, R.font.sfnsdisplay, R.font.alex_brush};
        String fontNames[] = {"Montserrat", "Maven Pro Regular", "Open Sans Regular", "Roboto Regular", "Sfnsdisplay", "Alex Brush"};
        ArrayList<EditorActivityTypefaces> typefaces = new ArrayList<>();
        for (int i = 0; i < fonts.length; i++) {
            Typeface typeface = ResourcesCompat.getFont(context, fonts[i]);
            typefaces.add(new EditorActivityTypefaces(fontNames[i], typeface));
        }
        return typefaces;
    }

    class FontPickerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.font_name)
        TextView textView;

        public FontPickerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (onFontPickerClickListener != null)
                    onFontPickerClickListener.onFontPickerClickListener(editorActivityTypefaces.get(getAdapterPosition()).getTypeface());
            });
        }
    }

    public interface OnFontPickerClickListener {
        void onFontPickerClickListener(Typeface typeface);
    }

}
