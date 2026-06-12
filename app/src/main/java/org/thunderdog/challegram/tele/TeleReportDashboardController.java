package org.thunderdog.challegram.tele;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.SettingsAdapter;
import java.util.ArrayList;
import java.util.List;

public class TeleReportDashboardController extends ViewController<Void> {
    private SettingsAdapter adapter;
    private TeleSyncManager syncManager;

    public TeleReportDashboardController(Context context, Tdlib tdlib) {
        super(context, tdlib);
    }

    @Override
    public int getId() {
        return R.id.controller_main; // وانمود می‌کنیم که همان صفحه اصلی هستیم
    }

    @Override
    protected View onCreateView(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Theme.getColor(ColorId.filling));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new SettingsAdapter(context, tdlib);
        recyclerView.setAdapter(adapter);

        layout.addView(recyclerView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        loadData();
        startSync();

        return layout;
    }

    private void startSync() {
        if (syncManager == null) {
            syncManager = new TeleSyncManager(context, tdlib);
        }
        syncManager.sync();
    }

    private void loadData() {
        new Thread(() -> {
            List<String> models = TeleDatabase.getInstance(context).deviceDao().getAllModels();
            tdlib.ui().post(() -> {
                List<ListItem> items = new ArrayList<>();
                items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, "دستگاه‌های متصل", 0));
                for (String model : models) {
                    items.add(new ListItem(ListItem.TYPE_DEFAULT, 0, 0, model.toUpperCase(), 0)
                        .setTag(model)
                        .setOnClickListener(v -> openDeviceDetails((String) v.getTag())));
                }
                adapter.setItems(items);
            });
        }).start();
    }

    private void openDeviceDetails(String model) {
        navigateTo(new DeviceDetailController(context, tdlib, model));
    }

    @Override
    public CharSequence getName() {
        return "داشبورد گزارش‌ها";
    }
}
