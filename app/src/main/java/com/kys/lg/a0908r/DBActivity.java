package com.kys.lg.a0908r;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DBActivity extends AppCompatActivity {

    Button btn_all, btn_search, btn_delete, btn_insert, send;
    EditText input_et, name, age, phone;
    TextView result_txt;
    Dialog dialog;
    String Dname, Dage, Dphone;


    boolean isFirst = true;

    SQLiteDatabase mDatabase;
    SharedPreferences pref;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        input_et = findViewById(R.id.input_et);
        result_txt = findViewById(R.id.result_txt);
        btn_all = findViewById(R.id.btn_all);
        btn_search = findViewById(R.id.btn_search);
        btn_delete = findViewById(R.id.btn_delete);
        btn_insert = findViewById(R.id.btn_insert);


        btn_all.setOnClickListener(click);
        btn_search.setOnClickListener(click);
        btn_delete.setOnClickListener(click);
        btn_insert.setOnClickListener(click);


        //권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            setPermission();
            return;
        }

        load();
        copyAssets();
        copyAssets();

        //DB 읽기
        mDatabase = openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/database/testDB.db"
                , SQLiteDatabase.CREATE_IF_NECESSARY, null);

    }//onCreate()


    //앱을 껏다 키면 무조건  db파일을 새로 만들게 되어 있어서 sharedfrefernce를 통해 처음 설치와 후에 들어왔을 떄 여부를 저장한다.
    private void save() {

        SharedPreferences.Editor edit = pref.edit();

        edit.putBoolean("save", isFirst);
        edit.commit();
    }

    private void load() {

        isFirst = pref.getBoolean("save", true);

    }


    View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btn_all:
                    //모든데이터 다 검색
                    searchQuery("select *from friend");

                    break;
                case R.id.btn_search:

                    String str = input_et.getText().toString().trim();
                    if (str.length() != 0) {
                        //다 가져오되 이름이 이것인 것
                        String s = "select *from friend where name like '%" + str + "%'";
                        searchQuery(s);

                    } else {
                        Toast.makeText(getApplicationContext(), "검색할 이름을 먼저 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_delete:
                    str = input_et.getText().toString().trim();
                    if (str.length() != 0) {
                        String s = String.format("delete from friend where name='%s'", str);
                        searchQuery(s);

                        //데이터 모두 가져오기
                        searchQuery("select *from friend");
                    } else {
                        Toast.makeText(getApplicationContext(), "삭제할 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_insert:
                    dialog = new Dialog(DBActivity.this);
                    dialog.setContentView(R.layout.dialog);
                    dialog.show();

                    name = dialog.findViewById(R.id.name);
                    age = dialog.findViewById(R.id.age);
                    phone = dialog.findViewById(R.id.phone);
                    send = dialog.findViewById(R.id.send);

                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String s = String.format("insert into friend values( '%s','%s', %d )",
                                    name.getText().toString().trim(),
                                    phone.getText().toString().trim(),
                                    Integer.parseInt(age.getText().toString().trim()));

                            searchQuery(s);

                            searchQuery("select *from friend");
                            dialog.dismiss();
                        }
                    });


                    /*
                    String s = String.format("insert into friend values ('김길동','010-2344-0983',40)");
                    searchQuery(s);*/

                    break;
            }

        }
    };

    private void searchQuery(String query) {

        Cursor c = mDatabase.rawQuery(query, null);
        String[] col = c.getColumnNames();//name,age,phone의 세가지 항목

        String[] str = new String[c.getColumnCount()];//3개
        String result = "";

        while (c.moveToNext()) {

            for (int i = 0; i < c.getColumnCount(); i++) {
                str[i] = "";
                str[i] += c.getString(i); //각 컬럼별 데이터
                //name : 홍길동
                //phone :010...
                result += col[i] + " : " + str[i] + "\n";
                //result 한 군데로 다 묶는다.
            }

            result += "\n";

        }

        result_txt.setText(result);

    }//searchQuery()

    //assets폴더의 DB파일을 휴대폰 내부저장소에 저장
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
            //로그를 찍어서 db의 위치를 확인한다.
            // for (int i= 0;i<files.length;i++){
            //   Log.i("MY",files[i]);
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream in = null;
        OutputStream out = null;

        try {

            in = assetManager.open(files[1]);

            //폴더생성
            //휴대폰 내부 저장소의 root경로
            String str = "" + Environment.getExternalStorageDirectory();
            String mkdir = str + "/database";

            File path = new File(mkdir);


            if (!path.exists()) {

                //root경로/database 폴더가 존재하지 않으면
                isFirst = true;

            }

            if (isFirst) {
                path.mkdirs();//폴더생성

                out = new FileOutputStream(mkdir + "/" + files[1]);

                //크기
                byte[] buffer = new byte[2048];

                int read;

                while ((read = in.read(buffer)) != -1) {
                    //다 읽을 때 까지
                    out.write(buffer, 0, read);
                }

                in.close();
                out.flush();
                out.close();

                isFirst = false;

            }

        } catch (Exception e) {

        }
    }

    //라이브러리 제공 클래스

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //모든 권한 수락 완료가 되었을 때 실행되는 메서드
            //앱 권한 설정이 되어 있을 때
            //위에서 return 을 써서 onCreate()밑에서 아무 작동이 안됨 그래서 인텐트로 다시 들어옴.
            Intent i = new Intent(DBActivity.this, DBActivity.class);
            startActivity(i);
            finish();

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            //앱 권한 설정이 안되어 있을 때
            //수락 안된 권한이 있을 때 아예 못키게 함.
            finish();

        }
    };

    private void setPermission() {
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("모든 권한을 수락하세요").setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }
}

