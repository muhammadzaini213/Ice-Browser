package com.ibndev.icebrowser.browserparts.bottom.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.ibndev.icebrowser.R;

public class ExitDialog extends Dialog {
    Activity activity;

    public ExitDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_exit_dialog_confirmation);

        findViewById(R.id.main_exit_dialog_yes).setOnClickListener(view -> {
            activity.finishAffinity();
            System.exit(0);
        });

        findViewById(R.id.main_exit_dialog_no).setOnClickListener(view -> dismiss());
    }

}

