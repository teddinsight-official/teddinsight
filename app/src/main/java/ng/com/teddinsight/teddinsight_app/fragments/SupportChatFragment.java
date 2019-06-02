package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.TicketMessageAdapter;
import ng.com.teddinsight.teddinsight_app.models.TicketMessages;
import ng.com.teddinsight.teddinsight_app.models.Tickets;


public class SupportChatFragment extends Fragment {
    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mTicketMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<TicketMessages> TicketMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private TicketMessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private String mTicketId = "";
    View v;
    private String firstName;
    private String lastName;
    private SharedPreferences sharedPreferences;
    Tickets tickets;
    private TextView toolbarTitle;
    private Context mContext;

    public static SupportChatFragment NewInstance(Tickets tickets) {
        Bundle args = new Bundle();
        args.putParcelable("ticket", tickets);
        SupportChatFragment supportChatFragment = new SupportChatFragment();
        supportChatFragment.setArguments(args);
        return supportChatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_chat, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        firstName = sharedPreferences.getString("firstName", "client");
        lastName = sharedPreferences.getString("lastName", "client");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        tickets = getArguments().getParcelable("ticket");
        mTicketId = tickets.id;
        mChatUser = "";//getIntent().getStringExtra("user_id");
        String userName = "";//getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mChatSendBtn = (ImageButton) view.findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) view.findViewById(R.id.chat_message_view);

        mAdapter = new TicketMessageAdapter(TicketMessagesList);

        mTicketMessagesList = (RecyclerView) view.findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(mContext);

        toolbarTitle = view.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Ticket Conversation");

        mTicketMessagesList.setHasFixedSize(true);
        mTicketMessagesList.setLayoutManager(mLinearLayout);

        mTicketMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();
        loadTicketMessages();


        mChatSendBtn.setOnClickListener(view1 -> sendMessage());
        mRefreshLayout.setOnRefreshListener(() -> {

            mCurrentPage++;

            itemPos = 0;

            loadMoreTicketMessages();


        });

        if (tickets.status.equalsIgnoreCase("closed")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(tickets.category + " ticket closed")
                    .setMessage("This ticket has been closed, Can't send any more messages")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();

        }
    }

    private void loadMoreTicketMessages() {
        DatabaseReference messageRef = mRootRef.child("ticketConversations").child(mTicketId);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                TicketMessages message = dataSnapshot.getValue(TicketMessages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    TicketMessagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadTicketMessages() {
        DatabaseReference messageRef = mRootRef.child("ticketConversations").child(mTicketId);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                TicketMessages message = dataSnapshot.getValue(TicketMessages.class);

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                TicketMessagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mTicketMessagesList.scrollToPosition(TicketMessagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        if (tickets.status.equals("closed")) {
            Toast.makeText(mContext, "This ticket has been closed, cant send any more messages", Toast.LENGTH_LONG).show();
            return;
        }
        String message = mChatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ticketConversations").child(mTicketId);
            String push_id = ref.push().getKey();
            TicketMessages TicketMessages = new TicketMessages(message, mCurrentUserId, true, firstName, push_id);
            Map<String, Object> messageMap = TicketMessages.toMap();
            mChatMessageView.setText("");
            ref.child(push_id).updateChildren(messageMap, (databaseError, databaseReference) -> {

                if (databaseError != null) {
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }

            });

        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
