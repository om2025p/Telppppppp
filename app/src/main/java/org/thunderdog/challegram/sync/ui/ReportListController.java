package org.thunderdog.challegram.sync.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.core.Lang;
import java.util.concurrent.TimeUnit;
import org.thunderdog.challegram.sync.data.AppDatabase;
import org.thunderdog.challegram.sync.data.DeviceReportEntity;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.ui.WebkitController;
import org.thunderdog.challegram.v.CustomRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReportListController extends RecyclerViewController<Void> {
    private final String model;
    private final String type;
    private List<DeviceReportEntity> reports = new ArrayList<>();

    public ReportListController(Context context, Tdlib tdlib, String model, String type) {
        super(context, tdlib);
        this.model = model;
        this.type = type;
    }

    @Override
    public int getId() {
        return R.id.controller_chats; // Temporary reuse
    }

    @Override
    protected void onCreateView(Context context, CustomRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new ReportAdapter());
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<DeviceReportEntity> all = AppDatabase.getInstance(context).deviceReportDao().getReportsForDevice(model);
            reports.clear();
            if (all != null) {
                for (DeviceReportEntity r : all) {
                    if (r.fileType.equalsIgnoreCase(type)) {
                        reports.add(r);
                    }
                }
            }
            UI.post(() -> {
                if (getRecyclerView() != null) {
                    getRecyclerView().getAdapter().notifyDataSetChanged();
                }
            });
        }).start();
    }

    private class ReportAdapter extends RecyclerView.Adapter<ReportHolder> {
        @Override
        public ReportHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(context);
            tv.setPadding(32, 32, 32, 32);
            tv.setTextSize(16);
            return new ReportHolder(tv);
        }

        @Override
        public void onBindViewHolder(ReportHolder holder, int position) {
            DeviceReportEntity report = reports.get(position);
            ((TextView) holder.itemView).setText(Lang.getDatestamp(report.timestamp, TimeUnit.MILLISECONDS) + "\n" + report.caption);
            holder.itemView.setOnClickListener(v -> {
                if (report.fileType.equals("MEDIA")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + report.localPath), "image/*");
                    context.startActivity(intent);
                } else {
                    WebkitController<WebkitController.Args> web = new WebkitController<>(context, tdlib);
                    web.setArguments(new WebkitController.Args(report.localPath, report.normalizedModel));
                    navigateTo(web);
                }
            });
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }
    }

    private static class ReportHolder extends RecyclerView.ViewHolder {
        public ReportHolder(View itemView) {
            super(itemView);
        }
    }
}
