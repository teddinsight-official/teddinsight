package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.anychart.charts.Pie;

import org.parceler.Parcels;

import ng.com.teddinsight.teddinsight_app.adapter.CalendarReportAdapter;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentReportBinding;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModelFactory;

public class CalendarReportFragment extends Fragment {
    private static final String CLIENT_CALENDAR_ITEM = "client_calendar_item";

    public static CalendarReportFragment NewInstance(ClientCalendar clientCalendar) {
        Bundle args = new Bundle();
        args.putParcelable(CLIENT_CALENDAR_ITEM, Parcels.wrap(clientCalendar));
        CalendarReportFragment calendarReportFragment = new CalendarReportFragment();
        calendarReportFragment.setArguments(args);
        return calendarReportFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentReportBinding binding = FragmentReportBinding.inflate(inflater, container, false);
        ClientCalendar clientCalendar = Parcels.unwrap(getArguments().getParcelable(CLIENT_CALENDAR_ITEM));
        CalendarReportViewModelFactory calendarReportViewModelFactory = new CalendarReportViewModelFactory(clientCalendar);
        CalendarReportViewModel viewModel = ViewModelProviders.of(this, calendarReportViewModelFactory).get(CalendarReportViewModel.class);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        binding.reportList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.anyChartView.setProgressBar(binding.progressBar);
        viewModel.message().observe(this, s -> {
            if (s != null) {
                Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                viewModel.stopMessageDispatch();
            }
        });
        viewModel.pie().observe(this, pie -> {
            if (pie != null) {
                binding.anyChartView.setChart(pie);
            }
        });
        return binding.getRoot();
    }
}
