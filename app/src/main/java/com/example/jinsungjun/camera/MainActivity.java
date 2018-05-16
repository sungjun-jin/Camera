package com.example.jinsungjun.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.Permission;

/*
18.05.16 카메라 실습

1. 파일의 저장경로를 xml로 생성

2. Android Manifest에 생성된 xml을 경로로하는 provider를 생성
 */

public class MainActivity extends AppCompatActivity {


    ImageView imageView;
    Button btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // 1.버전 체크 - 마쉬멜로 이상이면
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

            checkPermission();

        } else {

            //마쉬멜로 이하이면 버튼 비활성화
            init();
        }
    }

    //2.마시멜로 이상일 경우 dangerous permission 체크해준다
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        //2.1 권한이 현재 있는지 체크 - 카메라 권한이 있으면 init();
        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            //2.2 권한이 없으면 - 사용자에게 권한 요청
            String permissions[] = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions,Const.PERM_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Const.PERM_CAMERA :
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // 사용자가 승인
                    init();
                } else {
                    // 사용자가 거절
                    Toast.makeText(this,"권한 요청을 승인하셔야 앱을 사용할 수 있습니다.",Toast.LENGTH_SHORT).show();
                    finish(); // 앱 종료
                }
                break;
        }
    }

    private void init() {
        imageView = findViewById(R.id.imageView);
        btnCamera = findViewById(R.id.btnCamera);
        //카메라 버튼 사용 가능
        btnCamera.setEnabled(true);
    }


    //갤러리에서 이미지 가져오기
    public void openGallery(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,Const.REQ_GALLERY);

    }
    //카메라에서 이미지 찍기
    Uri fileUri = null; // 이미지가 저장될 uri 주소

    public void openCamera(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //롤리팝 미만과 이상으로 나뉜다

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

            //이상버전 처리
            try {

                File photoFile = createFile();
                if(photoFile != null) {

                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

                        fileUri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".provider",photoFile);

                    } else {

                        fileUri = Uri.fromFile(photoFile);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
                    startActivityForResult(intent,Const.REQ_CAMERA);

                }


            } catch (Exception e) {
                //파일 생성이 제대로 되었는지에 대한 예외처리 구문
                e.printStackTrace();
            }
        } else {

            //미만버전 처리

            startActivityForResult(intent,Const.REQ_CAMERA);
        }

    }

    private File createFile() throws IOException { //내부에서 발생한 Exception을 호출측으로 전달

        //파일명
        String tempFilename = "Temp_" + System.currentTimeMillis();
        //파일을 저장하기 위한 디렉토리 생성
        File tempDir = new File(Environment.getExternalStorageDirectory() + "/CameraDir/");
        if(!tempDir.exists()) {
            //tempDir가 없으면
            tempDir.mkdirs();
        }

        //파일의 생성

        File file = File.createTempFile(tempFilename,".jpg",tempDir);

        return file;
    }

    //startActivityForResult가 호출되면 실행되는 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri imageUri;

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                //이미지 선택창에서 넘겨준 uri 화면에 세팅하기
                case Const.REQ_GALLERY :
                    imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                    break;
                case Const.REQ_CAMERA :
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        //롤리팝 버전 미만을 경우
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    } else {
                        imageView.setImageURI(fileUri);
                    }
            }
        }
    }
}
