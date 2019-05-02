package ng.com.teddinsight.teddinsight_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsightchat.models.Messages;


public class MessageOldAdapter extends RecyclerView.Adapter<MessageOldAdapter.MessageViewHolder> {


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    private final int LAYOUT_TYPE_ME = 1;
    private final int LAYOUT_TYPE_OTHERS = 2;

    public MessageOldAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case LAYOUT_TYPE_ME:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_me_layout, parent, false);
                break;
            case LAYOUT_TYPE_OTHERS:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_others_layout, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_others_layout, parent, false);
        }
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            //profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.sender_name);
            //messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages m = mMessageList.get(position);
        if (m.from.equals(FirebaseAuth.getInstance().getUid()))
            return LAYOUT_TYPE_ME;
        else return LAYOUT_TYPE_OTHERS;
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.from;
        boolean isText = c.isText;

        try {
            viewHolder.displayName.setText(c.senderName);
        } catch (Exception ignored) {

        }
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);


        if (isText) {
            viewHolder.messageText.setText(c.message);
        } else {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.message)
                    .placeholder(R.drawable.ic_add_24dp).into(viewHolder.messageImage);

        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}






