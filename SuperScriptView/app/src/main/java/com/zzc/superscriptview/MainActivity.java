package com.zzc.superscriptview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.zzc.superscript.SuperScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("" + i);
        }

        final GridViewAdapter gridViewAdapter = new GridViewAdapter(this, data);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gridViewAdapter.removeRecommendView(i);
            }
        });
    }

    private class GridViewAdapter extends BaseAdapter {
        private Context mContext;
        private List<String> mData;
        private HashMap<Long, SuperScript> hashMap = new HashMap<>();

        public GridViewAdapter(Context mContext, List<String> mData) {
            this.mContext = mContext;
            this.mData = mData;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.view_item, viewGroup, false);
            }

            TextView tvText = (TextView) view.findViewById(R.id.tv_text);
            tvText.setText("功能" + mData.get(i));
            showNewFunctionRecommendView(view, getItemId(i), "欢迎试用");
            return view;
        }

        private void showNewFunctionRecommendView(View target, long recommendId, String tip) {
            if (target == null || recommendId == -1 || tip == null) return;
            SuperScript newFunctionRecommendView = new SuperScript(mContext);
            newFunctionRecommendView.setTarget(mContext, target, tip);
            if(!hashMap.containsKey(recommendId)) {
                hashMap.put(recommendId, newFunctionRecommendView);
            }
        }

        /**
         * 去除指定位置的推荐角标
         *
         * @param position 列表位置
         */
        public void removeRecommendView(int position) {
                SuperScript newFunctionRecommendView = hashMap.get(getItemId(position));
                if (newFunctionRecommendView != null) {
                    newFunctionRecommendView.hide();
                }
        }
    }
}
