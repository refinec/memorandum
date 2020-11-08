package com.nbut.memorandum;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class Edit extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    LinearLayout myLayout;
    EditText noteTitle, edt;
    Spinner currentFlag;
    TextView alarmView;
    RadioGroup tagRadio;
    RadioButton rdButton;
    int tag;//颜色
    String textDate;
    String textTime;
    String titleText;
    String mainText;
    String flagText = "其他";
    String[] tabsList;
    //提醒闹钟
    int num = 0; //for requestcode,是MainActivity中startActivityForResult(intent,position)的position，或者通俗说是该note列表位置
    int id = 0;
    String alarm = "";
    int alarm_hour = 0;
    int alarm_minute = 0;
    int alarm_year = 0;
    int alarm_month = 0;
    int alarm_day = 0;
    private DatePickerDialog dialogDate;
    private TimePickerDialog dialogTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);//全屏显示
        setContentView(R.layout.activity_edit);

        //加载toolbar
        Toolbar edit_toolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(edit_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //返回键
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        //获取从MainActivity的intent
        Intent it = getIntent();
        getInformationFromMain(it);

        //设置背景
        myLayout = (LinearLayout) findViewById(R.id.whole);
        noteTitle = (EditText) findViewById(R.id.edit_title);
        edt = (EditText) findViewById(R.id.editText);
        alarmView = (TextView) findViewById(R.id.alarmView);
        currentFlag = (Spinner) findViewById(R.id.currentFlag);

        //设置初始化的标题、编辑框
        noteTitle.setText(titleText);
        edt.setText(mainText);

        //长按隐藏提醒时间显示框
        alarmView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (v.getId() == R.id.alarmView || v.getId() == R.id.alarmButton) {
                    //删除提醒信息
                    alarm = "";
                    //隐藏textView
                    alarmView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        //设置提醒时间显示
        if (alarm.length() > 1) {
            alarmView.setText("提醒时间：" + alarm);
        } else {
            alarmView.setVisibility(View.GONE);
        }

        /**
         * tagRadio中tagRadio改变背景色
         */
        tagRadio = (RadioGroup) findViewById(R.id.tagRadio);
        tagRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (tagRadio.getCheckedRadioButtonId()) {
                    case R.id.yellow:
                        tag = 0;
                        myLayout.setBackgroundResource(R.color.colorYellow);
                        break;
                    case R.id.blue:
                        tag = 1;
                        myLayout.setBackgroundResource(R.color.colorBlue);
                        break;
                    case R.id.green:
                        tag = 2;
                        myLayout.setBackgroundResource(R.color.colorGreen);
                        break;
                    case R.id.red:
                        tag = 3;
                        myLayout.setBackgroundResource(R.color.colorRed);
                        break;
                    default:
                        break;
                }
                //当点击该个tagRadio时,该tagRadio换图标
                setRadioButtonCheckedAccordingToTag(tag);
                rdButton.setChecked(true);
            }
        });

        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tabsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        currentFlag.setAdapter(adapter);
        currentFlag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("选择的分组类型:",tabsList[position]);
                flagText = tabsList[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * 添加toolbar的menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_toolbar, menu);
        return true;
    }

    /**
     * 菜单选择
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //保存键
            case R.id.saveButton:
                //不为空就保存
                if (!(edt.getText().toString()).trim().equals("")) {
                    returnResult();
                }
                finish();
                break;
                //返回键
            case android.R.id.home:
                finish();
                break;
            case R.id.alarmButton:
                setAlarm();
                break;
            default:
        }
        return true;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按了手机返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!(edt.getText().toString().trim()).equals("")) {
                returnResult();
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 从MainActivity获取intent
     * @param it
     */
    private void getInformationFromMain(Intent it) {
        id = it.getIntExtra("id", 0);
        num = it.getIntExtra("num", 0);
        tag = it.getIntExtra("tag", 0);
        textDate = it.getStringExtra("textDate");
        textTime = it.getStringExtra("textTime");
        alarm = it.getStringExtra("alarm");
        titleText = it.getStringExtra("noteTitle");
        mainText = it.getStringExtra("mainText");
        flagText = it.getStringExtra("flag");
        tabsList = it.getStringArrayExtra("tabsArray");
    }

    /**
     * 返回给MainActivity的intent
     */
    private void returnResult() {
        Intent it = new Intent();
        it.putExtra("id", id);
        it.putExtra("num", num);
        it.putExtra("tag", tag);
        //不用返回现在时间，现在时间在MainActivity中再获取保存进数据库
        it.putExtra("alarm", alarm);
        it.putExtra("noteTitle", noteTitle.getText().toString().trim());
        it.putExtra("mainText", edt.getText().toString().trim());
        it.putExtra("flag", flagText);
        setResult(RESULT_OK, it);//MainActivity中调用了startActivityForResult()
    }

    //点击该radion时，获取该tagRadio
    private void setRadioButtonCheckedAccordingToTag(int tag) {
        switch (tag) {
            case 0:
                rdButton = (RadioButton) findViewById(R.id.yellow);
                break;
            case 1:
                rdButton = (RadioButton) findViewById(R.id.blue);
                break;
            case 2:
                rdButton = (RadioButton) findViewById(R.id.green);
                break;
            case 3:
                rdButton = (RadioButton) findViewById(R.id.red);
                break;
//            case 4:
//                rdButton = (RadioButton) findViewById(R.id.white);
//                break;
            default:
                break;
        }
    }

    //提醒闹钟事件
//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setAlarm() {
        //如果之前没有设置闹钟
        if (alarm.length() <= 1) {
            //显示当前时间
            Calendar c = Calendar.getInstance();
            alarm_hour = c.get(Calendar.HOUR_OF_DAY);
            alarm_minute = c.get(Calendar.MINUTE);
            alarm_year = c.get(Calendar.YEAR);
            alarm_month = c.get(Calendar.MONTH) + 1;
            alarm_day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            //显示之前设置的闹钟时间
            int i = 0, k = 0;
            while (i < alarm.length() && alarm.charAt(i) != '-') i++;
            alarm_year = Integer.parseInt(alarm.substring(k, i));
            k = i + 1;
            i++;
            while (i < alarm.length() && alarm.charAt(i) != '-') i++;
            alarm_month = Integer.parseInt(alarm.substring(k, i));
            k = i + 1;
            i++;
            while (i < alarm.length() && alarm.charAt(i) != ' ') i++;
            alarm_day = Integer.parseInt(alarm.substring(k, i));
            k = i + 1;
            i++;
            while (i < alarm.length() && alarm.charAt(i) != ':') i++;
            alarm_hour = Integer.parseInt(alarm.substring(k, i));
            k = i + 1;
            i++;
            alarm_minute = Integer.parseInt(alarm.substring(k));
        }

        //顺序不能调换
        dialogTime = new TimePickerDialog(this,
                android.app.AlertDialog.THEME_HOLO_LIGHT, this,
                alarm_hour, alarm_minute, true);
        dialogTime.setTitle("请选择时间");
        dialogTime.show();

        Calendar calendar = Calendar.getInstance();
        dialogDate = new DatePickerDialog(this,
                android.app.AlertDialog.THEME_HOLO_LIGHT, this,
                alarm_year, alarm_month - 1, alarm_day);
        dialogDate.getDatePicker().setCalendarViewShown(false);
        dialogDate.getDatePicker().setMinDate(calendar.getTime().getTime());
        dialogDate.setTitle("请选择日期");
        dialogDate.show();
    }

    /**
     * 设置日期
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        alarm_year = year;
        alarm_month = month + 1;
        alarm_day = dayOfMonth;
    }

    /**
     * 设置时间
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        alarm_hour = hourOfDay;
        alarm_minute = minute;
        alarm = alarm_year + "-" + alarm_month + "-" + alarm_day + " " + alarm_hour + ":" + alarm_minute;
        alarmView.setText("提醒时间：" + alarm);
        alarmView.setVisibility(View.VISIBLE);
        Toast.makeText(this, "提醒时间: " + alarm + " !", Toast.LENGTH_LONG).show();
    }
}