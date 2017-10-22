package com.example.anshpuri.chattime;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Toolbar main_app_bar;
    ViewPager main_tab_pager;
    SectionPagerAdapter sectionPagerAdapter;

    TabLayout main_tabs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_tab_pager = (ViewPager) findViewById(R.id.main_tab_Pager);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        main_tab_pager.setAdapter(sectionPagerAdapter);
        main_tabs= (TabLayout) findViewById(R.id.main_tabs);
        main_tabs.setupWithViewPager(main_tab_pager);


        main_app_bar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(main_app_bar);
        getSupportActionBar().setTitle("ChatTime");
        mAuth = FirebaseAuth.getInstance();




    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if(currentuser == null)
        {
            sendToStart();
        }

    }

    public void sendToStart()
    {
        Intent startIntent = new Intent(MainActivity.this , StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_btn)
        {
            mAuth.signOut();
            sendToStart();

        }
        if(item.getItemId()==R.id.main_settings_btn)
        {

            Intent settingsIntent = new Intent(MainActivity.this , SettingsActivity.class);
            startActivity(settingsIntent);


        }
        if (item.getItemId() == R.id.main_all_btn)
        {
            Intent intent  = new Intent(MainActivity.this  , UsersActivity.class);
            startActivity(intent);
        }



        return true;
    }
}