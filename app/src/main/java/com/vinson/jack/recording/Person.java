package com.vinson.jack.recording;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jack on 2018/1/13.
 * person class, handle person information.
 */

public class Person extends AppCompatActivity {
    //声明视图
    private ListView personList;
    private Button personSave;
    private Button personSpend;

    //声明数据类型
    private ArrayList<String> personArrayList;
    private ArrayAdapter<String> personArrayAdapter;

    //接受从index的数据
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);

        initView();
        initData();
        addListener();
    }

    private void initView(){
        personList = findViewById(R.id.personList);
        personSave = findViewById(R.id.saveButton);
        personSpend = findViewById(R.id.spendButton);
    }

    private void initData(){
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
        personArrayList = db.getPersonData(name);
        personArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                personArrayList);
        personList.setAdapter(personArrayAdapter);
        db.closeDataBase();
    }
    private void addListener(){
        personList.setOnItemLongClickListener((parent, view, position, id) -> {
            if(position == 0){
                DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                ArrayList<String> personData = db.getPersonData(name);
                db.closeDataBase();
                String path = String.valueOf(Environment.getExternalStorageDirectory());
                File file = new File(path + "/personData.txt");
                if(!file.exists()){
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(Person.this, "请先赋予读写文件权限", Toast.LENGTH_SHORT).show();
                    }
                }
                try {
                    FileWriter writer = new FileWriter(file);
                    String output = personData.toString();
                    output = output.substring(output.indexOf('[')+1, output.lastIndexOf(']'));
                    writer.write(output);
                    writer.close();
                    Toast.makeText(Person.this, "导出个人数据成功", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Person.this, "请先赋予读写文件权限", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        personSave.setOnClickListener((l)->{
            EditText saveMoney = new EditText(Person.this);
            saveMoney.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(Person.this);
            saveDialog.setTitle("请输入存入金额").setView(saveMoney);
            saveDialog.setPositiveButton("确定", (dialog, which) -> {
                String moneyString = saveMoney.getText().toString();
                if(moneyString.equals("")){
                    Toast.makeText(Person.this, "请输入金额", Toast.LENGTH_SHORT).show();
                }else{
                    float money = Float.valueOf(moneyString);
                    Event event = new Event(false, money);
                    Data data = new Data(name, event);
                    data.addData("合计", event);
                    DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                    try{
                        db.insertData(data);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    db.closeDataBase();
                    initData();
                }
            }).show();
        });

        personSpend.setOnClickListener((l)->{
            View dialogView = LayoutInflater.from(Person.this).inflate(R.layout.person_dialog, null);
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(Person.this);
            saveDialog.setTitle("请输入消费金额").setView(dialogView);
            saveDialog.setNegativeButton("取消", (dialog, which) -> {

            });
            saveDialog.setPositiveButton("确定", (dialog, which)->{
                EditText money = dialogView.findViewById(R.id.money);
                EditText note = dialogView.findViewById(R.id.note);
                String moneyString = money.getText().toString();
                String noteString = note.getText().toString();
                if(moneyString.equals("")){
                    Toast.makeText(Person.this, "请输入金额", Toast.LENGTH_SHORT).show();
                }else{
                    float moneyFloat = Float.valueOf(moneyString);
                    Event event = new Event(true, moneyFloat);
                    event.setNote(noteString);
                    Data data = new Data(name, event);
                    data.addData("合计", event);
                    DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                    try{
                        db.insertData(data);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    db.closeDataBase();
                    initData();
                }
            }).show();
        });
    }
}
