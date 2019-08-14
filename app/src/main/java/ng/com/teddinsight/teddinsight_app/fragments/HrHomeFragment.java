package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.DCSHomeActivity;
import ng.com.teddinsight.teddinsight_app.adapter.UserListAdapter;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.utils.SwipeToDeleteCallback;
import ng.com.teddinsight.teddinsightchat.models.Notifications;
import ng.com.teddinsight.teddinsightchat.models.User;

public class HrHomeFragment extends Fragment {

    public static final String LOG_TAG = HrHomeFragment.class.getSimpleName();

    @BindView(R.id.hr_mail)
    TextView hrMailTextview;
    @BindView(R.id.hr_name)
    TextView hrNameView;
    @BindView(R.id.profile_image)
    CircleImageView profileImageView;
    @BindView(R.id.notif_text)
    TextView notifTextView;
    @BindView(R.id.user_recycler_view)
    RecyclerView userRecyclerView;
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.hr_root_view)
    View hrRootVIew;
    @BindView(R.id.task_notif)
    TextView taskNotif;

    private Listeners.UserItemClickListener userItemClickListener;
    private SharedPreferences preferences;
    private Context context;
    private AppCompatActivity activity;
    private FirebaseUser currentUser;
    DatabaseReference reference;
    DatabaseReference notifRef;
    ValueEventListener valueEventListener;
    private UserListAdapter userListAdapter;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private Context mContext;

    public static HrHomeFragment NewInstance() {
        return new HrHomeFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hr_home, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = mContext;
        activity = (AppCompatActivity) getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userMap = preferences.getString("user_map", "map");
        reference = FirebaseDatabase.getInstance().getReference();
        userListAdapter = new UserListAdapter();
        userListAdapter.setUserItemListener(userItemClickListener);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        userRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        userRecyclerView.setAdapter(userListAdapter);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @OnClick(R.id.addNewUser)
    public void addNewUser() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        Fragment prev = getChildFragmentManager().findFragmentByTag("adduser");
        if (prev != null)
            fragmentTransaction.remove(prev);
        WebViewFragment.NewInstance("https://teddinsight.com.ng/admin.php").show(fragmentTransaction, "adduser");

    }

    @Override
    public void onStart() {
        super.onStart();
        setUpUser();
        getAllStaffs();
        enableSwipeToDeleteAndUndo();
    }

    private void setUpUser() {
        reference.child(User.getTableName()).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                hrMailTextview.setText(u.email);
                hrNameView.setText(u.getFirstName().concat(" ").concat(u.getLastName()));
                Picasso.get().load(u.profileImageUrl == null || TextUtils.isEmpty(u.profileImageUrl)
                        ? "https://png.pngtree.com/svg/20161021/de74bae88b.png"
                        : u.profileImageUrl).into(profileImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        notifRef = reference.child(Notifications.getTableName()).child(currentUser.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(LOG_TAG, "new notification");
                Notifications notifications = dataSnapshot.getValue(Notifications.class);
                if (notifications != null) {
                    if (notifications.newTaskReceived) {
                        taskNotif.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext, "You have new tasks", Toast.LENGTH_LONG).show();
                    } else {
                        taskNotif.setVisibility(View.INVISIBLE);
                    }
                    int count = notifications.count;
                    if (count > 0) {
                        String countText = count > 99 ? "9+" : String.valueOf(count);
                        notifTextView.setText(countText);
                        notifTextView.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext, "You have unread messages", Toast.LENGTH_LONG).show();
                    } else {
                        notifTextView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        notifRef.addValueEventListener(valueEventListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        notifRef.removeEventListener(valueEventListener);
    }

    private void getAllStaffs() {
        reference.child(User.getTableName()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shimmerFrameLayout.stopShimmer();
                List<User> userList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user.id.equalsIgnoreCase(firebaseUser.getUid())
                            || user.role.equalsIgnoreCase(User.USER_CLIENT)
                            || user.role.equalsIgnoreCase(User.USER_PARTNER) || user.id.equalsIgnoreCase(currentUser.getUid()))
                        continue;
                    userList.add(user);
                }
                shimmerFrameLayout.setVisibility(View.GONE);
                userListAdapter.setUserList(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(mContext) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final User user = userListAdapter.getData().get(position);
                userListAdapter.removeItem(position);
                Handler handler = new Handler();
                Runnable runnable = () -> reference.child(User.getTableName()).child(user.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        showToast(user.getFirstName() + " deleted", Toast.LENGTH_SHORT);
                    else
                        userListAdapter.restoreItem(user, position);
                });
                handler.postDelayed(runnable, 3500);

                Snackbar snackbar = Snackbar
                        .make(hrRootVIew, "User Deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", view -> {
                    userListAdapter.restoreItem(user, position);
                    userRecyclerView.scrollToPosition(position);
                    handler.removeCallbacks(runnable);
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(userRecyclerView);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(mContext, message, duration).show();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        userItemClickListener = (Listeners.UserItemClickListener) context;
        mContext = context;
    }
}
