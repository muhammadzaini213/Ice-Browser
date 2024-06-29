package com.ibndev.icebrowser.browserparts.top.autocomplete;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.ibndev.icebrowser.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchAutocompleteAdapter extends BaseAdapter implements Filterable {

    static final String searchCompleteUrl = "https://www.google.com/complete/search?client=firefox&q=%s";
    private final Context mContext;
    private final OnSearchCommitListener commitListener;
    private List<String> completions = new ArrayList<>();

    public SearchAutocompleteAdapter(Context context, OnSearchCommitListener commitListener) {
        mContext = context;
        this.commitListener = commitListener;
    }

    @Override
    public int getCount() {
        return completions.size();
    }

    @Override
    public Object getItem(int position) {
        return completions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        TextView v = convertView.findViewById(android.R.id.text1);
        v.setText(completions.get(position));
        Drawable d = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.top_navbar_autocomplete_search, null);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mContext.getResources().getDisplayMetrics());
        assert d != null;
        d.setBounds(0, 0, size, size);
        v.setCompoundDrawables(null, null, d, null);

        v.setOnTouchListener((v1, event) -> {
            if (event.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            TextView t = (TextView) v1;
            if (event.getX() > t.getWidth() - t.getCompoundPaddingRight()) {
                commitListener.onSearchCommit(getItem(position).toString());
                return true;
            }
            return false;
        });
        parent.setOnTouchListener((dropdown, event) -> event.getX() > dropdown.getWidth() - size * 2);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<String> results = getCompletions(constraint.toString());
                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    completions = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private List<String> getCompletions(String text) {
        int total = 0;
        byte[] data = new byte[16384];
        try {
            URL url = new URL(URLUtil.composeSearchUrl(text, searchCompleteUrl, "%s"));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                while (total <= data.length) {
                    int count = in.read(data, total, data.length - total);
                    if (count == -1) {
                        break;
                    }
                    total += count;
                }
                if (total == data.length) {
                    return new ArrayList<>();
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(new String(data, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            return new ArrayList<>();
        }
        jsonArray = jsonArray.optJSONArray(1);
        if (jsonArray == null) {
            return new ArrayList<>();
        }
        final int MAX_RESULTS = 10;
        List<String> result = new ArrayList<>(Math.min(jsonArray.length(), MAX_RESULTS));
        for (int i = 0; i < jsonArray.length() && result.size() < MAX_RESULTS; i++) {
            String s = jsonArray.optString(i);
            if (s != null && !s.isEmpty()) {
                result.add(s);
            }
        }
        return result;
    }

    public interface OnSearchCommitListener {
        void onSearchCommit(String text);
    }
}

