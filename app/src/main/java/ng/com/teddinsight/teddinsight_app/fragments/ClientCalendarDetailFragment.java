package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.parceler.Parcels;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarTaskAdapter;
import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarTaskAdapter.*;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentClientCalendarDetailBinding;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.viewmodels.ClientCalendarDetailsViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.ClientCalendarDetailsViewModelFactory;

public class ClientCalendarDetailFragment extends Fragment {

    public static final String CLIENT_CALENDAR_ITEM = "client_calendar_item";

    public ClientCalendarDetailFragment() {
        // Required empty public constructor
    }

    public static Fragment NewInstance(ClientCalendar clientCalendar) {
        ClientCalendarDetailFragment fragment = new ClientCalendarDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(CLIENT_CALENDAR_ITEM, Parcels.wrap(clientCalendar));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentClientCalendarDetailBinding binding = FragmentClientCalendarDetailBinding.inflate(inflater, container, false);
        binding.toolbar.inflateMenu(R.menu.client_calendar_menu);
        Bundle bundle = getArguments();
        ClientCalendar clientCalendar = (ClientCalendar) Parcels.unwrap(bundle.getParcelable(CLIENT_CALENDAR_ITEM));
        binding.toolbar.setTitle(clientCalendar.getName());
        binding.setLifecycleOwner(this);
        ClientCalendarDetailsViewModelFactory clientCalendarDetailsViewModelFactory = new ClientCalendarDetailsViewModelFactory(clientCalendar);
        ClientCalendarDetailsViewModel viewModel = ViewModelProviders.of(this, clientCalendarDetailsViewModelFactory).get(ClientCalendarDetailsViewModel.class);
        binding.setViewmodel(viewModel);
        ClientCalendarTaskAdapter adapter = new ClientCalendarTaskAdapter(new ClientCalendarTaskDiffUtil());
        binding.tasksRecyclerView.setAdapter(adapter);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item != null)
                Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            return false;
        });
        viewModel.message().observe(this, s -> {
            if (s != null)
                showToast(s, Toast.LENGTH_LONG);
            viewModel.stopMessageDispatch();

        });
        return binding.getRoot();
    }

    private void showToast(String message, int length) {
        Toast.makeText(getContext(), message, length).show();
    }


    private AdminActivity getActivityCast() {
        return (AdminActivity) getActivity();
    }

}
