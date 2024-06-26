package com.ibndev.icebrowser.browserparts.utilities;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import com.ibndev.icebrowser.R;

public class ShowAndHideKeyboard {

    Activity activity;

    public ShowAndHideKeyboard(Activity activity) {
        this.activity = activity;
    }

    public void hideKeyboard() {
        AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }
}
