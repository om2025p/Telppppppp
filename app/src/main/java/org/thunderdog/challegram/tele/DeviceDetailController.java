package org.thunderdog.challegram.tele;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.navigation.ViewPagerController;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.SettingsAdapter;
import java.util.ArrayList;
import java.util.List;

public class DeviceDetailController extends ViewPagerController<Void> {
    private final String model;

    public DeviceDetailController(Context context, Tdlib tdlib, String model) {
        super(context, tdlib);
        this.model = model;
    }

    @Override
    protected int getPagerItemCount() {
        return 3;
    }

    @Override
    protected CharSequence[] getPagerSections() {
        return new CharSequence[]{"SMS", "MEDIA", "REPORT"};
    }

    @Override
    protected ViewController<?> onCreatePagerItemForPosition(Context context, int position) {
        switch (position) {
            case 0: return createListController("SMS");
            case 1: return createListController("MEDIA");
            case 2: return createWebViewController();
        }
        return null;
    }

    private ViewController<?> createListController(String type) {
        return new ViewController<Void>(context, tdlib) {
            @Override
            protected android.view.View onCreateView(Context context) {
                androidx.recyclerview.widget.RecyclerView rv = new androidx.recyclerview.widget.RecyclerView(context);
                rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                SettingsAdapter adapter = new SettingsAdapter(context, tdlib);
                rv.setAdapter(adapter);

                new Thread(() -> {
                    List<DeviceEntity> reports = TeleDatabase.getInstance(context).deviceDao().getReportsByType(model, type);
                    tdlib.ui().post(() -> {
                        List<ListItem> items = new ArrayList<>();
                        for (DeviceEntity r : reports) {
                            items.add(new ListItem(ListItem.TYPE_DEFAULT, 0, 0, r.localPath.substring(r.localPath.lastIndexOf("/") + 1), 0));
                        }
                        adapter.setItems(items);
                    });
                }).start();

                return rv;
            }
        };
    }

    private ViewController<?> createWebViewController() {
        return new ViewController<Void>(context, tdlib) {
            @Override
            protected android.view.View onCreateView(Context context) {
                WebView webView = new WebView(context);
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setBuiltInZoomControls(true);
                settings.setDisplayZoomControls(false);
                settings.setAllowFileAccess(true);

                webView.setWebViewClient(new WebViewClient());

                new Thread(() -> {
                    List<DeviceEntity> reports = TeleDatabase.getInstance(context).deviceDao().getReportsByType(model, "SYSTEM");
                    if (!reports.isEmpty()) {
                        tdlib.ui().post(() -> webView.loadUrl("file://" + reports.get(0).localPath));
                    }
                }).start();

                return webView;
            }
        };
    }

    @Override
    public CharSequence getName() {
        return model.toUpperCase();
    }
}
