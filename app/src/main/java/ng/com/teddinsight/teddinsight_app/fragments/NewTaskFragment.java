package ng.com.teddinsight.teddinsight_app.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Array;
import java.util.Calendar;
import java.util.List;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentNewTaskBinding;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.viewmodels.NewTaskViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.NewTaskViewModelFactory;
import ng.com.teddinsight.teddinsightchat.models.User;

public class NewTaskFragment extends Fragment {

    private static final String TASK_ITEM = "task_item";

    public static NewTaskFragment NewInstance(Tasks tasks) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TASK_ITEM, tasks);
        NewTaskFragment newTaskFragment = new NewTaskFragment();
        newTaskFragment.setArguments(bundle);
        return newTaskFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment\
        Tasks tasks = (Tasks) getArguments().getParcelable(TASK_ITEM);
        FragmentNewTaskBinding binding = FragmentNewTaskBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        NewTaskViewModelFactory factory = new NewTaskViewModelFactory(tasks);
        NewTaskViewModel viewModel = ViewModelProviders.of(this, factory).get(NewTaskViewModel.class);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        viewModel.users().observe(this, users -> {
            CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(users);
            binding.user.setAdapter(spinnerAdapter);
            binding.user.setSelection(0, true);
            binding.user.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    viewModel.setUserData(users.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        });
        viewModel.shouldRequestNewDeadline().observe(this, aBoolean -> {
            if (aBoolean) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                    view.setVisibility(View.GONE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                            showToast("Due date cannot be less than or equal to now, Task not assigned");
                            return;
                        }
                        //task_due_date.setText(ExtraUtils.getHumanReadableString(calendar.getTimeInMillis()));
                        viewModel.setTaskDueDate(calendar.getTimeInMillis());
                        view1.setVisibility(View.GONE);
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                viewModel.stopNewDeadlineRequest();
            }
        });
        viewModel.message().observe(this, s -> {
            if (s != null) {
                showToast(s);
                viewModel.stopMessageDispatch();
            }
        });
        viewModel.taskSaveSuccessful().observe(this, aBoolean -> {
            if (aBoolean) {
                getActivity().onBackPressed();
                viewModel.stopTaskSaveSuccessful();
            }
        });
        return binding.getRoot();
    }

    private void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

    class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        List<User> userList;
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.montserrat);


        public CustomSpinnerAdapter(List<User> users) {
            this.userList = users;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;
            if (view == null)
                view = new TextView(getContext());
            User user = userList.get(position);
            view.setText(user.getFirstName().concat(" " + user.getLastName()).concat(" - ").concat(user.getRole()));
            view.setPadding(30, 30, 30, 30);
            view.setTextColor(Color.BLUE);
            //view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            view.setTypeface(typeface);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            TextView view = (TextView) convertView;
            if (view == null)
                view = new TextView(getContext());
            User user = userList.get(position);
            view.setText(user.getFirstName().concat(" " + user.getLastName()).concat(" - ").concat(user.getRole()));
            view.setPadding(30, 30, 30, 30);
            view.setTypeface(typeface);
            return view;
        }
    }

}
