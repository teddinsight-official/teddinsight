package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.evernote.android.state.State;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsightchat.models.User;

public class ClientListFragment extends Fragment {

    private static final String LOG_TAG = ClientListFragment.class.getSimpleName();
    @BindView(R.id.list_recyclerView)
    RecyclerView clientListRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyViewTextView;
    private ClientItemClickedListener clientItemClickedListener;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ClientListAdapter clientListAdapter;
    @State
    boolean isAdminView = false;
    private String role;
    public static final String IS_ADMIN_VIEW = "isAdminView";
    public static final String USER_ROLE = "user_role";
    private SocialAccounts socialAccounts;
    private Context mContext;


    public static ClientListFragment NewInstance() {
        return new ClientListFragment();
    }

    public static ClientListFragment NewInstance(boolean isAdminView, String role) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_ADMIN_VIEW, isAdminView);
        bundle.putString(USER_ROLE, role);
        ClientListFragment clientListFragment = new ClientListFragment();
        clientListFragment.setArguments(bundle);
        return clientListFragment;
    }


    public static ClientListFragment NewInstance(String role, SocialAccounts socialAccounts) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_ROLE, role);
        bundle.putParcelable("socialAccount", Parcels.wrap(socialAccounts));
        ClientListFragment clientListFragment = new ClientListFragment();
        clientListFragment.setArguments(bundle);
        return clientListFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clientListAdapter = new ClientListAdapter();
        clientListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        clientListRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        clientListRecyclerView.setAdapter(clientListAdapter);
        Bundle bundle = getArguments();
        isAdminView = bundle != null && bundle.containsKey(IS_ADMIN_VIEW);
        role = bundle != null && bundle.containsKey(USER_ROLE) ? bundle.getString(USER_ROLE) : "";
        if (bundle != null && bundle.containsKey("socialAccount")) {
            socialAccounts = Parcels.unwrap(bundle.getParcelable("socialAccount"));
        }
        if (!TextUtils.isEmpty(role) && role.equalsIgnoreCase(User.USER_ADMIN))
            getActivityCast().setToolbarTitle(role + "s");
        Log.e(LOG_TAG, "" + isAdminView);
        if (isAdminView) {
            getClientList();
            getActivityCast().setToolbarTitle(role);
            toolbar.setVisibility(View.GONE);
        } else if (socialAccounts != null)
            getClientList();
        else
            getClientUploadList();
    }

    private void getClientList() {
        emptyViewTextView.setText(mContext.getString(R.string.no_registered_client, role));
        FirebaseDatabase.getInstance().getReference(User.getTableName()).orderByChild("role").equalTo(role).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> clients = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    clients.add(snapshot.getValue(User.class));
                }
                clientListAdapter.swapClientList(clients);
                if (clientListAdapter.getItemCount() > 0)
                    emptyViewTextView.setVisibility(View.GONE);
                else
                    emptyViewTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, "An error occurred while loading " + role + " list", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getClientUploadList() {
        FirebaseDatabase.getInstance().getReference("clientUploads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> clients = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    clients.add(snapshot.getKey());
                }
                clientListAdapter.swapClientUploadList(clients);
                if (clientListAdapter.getItemCount() > 0)
                    emptyViewTextView.setVisibility(View.GONE);
                else
                    emptyViewTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, "An error occurred while loading client list", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        clientItemClickedListener = (ClientItemClickedListener) context;
        mContext = context;
    }

    class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ClientListViewHolder> {
        List<String> clienUploadtList;
        private List<User> clientList;

        public ClientListAdapter() {
            this.clientList = new ArrayList<>();
            this.clienUploadtList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ClientListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false);
            return new ClientListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClientListViewHolder holder, int position) {
            if (isAdminView || socialAccounts != null)
                holder.bindClients(clientList.get(position));
            else
                holder.clientBusinessName.setText(clienUploadtList.get(position));
        }

        @Override
        public int getItemCount() {
            return isAdminView || socialAccounts != null ? clientList.size() : clienUploadtList.size();
        }

        private void swapClientUploadList(List<String> clienUploadtList) {
            this.clienUploadtList = clienUploadtList;
            notifyDataSetChanged();
        }

        private void swapClientList(List<User> clientList) {
            this.clientList = clientList;
            notifyDataSetChanged();
        }


        class ClientListViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.client_businessName)
            TextView clientBusinessName;
            @BindView(R.id.image)
            ImageView imageView;

            private ClientListViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    if (isAdminView || socialAccounts != null) {
                        clientItemClickedListener.onClientItemClicked(clientList.get(getAdapterPosition()), socialAccounts);
                    } else
                        clientItemClickedListener.onClientItemClicked(clienUploadtList.get(getAdapterPosition()));

                });
            }

            private void bindClients(User user) {
                clientBusinessName.setText(role.equalsIgnoreCase(User.USER_CLIENT) ? user.getBusinessName() : user.getFirstName().concat(" ").concat(user.getLastName()));
                if (user.role.equals(User.USER_CLIENT)) {
                    imageView.setVisibility(View.VISIBLE);
                    String businesslogo = (user.getProfileImageUrl() == null || TextUtils.isEmpty(user.getProfileImageUrl())) ?
                            "https://i.cbc.ca/1.4663764!/fileImage/httpImage/image.jpg_gen/derivatives/16x9_1180/business-video-thumbnail.jpg" :
                            user.getProfileImageUrl();
                    Glide.with(mContext).load(businesslogo)
                            .apply(new RequestOptions().placeholder(R.drawable.loading_img))
                            .into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }
        }
    }

    public AdminActivity getActivityCast() {
        return (AdminActivity) getActivity();
    }

    public interface ClientItemClickedListener {
        void onClientItemClicked(String businessName);

        void onClientItemClicked(User client, SocialAccounts socialAccounts);
    }
}
