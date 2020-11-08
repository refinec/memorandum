package com.nbut.memorandum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnFloatAdd;
    private Toolbar main_toolbar;
    private TabLayout allTabs;
    private ImageButton imageButton;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    //存储所有备忘录的列表
    private List<Note> noteList = new ArrayList<>();
    //存储指定分组的备忘录列表
    private List<Note> appointList = new ArrayList<>();
    //存储所有标签
    private List<Tabs> tabsList = new ArrayList<>();
    private RecyclerView recyclerView;
    //当前所在的标签页
    String flag = "首页";
    NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);
        ActionBar actionBar = getSupportActionBar();
        imageButton = findViewById(R.id.imageButton);
        allTabs = findViewById(R.id.tabs);

        //加载note
        loadHistoryData();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //设置管理器
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        useAdapter(recyclerView, noteList, "首页");

        //标签页选择
        allTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                flag = (String) tab.getText();
                if (!flag.equals("首页")) {
                    useAdapter(recyclerView, loadAppointData(flag), flag);
                } else {
                    useAdapter(recyclerView, noteList, flag);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        /**
         * 添加分组
         */
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText inputServer = new EditText(MainActivity.this);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("添加新分组").setView(inputServer).setIcon(R.drawable.group).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = inputServer.getText().toString().trim();
                        int tabCount = allTabs.getTabCount();
                        Boolean isExist = false;
                        for (int i = 0; i < tabsList.size(); i++) {
                            if (inputText.equals(tabsList.get(i).getTabName().trim())) {
                                isExist = true;
                                break;
                            }
                        }
                        if (isExist || inputText.isEmpty() || inputText.equals("首页")) {
                            dialog.dismiss();
                        } else {
                            try {
                                allTabs.addTab(allTabs.newTab().setText(inputText), tabCount);
                                tabsList.add(new Tabs(0, tabCount, inputText));
                                addTabToDataBase(tabCount, inputText);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                dialog.show();
            }
        });
        //浮动的添加按钮
        btnFloatAdd = (FloatingActionButton) findViewById(R.id.btnFloatAdd);
        btnFloatAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, Edit.class);
                //note列表有多少记录，position可以作为新note的位置(因为noteList是数组，下标为长度减一)
                int position = noteList.size();
                if (!flag.equals("首页")){
                    position = appointList.size();
                }
                String[] tabsArray = new String[tabsList.size()];
                Calendar c = Calendar.getInstance();
                String current_date = getCurrentDate(c);
                String current_time = getCurrentTime(c);
                it.putExtra("id", 0);
                it.putExtra("num", position);
                it.putExtra("tag", 4);
                it.putExtra("textDate", current_date);
                it.putExtra("textTime", current_time);
                it.putExtra("alarm", "");
                it.putExtra("noteTitle", "");
                it.putExtra("mainText", "");

                if (flag.equals("首页")) {
                    it.putExtra("flag", "其他");
                    int tabCount = allTabs.getTabCount();

                    //就一个首页标签，则自动添加"其他"标签
                    if (tabCount == 1) {
                        allTabs.addTab(allTabs.newTab().setText("其他"), tabCount);
                        tabsList.add(new Tabs(0, tabCount, "其他"));
                        tabsArray = new String[]{"其他"};
                        addTabToDataBase(tabCount, "其他");
                    } else {
                        for (int i = 0; i < tabsList.size(); i++) {
                            tabsArray[i] = tabsList.get(i).getTabName();
                        }
                    }
                } else {
                    it.putExtra("flag", flag);
                    tabsArray[0] = flag;
                    for (int i = 0, j = 1; i < tabsList.size(); i++, j++) {
                        String currentFlag = tabsList.get(i).getTabName();
                        if (flag.equals(currentFlag)) {
                            j--;
                            continue;
                        }
                        tabsArray[j] = currentFlag;
                    }
                }

                it.putExtra("tabsArray", tabsArray);
                startActivityForResult(it, position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.searchButton:
                Intent toSearch = new Intent(MainActivity.this, Search.class);
                String[] tabsArray = new String[tabsList.size()];
                for (int i = 0; i < tabsList.size(); i++) {
                    tabsArray[i] = tabsList.get(i).getTabName();
                }
                toSearch.putExtra("tabsArray", tabsArray);
                toSearch.putExtra("noteListSize", noteList.size());
                startActivityForResult(toSearch, 0);
                break;
            case R.id.deleteGroup:
                //显示删除分组弹窗
                dialogMoreChoice();
                break;
            default:
        }
        return true;
    }

    /**
     * 多选删除分组
     */
    private void dialogMoreChoice() {
        final String items[] = new String[allTabs.getTabCount()-1];
        final boolean selected[] = new boolean[allTabs.getTabCount()-1];
        //获得所有标签名
        for (int i=0;i<allTabs.getTabCount()-1;i++){
            items[i] = (String) allTabs.getTabAt(i+1).getText();
            selected[i] = false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, 0);
        builder.setTitle("删除分组");
        builder.setIcon(R.drawable.delete_group2);
        builder.setMultiChoiceItems(items, selected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                    }
                });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            try {
                if (selected.length!=0){
                    Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
                    Uri uriTabs = Uri.parse("content://com.nbut.memorandum.provider/tabs");
                    //先获取未删除之前便签的数量
                    int mNum = 0;
                    Cursor cursorNum = getContentResolver().query(uri, null, null, null, null);
                    if (cursorNum!=null){
                        mNum = cursorNum.getCount();
                        Log.e("未删除之前便签的数量", String.valueOf(mNum));
                    }
                    cursorNum.close();

                    for (int i=selected.length-1;0<=i;i--){
                        if(selected[i]){
                            Log.e("删除分组中为true的", String.valueOf(selected[i]));
                            //删除标签
                            getContentResolver().delete(uriTabs,"tabName = ?",new String[]{String.valueOf(tabsList.get(i).getTabName())});
                            //删除便签
                            getContentResolver().delete(uri,"flag = ?",new String[]{tabsList.get(i).getTabName()});
                            tabsList.remove(i);
                            allTabs.removeTabAt(i+1);
                        }
                    }

                    //更新分组表中标签的position字段
                    Cursor cursor = getContentResolver().query(uriTabs, null, null, null, null);
                    if (cursor != null && cursor.getCount()!=0) {
                        tabsList.clear();
                        int pos = 1;
                        ContentValues va = new ContentValues();
                        while (cursor.moveToNext()) {
                            int position = pos++;
                            va.put("position",position);
                            int newId = cursor.getInt(cursor.getColumnIndex("id"));
                            getContentResolver().update(uriTabs,va,"id = ?",new String[]{String.valueOf(newId)});
                            Log.e("更新分组position的tag", String.valueOf(cursor.getString(cursor.getColumnIndex("tabName"))));
                            String tabName = cursor.getString(cursor.getColumnIndex("tabName"));
                            Tabs tabs = new Tabs(newId, position, tabName);
                            tabsList.add(tabs);
                            va.clear();
                        }
                        cursor.close();
                    }

                    //更新便签在列表中的num位置
                    Cursor cursorNote = getContentResolver().query(uri, null, null, null, null);
                    if (cursorNote!=null && cursorNote.getCount()!= mNum){
                        noteList.clear();
                        ContentValues vaNote = new ContentValues();
                        int posNote = 0;
                        while (cursorNote.moveToNext()){
                            int num = posNote++;
                            vaNote.put("num",num);
                            int noteId = cursorNote.getInt(cursorNote.getColumnIndex("id"));
                            getContentResolver().update(uri,vaNote,"id = ?",new String[]{String.valueOf(noteId)});

                            int tag = cursorNote.getInt(cursorNote.getColumnIndex("tag"));
                            String textDate = cursorNote.getString(cursorNote.getColumnIndex("textDate"));
                            String textTime = cursorNote.getString(cursorNote.getColumnIndex("textTime"));
                            boolean alarm = cursorNote.getString(cursorNote.getColumnIndex("alarm")).length() > 1 ? true : false;
                            String noteTitle = cursorNote.getString(cursorNote.getColumnIndex("noteTitle"));
                            String mainText = cursorNote.getString(cursorNote.getColumnIndex("mainText"));
                            String flagText = cursorNote.getString(cursorNote.getColumnIndex("flag"));
                            Note temp = new Note(noteId, num, tag, textDate, textTime, alarm, noteTitle, mainText, flagText);
                            noteList.add(temp);
                            vaNote.clear();
                        }
                    }
                    cursorNote.close();

                    adapter.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }


            }
        });
        builder.create().show();

    }
        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) updateDataBaseAndListView(requestCode, data);
    }

    private void useAdapter(RecyclerView recyclerView, final List<Note> currentNoteList, final String flag) {
        // 添加适配器
        adapter = new NoteAdapter(currentNoteList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new NoteAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                int cuId = 1;
                Intent intent = new Intent(MainActivity.this, Edit.class);
                if (!flag.equals("首页")) {
                    cuId = currentNoteList.get(position).getId();
                } else {
                    cuId = noteList.get(position).getId();
                }
                Note record = getNoteWithNum(cuId);
                //把选中的备忘记录添加进intent
                transportInformationToEdit(intent, record);
                startActivityForResult(intent, position);
            }
        });

        adapter.setOnItemLongClickListener(new NoteAdapter.ItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final int position) {

                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("删除");
                dialog.setMessage("是否删除？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("###删除position--->:", String.valueOf(position));
                        int cuId = 1;
                        int pos = 0;
                        int n = noteList.size();

                        if (!flag.equals("首页")) {
                            cuId = currentNoteList.get(position).getId();
                            pos = currentNoteList.get(position).getNum();
                            //如果这个备忘录有一个闹钟,取消它
                            if (currentNoteList.get(position).isbAlarm()) {
                                cancelAlarm(pos);
                            }
                            currentNoteList.remove(position);

                            for (int i =0;i<noteList.size();i++){
                                if (cuId==noteList.get(i).getId()){
                                    noteList.remove(i);
                                    break;
                                }
                            }

                        } else {
                            //如果这个备忘录有一个闹钟,取消它
                            if (noteList.get(position).isbAlarm()) {
                                cancelAlarm(position);
                            }
                            cuId = noteList.get(position).getId();
                            pos = position;
                            noteList.remove(position);
                        }

                        //界面刷新
                        adapter.notifyDataSetChanged();
                        String whereArgs = String.valueOf(cuId);
                        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
                        getContentResolver().delete(uri, "id = ?", new String[]{whereArgs});

                        for (int i = pos + 1; i < n; i++) {
                            ContentValues values = new ContentValues();
                            values.put("num", i - 1);//重新设置记录在列表中的位置
                            String where = String.valueOf(i);
                            getContentResolver().update(uri, values, "num = ?", new String[]{where});
                            values.clear();
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.
                        OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                dialog.show();
                return true;
            }
        });

    }

    /**
     * 加载在搜索时可能更改的数据
     */
    private void loadScreenData() {
        noteList.clear();
        appointList.clear();
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                int tag = cursor.getInt(cursor.getColumnIndex("tag"));
                String textDate = cursor.getString(cursor.getColumnIndex("textDate"));
                String textTime = cursor.getString(cursor.getColumnIndex("textTime"));
                boolean alarm = cursor.getString(cursor.getColumnIndex("alarm")).length() > 1 ? true : false;
                String noteTitle = cursor.getString(cursor.getColumnIndex("noteTitle"));
                String mainText = cursor.getString(cursor.getColumnIndex("mainText"));
                String flagText = cursor.getString(cursor.getColumnIndex("flag"));
                Note temp = new Note(id, num, tag, textDate, textTime, alarm, noteTitle, mainText, flagText);
                if (flag.equals(flagText)) {
                    appointList.add(temp);
                }
                noteList.add(temp);
            }
        }

    }

    /**
     * 加载已经存在所有的note
     */
    private void loadHistoryData() {
        noteList.clear();
        tabsList.clear();
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
        Uri uriTabs = Uri.parse("content://com.nbut.memorandum.provider/tabs");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        Cursor cursorTabs = getContentResolver().query(uriTabs, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int num = cursor.getInt(cursor.getColumnIndex("num"));
                int tag = cursor.getInt(cursor.getColumnIndex("tag"));
                String textDate = cursor.getString(cursor.getColumnIndex("textDate"));
                String textTime = cursor.getString(cursor.getColumnIndex("textTime"));
                boolean alarm = cursor.getString(cursor.getColumnIndex("alarm")).length() > 1 ? true : false;
                String noteTitle = cursor.getString(cursor.getColumnIndex("noteTitle"));
                String mainText = cursor.getString(cursor.getColumnIndex("mainText"));
                String flagText = cursor.getString(cursor.getColumnIndex("flag"));
                Note temp = new Note(id, num, tag, textDate, textTime, alarm, noteTitle, mainText, flagText);
                noteList.add(temp);
            }
        }
        try {
            if (cursorTabs != null) {
                while (cursorTabs.moveToNext()) {
                    int id = cursorTabs.getInt(cursorTabs.getColumnIndex("id"));
                    int position = cursorTabs.getInt(cursorTabs.getColumnIndex("position"));
                    String tabName = cursorTabs.getString(cursorTabs.getColumnIndex("tabName"));
                    allTabs.addTab(allTabs.newTab().setText(tabName), position);
                    Tabs tabs = new Tabs(id, position, tabName);
                    tabsList.add(tabs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursorTabs.close();
            cursor.close();
        }

    }

    /**
     * 加载指定标签的分组
     *
     * @param flagT
     * @return List<Note>
     */
    private List<Note> loadAppointData(String flagT) {
        appointList.clear();
        int i = 0;
        try {
            //使用Iterator迭代器遍历指定分组的记录
            Iterator<Note> it = noteList.iterator();
            while (it.hasNext()) {
                Note current = it.next();
                if (current.getFlag().equals(flagT)) {
                    appointList.add(current);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointList;
    }

    /**
     * 根据Edit.class返回的“num”,更新备忘录数据库和备忘录列表
     * @param requestCode
     * @param it
     */
    private void updateDataBaseAndListView(int requestCode, Intent it) {
        if ("search".equals(it.getStringExtra("iam")) || it.getBooleanExtra("isChanged", false)) {
            loadScreenData();
            staggeredGridLayoutManager.invalidateSpanAssignments();
            adapter.notifyDataSetChanged();
            return;
        }
        int num = requestCode;
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");

        //当前时间
        Calendar c = Calendar.getInstance();
        String current_date = getCurrentDate(c);
        String current_time = getCurrentTime(c);

        //接收参数
        int id = it.getIntExtra("id", 0);
        int tag = it.getIntExtra("tag", 0);
        String alarm = it.getStringExtra("alarm");
        String noteTitle = it.getStringExtra("noteTitle");
        String mainText = it.getStringExtra("mainText");
        String flagText = it.getStringExtra("flag");

        boolean gotAlarm = alarm.length() > 1 ? true : false;
        Note new_note = new Note(id, num, tag, current_date, current_time, gotAlarm, noteTitle, mainText, flagText);
        //添加
        if (!flag.equals("首页") && (requestCode+1) > appointList.size()) {
            num = noteList.size();
            //将新的备忘录记录添加到数据库中
            addRecordToDataBase(num, tag, current_date, current_time, alarm, noteTitle, mainText, flagText);
            Cursor cursor = getContentResolver().query(uri, null, " num = ? ", new String[]{String.valueOf(num)}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int newId = cursor.getInt(cursor.getColumnIndex("id"));
                    new_note = new Note(newId, num, tag, current_date, current_time, gotAlarm, noteTitle, mainText, flagText);
                }
                cursor.close();
            }
            //将一个新的OneNote对象添加到noteList,appointList并显示
            noteList.add(new_note);
            appointList.add(new_note);
        } else if (flag.equals("首页") && (requestCode + 1) > noteList.size()) {
            //将新的备忘录记录添加到数据库中
            addRecordToDataBase(num, tag, current_date, current_time, alarm, noteTitle, mainText, flagText);
            Cursor cursor = getContentResolver().query(uri, null, " num = ? ", new String[]{String.valueOf(num)}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int newId = cursor.getInt(cursor.getColumnIndex("id"));
                    new_note = new Note(newId, num, tag, current_date, current_time, gotAlarm, noteTitle, mainText, flagText);
                }
                cursor.close();
            }
            //将一个新的OneNote对象添加到noteList,appointList并显示
            noteList.add(new_note);
        } else {

            //更新以前备忘录的“num”位置(因为note在列表中的位置可能变化)
            ContentValues values = new ContentValues();
            values.put("tag", tag);
            values.put("textDate", current_date);
            values.put("textTime", current_time);
            values.put("alarm", alarm);
            values.put("noteTitle", noteTitle);
            values.put("mainText", mainText);
            values.put("flag", flagText);

            if (!flag.equals("首页")&&flag.equals(flagText)) {
                appointList.set(requestCode, new_note);
                num = appointList.get(requestCode).getNum();
            }
            if (!flag.equals("首页")&&!flag.equals(flagText)){
                num = appointList.get(requestCode).getNum();
                appointList.remove(requestCode);
            }
            //如果之前一个有闹钟,先取消它
            if (noteList.get(num).isbAlarm()) {
                cancelAlarm(num);
            }

            noteList.get(num).setTag(tag);
            noteList.get(num).setTextDate(current_date);
            noteList.get(num).setTextTime(current_time);
            noteList.get(num).setAlarm(alarm);
            noteList.get(num).setTitle(noteTitle);
            noteList.get(num).setMainText(mainText);
            noteList.get(num).setFlag(flagText);

            getContentResolver().update(uri, values, "id = ?", new String[]{String.valueOf(id)});

        }

        //如果用户已设置闹钟
        if (gotAlarm) {
            Log.i("闹钟时间---->", alarm);
            loadAlarm(alarm, id, 0);
        }
        staggeredGridLayoutManager.invalidateSpanAssignments();
        adapter.notifyDataSetChanged();
    }


    /**
     * 以XX - XX格式获取当前日期
     * @param c
     * @return
     */
    private String getCurrentDate(Calendar c) {
        return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 以XX：XX格式获取当前时间
     * @param c
     * @return
     */
    private String getCurrentTime(Calendar c) {
        String current_time = "";
        int time = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        current_time = time < 10 ?
                current_time + "0" + time + ":" :
                current_time + time + ":";

        current_time = minute < 10 ?
                current_time + "0" + minute :
                current_time + minute;

        return current_time;
    }

    /**
     * 新数据添加到数据库
     *
     * @param num
     * @param tag
     * @param textDate
     * @param textTime
     * @param alarm
     * @param mainText
     */
    private void addRecordToDataBase(int num, int tag, String textDate, String textTime, String alarm, String noteTitle, String mainText, String flag) {
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
        ContentValues values = new ContentValues();
        values.put("num", num);
        values.put("tag", tag);
        values.put("textDate", textDate);
        values.put("textTime", textTime);
        values.put("alarm", alarm);
        values.put("noteTitle", noteTitle);
        values.put("mainText", mainText);
        values.put("flag", flag);
        getContentResolver().insert(uri, values);
    }

    /**
     * 添加新标签到数据库
     *
     * @param position
     * @param tabName
     */
    private void addTabToDataBase(int position, String tabName) {
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/tabs");
        ContentValues values = new ContentValues();
        values.put("position", position);
        values.put("tabName", tabName);
        getContentResolver().insert(uri, values);
    }


    /**
     * 根据num获取数据库中指定的note
     *
     * @param num 选中的备忘记录位置
     * @return 该记录所有信息
     */
    private Note getNoteWithNum(int num) {
        String whereArgs = String.valueOf(num);
        List<Note> list = new ArrayList<>();
        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
        Cursor cursor = getContentResolver().query(uri, null, "id = ?", new String[]{whereArgs}, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int n = cursor.getInt(cursor.getColumnIndex("num"));
                int tag = cursor.getInt(cursor.getColumnIndex("tag"));
                String textDate = cursor.getString(cursor.getColumnIndex("textDate"));
                String textTime = cursor.getString(cursor.getColumnIndex("textTime"));
                String alarm = cursor.getString(cursor.getColumnIndex("alarm"));
                String noteTitle = cursor.getString(cursor.getColumnIndex("noteTitle"));
                String mainText = cursor.getString(cursor.getColumnIndex("mainText"));
                String flagText = cursor.getString(cursor.getColumnIndex("flag"));
                Note temp = new Note(id, n, tag, textDate, textTime, alarm, noteTitle, mainText, flagText);
                list.add(temp);
            }
            cursor.close();
        }
        return list.get(0);
    }

    /**
     * 传递 intent 到 EditActivity
     *
     * @param it
     * @param record
     */
    private void transportInformationToEdit(Intent it, Note record) {
        String[] tabsArray = new String[tabsList.size()];

        it.putExtra("id", record.getId());
        it.putExtra("num", record.getNum());
        it.putExtra("tag", record.getTag());
        it.putExtra("textDate", record.getTextDate());
        it.putExtra("textTime", record.getTextTime());
        it.putExtra("alarm", record.getAlarm());
        it.putExtra("noteTitle", record.getTitle());
        it.putExtra("mainText", record.getMainText());
        it.putExtra("flag", record.getFlag());

        tabsArray[0] = record.getFlag();
        for (int i = 0, j = 1; i < tabsList.size(); i++, j++) {
            if (record.getFlag().equals(tabsList.get(i).getTabName())) {
                j--;
                continue;
            }
            tabsArray[j] = tabsList.get(i).getTabName();
        }
        it.putExtra("tabsArray", tabsArray);
    }

    //根据"alarm"设置闹钟
    private void loadAlarm(String alarm, int num, int days) {
        /**
         * 当闹钟响起时，我们想向broadcast 一个intent给BroadcastReceiver
         *  在这里，我们使用一个显式类名创建一个Intent，已拥有我们自己的接收器（已发布于AndroidManifest.xml）实例化并调用
         * 然后创建一个IntentSender以将意图作为广播执行。
         */
        //安排闹钟服务
        Intent intent = new Intent(MainActivity.this, AlarmService.class);
        intent.putExtra("alarmId", num);
        intent.putExtra("alarm", alarm);
        startService(intent); //开启闹钟服务
    }

    //删除列表中note时，如果设置了闹钟就取消闹钟
    private void cancelAlarm(int num) {
        Intent intent = new Intent(MainActivity.this, AlarmService.class);
        stopService(intent);
    }

}