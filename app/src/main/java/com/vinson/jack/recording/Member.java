package com.vinson.jack.recording;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by jack on 2018/1/13.
 * member class.
 */

public class Member extends AppCompatActivity {
    private ListView memberList;
    private Button memberDelete;
    private Button memberAdd;

    private ArrayAdapter<String> memberArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member);

        initView();
        initData();
        addListener();
    }

    //初始化视图
    private void initView(){
        memberList = findViewById(R.id.memberList);
        memberDelete = findViewById(R.id.deleteButton);
        memberAdd = findViewById(R.id.addButton);
    }

    //初始化数据
    private void initData(){
        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
        ArrayList<String> memberArrayList = db.getStatistics(false);
        memberArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                memberArrayList);
        memberList.setAdapter(memberArrayAdapter);
        memberList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        db.closeDataBase();
    }

    //添加监听事件
    private void addListener(){
        //删除按钮事件
        memberDelete.setOnClickListener((l)->{
            SparseBooleanArray checked = memberList.getCheckedItemPositions();
            ArrayList<Integer> checkedKeyList = new ArrayList<>();
            for(int i = 0; i < checked.size(); i++){
                if(checked.valueAt(i)){
                    checkedKeyList.add(checked.keyAt(i));
                }
            }
            if(checkedKeyList.size() == 0){
                Toast.makeText(Member.this, "请至少选择一人", Toast.LENGTH_SHORT).show();
            }else{
                AlertDialog.Builder sureDialog = new AlertDialog.Builder(Member.this);
                sureDialog.setTitle("确认");
                sureDialog.setMessage("确认要删除这些人吗？");
                sureDialog.setNegativeButton("取消", (dialog, which) -> {

                });
                sureDialog.setPositiveButton("确认", (dialog, which) -> {
                    for(int i = 0; i < checkedKeyList.size(); i++){
                        int position = checkedKeyList.get(i);
                        String itemString = memberArrayAdapter.getItem(position);
                        assert itemString != null;
                        String name = itemString.substring(0, itemString.indexOf(':'));
                        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                        //默认不保留个人余额，有需求是再行添加
                        db.deleteData(name);
                        db.closeDataBase();
                    }
                    initData();
                    Toast.makeText(Member.this, "删除成功", Toast.LENGTH_SHORT).show();
                }).show();
            }
        });
        //添加按钮事件
        memberAdd.setOnClickListener((l)->{
            @SuppressLint("InflateParams") View dialogView =
                    LayoutInflater.from(Member.this).inflate(R.layout.member_dialog, null);
            AlertDialog.Builder inputDialog = new AlertDialog.Builder(Member.this);
            inputDialog.setTitle("添加成员").setView(dialogView);
            inputDialog.setNegativeButton("取消", (dialog, which) -> Log.v("cancel", "取消"));
            inputDialog.setPositiveButton("确定", (dialog, which) -> {
                EditText memberName = dialogView.findViewById(R.id.member_name);
                EditText memberMoney = dialogView.findViewById(R.id.member_money);
                String name = memberName.getText().toString();
                String moneyString = memberMoney.getText().toString();
                if(name.equals("") || moneyString.equals("")){
                    Toast.makeText(Member.this, "请输入完全", Toast.LENGTH_SHORT).show();
                }else{
                    float money = Float.valueOf(moneyString);
                    DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                    if(db.isExist(name)){
                        Toast.makeText(Member.this, "该用户已存在", Toast.LENGTH_SHORT).show();
                        db.closeDataBase();
                    }else{
                        db.createTable(name, money);
                        db.closeDataBase();
                        initData();
                    }
                }
            }).show();
        });
    }
}
