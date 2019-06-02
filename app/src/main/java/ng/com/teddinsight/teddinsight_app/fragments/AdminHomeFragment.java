package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;

public class AdminHomeFragment extends Fragment {

    public static AdminHomeFragment NewInstance() {
        return new AdminHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getAdminActivityCast().setToolbarTitle(getAdminActivityCast().getString(R.string.app_name));
    }

    private AdminActivity getAdminActivityCast() {
        return (AdminActivity) getActivity();
    }
}
