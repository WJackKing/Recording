package com.vinson.jack.recording;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by jack on 2018/1/14.
 * Database handle.
 */

class DataBase {
    private SQLiteDatabase db;

    DataBase(String path, String fileName){
        db = SQLiteDatabase.openOrCreateDatabase(path + fileName, null);
    }

    //初始化数据库，生成statistics表
    void initBase(){
        try{
            Cursor cursor = db.query("statistics",
                    null, null, null,
                    null, null, null);
            cursor.moveToFirst();
            float a = cursor.getFloat(2);
            Log.v("over", a + "");
            cursor.close();
        }catch (Exception e){
            //初始化统计表
            String statisticsTable = "create table statistics " +
                    "(personID INTEGER primary key autoincrement, " +
                    "name TEXT, over REAL)";
            db.execSQL(statisticsTable);
            ContentValues contentValues;
            contentValues = new ContentValues();
            contentValues.put("name", "合计");
            contentValues.put("over", 0.0);
            db.insert("statistics", null, contentValues);
        }
    }

    //创建一个表
    void createTable(String name, Float money){
        //创建个人表
        String table = "create table if not exists " + name +
                " (personID INTEGER primary key autoincrement, " +
                "over REAL, time data, isSpend BOOLEAN, spend REAL, " +
                "isInvite BOOLEAN, inviteName TEXT, note TEXT)";
        db.execSQL(table);
        //时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        String time = format.format(curDate);

        ContentValues cValuesNew = new ContentValues();
        cValuesNew.put("over", money);
        cValuesNew.put("time", time);
        cValuesNew.put("isSpend", false);
        cValuesNew.put("spend", money);
        cValuesNew.put("note", "新加入");
        db.insert(name, null, cValuesNew);

        //将人加入到统计数据表
        //添加个人信息
        ContentValues cValuesStatistics = new ContentValues();
        cValuesStatistics.put("name", name);
        cValuesStatistics.put("over", money);
        db.insert("statistics", null, cValuesStatistics);
        //修改小金库余额
        Cursor cursor = db.query("statistics",
                null, null, null,
                null, null, null);
        float all = 0f;
        if(cursor.moveToFirst()){
            all = cursor.getFloat(2);
        }
        cursor.close();
        all = all + money;
        String change = "update statistics set over = " + all +
                " where personID = 1";
        db.execSQL(change);
    }

    //插入一次数据
    void insertData(Data data) throws IOException {
        HashMap<String, Event> getData = data.getData();
        boolean isTeam;
        isTeam = getData.size() > 2;
        //获得当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        String time = format.format(curDate);
        //写入消费数据
        File file = new File(MainActivity.TEMP+MainActivity.TEMPNAME);
        FileWriter writer = null;
        if(isTeam){
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer = new FileWriter(file);
            writer.write(time);
        }

        for(String key : getData.keySet()){
            //获得事件
            Event event = getData.get(key);
            if(key.equals("合计")){
                Cursor allCursor = db.query("statistics", null,
                        null, null, null,
                        null, null);
                allCursor.moveToFirst();
                float all = allCursor.getFloat(2);
                allCursor.close();
                if(event.getSpend()){
                    all = all - event.getMoney();
                }else{
                    all = all + event.getMoney();
                }
                String changeAll = "update statistics set over = " + all +
                        " where personID = 1";
                db.execSQL(changeAll);
                continue;
            }
            if(isTeam){
                writer.append(",").append(key);
            }

            //获得数据isSpend, money, isInvite, inviteName, note, oneLastMoney
            boolean isSpend = event.getSpend();
            float money = event.getMoney();
            boolean isInvite = event.getInvite();
            String inviteName = event.getInviteName();
            String note = event.getNote();
            //获取上次余额
            Cursor cursor = db.query(key,
                    null, null, null,
                    null, null, null);
            cursor.moveToLast();
            float over = cursor.getFloat(1);
            cursor.close();

            //余额
            float oneLastMoney;
            if(isSpend){
                oneLastMoney = (float)(Math.round((over - money)*100))/100;
            }else{
                oneLastMoney = (float)(Math.round((over + money)*100))/100;
            }
            //插入值：余额，时间，花费，是否被邀请，邀请人，备注
            ContentValues contentValues = new ContentValues();
            contentValues.put("over", oneLastMoney);
            contentValues.put("time", time);
            contentValues.put("isSpend", isSpend);
            contentValues.put("spend", money);
            contentValues.put("isInvite", isInvite);
            contentValues.put("inviteName", inviteName);
            contentValues.put("note", note);
            db.insert(key, null,  contentValues);

            //修改统计表
            String changeName = "update statistics set over = " + oneLastMoney +
                    " where name = " + "'" + key + "'";
            db.execSQL(changeName);
        }
        if(isTeam){
            writer.append(",");
            writer.close();
        }
    }

    //删除一个数据
    @SuppressLint("Recycle")
    void deleteData(String name){
        Cursor cursor = db.query(name, null, null, null, null, null, null);
        cursor.moveToLast();
        float over = cursor.getFloat(1);
        //删除小金库金额，在统计表中删除用户
        cursor = db.query("statistics", null, null, null, null, null, null);
        cursor.moveToFirst();
        float all = cursor.getFloat(2);
        all = all - over;
        String change = "update statistics set over = " + all +
                " where personID = 1";
        db.execSQL(change);
        String[] nameArray = new String[1];
        nameArray[0] = name;
        db.delete("statistics", "name = ?", nameArray);
        String deleteTable = "DROP TABLE " + name;
        db.execSQL(deleteTable);
    }

    //获得首页统计数据
    ArrayList<String> getStatistics(boolean isStatistics){
        int statistics;
        if(isStatistics){
            statistics = 0;
        }else{
            statistics = 1;
        }
        ArrayList<String> all = new ArrayList<>();
        Cursor cursor = db.query("statistics",
                null, null, null,
                null, null, null);
        if(cursor.moveToFirst()){
            for(int i = statistics; i < cursor.getCount(); i++){
                cursor.move(i);
                String name = cursor.getString(1);
                float over = cursor.getFloat(2);
                String show = name +": 余额" + over;
                all.add(show);
                cursor.moveToFirst();
            }
        }
        cursor.close();
        return all;
    }

    //获得统计数据
    ArrayList<String> getAll() throws IOException {
        ArrayList<String> all = new ArrayList<>();
        File file = new File(MainActivity.TEMP+MainActivity.TEMPNAME);
        FileReader reader;
        StringBuilder readAll = new StringBuilder();
        String[] readAllArray;
        boolean isEmpty;
        if(file.exists()){
            reader = new FileReader(MainActivity.TEMP+MainActivity.TEMPNAME);
            char[] readChar = new char[16];
            while(reader.read(readChar) != -1){
                readAll.append(String.valueOf(readChar));
                for(int i = 0; i < readChar.length; i++){
                    readChar[i] = ' ';
                }
            }
            readAll.append(String.valueOf(readChar));
            reader.close();
            readAllArray = readAll.toString().split(",");
            isEmpty = false;
        }else{
            isEmpty = true;
            readAllArray = null;
        }

        Cursor cursor = db.query("statistics",
                null, null, null,
                null, null, null);
        String allString;
        int peopleNum = 0;
        float allOver = 0f;
        int arrearsPeople = 0;
        if(cursor.moveToFirst()) {
            peopleNum = cursor.getCount() - 1;
            allOver = cursor.getFloat(2);
            allString = "统计\n" + "总人数：" + peopleNum +
                    "\n总金额：" + allOver + "\n欠费人数：" + arrearsPeople;
            all.add(allString);
        }
        if(!isEmpty){
            for(int i = 1; i < cursor.getCount(); i++){
                cursor.move(i);
                String name = cursor.getString(1);
                for(int j = 1; j < readAllArray.length-1; j++){
                    if(name.equals(readAllArray[j])){
                        Float over = cursor.getFloat(2);
                        if(over < 0){
                            arrearsPeople = arrearsPeople + 1;
                        }
                        Cursor oneCursor = db.query(name,
                                null, null, null,
                                null, null, null);
                        oneCursor.moveToLast();
                        for(int k = 0; k < oneCursor.getCount(); k++){
                            oneCursor.move(-k);
                            String time = oneCursor.getString(2);
                            if(time.equals(readAllArray[0])) {
                                String oneString = getPersonDataTime(name, time);
                                oneString = "\n" + name + "：\n" + oneString + "\n";
                                all.add(oneString);
                                oneCursor.close();
                                break;
                            }
                        }
                    }
                }
                cursor.moveToFirst();
            }
            allString = "统计\n"+"\t总人数："+peopleNum+
                    "\n\t总金额："+allOver+"\n\t欠费人数："+arrearsPeople;
            all.set(0, allString);
        }
        cursor.close();
        return all;
    }

    //获得个人数据
    ArrayList<String> getPersonData(String name){
        ArrayList<String> all = new ArrayList<>();
        float allSpend = 0f;
        float allSave = 0f;
        float lastSave;
        all.add(name);
        Cursor cursor = db.query(name,
                null, null, null,
                null, null, null);
        if(cursor.moveToLast()){
            lastSave = cursor.getFloat(1);
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.move(-i);
                float over = cursor.getFloat(1);
                String time = cursor.getString(2);
                String isSpend = cursor.getString(3);
                float spend = cursor.getFloat(4);
                //boolean isInvite = Boolean.valueOf(cursor.getString(5));
                //String inviteName = cursor.getString(6);
                String note = cursor.getString(7);
                String show = "\n" + "时间：" + time + "\n";
                if(isSpend.equals("1")){
                    show = show + "消费：" + spend + "\n";
                    allSpend = allSpend + spend;
                }else{
                    show = show + "存入：" + spend + "\n";
                    allSave = allSave + spend;
                }
                show = show + "余额：" + over + "\n";
                if(!note.equals("")){
                    show = show + "备注：" + note + "\n";
                }
                all.add(show);
                cursor.moveToLast();
            }
            cursor.close();
            all.set(0, "\n" + name + "\n总消费：" + allSpend +
                    "\n总存入：" + allSave + "\n最后余额：" +
                    lastSave + "\n");
        }
        return all;
    }

    //获得个人数据，根据时间
    String getPersonDataTime(String name, String inputTime){
        String all = "";
        float lastOver;
        float spend;
        float over;
        String note;
        Cursor cursor = db.query(name,
                null, null, null,
                null, null, null);
        if(cursor.moveToLast()){
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.move(-i);
                String time = cursor.getString(2);
                if(time.equals(inputTime)){
                    spend = cursor.getFloat(4);
                    over = cursor.getFloat(1);
                    note = cursor.getString(7);
                    cursor.move(-1);
                    lastOver = cursor.getFloat(1);
                    all = "时间：" + time + "\n上次余额：" + lastOver + "\n消费：" + spend + "\n现在余额：" + over + "\n备注：" + note;
                    cursor.close();
                    break;
                }else{
                    cursor.moveToLast();
                }
            }
        }
        return all;
    }

    //判断用户是否存在
    boolean isExist(String name){
        boolean isExist = false;
        @SuppressLint("Recycle") Cursor cursor = db.query("statistics",
                null, null, null,
                null, null, null);
        if(cursor.moveToFirst()){
            for(int i = 1; i < cursor.getCount(); i++){
                cursor.move(i);
                String getName = cursor.getString(1);
                if(getName.equals(name)){
                    isExist = true;
                }
                cursor.moveToFirst();
            }
        }
        return isExist;
    }

    //关闭数据库
    void closeDataBase(){
        db.close();
    }
}
