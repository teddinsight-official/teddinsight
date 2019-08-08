package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.downloader.Progress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ng.com.teddinsight.teddinsight_app.adapter.CalendarReportAdapter;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel.HeaderTags.COMPLETED;
import static ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel.HeaderTags.COMPLETEDAFTERDEADLINE;
import static ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel.HeaderTags.COMPLETEDBEFOREDEADLINE;
import static ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel.HeaderTags.INCOMPLETE;

public class CalendarReportViewModel extends ViewModel {

    private ValueEventListener myValueEventListener;
    private Query taskQuery;
    private MutableLiveData<String> _message = new MutableLiveData<>();
    private MutableLiveData<Pie> _pie = new MutableLiveData<>();
    private MutableLiveData<List<ReportListItem>> _repostList = new MutableLiveData<>();


    public LiveData<List<ReportListItem>> reportList() {
        return _repostList;
    }

    public LiveData<Pie> pie() {
        return _pie;
    }

    public LiveData<String> message() {
        return _message;
    }

    public CalendarReportViewModel(ClientCalendar clientCalendar) {
        _message.setValue(null);
        myValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ReportListItem> allReportItemList = new ArrayList<>();
                List<ReportItem> completedTasks = new ArrayList<>();
                List<ReportItem> completedAfterDeadline = new ArrayList<>();
                List<ReportItem> incompleteTask = new ArrayList<>();
                List<ReportItem> completedBeforeDeadline = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Tasks tasks = snapshot.getValue(Tasks.class);
                        if (tasks == null)
                            continue;
                        tasks.setId(snapshot.getKey());
                        ReportItem reportItem = new ReportItem(tasks.assignedTo, tasks.dueDate, tasks.taskTitle, tasks.getId());
                        if (tasks.getStatus() > 0) {
                            reportItem.setWasTaskCompleted(true);
                            reportItem.setDateCompleted(tasks.dateCompleted);
                            completedTasks.add(reportItem);
                        } else {
                            reportItem.setWasTaskCompleted(false);
                            incompleteTask.add(reportItem);
                        }

                        if (tasks.getStatus() > 0 && (tasks.getDueDate() <= tasks.dateCompleted)) {
                            reportItem.setWasTaskCompleted(true);
                            completedAfterDeadline.add(reportItem);
                        }
                        if (tasks.getStatus() > 0 && (tasks.getDueDate() > tasks.dateCompleted)) {
                            reportItem.setWasTaskCompleted(true);
                            completedBeforeDeadline.add(reportItem);
                        }
                    }
                }
                Pie pie = AnyChart.pie();
                pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
                    @Override
                    public void onClick(Event event) {
                        _message.setValue(event.getData().get("x") + ":" + event.getData().get("value"));
                    }
                });
                List<DataEntry> data = new ArrayList<>();
                data.add(new ValueDataEntry("Done", completedTasks.size()));
                data.add(new ValueDataEntry("Pending", incompleteTask.size()));
                data.add(new ValueDataEntry("Done Before Deadline", completedBeforeDeadline.size()));
                data.add(new ValueDataEntry("Done After Deadline", completedAfterDeadline.size()));

                pie.data(data);
                pie.title("Task Report for " + clientCalendar.getName() + " created on " + ExtraUtils.getHumanReadableString(clientCalendar.getDateCreated(), true));

                pie.labels().position("outside");

                pie.legend().title().enabled(true);
                pie.legend().title()
                        .text("Tasks Status")
                        .padding(0d, 0d, 10d, 0d);

                pie.legend()
                        .position("center-bottom")
                        .itemsLayout(LegendLayout.HORIZONTAL)
                        .align(Align.CENTER);
                _pie.setValue(pie);
                allReportItemList.add(new HeaderItem(COMPLETED, completedTasks));
                allReportItemList.addAll(completedTasks);
                allReportItemList.add(new HeaderItem(INCOMPLETE, incompleteTask));
                allReportItemList.addAll(incompleteTask);
                allReportItemList.add(new HeaderItem(COMPLETEDBEFOREDEADLINE, completedBeforeDeadline));
                allReportItemList.addAll(completedBeforeDeadline);
                allReportItemList.add(new HeaderItem(COMPLETEDAFTERDEADLINE, completedAfterDeadline));
                allReportItemList.addAll(completedAfterDeadline);
                _repostList.setValue(allReportItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        taskQuery = FirebaseDatabase.getInstance().getReference().child(ClientCalendar.getTableName()).child(clientCalendar.getKey()).child("tasks");
        taskQuery.addValueEventListener(myValueEventListener);
    }

    @BindingAdapter("setReportList")
    public static void setReportList(RecyclerView recyclerView, List<ReportListItem> reportListItems) {
        CalendarReportAdapter adapter = (CalendarReportAdapter) recyclerView.getAdapter();
        if (adapter == null) {
            adapter = new CalendarReportAdapter(new CalendarReportAdapter.ReportDiffUtil());
            recyclerView.setAdapter(adapter);
        }
        adapter.submitList(reportListItems);
    }

    public void stopMessageDispatch() {
        _message.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        taskQuery.removeEventListener(myValueEventListener);
        myValueEventListener = null;
    }

    public abstract class ReportListItem {
        public abstract boolean isHeader();

        public abstract String getUniqueId();

        final String uniqueId;

        public ReportListItem(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }
    }

    public enum HeaderTags {
        COMPLETED("Completed Tasks"),
        INCOMPLETE("Pending Tasks"),
        COMPLETEDBEFOREDEADLINE("Tasks completed before deadline"),
        COMPLETEDAFTERDEADLINE("Tasks completed after deadline");

        public String getValue;

        HeaderTags(String tag) {
            this.getValue = tag;
        }
    }

    public class ReportItem extends ReportListItem {

        private String staffName;
        private String taskAssigned;
        private long deadline;
        private long dateCompleted;
        private boolean wasTaskCompleted;

        public ReportItem(String staffName, long deadline, String taskAssigned, String id) {
            super(id);
            this.staffName = staffName;
            this.taskAssigned = taskAssigned;
            this.deadline = deadline;
        }

        public void setDateCompleted(long dateCompleted) {
            this.dateCompleted = dateCompleted;
        }

        public void setWasTaskCompleted(boolean wasTaskCompleted) {
            this.wasTaskCompleted = wasTaskCompleted;
        }

        @Override
        public String getUniqueId() {
            return uniqueId;
        }


        public String getStaffName() {
            return staffName;
        }

        public String getTaskAssigned() {
            return taskAssigned;
        }

        public long getDeadline() {
            return deadline;
        }

        public long getDateCompleted() {
            return dateCompleted;
        }

        public boolean WasTaskCompleted() {
            return wasTaskCompleted;
        }

        @Override
        public boolean isHeader() {
            return false;
        }
    }

    public class HeaderItem extends ReportListItem {

        private String headerTagsText;
        private HeaderTags headerTags;

        public HeaderTags getHeaderTags() {
            return headerTags;
        }

        public String getHeaderTagsText() {
            return headerTagsText;
        }


        public HeaderItem(HeaderTags headerTags, List<ReportItem> reportListItems) {
            super(headerTags.getValue.concat(" (" + reportListItems.size() + ")"));
            this.headerTags = headerTags;
            this.headerTagsText = headerTags.getValue.concat(" (" + reportListItems.size() + ")");
        }

        @Override
        public boolean isHeader() {
            return true;
        }

        @Override
        public String getUniqueId() {
            return uniqueId;
        }
    }
}