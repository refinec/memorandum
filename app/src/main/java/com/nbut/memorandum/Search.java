package com.nbut.memorandum;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Search extends AppCompatActivity {
    private SearchView searchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private Toolbar search_toolbar;
    private RecyclerView recyclerView;
    private String[] tabsList;
    private int noteListSize = 0;
    Boolean isChanged = false;
    NoteAdapter adapter;
    Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");

    //存储筛选备忘录的列表
    private List<Note> screenList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_search_view);
        search_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(search_toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            //返回键
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        //获取从MainActivity的intent
        Intent it = getIntent();
        tabsList = it.getStringArrayExtra("tabsArray");
        noteListSize = it.getIntExtra("noteListSize", 0);
        //设置管理器
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        search_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchAutoComplete.isShown()) {
                    try {
                        //如果搜索框中有文字，则会先清空文字，但网易云音乐是在点击返回键时直接关闭搜索框
                        mSearchAutoComplete.setText("");
                        Method method = searchView.getClass().getDeclaredMethod("onCloseClicked");
                        method.setAccessible(true);
                        method.invoke(searchView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent it = new Intent();
                    it.putExtra("iam","search");
                    it.putExtra("isChanged",isChanged);
                    setResult(RESULT_OK,it);
                    finish();
                }
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_toolbar, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchItem.expandActionView();//展开
        // 获取SearchView对象
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        //通过id得到搜索框控件
        mSearchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        //设置输入框内提示文字样式
        mSearchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.darker_gray));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.black));//设置内容文字颜色
        searchView.setQuery("", false);//设置文字
        searchView.clearFocus();//清除焦点
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框外)
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("搜索便签...");
        searchView.setMaxWidth(1200);

        //搜索框展开时后面叉叉按钮的点击事件
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        //搜索图标按钮(打开搜索框的按钮)的点击事件
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //搜索框文字变化监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if (s.trim().length() != 0) {
                    useAdapter(recyclerView, fuzzyQuery(screenList, s.trim()));
                } else {
                    useAdapter(recyclerView, new ArrayList<Note>());
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    private List<Note> fuzzyQuery(List<Note> currentList, String contentText) {
        currentList.clear();//清除上一次的内容
        Cursor contentTextCursor = getContentResolver().query(uri, null, "mainText like ? or noteTitle like ?", new String[]{"%" + contentText + "%", "%" + contentText + "%"}, null);
        try {
            if (contentTextCursor != null) {
                while (contentTextCursor.moveToNext()) {
                    int newId = contentTextCursor.getInt(contentTextCursor.getColumnIndex("id"));
                    int num = contentTextCursor.getInt(contentTextCursor.getColumnIndex("num"));
                    int tag = contentTextCursor.getInt(contentTextCursor.getColumnIndex("tag"));
                    String current_date = contentTextCursor.getString(contentTextCursor.getColumnIndex("textDate"));
                    String current_time = contentTextCursor.getString(contentTextCursor.getColumnIndex("textTime"));
                    String alarm = contentTextCursor.getString(contentTextCursor.getColumnIndex("alarm"));
                    String noteTitle = contentTextCursor.getString(contentTextCursor.getColumnIndex("noteTitle"));
                    String mainText = contentTextCursor.getString(contentTextCursor.getColumnIndex("mainText"));
                    String flagText = contentTextCursor.getString(contentTextCursor.getColumnIndex("flag"));
                    Note currentNote = new Note(newId, num, tag, current_date, current_time, alarm, noteTitle, mainText, flagText);
                    currentList.add(currentNote);
                }
                contentTextCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) updateDataBaseAndListView(requestCode, data);
    }

    private void useAdapter(RecyclerView recyclerView, final List<Note> currentNoteList) {
        adapter = new NoteAdapter(currentNoteList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new NoteAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                isChanged = true;
                Intent intent = new Intent(Search.this, Edit.class);
                Note record = currentNoteList.get(position);
                //把选中的备忘记录添加进intent
                transportInformationToEdit(intent, record);
                startActivityForResult(intent, position);
            }
        });

        //长按
        adapter.setOnItemLongClickListener(new NoteAdapter.ItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final int position) {
                isChanged = true;
                final AlertDialog.Builder dialog = new AlertDialog.Builder(Search.this);
                dialog.setTitle("删除");
                dialog.setMessage("是否删除？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cuId = 1;
                        //如果这个备忘录有一个闹钟,取消它
                        if (currentNoteList.get(position).isbAlarm()) {
                            cancelAlarm(position);
                        }
                        cuId = currentNoteList.get(position).getId();

                        //删除数据库对应的便签
                        Uri uri = Uri.parse("content://com.nbut.memorandum.provider/note");
                        getContentResolver().delete(uri, "id = ?", new String[]{String.valueOf(cuId)});
                        //更新数据库
                        for (int i = currentNoteList.get(position).getNum() + 1; i <= noteListSize; i++) {
                            ContentValues values = new ContentValues();
                            values.put("num", i - 1);//重新设置记录在列表中的位置
                            getContentResolver().update(uri, values, "num = ?", new String[]{String.valueOf(i)});
                            values.clear();
                        }
                        currentNoteList.remove(position);
                        //界面刷新
                        adapter.notifyDataSetChanged();
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
     * 传递 intent 到 EditActivity
     * @param it
     * @param record
     */
    private void transportInformationToEdit(Intent it, Note record) {
        it.putExtra("id", record.getId());
        it.putExtra("num", record.getNum());
        it.putExtra("tag", record.getTag());
        it.putExtra("textDate", record.getTextDate());
        it.putExtra("textTime", record.getTextTime());
        it.putExtra("alarm", record.getAlarm());
        it.putExtra("noteTitle", record.getTitle());
        it.putExtra("mainText", record.getMainText());
        it.putExtra("flag", record.getFlag());
        it.putExtra("tabsArray", tabsList);
    }

    /**
     * 删除列表中note时，如果设置了闹钟就取消闹钟
     * @param num
     */
    private void cancelAlarm(int num) {
        Intent intent = new Intent(Search.this, AlarmService.class);
        stopService(intent);
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

    private void updateDataBaseAndListView(int requestCode, Intent it) {
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

        //更新以前备忘录的“num”位置(因为note在列表中的位置可能变化)
        ContentValues values = new ContentValues();
        values.put("tag", tag);
        values.put("textDate", current_date);
        values.put("textTime", current_time);
        values.put("alarm", alarm);
        values.put("noteTitle", noteTitle);
        values.put("mainText", mainText);
        values.put("flag", flagText);

        //如果之前一个有闹钟,先取消它
        if (screenList.get(num).isbAlarm()) {
            cancelAlarm(num);
        }
        getContentResolver().update(uri, values, "id = ?", new String[]{String.valueOf(id)});
        screenList.set(num, new_note);

        //如果用户已设置闹钟
        if (gotAlarm) {
            loadAlarm(alarm, id, 0);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * 当闹钟响起时，我们想向broadcast 一个intent给BroadcastReceiver
     *  在这里，我们使用一个显式类名创建一个Intent，已拥有我们自己的接收器（已发布于AndroidManifest.xml）实例化并调用
     * 然后创建一个IntentSender以将意图作为广播执行。
     */
    private void loadAlarm(String alarm, int num, int days) {
        //安排闹钟服务
        Intent intent = new Intent(Search.this, AlarmService.class);
        intent.putExtra("alarmId", num);
        intent.putExtra("alarm", alarm);
        startService(intent); //开启闹钟服务
    }

}
