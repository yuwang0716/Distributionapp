package com.liuhesan.app.distributionapp.ui.personcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.liuhesan.app.distributionapp.R;
import com.liuhesan.app.distributionapp.bean.Tab;
import com.liuhesan.app.distributionapp.fragment.MineFragment;
import com.liuhesan.app.distributionapp.fragment.OrderFragment;
import com.liuhesan.app.distributionapp.fragment.TotalFragment;
import com.liuhesan.app.distributionapp.utility.AppManager;
import com.liuhesan.app.distributionapp.widget.FragmentTabHost;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private LayoutInflater mInflater;
    private FragmentTabHost mTabhost;
    private List<Tab> mTabs = new ArrayList<>(3);
    private MineFragment mineFragment;
    private long mExitTime;
    private ImageView msgTV_news,msgTV_order;
    private boolean isReddot_news,isReddot_order;
    public static final String RECEIVER_ACTION = "location_in_background";
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private ReseizeReceive localReceive;
    private String notification = "0";
    private String timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intentFilter = new IntentFilter();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter.addAction("com.liuhesan.app.distributionapp.ISREDDOTNEWS");
        localReceive = new ReseizeReceive();
        localBroadcastManager.registerReceiver(localReceive,intentFilter);

        AppManager.getAppManager().addActivity(MainActivity.this);
        JPushInterface.init(getApplicationContext());
        //设置colorPrimaryDark图片
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initTab();
    }



    private void initTab() {
        mineFragment = new MineFragment();
        Tab tab_order = new Tab(OrderFragment.class,R.string.order,R.drawable.selector_icon_order);
        Tab tab_total = new Tab(TotalFragment.class,R.string.total,R.drawable.selector_icon_total);
        Tab tab_mine = new Tab(MineFragment.class,R.string.mine,R.drawable.selector_icon_mine);

        mTabs.add(tab_order);
        mTabs.add(tab_total);
        mTabs.add(tab_mine);



        mInflater = LayoutInflater.from(this);
        mTabhost = (FragmentTabHost) this.findViewById(android.R.id.tabhost);
        mTabhost.setup(this,getSupportFragmentManager(),R.id.realtabcontent);
        for (Tab tab : mTabs){
            TabHost.TabSpec tabSpec = mTabhost.newTabSpec(getString(tab.getTitle()));

            tabSpec.setIndicator(buildIndicator(tab));

            mTabhost.addTab(tabSpec,tab.getFragment(),null);

        }

        mTabhost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabhost.setCurrentTab(0);
        mTabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("我的")){
                    msgTV_news.setVisibility(View.GONE);
                } if (tabId.equals("订单")){
                    msgTV_order.setVisibility(View.GONE);
                }
            }
        });
    }


    private View buildIndicator(Tab tab){

        View view =mInflater.inflate(R.layout.tab_indicator,null);
        ImageView img = (ImageView) view.findViewById(R.id.icon_tab);
        TextView text = (TextView) view.findViewById(R.id.txt_indicator);
        if (tab.getTitle() ==R.string.mine ){
            msgTV_news = (ImageView) view.findViewById(R.id.message_count);
        }
        if (tab.getTitle() ==R.string.order ){
            msgTV_order = (ImageView) view.findViewById(R.id.message_count);
        }
        img.setBackgroundResource(tab.getIcon());
        text.setText(tab.getTitle());

        return  view;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mineFragment.onActivityResult(requestCode,resultCode,data);
    }

    //两次返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK){
            if ((System.currentTimeMillis() - mExitTime) > 2000){
                Toast.makeText(MainActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }else {
                finish();
            }
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class ReseizeReceive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
         isReddot_news = intent.getBooleanExtra("isreddotnews",false);
         isReddot_order = intent.getBooleanExtra("isreddotoder",false);
            if (isReddot_news){
                msgTV_news.setVisibility(View.VISIBLE);
            }
            if (isReddot_order){
                msgTV_order.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceive);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        notification = intent.getStringExtra("notification");
        timeout = intent.getStringExtra("timeout");
        if (!TextUtils.isEmpty(notification)){
            if (notification.equals("notification")){
                mTabhost.setCurrentTab(0);
                Intent intent_news = new Intent("com.liuhesan.app.distributionapp.news");
                intent_news.putExtra("notification", "notification");
                localBroadcastManager.sendBroadcast(intent_news);
            }
        }
        if (!TextUtils.isEmpty(timeout)){
            if (timeout.equals("timeout")){
                mTabhost.setCurrentTab(0);
                Intent intent_timeout = new Intent("com.liuhesan.app.distributionapp.news");
                intent_timeout.putExtra("timeout", "timeout");
                localBroadcastManager.sendBroadcast(intent_timeout);
            }
        }
    }
}
