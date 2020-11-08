package com.nbut.memorandum;

public class Tabs {
    private int id;
    private int position;//tab位置
    private String tabName;//tab名

    public Tabs(int id, int position, String tabName){
        this.id = id;
        this.position = position;
        this.tabName = tabName;
    }

    public int getId() {
        return id;
    }
    public String getTabName() {
        return tabName;
    }
    public void setId(int id) {
        this.id = id;
    }

}
