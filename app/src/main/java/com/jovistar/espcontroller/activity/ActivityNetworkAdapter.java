package com.jovistar.espcontroller.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jovistar.espcontroller.R;
import com.jovistar.espcontroller.dto.Device;
import com.jovistar.espcontroller.task.TaskDiscoverControllers;

import java.util.List;

/**
 * Created by jovika on 4/14/2018.
 */

public class ActivityNetworkAdapter extends RecyclerView.Adapter<ActivityNetworkAdapter.ViewHolder> {
    private static final String TAG = "ActivityNetworkAdapter";
    private List<Device> deviceList;
    private ActivityNetwork context;

    public ActivityNetworkAdapter(Context context, List<Device> itemList) {
        this.deviceList = itemList;
        this.context = (ActivityNetwork) context;
    }

    @Override
    public ActivityNetworkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_network_grid_item, parent,
                false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ActivityNetworkAdapter.ViewHolder holder, int position) {
        holder.icon.setImageResource(R.drawable.ic_blur_circular_white);
        holder.label.setText(deviceList.get(position).getDeviceName());
        holder.subject.setText(deviceList.get(position).getDeviceLocation());
        holder.position = position;
    }

    // 11122019 commented, not used
//    public void add(Device device) {
//        deviceList.add(device);
//    }

    public void clear() {
        deviceList.clear();
    }

    @Override
    public int getItemCount() {
        return (null != deviceList ? deviceList.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Declaration of Views

        int position;
        ImageButton icon;
        TextView label;
        TextView subject;
        View rowView;

        public ViewHolder(View view, int i) {
            super(view);

            // Find Views
            this.rowView = view.findViewById(R.id.activity_network_device);
            this.icon = view.findViewById(R.id.activity_network_device_status_icon);
            this.label = view.findViewById(R.id.activity_network_device_status_label);
            this.subject = view.findViewById(R.id.activity_network_device_status_subject);

            this.rowView.setOnClickListener(this);
            this.rowView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(final View view) {

            new TaskDiscoverControllers(context, context, deviceList.get(position).getUID()).execute(null, null);
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
