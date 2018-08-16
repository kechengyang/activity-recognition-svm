package com.example.prateekvishnu.walkrunjump.util;

import java.io.File;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Activityrecord extends BmobObject {
    private BmobFile record;
    private String name;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
    public BmobFile getRecord(){
        return record;
    }
    public void setRecord(BmobFile record) {
        this.record = record;
    }
}
