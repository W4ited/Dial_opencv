package com.lwd.dial_mgp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lwd.dial_mgp.Picture.PictureActivity;
import com.lwd.dial_mgp.Result.ResultActivity;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    private List<Fragment> fragmentList = new ArrayList<>();
    final String[] titleArray = new String[]{"1", "2"};
    final int[] titleItem = new int[]{R.drawable.fragment_operation, R.drawable.fragment_picture};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //导入Opencv
        iniLoadOpencv();

        tabLayout = findViewById(R.id.home_tabLayout);
        viewPager2 = findViewById(R.id.home_viewPager2);

        fragmentList.add(new PictureActivity());
        fragmentList.add(new ResultActivity());

        viewPager2.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList));

        TabLayoutMediator tab = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titleArray[position]);
                tab.setIcon(titleItem[position]);
            }
        });
        tab.attach();
    }

    /**
     * 导入opencv
     */
    private void iniLoadOpencv() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Toast.makeText(MainActivity.this, "Loading Opencv Libraries..", Toast.LENGTH_LONG).show();
            Log.d("666","Loading Opencv Libraries..");
        } else {
            Toast.makeText(MainActivity.this, "WARNING: Could not load Opencv Libraries !", Toast.LENGTH_LONG).show();
            Log.d("666","WARNING");
        }

    }

}