package com.hichip.thecamhi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hichip.R;

import java.util.List;

/**
 * 下拉列表适配
 * */
public class SpAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    List<String> list;

    public SpAdapter(Context context, List<String> list) {
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {

        return list.get(position);
    }

    public long getItemId(int position) {

        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        String content = list.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_sp, null);
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_content_sp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvContent.setText(content);


        return convertView;
    }

    public final class ViewHolder {
        public TextView tvContent;

    }

}
