package ng.com.teddinsight.teddinsight_app.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.databinding.ContentRepositoryItemBinding;
import ng.com.teddinsight.teddinsight_app.fragments.ContentCuratorHomeFragment;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes.ContentNotesStatus;

public class ContentsNotesAdapter extends ListAdapter<ContentNotes, ContentsNotesAdapter.ContentsNoteViewholder> {

    private ContentCuratorHomeFragment.OnFragmentInteractionListener fragmentInteractionListener;

    public ContentsNotesAdapter(@NonNull DiffUtil.ItemCallback<ContentNotes> diffCallback, ContentCuratorHomeFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
        super(diffCallback);
        this.fragmentInteractionListener = onFragmentInteractionListener;
    }

    @NonNull
    @Override
    public ContentsNoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ContentsNoteViewholder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentsNoteViewholder holder, int position) {
        holder.bind(getItem(position), fragmentInteractionListener);
    }

    public static class ContentsNoteViewholder extends RecyclerView.ViewHolder {
        ContentRepositoryItemBinding binding;

        private ContentsNoteViewholder(@NonNull ContentRepositoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ContentsNoteViewholder from(ViewGroup parent) {
            ContentRepositoryItemBinding binding = ContentRepositoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ContentsNoteViewholder(binding);
        }

        public void bind(ContentNotes contentNotes, ContentCuratorHomeFragment.OnFragmentInteractionListener fragmentInteractionListener) {
            binding.setContentNotes(contentNotes);
            binding.executePendingBindings();
            binding.itemview.setOnClickListener(v -> fragmentInteractionListener.onFragmentInteraction(binding.noteTitle, binding.noteContent, contentNotes));
        }

        @BindingAdapter("statusImage")
        public static void setStatusImage(ImageView image, ContentNotes contentNotes) {
            if (contentNotes.getContentNotesStatus() == ContentNotesStatus.APPROVED) {
                image.setImageResource(R.drawable.ic_check_circle_green_24dp);
                image.setVisibility(View.VISIBLE);

            } else if (contentNotes.getContentNotesStatus() == ContentNotesStatus.DISAPPROVED) {
                image.setImageResource(R.drawable.ic_cancel);
                image.setVisibility(View.VISIBLE);
            } else if (contentNotes.getContentNotesStatus() == ContentNotesStatus.PENDING) {
                image.setVisibility(View.GONE);
            }
        }

        @BindingAdapter("notesBorder")
        public static void setNotesBorder(View view, ContentNotes contentNotes) {
            if (contentNotes.getContentNotesStatus() == ContentNotesStatus.APPROVED) {
                view.setBackground(view.getContext().getResources().getDrawable(R.drawable.green_border));
            } else if (contentNotes.getContentNotesStatus() == ContentNotesStatus.DISAPPROVED) {
                view.setBackground(view.getContext().getResources().getDrawable(R.drawable.red_border));
            } else if (contentNotes.getContentNotesStatus() == ContentNotesStatus.PENDING) {
                view.setBackground(view.getContext().getResources().getDrawable(R.drawable.rect_white1));
            }
        }

    }


    public static class ContentCuratorDiffUtil extends DiffUtil.ItemCallback<ContentNotes> {

        @Override
        public boolean areItemsTheSame(@NonNull ContentNotes oldItem, @NonNull ContentNotes newItem) {
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ContentNotes oldItem, @NonNull ContentNotes newItem) {
            return oldItem.equals(newItem);
        }
    }

}
