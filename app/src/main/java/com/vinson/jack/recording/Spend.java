package com.vinson.jack.recording;

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

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jack on 2018/1/13.
 * Spend class, handle spend information.
 */

public class Spend extends AppCompatActivity {
    //视图声明
    private EditText spendMoney;
    private ListView spendList;
    private Button spendSelect;
    private Button spendSure;

    //数据声明
    private ArrayList<String> spendArrayList;
    private ArrayAdapter<String> spendArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spend);

        initView();
        initData();
        addListener();
    }

    private void initView(){
        spendMoney = findViewById(R.id.spendMoney);
        spendList = findViewById(R.id.spendList);
        spendSelect = findViewById(R.id.selectButton);
        spendSure = findViewById(R.id.sureButton);
    }

    //初始化数据
    private void initData(){
        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
        spendArrayList = db.getStatistics(false);
        spendArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                spendArrayList);
        spendList.setAdapter(spendArrayAdapter);
        spendList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        db.closeDataBase();
    }

    private void addListener(){
        spendSelect.setOnClickListener((l) -> {
            boolean isAllSelected = false;
            SparseBooleanArray checked = spendList.getCheckedItemPositions();
            if(checked.size() == spendArrayList.size()){
                for(int i = 0; i < checked.size(); i++){
                    if(checked.valueAt(i)){
                        isAllSelected = true;
                    }else{
                        isAllSelected = false;
                        break;
                    }
                }
            }
            if(!isAllSelected){
                for(int i = 0; i < spendArrayList.size(); i++){
                    spendList.setItemChecked(i, true);
                }
            }else{
                for(int i = 0; i < spendArrayList.size(); i++){
                    spendList.setItemChecked(i, false);
                }
            }
        });

        spendSure.setOnClickListener((l) -> {
            SparseBooleanArray checked = spendList.getCheckedItemPositions();
            //储存被选中的项目的position
            ArrayList<Integer> checkedKeyList = new ArrayList<>();
            for(int i = 0; i < checked.size(); i++){
                if(checked.valueAt(i)){
                    checkedKeyList.add(checked.keyAt(i));
                }
            }
            if(checkedKeyList.size() == 0){
                Toast.makeText(Spend.this, "没人被选中", Toast.LENGTH_SHORT).show();
            }else{
                String money = spendMoney.getText().toString();
                if(money.equals("")){
                    Toast.makeText(Spend.this, "请输入消费金额", Toast.LENGTH_SHORT).show();
                }else{
                    //全部消费
                    float allSpend = Float.valueOf(money);
                    //个人消费
                    float oneSpendAll = allSpend/checkedKeyList.size();
                    float oneSpend = (float)(Math.round(oneSpendAll*100))/100;
                    //参与人
                    String[] allName = new String[checkedKeyList.size()];
                    StringBuilder allNameString = new StringBuilder();
                    //插入数据
                    Data data = new Data();
                    Event allEvent = new Event(true, allSpend);
                    data.addData("合计", allEvent);
                    for(int i = 0; i < checkedKeyList.size(); i++){
                        int position = checkedKeyList.get(i);
                        String itemString = spendArrayAdapter.getItem(position);
                        assert itemString != null;
                        //参与消费的人名
                        String name = itemString.substring(0, itemString.indexOf(':'));
                        allName[i] = name;
                        allNameString.append(name).append(" ");
                    }

                    String spendInfo = "\t总消费：" + allSpend + "\n\t人均消费:"
                            + oneSpend + "\n\t参与人：" + allNameString;
                    View dialogView = LayoutInflater.from(Spend.this).inflate(R.layout.spend_dialog, null);
                    EditText noteInfo = dialogView.findViewById(R.id.spendNote);

                    AlertDialog.Builder infoDialog = new AlertDialog.Builder(Spend.this);
                    infoDialog.setTitle("消费信息").setView(dialogView).setMessage(spendInfo);
                    infoDialog.setPositiveButton("确定", (dialog, which) -> {
                        String note = noteInfo.getText().toString();
                        if(!note.equals("")){
                            for(String name : allName){
                                Event event = new Event(true, oneSpend);
                                event.setNote(note);
                                data.addData(name, event);
                            }
                        }else{
                            for(String name : allName){
                                Event event = new Event(true, oneSpend);
                                data.addData(name, event);
                            }
                        }
                        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
                        try{
                            db.insertData(data);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        db.closeDataBase();
                        spendMoney.setText("");
                        initData();
                        Toast.makeText(Spend.this, "已消费", Toast.LENGTH_SHORT).show();
                    });
                    infoDialog.setNegativeButton("取消", (dialog, which) -> Log.v("spend cancel", "cancel")).show();
                }
            }
        });
    }
}
