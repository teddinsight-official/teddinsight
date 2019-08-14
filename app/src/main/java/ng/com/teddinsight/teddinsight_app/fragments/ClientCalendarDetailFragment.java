package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
    private ClientCalendar clientCalendar;

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
        Bundle bundle = getArguments();
        clientCalendar = Parcels.unwrap(bundle.getParcelable(CLIENT_CALENDAR_ITEM));
        binding.toolbar.setTitle(clientCalendar.getName());
        binding.setLifecycleOwner(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Warning")
                .setIcon(R.drawable.ic_warning)
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        ClientCalendarDetailsViewModelFactory clientCalendarDetailsViewModelFactory = new ClientCalendarDetailsViewModelFactory(clientCalendar);
        ClientCalendarDetailsViewModel viewModel = ViewModelProviders.of(this, clientCalendarDetailsViewModelFactory).get(ClientCalendarDetailsViewModel.class);
        binding.setViewmodel(viewModel);
        ClientCalendarTaskAdapter adapter = new ClientCalendarTaskAdapter(new ClientCalendarTaskDiffUtil(), tasks -> {
            getActivityCast().replaceFragmentContainerContent(NewTaskFragment.NewInstance(tasks, clientCalendar), true);
        });
        binding.tasksRecyclerView.setAdapter(adapter);
        binding.toolbar.inflateMenu(R.menu.client_calendar_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item != null) {
                switch (item.getItemId()) {
                    case R.id.delete_menu:
                        viewModel.deleteCalendar();
                        break;
                    case R.id.dispatch_calendar:
                        viewModel.startCalendarDispatch();
                        break;
                    case R.id.report_menu:
                        viewModel.startViewCalendarReport();
                        break;
                }
            }
            return false;
        });
        viewModel.getClientCalendar().observe(this, clientCalendar13 -> {
            if (clientCalendar13 != null) {
                this.clientCalendar = clientCalendar13;
                if (clientCalendar13.isBeginPublishing())
                    binding.toolbar.getMenu().getItem(0).setVisible(false);
                else
                    binding.toolbar.getMenu().getItem(0).setVisible(true);
            }
        });
        viewModel.message().observe(this, s -> {
            if (s != null) {
                showToast(s, Toast.LENGTH_LONG);
                viewModel.stopMessageDispatch();
            }

        });
        viewModel.creatNewTask().observe(this, tasks -> {
            if (tasks != null) {
                tasks.clientCalendarId = clientCalendar.getKey();
                tasks.clientId = clientCalendar.getClientId();
                getActivityCast().replaceFragmentContainerContent(NewTaskFragment.NewInstance(tasks, clientCalendar), true);
                viewModel.stopCreateNewTask();
            }
        });
        viewModel.startDeleteClientCalendar().observe(this, clientCalendar1 -> {
            if (clientCalendar1 != null) {
                String message = clientCalendar1.isNeedsPublishing()
                        ? "Calendar has not been dispatched, all created tasks will be lost" :
                        "You will no longer get any report for tasks in this calendar";
                dialogBuilder.setMessage(message)
                        .setPositiveButton("Yes, Delete!", (dialog, which) -> {
                            viewModel.performDelete(clientCalendar1);
                            dialog.dismiss();
                        });
                dialogBuilder.show();
                viewModel.finishClientCalendarDeletion();
            }
        });
        viewModel.finishOperation().observe(this, aBoolean -> {
            if (aBoolean) {
                showToast("Operation done", Toast.LENGTH_LONG);
                getActivityCast().onBackPressed();
                viewModel.endFinishOperation();
            }
        });
        viewModel.startDispatchClientCalendar().observe(this, clientCalendar12 -> {
            if (clientCalendar12 != null) {
                if (clientCalendar12.getTaskCount() < 1) {
                    showToast("Calendar has no task, can't publish", Toast.LENGTH_LONG);
                } else {
                    dialogBuilder.setMessage("Calendar can only be dispatched once, make sure you have added all tasks.")
                            .setPositiveButton("Publish", (dialog, which) -> {
                                showToast("assigning tasks in calendar, please wait", Toast.LENGTH_LONG);
                                viewModel.dispatchNow(clientCalendar12);
                                dialog.dismiss();
                            });
                    dialogBuilder.show();
                }
                viewModel.finishClientCalendarDispatch();
            }
        });
        viewModel.viewCalendarReport().observe(this, clientCalendar14 -> {
            if (clientCalendar14 != null) {
                if (!clientCalendar14.isBeginPublishing()) {
                    showToast("Cannot view report of undispatched calendar", Toast.LENGTH_LONG);
                } else {
                    showToast("Please wait!!!", Toast.LENGTH_LONG);
                    getActivityCast().replaceFragmentContainerContent(CalendarReportFragment.NewInstance(clientCalendar14), true);
                }
                viewModel.stopViewCalendarReport();
            }
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
