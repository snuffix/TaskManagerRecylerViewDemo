package com.github.snuffix.recyclerviewdemoapp;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.recycler_view_holder)
    public FrameLayout recyclerViewHolder;

    private RecyclerView recyclerView;

    private List<Task> tasks = new LinkedList<Task>();

    private TaskAdapter taskAdapter;

    private static final Bus BUS = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initTasks();

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter = new TaskAdapter());
        recyclerViewHolder.addView(recyclerView);
    }

    private void initTasks() {
        tasks.add(new Task("Wake up"));
        tasks.add(new Task("Go to work"));
        tasks.add(new Task("Make a coffee"));
        tasks.add(new Task("Go to standup"));
        tasks.add(new Task("Make a coffee"));
        tasks.add(new Task("Spend some time in chillout room"));
        tasks.add(new Task("Make a coffee"));
        tasks.add(new Task("Go home"));
        tasks.add(new Task("Make a coffee"));
        tasks.add(new Task("Sleep"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(taskAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BUS.unregister(taskAdapter);
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskRowHolder> {

        private SparseBooleanArray itemsState = new SparseBooleanArray();

        @Override
        public TaskRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TaskRowHolder(getLayoutInflater().inflate(R.layout.view_task_row, parent, false));
        }

        @Override
        public void onBindViewHolder(TaskRowHolder holder, int position) {
            holder.setTask(tasks.get(position), position);
            holder.setChecked(itemsState.get(position));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        @Subscribe
        public void onTaskCheckStateChangedEvent(TaskCheckStateChangedEvent event){
            itemsState.put(event.taskNumber, event.isChecked);
        }
    }

    class TaskRowHolder extends RecyclerView.ViewHolder {

        private TextView taskNameLabel;
        private CheckBox checkBox;

        private Task task;
        private int taskNumber;

        public TaskRowHolder(View itemView) {
            super(itemView);
            taskNameLabel = ButterKnife.findById(itemView, R.id.task_name);
            checkBox = ButterKnife.findById(itemView, R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    BusProvider.getInstance().post(new TaskCheckStateChangedEvent(isChecked, taskNumber));
                }
            });
        }

        public void setTask(Task task, int taskNumber) {
            this.task = task;
            this.taskNumber = taskNumber;
            taskNameLabel.setText(task.name);
        }

        public void setChecked(boolean checked) {
            checkBox.setChecked(checked);
        }
    }

    public class TaskRowItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable dividerDrawable;

        public TaskRowItemDecoration(Drawable drawable) {
            this.dividerDrawable = drawable.mutate();
        }

        @Override
        public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + dividerDrawable.getIntrinsicHeight();

                dividerDrawable.setBounds(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
                dividerDrawable.draw(canvas);
            }
        }
    }

    private class Task {
        String name;

        public Task(String name) {
            this.name = name;
        }
    }
}
