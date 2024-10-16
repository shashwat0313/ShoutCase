package com.example.shoutcase;

import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.accessibilityservice.AccessibilityService;

import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import java.util.List;

public class shoutCaseService extends AccessibilityService {

    public static final String TAG = "shoutCaseService";
    private WindowManager windowManager;

    private WindowManager.LayoutParams windowLayoutParams;

    private FrameLayout frameLayout;
    public MovableFloatingActionButton floatingButton;

    private CharSequence newText;

    private AccessibilityNodeInfo textNode;

    private boolean exclamation = false;

    String buttonText;
    int buttonSize_dp;
    int buttonColor;
    int buttonTextSize_dp;
    int buttonTextColor;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        loadPreferences();

//        Log.i(TAG, "service is alive");

        applyButtonPreferences();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i(TAG, "onStartCommand: ");
        applyButtonPreferences();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if(accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED){

            CharSequence packageName = accessibilityEvent.getPackageName();

            if(packageName != null){
//                Log.d(TAG, "onAccessibilityEvent: trig package name = " + packageName);
//                Log.d(TAG, "onAccessibilityEvent: my package name = " + this.getPackageName());

                if(!packageName.equals(this.getPackageName())){
                    if(frameLayout != null) {
//                        Log.d(TAG, "onAccessibilityEvent: removing mfab now");
                        removeMFAB();
                    }
                }
            }
        }

        if(accessibilityEvent.getEventType() == TYPE_VIEW_TEXT_SELECTION_CHANGED){
//            Log.i("on-accessibilityEvent", accessibilityEvent.toString());
            showMFABForSelectedText(accessibilityEvent);
        }
    }
    private void setTextInNode(AccessibilityNodeInfo node, CharSequence text) {
        if (node != null && node.isEditable()) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else {
//            Log.e(TAG, "Node is null or not editable.");
        }
    }

    private CharSequence processText(CharSequence cs){
        if(cs.length() < 1){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder(cs.length());
        for(int i = 0 ; i < cs.length(); i++){
            if(i%2 == 0){
                stringBuilder.append(Character.toUpperCase(cs.charAt(i)));
            }
            else{
//                Log.i("for loop i = ", String.valueOf(i));
                stringBuilder.append(Character.toLowerCase(cs.charAt(i)));
            }
        }

        return (stringBuilder);
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("button-settings", Context.MODE_PRIVATE);

        buttonText = sharedPreferences.getString("button-text", "!!!");
        buttonSize_dp = sharedPreferences.getInt("button-size", 100);
        buttonColor = sharedPreferences.getInt("button-color",  Color.RED);
        buttonTextSize_dp = sharedPreferences.getInt("button-text-size", 70);
        buttonTextColor = sharedPreferences.getInt("button-text-color", Color.BLACK);
        exclamation = sharedPreferences.getBoolean("exclamation?", false);

// Log.i(TAG, "Reloaded Preferences: " +
//         "buttonText=" + buttonText +
//         ", buttonSize=" + buttonSize_dp +
//         ", buttonColor=" + buttonColor +
//         ", buttonTextSize=" + buttonTextSize_dp +
//         ", buttonTextColor=" + buttonTextColor +
//         ", exclamation=" + exclamation);
    }

    private void showMFABForSelectedText(AccessibilityEvent e) {
        CharSequence finalText = textHandler(e);

        if(finalText != null && finalText.length() > 0){
            if(Settings.canDrawOverlays((this))){
                newText = finalText;
                textNode = e.getSource();
                showMFAB();
            }else{
                requestOverlayPermission();
            }
        }
    }

    void showMFAB(){
        if (floatingButton.getParent() == null) {
            int sizeInPx = dpToPx(buttonSize_dp); // Adjust this value as needed
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeInPx, sizeInPx);
            floatingButton.setLayoutParams(params);
            frameLayout.addView(floatingButton, params);
            windowManager.addView(frameLayout, windowLayoutParams);
        }
    }


    private void requestOverlayPermission() {
        Intent intentForOverlayPermissionOverlay = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + this.getPackageName()));

        intentForOverlayPermissionOverlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentForOverlayPermissionOverlay);
    }

    void setFloatingButton(){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.Theme_ShoutCase_Service);
        this.floatingButton = null;
        this.floatingButton = new MovableFloatingActionButton(contextThemeWrapper, buttonTextColor, dpToPx(buttonTextSize_dp));

        int buttonSizePx = dpToPx(buttonSize_dp);

//        Log.i(TAG, "setFloatingButton: Setting px = " + buttonSizePx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(buttonSizePx,buttonSizePx);
        floatingButton.setLayoutParams(params);

        floatingButton.setButtonText(buttonText);

        floatingButton.setBackgroundTintList(ColorStateList.valueOf(buttonColor));

        floatingButton.setOnClickListener(view -> {
//            Log.i(TAG, "onclick listener");
            setTextInNode(textNode, newText);
            removeMFAB();
        });
    }

    void applyButtonPreferences(){
        loadPreferences();

        setWindowManager();

        setFloatingButton();

        setFrameLayout();
    }

    @SuppressLint("ClickableViewAccessibility")
    void setFrameLayout(){
        frameLayout = null;
        frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        frameLayout.setOnTouchListener((view, motionEvent) -> {
            removeMFAB();
            return true; // Indicate that the event was consumed
        });
    }

    void setWindowManager(){
        this.windowManager = null;
        this.windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT // Change to TRANSLUCENT to allow background to be seen
        );
    }


    private void removeMFAB() {
        if(floatingButton != null && floatingButton.getParent() != null){
            frameLayout.removeView(floatingButton);
            windowManager.removeView(frameLayout);
        }
    }

    public CharSequence textHandler(AccessibilityEvent e){
        List<CharSequence> text = e.getText();
        int fromIndex = e.getFromIndex();
        int toIndex = e.getToIndex();

        CharSequence selectedText, finalText = null;

        if( text.size() > 0 && toIndex > fromIndex){
            CharSequence fullText = text.get(0);
            selectedText = text.get(0).toString().substring(fromIndex, toIndex);
            CharSequence resultText = processText(selectedText);
            if(resultText.length() > 0) {

                String leftSubstr = fullText.toString().substring(0, fromIndex);
//                Log.i(TAG, "leftsubstr=" + leftSubstr);

//                Log.i("modified cs", resultText.toString());

                String rightSubstr= fullText.toString().substring(toIndex, fullText.length());
//                Log.i(TAG, "right=" + rightSubstr);

                finalText = leftSubstr + resultText + rightSubstr + (rightSubstr.length() == 0 && exclamation ? "!" : "");
//                Log.i(TAG, "finalText: " + finalText);
            }
        }

        return finalText;
    }

    @Override
    public void onInterrupt() {
//        Log.i(TAG, "onInterrupt: ");
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}
