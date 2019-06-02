package ng.com.teddinsight.teddinsight_app.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewholder> {

    public static final String DEFAULT_PROFILE_IMAGE_URL = "https://png.pngtree.com/svg/20161021/de74bae88b.png";
    private List<User> userList;
    private Listeners.UserItemClickListener listener;

    public UserListAdapter() {
        this.userList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserListViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new UserListViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewholder holder, int position) {

        User user = userList.get(position);


        Picasso.get().load(user.profileImageUrl == null || TextUtils.isEmpty(user.profileImageUrl)
                ? DEFAULT_PROFILE_IMAGE_URL
                : user.profileImageUrl).into(holder.staffAvatar);

        holder.emailTextview.setText(user.email);
        holder.nameTextview.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
        holder.positionTextview.setText(user.role);

    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        this.notifyDataSetChanged();
    }

    public void setUserItemListener(Listeners.UserItemClickListener userItemListener) {
        this.listener = userItemListener;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserListViewholder extends RecyclerView.ViewHolder {
        @BindView(R.id.staff_avatar)
        CircleImageView staffAvatar;
        @BindView(R.id.staff_email)
        TextView emailTextview;
        @BindView(R.id.staff_name)
        TextView nameTextview;
        @BindView(R.id.staff_position)
        TextView positionTextview;
        View v;


        public UserListViewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                listener.onUserItemClicked(userList.get(getAdapterPosition()));
            });
        }
    }
}
