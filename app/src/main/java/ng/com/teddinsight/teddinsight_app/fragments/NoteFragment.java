package ng.com.teddinsight.teddinsight_app.fragments;


import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import org.parceler.Parcels;

import ng.com.teddinsight.teddinsight_app.databinding.FragmentNoteBinding;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes;
import ng.com.teddinsight.teddinsight_app.viewmodels.NoteViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.NoteViewModelFactory;
import ng.com.teddinsight.teddinsightchat.models.User;

public class NoteFragment extends Fragment {

    AlertDialog.Builder alertDialog;

    public NoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
    }

    public static NoteFragment NewInstance(ContentNotes contentNotes) {
        NoteFragment noteFragment = new NoteFragment();
        Bundle b = new Bundle();
        b.putParcelable("NOTE", Parcels.wrap(contentNotes));
        noteFragment.setArguments(b);
        return noteFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = getArguments();
        if (b == null) {
            Toast.makeText(getContext(), "An error occured", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
            return null;
        }
        alertDialog = new AlertDialog.Builder(getContext());
        ContentNotes contentNotes = Parcels.unwrap(b.getParcelable("NOTE"));
        FragmentNoteBinding binding = FragmentNoteBinding.inflate(inflater, container, false);
        NoteViewModelFactory noteViewModelFactory = new NoteViewModelFactory(getActivity().getApplication(), contentNotes);
        NoteViewModel noteViewModel = ViewModelProviders.of(this, noteViewModelFactory).get(NoteViewModel.class);
        binding.setViewmodel(noteViewModel);
        binding.setLifecycleOwner(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.note.setTransitionName(contentNotes.noteTransitionName);
        }
        if (contentNotes.viewer.equals(User.USER_ADMIN)) {
            binding.noteTitle.setFocusable(false);
            binding.note.setFocusable(false);
        }
        alertDialog.setNegativeButton("No, cancel", (dialog, which) -> {
            noteViewModel.abortOperation();
            dialog.dismiss();
        });
        noteViewModel.operationDeleteOrDisapproveRequested().observe(this, aBoolean -> {
            if (!aBoolean)
                return;
            if (contentNotes.viewer.equals(User.USER_CONTENT)) {
                alertDialog.setTitle("Delete Note")
                        .setMessage(ContentNotes.ContentNotesStatus.DELETED.getMessage())
                        .setPositiveButton("Yes", (dialog, which) -> {
                            noteViewModel.deleteNote();
                            showToast("Delete Note");
                            dialog.dismiss();
                        });
            } else {
                alertDialog.setTitle("Disapprove?")
                        .setMessage(ContentNotes.ContentNotesStatus.DISAPPROVED.getMessage())
                        .setPositiveButton("Yes", (dialog, which) -> {
                            contentNotes.setReviewedByAdmin(true);
                            contentNotes.setContentNotesStatus(ContentNotes.ContentNotesStatus.DISAPPROVED);
                            noteViewModel.updateContentNotes(contentNotes);
                            noteViewModel.disapproveNote();
                            showToast("Disapproving Note");
                            dialog.dismiss();
                        });
            }
            alertDialog.show();
        });
        noteViewModel.operationSaveOrApprove().observe(this, aBoolean -> {
            if (!aBoolean)
                return;
            if (contentNotes.viewer.equals(User.USER_CONTENT)) {
                alertDialog.setTitle("Save Note")
                        .setMessage(ContentNotes.ContentNotesStatus.SAVED.getMessage())
                        .setPositiveButton("Yes", (dialog, which) -> {
                            contentNotes.setTitle(binding.noteTitle.getText().toString());
                            contentNotes.setNote(binding.note.getText().toString());
                            contentNotes.setUpdatedAt(System.currentTimeMillis());
                            contentNotes.setReviewedByAdmin(false);
                            contentNotes.setContentNotesStatus(ContentNotes.ContentNotesStatus.PENDING);
                            noteViewModel.updateContentNotes(contentNotes);
                            showToast("Saving note, please wait");
                            noteViewModel.saveNote();
                            dialog.dismiss();
                        });
            } else {
                alertDialog.setTitle("Approve Note")
                        .setMessage(ContentNotes.ContentNotesStatus.APPROVED.getMessage())
                        .setPositiveButton("Yes", (dialog, which) -> {
                            contentNotes.setContentNotesStatus(ContentNotes.ContentNotesStatus.APPROVED);
                            contentNotes.setReviewedByAdmin(true);
                            noteViewModel.updateContentNotes(contentNotes);
                            showToast("approving note, please wait");
                            noteViewModel.approveNote();
                            dialog.dismiss();
                        });
            }
            alertDialog.show();
        });

        noteViewModel.onSuccess().observe(this, s -> {
            if (s != null) {
                showToast(s);
                noteViewModel.stopSuccess();
                getActivity().onBackPressed();
            }
        });
        noteViewModel.onError().observe(this, s -> {
            if (s != null) {
                showToast(s);
                noteViewModel.stopError();
            }
        });
        return binding.getRoot();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
