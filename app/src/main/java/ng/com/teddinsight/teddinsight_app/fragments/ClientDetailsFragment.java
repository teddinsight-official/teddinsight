package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.Job;
import ng.com.teddinsight.teddinsight_app.models.Notifications;
import ng.com.teddinsight.teddinsight_app.models.Receipts;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.widgets.TextViewDrawableSize;
import ng.com.teddinsight.teddinsightchat.models.User;

public class ClientDetailsFragment extends Fragment {
    private User user;
    @BindView(R.id.business_logo)
    CircleImageView logoImageView;
    @BindView(R.id.business_name)
    TextView businessName;
    @BindView(R.id.business_mail)
    TextViewDrawableSize businessMail;
    @BindView(R.id.business_number)
    TextViewDrawableSize businessNumber;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.client_services_recycler_view)
    RecyclerView servicesRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.services_header)
    TextView serviceHeaderView;
    @BindView(R.id.add_job)
    FloatingActionButton actionButton;
    private ServicesRecyclerViewAdapter servicesRecyclerViewAdapter;
    private DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    TextView servicePakage;
    TextView packageDescription;
    TextView packageDate;
    SeekBar workProgress;
    TextView progressText;
    private DatabaseReference rootRef;
    private DatabaseReference jobRef;
    public static final String LOG_TAG = ClientDetailsFragment.class.getSimpleName();
    ClientServiceClickListener clientServiceClickListener;
    int newProgress;
    private Context mContext;


    public static ClientDetailsFragment NewInstance(User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        ClientDetailsFragment clientDetailsFragment = new ClientDetailsFragment();
        clientDetailsFragment.setArguments(bundle);
        return clientDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_details, container, false);
        ButterKnife.bind(this, view);
        rootRef = FirebaseDatabase.getInstance().getReference();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey("user"))
            user = args.getParcelable("user");
        else {
            getChildFragmentManager().popBackStack();
            return;
        }
        setUpAndGetServices();
        clientServiceClickListener = new ClientServiceClickListener() {
            @Override
            public void onClientServiceClicked(Receipts service) {
                showAlertDialog(service);
            }

            @Override
            public void onJobClicked(Job job) {
                Receipts jobService = new Receipts(job.getPostedOn(), job.getDueOn(), job.getProgress(), job.getJobId(), job.getJobType(), job.getStatus());
                showAlertDialog(jobService);
            }
        };
    }

    private void showAlertDialog(Receipts service) {
        newProgress = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = getLayoutInflater().inflate(R.layout.service_details, null, false);
        builder.setView(dialogView);
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                showToast("Updating work progress", Toast.LENGTH_SHORT);
                service.progress = newProgress;
                FirebaseDatabase.getInstance().getReference().child(Receipts.getTableName()).child(user.getId())
                        .child(service.getServiceId()).setValue(service.toMap()).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        showToast("Progress Updated", Toast.LENGTH_LONG);
                    else
                        showToast(task.getException().getMessage(), Toast.LENGTH_LONG);
                });
            }
            dialog.cancel();
        };
        servicePakage = dialogView.findViewById(R.id.service_package);
        packageDescription = dialogView.findViewById(R.id.package_description);
        packageDate = dialogView.findViewById(R.id.date);
        workProgress = dialogView.findViewById(R.id.progress);
        progressText = dialogView.findViewById(R.id.progress_text);
        if (user.role.equalsIgnoreCase(User.USER_CLIENT)) {
            servicePakage.setText(service.service);
            packageDescription.setText("" + service.getCustomService());
        } else {
            servicePakage.setText(service.name);
            String desc = "Job due on " + ExtraUtils.getHumanReadableString(service.due);
            if (service.getStatus() == 100) {
                desc += "\n Partner has rejected request";
            } else if (service.getStatus() == 0)
                desc += "\n Partner has not accepted request";
            else
                desc += "\n Partner has accepted request";

            packageDescription.setText(desc);
        }
        newProgress = service.progress;
        workProgress.setProgress(service.progress);
        progressText.setText(String.valueOf(service.progress).concat(" %"));
        packageDate.setText(ExtraUtils.getHumanReadableString(service.dateIssued));
        workProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < service.progress || user.role.equalsIgnoreCase(User.USER_PARTNER)) {
                    workProgress.setProgress(newProgress);
                    return;
                }
                newProgress = progress;
                progressText.setText(String.valueOf(newProgress).concat(" %"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (user.role.equalsIgnoreCase(User.USER_CLIENT))
            builder.setPositiveButton("Update Progress", dialogClickListener);
        builder.setNegativeButton("Cancel", dialogClickListener);
        builder.show();
    }

    private void setUpAndGetServices() {
        if (user.getProfileImageUrl() == null || TextUtils.isEmpty(user.getProfileImageUrl()))
            logoImageView.setImageResource(R.drawable.avatar);
        else
            Picasso.get().load(user.getProfileImageUrl()).into(logoImageView);
        if (user.role.equalsIgnoreCase(User.USER_CLIENT)) {
            emptyView.setText(mContext.getString(R.string.no_service_req));
            businessName.setText(user.getBusinessName());
        } else {
            actionButton.setVisibility(View.VISIBLE);
            serviceHeaderView.setText("Partner's Jobs");
            emptyView.setText(mContext.getString(R.string.no_jobs_assigned));
            businessName.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
        }
        businessMail.setText(user.getEmail());
        businessNumber.setText(user.getPhoneNumber());
        if (user.role.equalsIgnoreCase(User.USER_CLIENT))
            databaseReference = FirebaseDatabase.getInstance().getReference().child(Receipts.getTableName()).child(user.getId());
        else
            databaseReference = FirebaseDatabase.getInstance().getReference().child(Job.getTableName()).child(user.getId());
        servicesRecyclerViewAdapter = new ServicesRecyclerViewAdapter();
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        servicesRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        servicesRecyclerView.setAdapter(servicesRecyclerViewAdapter);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.CYAN, Color.GREEN, Color.MAGENTA);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> getServices());
        getServices();
    }


    private void getServices() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(LOG_TAG, "On data changed");
                if (user.role.equalsIgnoreCase(User.USER_CLIENT)) {
                    List<Receipts> services = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Receipts service = snapshot.getValue(Receipts.class);
                        service.setServiceId(snapshot.getKey());
                        services.add(service);
                    }
                    servicesRecyclerViewAdapter.swapData(services);
                } else {
                    List<Job> jobs = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Job job = snapshot.getValue(Job.class);
                        jobs.add(job);
                    }
                    servicesRecyclerViewAdapter.swapJobs(jobs);
                }
                emptyView.setVisibility(servicesRecyclerViewAdapter.getItemCount() < 1 ? View.VISIBLE : View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
                showToast(databaseError.getMessage(), Toast.LENGTH_LONG);
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    @OnClick(R.id.add_job)
    public void addJob() {
        Job job = new Job();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View promptView = layoutInflater.inflate(R.layout.new_task_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptView);
        TextView headerTextView = promptView.findViewById(R.id.new_task_header);
        headerTextView.setText(getString(R.string.new_task_header, user.getRole()));
        final EditText taskTitle = (EditText) promptView.findViewById(R.id.task_title);
        final EditText task_description = (EditText) promptView.findViewById(R.id.task_description);
        final EditText task_due_date = (EditText) promptView.findViewById(R.id.due_date);
        Calendar calendar = Calendar.getInstance();
        task_due_date.setOnClickListener((v) -> {
            DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                view.setVisibility(View.GONE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, (view1, hourOfDay, minute) -> {
                    calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        showToast("Due date cannot be less than or equal to now, Job not assigned", Toast.LENGTH_SHORT);
                        return;
                    }
                    task_due_date.setText(ng.com.teddinsight.teddinsightchat.utils.ExtraUtils.getHumanReadableString(calendar.getTimeInMillis()));
                    job.setDueOn(calendar.getTimeInMillis());
                    view1.setVisibility(View.GONE);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, listener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Add Task", (dialog, id) -> {
                    String task_title = taskTitle.getText().toString().trim();
                    String task_desc = task_description.getText().toString().trim();
                    job.setJobType(task_title);
                    job.setJobDescription(task_desc);
                    job.setOfferExpiresOn(job.getDueOn());
                    job.setPostedOn(System.currentTimeMillis());
                    job.setProgress(0);
                    sendJob(job);
                })
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void sendJob(Job job) {
        showToast("Sending Job Request", Toast.LENGTH_SHORT);
        Random rando = new Random();
        job.setPendingIntentId(rando.nextInt(100) + 1);
        jobRef = rootRef.child(Job.getTableName()).child(user.getId());
        String key = jobRef.push().getKey();
        jobRef.child(key).setValue(job).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Job Request sent", Toast.LENGTH_SHORT);
                rootRef.child(Notifications.getTableName()).child(user.id).child("newJobReceived")
                        .setValue(true).addOnCompleteListener(task1 -> {

                });
            } else
                showToast("an error occurred while sending task, try again", Toast.LENGTH_SHORT);
        });
    }


    private void showToast(String message, int length) {
        if (mContext != null)
            Toast.makeText(mContext, message, length).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListener != null)
            databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    class ServicesRecyclerViewAdapter extends RecyclerView.Adapter<ServicesRecyclerViewAdapter.ServivesRecyclerViewHolder> {

        List<Receipts> servicesList;
        List<Job> jobList;

        public ServicesRecyclerViewAdapter() {
            this.servicesList = new ArrayList<>();
            this.jobList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ServivesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ServivesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ServivesRecyclerViewHolder holder, int position) {
            if (user.role.equalsIgnoreCase(User.USER_CLIENT)) {
                Receipts service = servicesList.get(getItemCount() - position - 1);
                holder.serviceDescription.setText(service.service.concat(" Package requested on ").concat(ExtraUtils.getHumanReadableString(service.dateIssued)));
            } else {
                Job job = jobList.get(getItemCount() - position - 1);
                holder.serviceDescription.setText(job.getJobType());
            }
        }

        @Override
        public int getItemCount() {
            return user.role.equalsIgnoreCase(User.USER_CLIENT) ? servicesList.size() : jobList.size();
        }

        public void swapData(List<Receipts> servicesList) {
            this.servicesList = servicesList;
            notifyDataSetChanged();
        }

        public void swapJobs(List<Job> jobs) {
            this.jobList = jobs;
            notifyDataSetChanged();
        }

        class ServivesRecyclerViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.client_businessName)
            TextView serviceDescription;

            public ServivesRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    if (user.role.equalsIgnoreCase(User.USER_CLIENT))
                        clientServiceClickListener.onClientServiceClicked(servicesList.get(getItemCount() - getAdapterPosition() - 1));
                    else
                        clientServiceClickListener.onJobClicked(jobList.get(getItemCount() - getAdapterPosition() - 1));
                });
            }
        }
    }

    interface ClientServiceClickListener {
        void onClientServiceClicked(Receipts service);

        void onJobClicked(Job job);
    }
}
