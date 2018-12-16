package ng.com.teddinsight.teddinsight_app.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;

public class ConversationFragment extends Fragment {

    public static Fragment NewInstance(){
        return new ConversationFragment();
    }

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
