package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.Tickets;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.utils.ExtraUtils.getColor;

public class CustomerSupportFragment extends Fragment implements Listeners.TicketItemClicked {

    private final String TAG = this.getClass().getSimpleName();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @BindView(R.id.ticket_recyclerView)
    RecyclerView ticketsRecyclerView;
    TicketAdapter ticketAdapter;
    @BindView(R.id.ticket_swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.no_tickets)
    TextView noTicketsView;
    private Context mContext;

    public static Fragment NewInstance() {
        return new CustomerSupportFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragments_tickets, container, false);
        ButterKnife.bind(this, v);
        refreshLayout.setColorSchemeColors(Color.CYAN, Color.RED, Color.GREEN);
        refreshLayout.setRefreshing(true);
        refreshLayout.setOnRefreshListener(() -> getTickets());
        ticketAdapter = new TicketAdapter(new ArrayList<>(), this);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        ticketsRecyclerView.setAdapter(ticketAdapter);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTickets();
    }

    private void getTickets() {
        FirebaseDatabase.getInstance().getReference("tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Tickets> ticketsList = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ticketSnapshot : dataSnapshot.getChildren()) {
                        Tickets tickets = ticketSnapshot.getValue(Tickets.class);
                        ticketsList.add(tickets);
                    }
                    noTicketsView.setVisibility(View.GONE);
                } else {
                    noTicketsView.setText(getString(R.string.no_tickets_opened));
                    noTicketsView.setVisibility(View.VISIBLE);
                }
                refreshLayout.setRefreshing(false);
                ticketAdapter.swapData(ticketsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Can't retrieve tickets at the moment");
            }
        });
    }

    private void showToast(String message) {
        refreshLayout.setRefreshing(false);
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTicketItemClicked(Tickets tickets) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getAdminActivityCast());
        builder.setTitle("Ticket details");
        builder.setMessage("Ticked opened by: " + tickets.senderName + "\nOn: " + ExtraUtils.getHumanReadableString(tickets.openedOn) + "\nCategory: " + tickets.category + "\nTitle: " + tickets.title);
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (!tickets.reviewed) {
                        FirebaseDatabase.getInstance().getReference().child("tickets").child(tickets.id).child("reviewed").setValue(true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("ok");
                            } else
                                showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);

                        });
                    }
                    showTicketMessages(tickets);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    showToast("Closing ticket now");
                    tickets.status = "closed";
                    tickets.closedOn = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("tickets").child(tickets.id).setValue(tickets).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                showToast("Ticket closed", Toast.LENGTH_SHORT);
                            else
                                showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
                    break;
            }
            dialog.dismiss();
        };

        builder.setPositiveButton("Review", onClickListener)
                .setNegativeButton("Close Ticket", onClickListener)
                .setNeutralButton("View Later", onClickListener);
        builder.show();

    }

    private void showTicketMessages(Tickets tickets) {
        FragmentTransaction ft = getAdminActivityCast().getSupportFragmentManager().beginTransaction();
        assert getFragmentManager() != null;
        Fragment prev = getFragmentManager().findFragmentByTag("detail");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.replace(R.id.frame_container, SupportChatFragment.NewInstance(tickets)).addToBackStack(null).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        getAdminActivityCast().setToolbarTitle("Tickets");
    }

    private AdminActivity getAdminActivityCast() {
        return (AdminActivity) getActivity();
    }

    private void showToast(String message, int length) {
        if (mContext != null)
            Toast.makeText(mContext, message, length).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {


        List<Tickets> ticketsList;
        Listeners.TicketItemClicked ticketItemClicked;


        TicketAdapter(List<Tickets> tickets, Listeners.TicketItemClicked ticketItemClicked) {
            this.ticketsList = tickets;
            this.ticketItemClicked = ticketItemClicked;
        }

        @NonNull
        @Override
        public TicketAdapter.TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
            return new TicketViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
            Tickets tickets = ticketsList.get(getItemCount() - position - 1);
            holder.status.setText(tickets.status);
            holder.ticketTitleView.setText(tickets.title);
            GradientDrawable magnitudeCircle = (GradientDrawable) holder.circle.getBackground();
            int magnitudeColor = getColor(tickets.reviewed ? 1 : 0);
            magnitudeCircle.setColor(magnitudeColor);
        }


        @Override
        public int getItemCount() {
            return ticketsList.size();
        }

        void swapData(List<Tickets> tickets) {
            this.ticketsList = tickets;
            this.notifyDataSetChanged();
        }

        class TicketViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.ticket_title)
            TextView ticketTitleView;
            @BindView(R.id.status)
            TextView status;
            @BindView(R.id.circle)
            TextView circle;

            TicketViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    ticketItemClicked.onTicketItemClicked(ticketsList.get(getItemCount() - getAdapterPosition() - 1));
                });
            }
        }

    }
}
