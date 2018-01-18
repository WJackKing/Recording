package com.vinson.jack.recording;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //处理列表数据
    private ListView indexListView;
    private ArrayAdapter<String> indexArrayAdapter;

    //处理悬浮按钮
    private FloatingActionButton fab;
    private FloatingActionButton spend_fab;
    private FloatingActionButton statistics_fab;
    private FloatingActionButton member_fab;
    private TextView spend_hint;
    private TextView statistics_hint;
    private TextView member_hint;

    //数据库
    public static String PATH;
    public static String DATABASENAME = "/money.db";
    public static String TEMP;
    public static String TEMPNAME = "/temp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PATH = getFilesDir().toString();
        TEMP = getCacheDir().toString();
        initView();
        initData();
        addListener();
        getPermission();
    }

    //切换回activity的时候调用
    @Override
    protected void onRestart() {
        super.onRestart();
        DataBase dataBase = new DataBase(PATH, DATABASENAME);
        ArrayList<String> indexArrayList = dataBase.getStatistics(true);
        indexArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                indexArrayList);
        indexListView.setAdapter(indexArrayAdapter);
        dataBase.closeDataBase();
        showSubFab(false);
    }

    //初始化视图
    private void initView(){
        //对象实例化
        indexListView = findViewById(R.id.indexList);
        fab = findViewById(R.id.fab);
        spend_fab = findViewById(R.id.fab_spend);
        statistics_fab = findViewById(R.id.fab_statistics);
        member_fab = findViewById(R.id.fab_member);
        spend_hint = findViewById(R.id.spend_hint);
        statistics_hint = findViewById(R.id.statistics_hint);
        member_hint = findViewById(R.id.member_hint);
    }

    //初始化数据
    private void initData(){
        DataBase dataBase = new DataBase(PATH, DATABASENAME);
        dataBase.initBase();
        ArrayList<String> indexArrayList = dataBase.getStatistics(true);
        indexArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                indexArrayList);
        indexListView.setAdapter(indexArrayAdapter);
        dataBase.closeDataBase();
    }

    //显示子fab
    private void showSubFab(boolean show){
        if(show){
            spend_fab.setVisibility(FloatingActionButton.VISIBLE);
            statistics_fab.setVisibility(FloatingActionButton.VISIBLE);
            member_fab.setVisibility(FloatingActionButton.VISIBLE);
            spend_hint.setVisibility(TextView.VISIBLE);
            statistics_hint.setVisibility(TextView.VISIBLE);
            member_hint.setVisibility(TextView.VISIBLE);
        }else{
            spend_fab.setVisibility(FloatingActionButton.INVISIBLE);
            statistics_fab.setVisibility(FloatingActionButton.INVISIBLE);
            member_fab.setVisibility(FloatingActionButton.INVISIBLE);
            spend_hint.setVisibility(TextView.INVISIBLE);
            statistics_hint.setVisibility(TextView.INVISIBLE);
            member_hint.setVisibility(TextView.INVISIBLE);
        }
    }

    //权限申请
    private void getPermission(){
        int permission = checkCallingPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }
    }

    //添加监听事件
    private void addListener(){
        //跳转到个人
        indexListView.setOnItemClickListener((parent, view, position, id) -> {
            if(position != 0){
                String name = indexArrayAdapter.getItem(position);
                assert name != null;
                name = name.substring(0, name.indexOf(':'));
                Intent toPerson = new Intent(MainActivity.this, Person.class);
                toPerson.putExtra("name", name);
                startActivity(toPerson);
            }
        });

        indexListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if(position == 0){
                try {
                    File fileOut = new File(Environment.getExternalStorageDirectory()+"/money.db");
                    FileInputStream input = new FileInputStream(MainActivity.PATH+MainActivity.DATABASENAME);
                    FileOutputStream output = new FileOutputStream(fileOut);
                    int in = input.read();
                    while(in != -1){
                        output.write(in);
                        in = input.read();
                    }
                    input.close();
                    output.close();
                    Toast.makeText(MainActivity.this, "导出数据库成功", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        indexListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastVisibleItem = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem > lastVisibleItem){
                    fab.setVisibility(FloatingActionButton.INVISIBLE);
                    showSubFab(false);
                    lastVisibleItem = firstVisibleItem;
                }else if(firstVisibleItem < lastVisibleItem){
                    fab.setVisibility(FloatingActionButton.VISIBLE);
                    lastVisibleItem = firstVisibleItem;
                }
            }
        });

        fab.setOnClickListener((v) -> {
            if(spend_fab.getVisibility() == FloatingActionButton.INVISIBLE){
                showSubFab(true);
            }else{
                showSubFab(false);
            }
        });

        //跳转到消费视图
        spend_fab.setOnClickListener((v) ->{
            Intent toSpend = new Intent(MainActivity.this, Spend.class);
            startActivity(toSpend);
        });

        //跳转到统计视图
        statistics_fab.setOnClickListener((v) ->{
            Intent toStatistics = new Intent(MainActivity.this, Statistics.class);
            startActivity(toStatistics);
        });

        //跳转到成员视图
        member_fab.setOnClickListener((v) ->{
            Intent toMember = new Intent(MainActivity.this, Member.class);
            startActivity(toMember);
        });
    }
}
