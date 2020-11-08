package com.nbut.memorandum;

/**
 * 对应数据库的实体类
 */
public class Note {
    private int id;
    private int num;//列表中的位置
    private int tag;//颜色
    private String textDate;//日期
    private String textTime;//时间
    private String alarm="";//提醒时间
    private String title;//标题
    private String mainText;//内容
    private String flag; //分组标签

    private boolean bAlarm;//是否设置了提醒时间,显示闹钟图标

    public Note(int id, int num, int tag, String textDate, String textTime, String alarm,String title, String mainText, String flag) {
        this.id = id;
        this.num = num;
        this.tag = tag;
        this.textDate = textDate;
        this.textTime = textTime;
        this.alarm = alarm;
        this.title = title;
        this.mainText = mainText;
        this.flag = flag;
    }
    public Note(int id, int tag, String textDate, String textTime, boolean bAlarm, String title, String mainText, String flag) {
        this.id = id;
        this.tag = tag;
        this.textDate = textDate;
        this.textTime = textTime;
        this.bAlarm = bAlarm;
        this.title = title;
        this.mainText = mainText;
        this.flag = flag;
    }
    public Note(int id,int num, int tag, String textDate, String textTime, boolean bAlarm, String title, String mainText, String flag) {
        this.id = id;
        this.num = num;
        this.tag = tag;
        this.textDate = textDate;
        this.textTime = textTime;
        this.bAlarm = bAlarm;
        this.title = title;
        this.mainText = mainText;
        this.flag = flag;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getNum() {
        return num;
    }

    public String getTextDate() {
        return textDate;
    }

    public void setTextDate(String textDate) {
        this.textDate = textDate;
    }

    public String getTextTime() {
        return textTime;
    }

    public void setTextTime(String textTime) {
        this.textTime = textTime;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getMainText() {
        return mainText;
    }

    public String getTitle() {
        return title;
    }

    public boolean isbAlarm() {
        return bAlarm;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
