package com.ibndev.icebrowser.browserparts.top.autocomplete;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.utilities.ShowAndHideKeyboard;

public class SearchAutoComplete {

    public SearchAutoComplete(Activity activity, TabManager tabManager) {
        AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);
        ShowAndHideKeyboard showAndHideKeyboard = new ShowAndHideKeyboard(activity);
        et.setSelected(false);
        String initialUrl = getUrlFromIntent(activity.getIntent());
        et.setText(initialUrl.isEmpty() ? "google.com" : initialUrl);
        et.setAdapter(new SearchAutocompleteAdapter(activity, text -> {
            et.setText(text);
            et.setSelection(text.length());
        }));
        et.setOnItemClickListener((parent, view, position, id) -> {
            tabManager.getCurrentWebView().requestFocus();
            tabManager.loadUrl(et.getText().toString(), tabManager.getCurrentWebView());
        });


        et.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                tabManager.loadUrl(et.getText().toString(), tabManager.getCurrentWebView());
                tabManager.getCurrentWebView().requestFocus();
                return true;
            } else {
                return false;
            }
        });

        EditText searchEdit = activity.findViewById(R.id.main_top_navbar_search_edit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tabManager.getCurrentWebView().findAllAsync(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        activity.findViewById(R.id.main_top_navbar_search_next).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            tabManager.getCurrentWebView().findNext(true);
        });

        activity.findViewById(R.id.main_top_navbar_search_prev).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            tabManager.getCurrentWebView().findNext(false);
        });

        activity.findViewById(R.id.main_top_navbar_search_close).setOnClickListener(v -> {
            tabManager.getCurrentWebView().clearMatches();
            searchEdit.setText("");
            tabManager.getCurrentWebView().requestFocus();
            activity.findViewById(R.id.main_top_navbar_search_pane).setVisibility(View.GONE);
            showAndHideKeyboard.hideKeyboard();
        });

    }

    public String getUrlFromIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            return intent.getDataString();
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_WEB_SEARCH.equals(intent.getAction()) && intent.getStringExtra("query") != null) {
            return intent.getStringExtra("query");
        } else {
            return "";
        }
    }

}
