package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.ContentsNotesAdapter;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentContentCuratorHomeBinding;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes;
import ng.com.teddinsight.teddinsight_app.viewmodels.ContentCuratorHomeViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.ContentCuratorViewModelFactory;
import ng.com.teddinsight.teddinsightchat.models.User;


public class ContentCuratorHomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ContentCuratorHomeFragment() {
        // Required empty public constructor
    }

    public static ContentCuratorHomeFragment NewInstance(String userType) {
        Bundle bundle = new Bundle();
        bundle.putString("user", userType);
        ContentCuratorHomeFragment contentCuratorHomeFragment = new ContentCuratorHomeFragment();
        contentCuratorHomeFragment.setArguments(bundle);
        return contentCuratorHomeFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String user = getArguments().getString("user");
        if (user == null)
            user = User.USER_CONTENT;

        FragmentContentCuratorHomeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content_curator_home, container, false);
        binding.setLifecycleOwner(this);
        ContentsNotesAdapter adapter = new ContentsNotesAdapter(new ContentsNotesAdapter.ContentCuratorDiffUtil(), mListener);
        binding.contentThoughtsRecyclerView.setAdapter(adapter);
        binding.toolbarTitle.setText("Notes");
        if (user.equals(User.USER_ADMIN)) {
            binding.appbar.setVisibility(View.GONE);
            binding.newNoteButton.setVisibility(View.GONE);
            binding.emptyView.setText("No notes available for review");
        }
        ContentCuratorViewModelFactory contentCuratorViewModelFactory = new ContentCuratorViewModelFactory(getActivity().getApplication(), user);
        ContentCuratorHomeViewModel viewModel = ViewModelProviders.of(this, contentCuratorViewModelFactory).get(ContentCuratorHomeViewModel.class);
        viewModel.getNewNote().observe(this, aBoolean -> {
            if (aBoolean) {
                mListener.onFragmentInteraction(null, null, null);
                viewModel.stopAddNewNote();
            }
        });
        binding.setViewmodel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(TextView title, TextView note, ContentNotes contentNotes);
    }
}
