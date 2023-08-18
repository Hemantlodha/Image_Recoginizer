package com.example.image_recoginizer;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.image_recoginizer.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.BindException;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 22;
    static int ind = 0;
    Button btnpicture;
    Button button,button3;
    ImageView imageView,imageView2;
    ActivityMainBinding binding;
    ActivityResultLauncher<String> launcher;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Data db;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnpicture = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        imageView2=findViewById(R.id.imageView2);
        button=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        db=new Data(this);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        database= FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        launcher=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
               binding.imageView2.setImageURI(uri);
               StorageReference reference=storage.getReference().child("image");
               reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               database.getReference().child("image").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void unused) {
                                       Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                   }
                               });
                           }
                       });
                   }
               });
            }
        });

        btnpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            askpermission();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream bytearray=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,bytearray);
                byte[] img=bytearray.toByteArray();
                boolean insert=db.insertdata(img);
                if(insert==true){
                    Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Image was not saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = db.getData();
                if(res.getCount()==0){
                    Toast.makeText(MainActivity.this, "No entry Exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                int size=res.getCount();
                ind=(ind+1)%size;
                res.moveToFirst();
                int temp=0;
                while(temp<ind)
                {
                    res.moveToNext();
                    temp++;
                }
                byte[] bitmap= res.getBlob(0);
                Bitmap image=BitmapFactory.decodeByteArray(bitmap,0,bitmap.length);
                imageView2.setImageBitmap(image);
                imageView2.setVisibility(View.VISIBLE);
            }
        });
    }
    private void askpermission() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_CODE);
        }
        else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openCamera();
            }
            else{
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openCamera(){
        Toast.makeText(this, "Openning Camera", Toast.LENGTH_SHORT).show();
        Intent Camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Camera,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE){
            Bitmap image=(Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(image);
            button.setVisibility(View.VISIBLE);
        }
    }
}