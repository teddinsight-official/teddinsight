package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcels;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;

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
        return inflater.inflate(R.layout.fragment_client_calendar_detail, container, false);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
    }


}
