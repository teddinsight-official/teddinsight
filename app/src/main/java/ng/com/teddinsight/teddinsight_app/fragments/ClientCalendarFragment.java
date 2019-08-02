package ng.com.teddinsight.teddinsight_app.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarAdapter;
import ng.com.teddinsight.teddinsight_app.databinding.FragmentClientCalendarBinding;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.viewmodels.ClientCalendarViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.ClientCalendarViewModelFactory;
import ng.com.teddinsight.teddinsightchat.models.User;


public class ClientCalendarFragment extends Fragment {

    private static final String USER = "user";
    private User user;

    public ClientCalendarFragment() {
        // Required empty public constructor
    }

    public static ClientCalendarFragment NewInstance(User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER, user);
        ClientCalendarFragment clientCalendarFragment = new ClientCalendarFragment();
        clientCalendarFragment.setArguments(bundle);
        return clientCalendarFragment;
    }

    private ClientCalendarAdapter.OnClientCalendarItemClick onClientCalendarItemClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            user = (User) args.getParcelable(USER);
            Log.e("TAG", user.getId());
        }
        FragmentClientCalendarBinding binding = FragmentClientCalendarBinding.inflate(inflater, container, false);
        ClientCalendarViewModelFactory factory = new ClientCalendarViewModelFactory(user.getId());
        ClientCalendarViewModel viewModel = ViewModelProviders.of(this, factory).get(ClientCalendarViewModel.class);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        ClientCalendarAdapter adapter = new ClientCalendarAdapter(new ClientCalendarAdapter.ClientCalendarDiffUtil(), onClientCalendarItemClick);
        binding.clientCalendarRecycler.setAdapter(adapter);
        viewModel.requestNewCalendarName().observe(this, aBoolean -> {
            if (aBoolean) {
                viewModel.addNewCalendarItem(null);
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                EditText editText = new EditText(getContext());
                linearLayout.addView(editText);
                editText.setHint(R.string.calendar_name);
                ///View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setView(linearLayout).setCancelable(false);
                dialog.setPositiveButton("Create Calendar", (dialog1, which) -> {
                    if (TextUtils.isEmpty(editText.getText().toString())) {
                        Toast.makeText(getContext(), "Calendar must bear a name", Toast.LENGTH_SHORT).show();
                    } else {
                        ClientCalendar clientCalendar = new ClientCalendar();
                        clientCalendar.setName(editText.getText().toString());
                        viewModel.addNewCalendarItem(clientCalendar);
                        dialog1.dismiss();
                    }
                }).setNegativeButton("Cancel", (dialog12, which) -> {
                    dialog12.cancel();
                });
                dialog.show();
            }
        });
        viewModel.newClientCalendarItem().observe(this, clientCalendar -> {
            if (clientCalendar != null) {
                onClientCalendarItemClick.onClientCalendarItemClicked(clientCalendar);
                viewModel.addNewCalendarItem(null);
            }
        });
        viewModel.message().observe(this, s -> {
            if (s != null) {
                Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
                viewModel.stopShowError();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onClientCalendarItemClick = (ClientCalendarAdapter.OnClientCalendarItemClick) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClientCalendarItemClick = null;
    }
}
