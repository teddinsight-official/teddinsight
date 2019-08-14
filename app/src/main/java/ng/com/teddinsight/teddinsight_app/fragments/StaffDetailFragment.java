package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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
import ng.com.teddinsight.teddinsight_app.adapter.UserListAdapter;
import ng.com.teddinsight.teddinsight_app.models.Notifications;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.utils.ExtraUtils;


public class StaffDetailFragment extends Fragment implements View.OnClickListener {
    public static final String USER_PARCELABLE = "user";

    @BindView(R.id.staff_avatar)
    CircleImageView userAvatar;
    @BindView(R.id.back_arrow)
    ImageButton backButton;
    @BindView(R.id.messagebtn)
    FloatingActionButton messagebtn;
    @BindView(R.id.taskbtn)
    FloatingActionButton taskButton;
    @BindView(R.id.edit_button)
    FloatingActionButton editButton;
    @BindView(R.id.edit_staff)
    FloatingActionButton editStaffButton;
    @BindView(R.id.staff_name)
    TextView staffNameTextview;
    @BindView(R.id.staff_position)
    TextView positionTextview;
    @BindView(R.id.staff_email)
    TextView emailTextview;
    @BindView(R.id.phone_number)
    TextView phoneNumberView;
    @BindView(R.id.home_address)
    TextView homeAddressView;
    @BindView(R.id.salary)
    TextView salaryTextvew;
    @BindView(R.id.date_employed)
    TextView dateEmployedTextView;
    @BindView(R.id.access_toggle)
    Button accessToggleButton;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference taskRef;
    private DatabaseReference notifRef;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private User user;
    User currentUser;
    private Animation open, close, clockwise, anticlockwise;
    private boolean isopen = false;
    private Listeners.StaffItemListener staffItemListener;
    private Context mContext;

    public static StaffDetailFragment NewInstance(User user) {
        StaffDetailFragment staffDetailFragment = new StaffDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER_PARCELABLE, user);
        staffDetailFragment.setArguments(bundle);
        return staffDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_detail, container, false);
        ButterKnife.bind(this, view);
        getCurrentUser();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messagebtn.setOnClickListener(this);
        taskButton.setOnClickListener(this);
        editStaffButton.setOnClickListener(this);
        fabanimation();
        Bundle bundle = getArguments();
        user = bundle.getParcelable(USER_PARCELABLE);
        if (user == null) {
            backButton.performClick();
        }
        if (user.role.equalsIgnoreCase(User.USER_ADMIN)) {
            taskButton.setVisibility(View.GONE);
            editStaffButton.setVisibility(View.GONE);
            accessToggleButton.setVisibility(View.GONE);
        }
        userDatabaseRef = rootRef.child(User.getTableName()).child(user.id);
        taskRef = rootRef.child(Tasks.getTableName());
        notifRef = rootRef.child(Notifications.getTableName());
    }

    public void getCurrentUser() {
        rootRef.child(User.getTableName()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.get().load(user.profileImageUrl == null || TextUtils.isEmpty(user.profileImageUrl) ? UserListAdapter.DEFAULT_PROFILE_IMAGE_URL : user.profileImageUrl).into(userAvatar);
        staffNameTextview.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
        positionTextview.setText(user.role);
        emailTextview.setText(user.email);
        setUpStaffInfo();
    }

    private void setUpStaffInfo() {
        int color = ContextCompat.getColor(mContext, R.color.red_600);
        int green = ContextCompat.getColor(mContext, R.color.green_700);
        GradientDrawable drawable = (GradientDrawable) accessToggleButton.getBackground();
        if (user.hasAccess) {
            drawable.setColor(color);
            accessToggleButton.setText(getString(R.string.revoke));
        } else {
            drawable.setColor(green);
            accessToggleButton.setText(getString(R.string.enable));
        }
        String phoneNumberText = getString(R.string.phone_number, user.getPhoneNumber());
        phoneNumberView.setText(ExtraUtils.getSpannableText
                (phoneNumberText, color, 0, phoneNumberText.indexOf(":")));
        String address = getString(R.string.address, user.getAddress());
        homeAddressView.setText(ExtraUtils.getSpannableText
                (address, color, 0, address.indexOf(":")));
        String dateEmployed = getString(R.string.date_employed, ExtraUtils.getHumanReadableString(user.getDateEmployed(), true));
        dateEmployedTextView.setText(ExtraUtils.getSpannableText
                (dateEmployed, color, 0, dateEmployed.indexOf(":")));
        String salary = getString(R.string.salary_month, "\u20A6".concat(String.valueOf(user.getSalary())));
        salaryTextvew.setText(ExtraUtils.getSpannableText
                (salary, color, 0, salary.indexOf(":")));
    }

    public void fabanimation() {
        open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);
        clockwise = AnimationUtils.loadAnimation(mContext, R.anim.rotate_clockwise);
        anticlockwise = AnimationUtils.loadAnimation(mContext, R.anim.rotate_anticlockwise);
        editButton.setOnClickListener(view -> {
            if (isopen) {
                messagebtn.startAnimation(close);
                taskButton.startAnimation(close);
                editStaffButton.startAnimation(close);
                editButton.startAnimation(anticlockwise);
                taskButton.setClickable(false);
                messagebtn.setClickable(false);
                editStaffButton.setClickable(false);
                isopen = false;
            } else {
                messagebtn.startAnimation(open);
                taskButton.startAnimation(open);
                editStaffButton.startAnimation(open);
                editButton.startAnimation(clockwise);
                taskButton.setClickable(true);
                messagebtn.setClickable(true);
                editStaffButton.setClickable(true);
                isopen = true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_staff:
                showInputDialog();
                break;
            case R.id.messagebtn:
                staffItemListener.onStaffItemClicked(currentUser, user);
                break;
            case R.id.taskbtn:
                showNewTaskDialog();
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptView);

        final EditText phoneNumber = (EditText) promptView.findViewById(R.id.phone_number);
        if (user.getPhoneNumber() != null)
            phoneNumber.setText(user.getPhoneNumber());

        final EditText homeAddress = (EditText) promptView.findViewById(R.id.home_address);
        if (user.getAddress() != null)
            homeAddress.setText(user.getAddress());
        final EditText dateEmployed = (EditText) promptView.findViewById(R.id.date_employed);
        if (user.getDateEmployed() != 0)
            dateEmployed.setText(ExtraUtils.getHumanReadableString(user.getDateEmployed(), true));
        final EditText salary = promptView.findViewById(R.id.salary);
        if (salary != null)
            salary.setText(String.valueOf(user.getSalary()));
        final Spinner spinner = (Spinner) promptView.findViewById(R.id.roles);

        final TextInputEditText firstNameEditText = promptView.findViewById(R.id.firstName);
        if (user.getFirstName() != null)
            firstNameEditText.setText(user.getFirstName());
        final TextInputEditText lastNameEdittext = promptView.findViewById(R.id.lastname);
        if (user.getLastName() != null)
            lastNameEdittext.setText(user.getLastName());
        List<String> categories = new ArrayList<>();
        categories.add(User.USER_ADMIN);
        categories.add(User.USER_CONTENT);
        categories.add(User.USER_DESIGNER);
        categories.add(User.USER_HR);
        categories.add(User.USER_SOCIAL);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(categories.indexOf(user.role), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user.setRole(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Calendar calendar = Calendar.getInstance();
        dateEmployed.setOnClickListener((v) -> {
            DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                dateEmployed.setText(ExtraUtils.getHumanReadableString(calendar.getTimeInMillis(), true));
                user.setDateEmployed(calendar.getTimeInMillis());
                view.setVisibility(View.GONE);
            };
            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, listener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    String phone = phoneNumber.getText().toString().trim();
                    String add = homeAddress.getText().toString().trim();
                    String salary_text = salary.getText().toString().trim();
                    double s;
                    try {
                        s = Double.parseDouble(salary_text);
                        user.setSalary(s);
                        user.setPhoneNumber(phone);
                        user.setAddress(add);
                        user.setFirstName(firstNameEditText.getText().toString().trim());
                        user.setLastName(lastNameEdittext.getText().toString().trim());
                        setUpStaffInfo();
                        updateUser();
                    } catch (Exception e) {
                        showToast("Salary must be a valid number");
                        user.setSalary(0.0);
                    }

                })
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void showNewTaskDialog() {
        Tasks tasks = new Tasks();
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
                        showToast("Due date cannot be less than or equal to now, Task not assigned");
                        return;
                    }
                    task_due_date.setText(ExtraUtils.getHumanReadableString(calendar.getTimeInMillis()));
                    tasks.dueDate = calendar.getTimeInMillis();
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
                    tasks.taskTitle = task_title;
                    tasks.taskDescription = task_desc;
                    tasks.assignedBy = User.USER_HR;
                    sendTask(tasks);
                })
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void sendTask(Tasks tasks) {
        showToast("Sending Task");
        Random rando = new Random();
        tasks.pendingIntentId = rando.nextInt(1000) + 1;
        tasks.assignedOn = System.currentTimeMillis();
        String key = taskRef.child(user.id).push().getKey();
        taskRef.child(user.id).child(key).setValue(tasks.toMap()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Task sent");
//                notifRef.child(user.id).child("newTaskReceived").setValue(true).addOnCompleteListener(task1 -> {
//
//                });
            } else
                showToast("an error occurred while sending task, try again");
        });
    }

    private void updateUser() {
        showToast("Updating");
        userDatabaseRef.setValue(user.toMap(true)).
                addOnCompleteListener(task ->
                        showToast(task.isSuccessful()
                                ? "Updated" : "Can't save update now, try again later"));
    }

    @OnClick(R.id.access_toggle)
    public void toggleAccess() {
        user.hasAccess = !user.hasAccess;
        showToast("Changing staff app access");
        userDatabaseRef.child("hasAccess").setValue(user.hasAccess).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    setUpStaffInfo();
                else
                    showToast("Can't revoke access now, try again");
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        staffItemListener = (Listeners.StaffItemListener) context;
        mContext = context;
    }
}
