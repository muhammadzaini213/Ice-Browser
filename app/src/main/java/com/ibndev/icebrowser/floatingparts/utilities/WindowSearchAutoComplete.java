package com.ibndev.icebrowser.floatingparts.utilities;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.autocomplete.SearchAutocompleteAdapter;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;

public class WindowSearchAutoComplete {

    public WindowSearchAutoComplete(FloatingWindow floatingWindow, WindowTabManager tabManager) {
        ViewGroup floatView = floatingWindow.floatView;
        Context context = floatingWindow.getApplicationContext();
        AutoCompleteTextView et = floatView.findViewById(R.id.window_main_top_navbar_autocomplete);
        et.setSelected(false);

        String initialUrl = "";
        et.setText(initialUrl.isEmpty() ? "google.com" : initialUrl);

        et.setAdapter(new SearchAutocompleteAdapter(context, text -> {
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

        EditText searchEdit = floatView.findViewById(R.id.window_main_top_navbar_search_edit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                tabManager.getCurrentWebView().findAllAsync(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        floatView.findViewById(R.id.window_main_top_navbar_search_next).setOnClickListener(v -> {
            tabManager.getCurrentWebView().findNext(true);
        });

        floatView.findViewById(R.id.window_main_top_navbar_search_prev).setOnClickListener(v -> {
            tabManager.getCurrentWebView().findNext(false);
        });

        floatView.findViewById(R.id.window_main_top_navbar_search_close).setOnClickListener(v -> {
            tabManager.getCurrentWebView().clearMatches();
            searchEdit.setText("");
            tabManager.getCurrentWebView().requestFocus();
            floatView.findViewById(R.id.window_main_top_navbar_search_pane).setVisibility(View.GONE);
        });

    }

}
