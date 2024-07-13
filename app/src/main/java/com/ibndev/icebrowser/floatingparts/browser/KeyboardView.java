package com.ibndev.icebrowser.floatingparts.browser;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;

public class KeyboardView {

    FloatingWindow floatingWindow;
    LinearLayout keyboardContainer;
    WindowTabManager tabManager;

    private final String[][] nonShiftKeys = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
            {"a", "s", "d", "f", "g", "h", "j", "k", "l"},
            {"SHIFT", "z", "x", "c", "v", "b", "n", "m", "DELETE"},
            {"HIDE", "CLEAR", "SYMBOL", "SPACE", "PASTE", "ENTER"}
    };

    private final String[][] shiftKeys = {
            {".", ",", "/", "?", "@", "(", ")", "x", "+", "-"},
            {"Q", "W", "W", "R", "T", "Y", "U", "I", "O", "P"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
            {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", "DELETE"},
            {"HIDE", "CLEAR", "SYMBOL", "SPACE", "PASTE", "ENTER"}
    };

    private final String[][] symbolKeys = {
            {"~", "`", "|", "•", "√", "π", "÷", "∆", "£", "¢"},
            {"€", "¥", "^", "°", "=", "{", "}", "\\", "%", "©"},
            {"®", "™", "✓", "[", "]", "<", ">", "#", "$", "_"},
            {"#", "&", "*", "\"", "'", ":", ";", "!", "DELETE"},
            {"HIDE", "CLEAR", "SYMBOL", "SPACE", "PASTE", "ENTER"}
    };

    AutoCompleteTextView autoCompleteTextView;

    int cursorPosition;
    int totalColumns;

    public KeyboardView(FloatingWindow floatingWindow, WindowTabManager tabManager) {
        this.floatingWindow = floatingWindow;
        this.tabManager = tabManager;
        totalColumns = 10;
        keyboardContainer = floatingWindow.floatView.findViewById(R.id.keyboard_container);
        autoCompleteTextView = floatingWindow.floatView.findViewById(R.id.window_main_top_navbar_autocomplete);

        hideKeyboard();
        keyboardNonShift();
        autoCompleteTextView.setOnFocusChangeListener((view, b) -> {
            if (b) {
                showKeyboard();
                keyboardNonShift();
            }
        });

        keyboardContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure this only happens once
                keyboardContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the height of the ImageView
                hideKeyboard();
            }
        });

    }

    public void showKeyboard() {
        if(!LayoutSetData.isNonFocusable){
            keyboardContainer.setVisibility(View.GONE);
        } else {
            keyboardContainer.setVisibility(View.VISIBLE);
        }
        int marginBottomHeight = keyboardContainer.getHeight();

        FrameLayout webViewFrame = floatingWindow.floatView.findViewById(R.id.window_main_framelayout_webview);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) webViewFrame.getLayoutParams();

        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, marginBottomHeight); // 50 is the bottom margin in pixels

        webViewFrame.setLayoutParams(params);
    }

    public void hideKeyboard() {
        keyboardContainer.setVisibility(View.GONE);
        autoCompleteTextView.clearFocus();

        FrameLayout webViewFrame = floatingWindow.floatView.findViewById(R.id.window_main_framelayout_webview);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) webViewFrame.getLayoutParams();

        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 0);

        webViewFrame.setLayoutParams(params);
    }

    private void keyboardNonShift() {
        keyboardContainer.removeAllViews();
        for (String[] rowKeys : nonShiftKeys) {
            LinearLayout row = new LinearLayout(floatingWindow.getApplicationContext());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            row.setLayoutParams(rowParams);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(totalColumns);
            row.setGravity(Gravity.CENTER_HORIZONTAL);

            for (String key : rowKeys) {
                View keyView;
                ImageView keyboardImage;
                switch (key) {
                    case "SHIFT":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_shift);

                        keyView.setOnClickListener(v -> keyboardShift());
                        break;
                    case "DELETE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_delete);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                int cursorPosition = autoCompleteTextView.getSelectionStart();
                                if (cursorPosition > 0) { // Ensure cursor is not at the beginning
                                    autoCompleteTextView.getText().delete(cursorPosition - 1, cursorPosition);
                                }
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteOneCharacter();", null);
                            }
                        });
                        break;
                    case "HIDE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_hide);

                        keyView.setOnClickListener(v -> hideKeyboard());
                        break;
                    case "CLEAR":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_clear);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.setText("");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteAllCharacters();", null);
                            }
                        });

                        break;
                    case "SYMBOL":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_symbols);

                        keyView.setOnClickListener(v -> keyboardSymbol());
                        break;
                    case "SPACE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_space);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, " ");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + " " + "');", null);
                            }
                        });
                        break;
                    case "PASTE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_paste);

                        keyView.setOnClickListener(v -> {
                            // Handle key press
                        });
                        break;
                    case "ENTER":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_enter);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                autoCompleteTextView.onEditorAction(EditorInfo.IME_ACTION_GO);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript("javascript:sendEnterKey()", null);
                            }
                        });
                        break;
                    default:
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_text, row, false);
                        TextView keyText = keyView.findViewById(R.id.key_text);
                        keyText.setText(key);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, key);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + key + "');", null);
                            }

                        });
                }


                row.addView(keyView);
            }
            keyboardContainer.addView(row);
        }

    }

    private void keyboardShift() {
        keyboardContainer.removeAllViews();
        for (String[] rowKeys : shiftKeys) {
            LinearLayout row = new LinearLayout(floatingWindow.getApplicationContext());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            row.setLayoutParams(rowParams);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(totalColumns);
            row.setGravity(Gravity.CENTER_HORIZONTAL);

            for (String key : rowKeys) {
                View keyView;
                ImageView keyboardImage;
                switch (key) {
                    case "SHIFT":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_non_shift);
                        keyView.setOnClickListener(v -> keyboardNonShift());
                        break;
                    case "DELETE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_delete);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                int cursorPosition = autoCompleteTextView.getSelectionStart();
                                if (cursorPosition > 0) { // Ensure cursor is not at the beginning
                                    autoCompleteTextView.getText().delete(cursorPosition - 1, cursorPosition);
                                }
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteOneCharacter();", null);
                            }
                        });
                        break;
                    case "HIDE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_hide);

                        keyView.setOnClickListener(v -> hideKeyboard());
                        break;
                    case "CLEAR":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_clear);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.setText("");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteAllCharacters();", null);
                            }
                        });
                        break;
                    case "SYMBOL":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_symbols);

                        keyView.setOnClickListener(v -> keyboardSymbol());
                        break;
                    case "SPACE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_space);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, " ");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + " " + "');", null);
                            }
                        });
                        break;
                    case "PASTE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_paste);

                        keyView.setOnClickListener(v -> {
                            // Handle key press
                        });
                        break;
                    case "ENTER":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_enter);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                autoCompleteTextView.onEditorAction(EditorInfo.IME_ACTION_GO);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript("javascript:sendEnterKey()", null);
                            }
                        });
                        break;
                    default:
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_text, row, false);
                        TextView keyText = keyView.findViewById(R.id.key_text);
                        keyText.setText(key);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, key);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + key + "');", null);
                            }

                        });
                }


                row.addView(keyView);
            }
            keyboardContainer.addView(row);
        }
    }

    private void keyboardSymbol() {
        keyboardContainer.removeAllViews();
        for (String[] rowKeys : symbolKeys) {
            LinearLayout row = new LinearLayout(floatingWindow.getApplicationContext());
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            row.setLayoutParams(rowParams);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(totalColumns);
            row.setGravity(Gravity.CENTER_HORIZONTAL);

            for (String key : rowKeys) {
                View keyView;
                ImageView keyboardImage;
                switch (key) {
                    case "SHIFT":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_non_shift);
                        keyView.setOnClickListener(v -> keyboardNonShift());
                        break;
                    case "DELETE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_delete);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                int cursorPosition = autoCompleteTextView.getSelectionStart();
                                if (cursorPosition > 0) { // Ensure cursor is not at the beginning
                                    autoCompleteTextView.getText().delete(cursorPosition - 1, cursorPosition);
                                }
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteOneCharacter();", null);
                            }
                        });
                        break;
                    case "HIDE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_hide);

                        keyView.setOnClickListener(v -> hideKeyboard());
                        break;
                    case "CLEAR":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_clear);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.setText("");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:deleteAllCharacters();", null);
                            }
                        });
                        break;
                    case "SYMBOL":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_alphabet);

                        keyView.setOnClickListener(v -> keyboardNonShift());
                        break;
                    case "SPACE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_space);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, " ");
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + " " + "');", null);
                            }
                        });
                        break;
                    case "PASTE":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_paste);

                        keyView.setOnClickListener(v -> {
                            // Handle key press
                        });
                        break;
                    case "ENTER":
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_image, row, false);
                        keyboardImage = keyView.findViewById(R.id.key_image);
                        keyboardImage.setImageResource(R.drawable.keyboard_enter);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                autoCompleteTextView.onEditorAction(EditorInfo.IME_ACTION_GO);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript("javascript:sendEnterKey()", null);
                            }
                        });
                        break;
                    default:
                        keyView = LayoutInflater.from(floatingWindow.getApplicationContext()).inflate(R.layout.window_keyboard_item_text, row, false);
                        TextView keyText = keyView.findViewById(R.id.key_text);
                        keyText.setText(key);

                        keyView.setOnClickListener(v -> {
                            if (autoCompleteTextView.isFocused()) {
                                cursorPosition = autoCompleteTextView.getSelectionStart();
                                autoCompleteTextView.getText().insert(cursorPosition, key);
                            } else {
                                tabManager.getCurrentWebView().evaluateJavascript(
                                        "javascript:insertText('" + key + "');", null);
                            }
                        });
                }


                row.addView(keyView);
            }
            keyboardContainer.addView(row);
        }
    }
}
