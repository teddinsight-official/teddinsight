package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;

public class ClientListFragment extends Fragment {

    @BindView(R.id.client_list_recyclerView)
    RecyclerView clientListRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyViewTextView;
    ClientItemClickedListener clientItemClickedListener;
    ClientListAdapter clientListAdapter;

    public static ClientListFragment NewInstance() {
        return new ClientListFragment();
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
        clientListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clientListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        clientListRecyclerView.setAdapter(clientListAdapter);
        getClientList();
    }

    private void getClientList() {
        FirebaseDatabase.getInstance().getReference("clientUploads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> clients = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    clients.add(snapshot.getKey());
                }
                clientListAdapter.swapClientList(clients);
                if (clientListAdapter.getItemCount() > 0)
                    emptyViewTextView.setVisibility(View.GONE);
                else
                    emptyViewTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "An error occurred while loading client list", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        clientItemClickedListener = (ClientItemClickedListener) context;
    }

    class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ClientListViewHolder> {
        List<String> clientList;

        private ClientListAdapter() {
            this.clientList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ClientListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false);
            return new ClientListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClientListViewHolder holder, int position) {
            holder.clientBusinessName.setText(clientList.get(position));
        }

        @Override
        public int getItemCount() {
            return clientList.size();
        }

        public void swapClientList(List<String> clientList) {
            this.clientList = clientList;
            notifyDataSetChanged();
        }

        class ClientListViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.client_businessName)
            TextView clientBusinessName;

            public ClientListViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    clientItemClickedListener.onClientItemClicked(clientList.get(getAdapterPosition()));
                });
            }
        }
    }

    public interface ClientItemClickedListener {
        void onClientItemClicked(String businessName);
    }
}
