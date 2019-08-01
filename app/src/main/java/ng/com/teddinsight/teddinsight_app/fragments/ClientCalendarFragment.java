package ng.com.teddinsight.teddinsight_app.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarAdapter;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentClientCalendarBinding;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.viewmodels.ClientCalendarViewModel;

public class ClientCalendarFragment extends Fragment {


    public ClientCalendarFragment() {
        // Required empty public constructor
    }

    public static ClientCalendarFragment NewInstance() {
        return new ClientCalendarFragment();
    }

    private ClientCalendarAdapter.OnClientCalendarItemClick onClientCalendarItemClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentClientCalendarBinding binding = FragmentClientCalendarBinding.inflate(inflater, container, false);
        ClientCalendarViewModel viewModel = ViewModelProviders.of(this).get(ClientCalendarViewModel.class);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        ClientCalendarAdapter adapter = new ClientCalendarAdapter(new ClientCalendarAdapter.ClientCalendarDiffUtil(), onClientCalendarItemClick);
        binding.clientCalendarRecycler.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onClientCalendarItemClick = new ClientCalendarAdapter.OnClientCalendarItemClick() {
            @Override
            public void onClientCalendarItemClicked(ClientCalendar clientCalendar) {
                getChildFragmentManager().beginTransaction().replace(R.id.frame_container, ClientCalendarDetailFragment.NewInstance(clientCalendar)).addToBackStack(null).commit();
            }
        };
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClientCalendarItemClick = null;
    }
}
