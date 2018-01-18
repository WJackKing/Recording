package com.vinson.jack.recording;

import java.util.HashMap;

/**
 * Created by jack on 2018/1/14.
 * save data about what have done, and time.
 */

public class Data {
    private HashMap<String, Event> data;

    Data(){
        data = new HashMap<>();
    }

    Data(String name, Event event){
        data = new HashMap<>();
        this.data.put(name, event);
    }

    void addData(String name, Event event){
        this.data.put(name, event);
    }

    public HashMap<String, Event> getData(){
        return this.data;
    }
}
