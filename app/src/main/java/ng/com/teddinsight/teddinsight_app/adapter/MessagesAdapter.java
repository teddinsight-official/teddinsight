package ng.com.teddinsight.teddinsight_app.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsightchat.models.Message;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MessagesAdapter extends FirebaseRecyclerAdapter<Message, MessagesAdapter.MessageViewHolder> {

    Context context;
    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_SENT_WITH_DATE = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_RECEIVED_WITH_DATE = 3;
    private ArrayList<Integer> selectedPositions;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MessagesAdapter(@NonNull FirebaseRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Message message) {
        messageViewHolder.itemMessageBodyTextView.setText(message.getBody());
        messageViewHolder.itemMessageDateTextView.setText(getDatePretty(message.getTimestamp(), true));
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_SENT:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                break;
            case VIEW_TYPE_SENT_WITH_DATE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                break;
            case VIEW_TYPE_RECEIVED:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                break;
            case VIEW_TYPE_RECEIVED_WITH_DATE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                break;
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
        }
        return new MessageViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.getFrom().equals(user.getUid())) {

            return VIEW_TYPE_SENT_WITH_DATE;

        } else {

            return VIEW_TYPE_RECEIVED_WITH_DATE;

        }
    }


    class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.item_message_date_text_view)
        TextView itemMessageDateTextView;
        @BindView(R.id.item_message_body_text_view)
        TextView itemMessageBodyTextView;
        @BindView(R.id.item_message_parent)
        ConstraintLayout itemMessageParent;

        MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemMessageBodyTextView.setOnClickListener(this);
            itemMessageBodyTextView.setOnLongClickListener(this);
        }

        void setMessage(Message message) {
            int viewType = MessagesAdapter.this.getItemViewType(getLayoutPosition());
            itemMessageBodyTextView.setText(message.getBody());
            boolean shouldHideDate = viewType == VIEW_TYPE_SENT || viewType == VIEW_TYPE_RECEIVED;
            itemMessageDateTextView.setVisibility(shouldHideDate ? GONE : VISIBLE);
            if (!shouldHideDate) {
                itemMessageDateTextView.setText(getDatePretty(message.getTimestamp(), true));
            }
        }

        @Override
        public void onClick(View v) {
            //if (selectedPositions.contains(getLayoutPosition())) {
            //    selectedPositions.remove(Integer.valueOf(getLayoutPosition()));
            //    setDateVisibility(GONE);
            //} else {
            //    selectedPositions.add(getLayoutPosition());
            //    setDateVisibility(VISIBLE);
            //}
        }

        private void setDateVisibility(int visibility) {
            TransitionManager.beginDelayedTransition(itemMessageParent);
            itemMessageDateTextView.setVisibility(visibility);
        }

        @Override
        public boolean onLongClick(View v) {
            Message message = getItem(getLayoutPosition());
            if (message != null) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(
                        context.getString(R.string.clipboard_title_copied_message),
                        message.getBody());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, R.string.message_message_copied, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    private String getDatePretty(long timestamp, boolean showTimeOfDay) {
        DateTime yesterdayDT = new DateTime(DateTime.now().getMillis() - 1000 * 60 * 60 * 24);
        yesterdayDT = yesterdayDT.withTime(0, 0, 0, 0);
        Interval today = new Interval(DateTime.now().withTimeAtStartOfDay(), Days.ONE);
        Interval yesterday = new Interval(yesterdayDT, Days.ONE);
        DateTimeFormatter timeFormatter = DateTimeFormat.shortTime();
        DateTimeFormatter dateFormatter = DateTimeFormat.shortDate();
        if (today.contains(timestamp)) {
            if (showTimeOfDay) {
                return timeFormatter.print(timestamp + TimeUnit.HOURS.toMillis(1));
            } else {
                return context.getString(R.string.today);
            }
        } else if (yesterday.contains(timestamp + TimeUnit.HOURS.toMillis(1))) {
            return context.getString(R.string.yesterday);
        } else {
            return dateFormatter.print(timestamp + TimeUnit.HOURS.toMillis(1));
        }
    }
}
