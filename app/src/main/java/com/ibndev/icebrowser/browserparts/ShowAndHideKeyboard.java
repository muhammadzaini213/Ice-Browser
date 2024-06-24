package com.ibndev.icebrowser.browserparts;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

public class ShowAndHideKeyboard {

    Context context;
    AutoCompleteTextView et;
    public ShowAndHideKeyboard(Context context, AutoCompleteTextView et){
        this.context = context;
        this.et = et;
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }
}
