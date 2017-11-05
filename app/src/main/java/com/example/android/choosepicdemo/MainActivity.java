package com.example.android.choosepicdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    private Button takePhoto;
    private ImageView imageView;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePhoto = (Button)findViewById(R.id.take_photo);
        imageView = (ImageView)findViewById(R.id.image_view);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(Environment.getExternalStorageDirectory(),
                        "maybe.jpg");
                if(outputImage.exists()){
                    outputImage.delete();
                }
                try {
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //将File对象转换成Uri对象，因为Uri对象标识着maybe.jpg这张图片的唯一地址。
                //由于Intent能传递的数据空间有限，所以需要转化成Uri
                //大图片用Uri,小图片用Bitmap
                //以下代码解决了Android 7.0 打开相机崩溃的问题
                if(Build.VERSION.SDK_INT > 24){
                    imageUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.example.android.choosepicdemo.fileprovider",outputImage);
                }else{
                    imageUri = Uri.fromFile(outputImage);
                }
                //Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                //第一个参数：一个Intent对象
                //第二个参数：如果> = 0,当Activity结束时requestCode将归还在onActivityResult()中,
                //以便确定返回的数据是从哪个Activity中返回
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
    }
    /*第一个参数：这个整数requestCode提供给onActivityResult，是以便确认返回的数据是从哪个Activity返回的。
            这个requestCode和startActivityForResult中的requestCode相对应。
      第二个参数：这整数resultCode是由子Activity通过其setResult()方法返回。
      第三个参数：一个Intent对象，带有返回的数据。*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    //以下两行代码适配Android 7.0 解决了无法加载图片的问题
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(imageUri,"image/*");
                    intent.putExtra("scale",true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(intent,CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if(resultCode == RESULT_OK){
                    //因为imageUri是Uri类型的，需要转换才能被decodeStream使用
                    //使用getContentResolver()
                    //因为在Android系统里面，数据库是私有的。
                    // 一般情况下外部应用程序是没有权限读取其他应用程序的数据。
                    // 如果你想公开你自己的数据，你有两个选择：
                    // 你可以创建你自己的内容提供器（一个ContentProvider子类）或者
                    // 你可以给已有的提供器添加数据-如果存在一个控制同样类型数据的内容提供器且你拥有写的权限。
                    //外界的程序通过ContentResolver接口可以访问ContentProvider提供的数据，
                    // 在Activity当中通过getContentResolver()可以得到当前应用的 ContentResolver实例
                    try {
                        //解析成Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
