package com.example.bletest.manager;


import com.example.bletest.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by e.konobeeva on 05.10.2017.
 */

public class TestManegerActivity extends Activity implements View.OnClickListener{

    public TestManegerActivity() {
        super();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_manager);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(final View v) {
        new StartConnectionServiceManager("E2:05:BA:0F:2C:63", this, "/storage/emulated/0/Download/JA_1636_V055_0_20170707.zip");
    }
}
