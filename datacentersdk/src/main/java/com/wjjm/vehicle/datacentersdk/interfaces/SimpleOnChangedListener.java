package com.wjjm.vehicle.datacentersdk.interfaces;

public abstract class SimpleOnChangedListener implements OnChangedListener {

    public abstract void onChanged(String value);

    @Override
    public final void onChanged(String key, String value) {
        onChanged(value);
    }
}
