package com.team123.mobilecollector.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.team123.mobilecollector.R;
import com.team123.mobilecollector.model.SensorConnected;
import java.util.ArrayList;


public class ConnectedDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private final String TAG = ConnectedDevicesAdapter.class.getSimpleName();
    private Context mContext;
    private final ArrayList<SensorConnected> devices;

    public static class DeviceConnectedViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView positionTextView;
        private final TextView recordTextView;
        private final TextView batteryTextView;
        private final ImageView batteryImmageView;

        DeviceConnectedViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.selectedDeviceName_tv);
            positionTextView = itemView.findViewById(R.id.movesense_position);
            recordTextView = itemView.findViewById(R.id.movesense_record);
            batteryTextView = itemView.findViewById(R.id.movesense_battery_text);
            batteryImmageView = itemView.findViewById(R.id.movesense_baterry);
        }
        public void setRecord(String record){
            recordTextView.setText(record);
        }
        public void setBattery(String level){
            int int_level = Integer.parseInt(level);

            /*if(level.equals("100")){
                batteryImmageView.setImageResource(R.drawable.ic_battery_full_24dp);
            }
            */
            if(int_level == 100){
                batteryImmageView.setImageResource(R.drawable.ic_battery_full_24dp);
            }
            if(int_level >= 90 && int_level < 100){
                batteryImmageView.setImageResource(R.drawable.ic_battery_90_24dp);
            }
            if(int_level >= 50 && int_level < 90){
                batteryImmageView.setImageResource(R.drawable.ic_battery_90_24dp);
            }
            if(int_level >= 30 && int_level < 50){
                batteryImmageView.setImageResource(R.drawable.ic_battery_50_24dp);
            }
            batteryTextView.setText(level);
        }
    }

    public ConnectedDevicesAdapter(Context context) {
        mContext = context;
        devices = new ArrayList<SensorConnected>();
        setHasStableIds(true);
    }

        public void handleAddSensor(SensorConnected device) {
        devices.add(device);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movesense_connected, parent, false);
        final ConnectedDevicesAdapter.DeviceConnectedViewHolder viewHolder = new ConnectedDevicesAdapter.DeviceConnectedViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceConnectedViewHolder deviceViewHolder = ( DeviceConnectedViewHolder) holder;
        SensorConnected device = devices.get(position);
        deviceViewHolder.nameTextView.setText(device.getSerial());
        deviceViewHolder.positionTextView.setText(device.getLocation());
        //deviceViewHolder.batteryTextView.setText(device.getBatteryLevel());
    }

    @Override
    public long getItemId(int position) {
        return devices.get(position).getSensor().getMacAddress().hashCode();
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
