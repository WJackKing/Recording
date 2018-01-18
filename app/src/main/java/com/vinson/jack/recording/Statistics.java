package com.vinson.jack.recording;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jack on 2018/1/13.
 * statistics.
 */

public class Statistics extends AppCompatActivity {
    private TextView statisticsText;
    private ListView statisticsList;
    private Button statisticsReport;

    private ArrayList<String> statisticsArrayList;
    private ArrayAdapter<String> statisticsArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        initView();
        initData();
        addListener();
    }

    private void initView(){
        statisticsText = findViewById(R.id.statistics);
        statisticsList = findViewById(R.id.statisticsList);
        statisticsReport = findViewById(R.id.report);
    }

    private void initData(){
        DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
        try {
            statisticsArrayList = db.getAll();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statisticsText.setText(statisticsArrayList.get(0));
        statisticsArrayList.remove(0);
        db.closeDataBase();
        statisticsArrayAdapter = new ArrayAdapter<>(this,
                R.layout.statistics_dialog, statisticsArrayList);
        statisticsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        statisticsList.setAdapter(statisticsArrayAdapter);
    }

    private void addListener(){
        statisticsList.setOnItemLongClickListener((parent, view, position, id) -> {
            boolean isAllSelected = false;
            SparseBooleanArray checked = statisticsList.getCheckedItemPositions();
            if(checked.size() == statisticsArrayList.size()){
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
                for(int i = 0; i < statisticsArrayList.size(); i++){
                    statisticsList.setItemChecked(i, true);
                }
            }else{
                for(int i = 0; i < statisticsArrayList.size(); i++){
                    statisticsList.setItemChecked(i, false);
                }
            }
            return true;
        });
        statisticsReport.setOnClickListener((l)->{
            ArrayList<String> allName = new ArrayList<>();
            SparseBooleanArray checked = statisticsList.getCheckedItemPositions();
            ArrayList<Integer> checkedKeyList = new ArrayList<>();
            String time = "";
            for(int i = 0; i < checked.size(); i++){
                if(checked.valueAt(i)){
                    checkedKeyList.add(checked.keyAt(i));
                }
            }
            if(checkedKeyList.size() == 0){
                Toast.makeText(Statistics.this, "没人被选中", Toast.LENGTH_SHORT).show();
            }else {
                for(int i = 0; i < checkedKeyList.size(); i++){
                    int position = checkedKeyList.get(i);
                    String itemString = statisticsArrayAdapter.getItem(position);
                    assert itemString != null;
                    //参与消费的人名
                    String name = itemString.substring(1, itemString.indexOf('：'));
                    time = itemString.substring(itemString.indexOf("时间：")+3, itemString.indexOf("\n上次"));
                    allName.add(name);
                }
                allName.add(time);
                writeStatistics(allName);
            }
        });
    }

    private void writeStatistics(ArrayList<String> nameArray){
        String path = Environment.getExternalStorageDirectory() + "/statistics.txt";
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Statistics.this, "请先赋予读写文件权限", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(statisticsText.getText().toString() + "\n\n");
            writer.append("姓名\t上次余额\t本次消费\t现在余额\t时间\t备注\n");
            DataBase db = new DataBase(MainActivity.PATH, MainActivity.DATABASENAME);
            String time = nameArray.get(nameArray.size()-1);
            for(int i = 0; i < nameArray.size()-1; i++){
                String name = nameArray.get(i);
                String lastSecondHappen = db.getPersonDataTime(name, time);
                String lastOver = lastSecondHappen.substring(lastSecondHappen.indexOf("上次")+5, lastSecondHappen.indexOf("\n消费"));
                String spend = lastSecondHappen.substring(lastSecondHappen.indexOf("消费")+3, lastSecondHappen.indexOf("\n现在"));
                String over = lastSecondHappen.substring(lastSecondHappen.indexOf("现在")+5, lastSecondHappen.indexOf("\n备注"));
                String note = lastSecondHappen.substring(lastSecondHappen.indexOf("备注")+3);
                writer.append(name+"\t"+lastOver+"\t"+spend+"\t"+over+"\t"+time+"\t"+note+"\n");
            }
            db.closeDataBase();
            writer.close();
            Toast.makeText(Statistics.this, "导出统计数据成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Statistics.this, "请先赋予读写文件权限", Toast.LENGTH_SHORT).show();
        }
    }
}
