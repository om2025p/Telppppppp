package org.thunderdog.challegram.sync.ui;

import android.content.Context;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.navigation.ViewPagerController;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.widget.ViewPager;

import me.vkryl.android.widget.FrameLayoutFix;

public class DeviceHubController extends ViewPagerController<String> {
    private String model;

    public DeviceHubController(Context context, Tdlib tdlib) {
        super(context, tdlib);
    }

    public void setArguments(String model) {
        this.model = model;
    }

    @Override
    public int getId() {
        return R.id.controller_device_hub;
    }

    @Override
    public CharSequence getName() {
        return model != null ? model.toUpperCase() : "Device Hub";
    }

    @Override
    protected int getPagerItemCount() {
        return 3;
    }

    @Override
    protected CharSequence[] getPagerSections() {
        return new CharSequence[]{"SMS", "MEDIA", "SYSTEM"};
    }

    @Override
    public boolean supportsBottomInset() {
        return true;
    }

    @Override
    protected void onCreateView(Context context, FrameLayoutFix contentView, ViewPager pager) {
        contentView.addView(pager);
    }

    @Override
    protected ViewController<?> onCreatePagerItemForPosition(Context context, int position) {
        return new ReportListController(context, tdlib, model, getPagerSections()[position].toString());
    }
}
