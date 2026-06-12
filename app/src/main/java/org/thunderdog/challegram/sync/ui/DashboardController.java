package org.thunderdog.challegram.sync.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.core.Lang;
import java.util.concurrent.TimeUnit;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.support.ViewSupport;
import org.thunderdog.challegram.sync.data.AppDatabase;
import org.thunderdog.challegram.sync.data.DeviceReportDao;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.v.CustomRecyclerView;
import org.thunderdog.challegram.widget.NoScrollTextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardController extends RecyclerViewController<Void> {
    private List<DeviceReportDao.DeviceSummary> summaries = new ArrayList<>();
    private DeviceReportDao dao;

    public DashboardController(Context context, Tdlib tdlib) {
        super(context, tdlib);
        this.dao = AppDatabase.getInstance(context).deviceReportDao();
    }

    @Override
    public int getId() {
        return R.id.controller_dashboard;
    }

    @Override
    public CharSequence getName() {
        return "TeleSync Dashboard";
    }

    @Override
    protected void onCreateView(Context context, CustomRecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new DashboardAdapter());
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            summaries = dao.getDeviceSummaries();
            UI.post(() -> {
                if (getRecyclerView() != null) {
                    getRecyclerView().getAdapter().notifyDataSetChanged();
                }
            });
        }).start();
    }

    private class DashboardAdapter extends RecyclerView.Adapter<DashboardHolder> {
        @Override
        public DashboardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new DashboardItemView(context);
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new DashboardHolder(view);
        }

        @Override
        public void onBindViewHolder(DashboardHolder holder, int position) {
            holder.bind(summaries.get(position));
        }

        @Override
        public int getItemCount() {
            return summaries.size();
        }
    }

    private class DashboardHolder extends RecyclerView.ViewHolder {
        public DashboardHolder(View itemView) {
            super(itemView);
        }

        public void bind(DeviceReportDao.DeviceSummary summary) {
            ((DashboardItemView) itemView).setData(summary);
            itemView.setOnClickListener(v -> {
                DeviceHubController hub = new DeviceHubController(context, tdlib);
                hub.setArguments(summary.normalizedModel);
                navigateTo(hub);
            });
        }
    }

    private class DashboardItemView extends ViewGroup {
        private final TextView modelText;
        private final TextView reportCountText;
        private final TextView lastActivityText;

        public DashboardItemView(Context context) {
            super(context);

            modelText = new NoScrollTextView(context);
            modelText.setTextSize(18);
            modelText.setTextColor(Theme.getColor(ColorId.text));
            addView(modelText);

            reportCountText = new NoScrollTextView(context);
            reportCountText.setTextSize(14);
            reportCountText.setTextColor(Theme.getColor(ColorId.textLight));
            addView(reportCountText);

            lastActivityText = new NoScrollTextView(context);
            lastActivityText.setTextSize(12);
            lastActivityText.setTextColor(Theme.getColor(ColorId.textLight));
            addView(lastActivityText);

            ViewSupport.setThemedBackground(this, ColorId.filling);
            setPadding(Screen.dp(16), Screen.dp(12), Screen.dp(16), Screen.dp(12));
        }

        public void setData(DeviceReportDao.DeviceSummary summary) {
            modelText.setText(summary.normalizedModel.toUpperCase());
            reportCountText.setText(summary.reportCount + " Reports");
            lastActivityText.setText("Last: " + Lang.getDatestamp(summary.lastActivity, TimeUnit.MILLISECONDS));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int innerWidth = width - getPaddingLeft() - getPaddingRight();

            modelText.measure(MeasureSpec.makeMeasureSpec(innerWidth, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED);
            reportCountText.measure(MeasureSpec.makeMeasureSpec(innerWidth, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED);
            lastActivityText.measure(MeasureSpec.makeMeasureSpec(innerWidth, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED);

            int height = modelText.getMeasuredHeight() + reportCountText.getMeasuredHeight() + lastActivityText.getMeasuredHeight() + getPaddingTop() + getPaddingBottom() + Screen.dp(8);
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int x = getPaddingLeft();
            int y = getPaddingTop();

            modelText.layout(x, y, x + modelText.getMeasuredWidth(), y + modelText.getMeasuredHeight());
            y += modelText.getMeasuredHeight() + Screen.dp(2);

            reportCountText.layout(x, y, x + reportCountText.getMeasuredWidth(), y + reportCountText.getMeasuredHeight());

            int lastX = getMeasuredWidth() - getPaddingRight() - lastActivityText.getMeasuredWidth();
            lastActivityText.layout(lastX, y, lastX + lastActivityText.getMeasuredWidth(), y + lastActivityText.getMeasuredHeight());
        }
    }
}
