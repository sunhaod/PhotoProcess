package com.example.sh.photoprocess;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.net.Uri;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.lqr.picselect.LQRPhotoSelectUtils;

import java.io.File;
import java.net.URL;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private LQRPhotoSelectUtils lqrPhotoSelectUtils;
    private Button btnTakePhoto;
    private Button btnSelectPhoto;
    private Button btnSelectAll;
    private Button btnUndo;
    private Button btnConfirm;
    private Button btnTranslate;
    private Button btnNegative;
    private Button btnOld;
    private Button btnRelief;
    private ImageView imgPic, imgPicChanged;
    private Bitmap mBmp, mBmp_new1 = null;//原图，画点的图。
    private Canvas canvas = null;
    private int[] oldArr, curArr;
    private int width, height, index, mPointNumber = 0;
    private boolean isChanged = false;
    private MyVector[] mPoint = new MyVector[4];
    private Paint paint;
    private float picx, picy, k_x, k_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakePhoto = (Button)findViewById(R.id.btn_take_photo);
        btnSelectPhoto = (Button)findViewById(R.id.btn_select_photo);
        btnSelectAll = (Button)findViewById(R.id.btn_select_all);
        imgPic = (ImageView)findViewById(R.id.iv_pic);
        imgPicChanged = (ImageView)findViewById(R.id.iv_pic_changed);
        btnUndo = (Button)findViewById(R.id.btn_undo);
        btnConfirm = (Button)findViewById(R.id.btn_confirm);
        btnTranslate = (Button)findViewById(R.id.btn_translate);
        btnNegative = (Button)findViewById(R.id.btn_negative);
        btnOld = (Button)findViewById(R.id.btn_old);
        btnRelief = (Button)findViewById(R.id.btn_relief);

        btnSelectAll.setOnClickListener(this);
        btnUndo.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnTranslate.setOnClickListener(this);
        btnNegative.setOnClickListener(this);
        btnOld.setOnClickListener(this);
        btnRelief.setOnClickListener(this);

        for(int i = 0; i < 4; i ++) mPoint[i] = new MyVector();
        init();
        initListener();
    }

    private void init() {
        lqrPhotoSelectUtils = new LQRPhotoSelectUtils(MainActivity.this, new LQRPhotoSelectUtils.PhotoSelectListener() {
            @Override
            public void onFinish(File outputFile, Uri outputUri) {
                //拍照或选择照片后回调
                Glide.with(MainActivity.this).load(outputUri).into(imgPic);
                mBmp = getBitmapFromUri(outputUri);
                mBmp_new1 = mBmp.copy(Bitmap.Config.ARGB_8888,true);
                canvas = new Canvas(mBmp_new1);
                paint = new Paint();
                paint.setStrokeWidth(6);
                paint.setColor(Color.RED);
                width = mBmp.getWidth();
                height = mBmp.getHeight();
                oldArr = new int[width * height];
                curArr = new int[width * height];


                //Log.e("haha","图片的高度"+height);
                //Log.e("haha","图片的宽度"+width);
                //Log.e("haha","图片位置"+imgPic.getRight()+" "+imgPic.getBottom());
            }
        },false);
    }

    private void initListener() {
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 3、调用拍照方法
                PermissionGen.with(MainActivity.this)
                        .addRequestCode(LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        ).request();
            }
        });

        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionGen.needPermission(MainActivity.this,
                        LQRPhotoSelectUtils.REQ_SELECT_PHOTO,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                );
            }
        });

        imgPic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Log.e("haha","点击位置"+motionEvent.getX()+" "+motionEvent.getY());

                if(mPointNumber < 4) {
                    if(mPointNumber == 0) {
                        picx = imgPic.getRight();
                        picy = imgPic.getBottom();
                        k_x = mBmp_new1.getWidth() / picx;
                        k_y = mBmp_new1.getHeight() / picy;
                        //Log.e("haha","k_x:"+k_x+" "+k_y);
                        //Log.e("haha",motionEvent.getX()*k_x+" "+motionEvent.getY()*k_y);
                        mPoint[mPointNumber].setXY(motionEvent.getX()*k_x,motionEvent.getY()*k_y);
                        canvas.drawCircle(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),20,paint);
                        imgPic.setImageBitmap(mBmp_new1);
                        mPointNumber ++;
                    }else if(mPointNumber == 3) {
                        //Log.e("haha",motionEvent.getX()*k_x+" "+motionEvent.getY()*k_y);
                        mPoint[mPointNumber].setXY(motionEvent.getX()*k_x,motionEvent.getY()*k_y);
                        canvas.drawCircle(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),20,paint);
                        canvas.drawLine(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),mPoint[mPointNumber - 1].getX(),mPoint[mPointNumber - 1].getY(),paint);
                        canvas.drawLine(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),mPoint[0].getX(),mPoint[0].getY(),paint);
                        imgPic.setImageBitmap(mBmp_new1);
                        mPointNumber ++;
                    }else {
                        //Log.e("haha",motionEvent.getX()*k_x+" "+motionEvent.getY()*k_y);
                        mPoint[mPointNumber].setXY(motionEvent.getX()*k_x,motionEvent.getY()*k_y);
                        canvas.drawCircle(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),20,paint);
                        canvas.drawLine(mPoint[mPointNumber].getX(),mPoint[mPointNumber].getY(),mPoint[mPointNumber - 1].getX(),mPoint[mPointNumber - 1].getY(),paint);
                        imgPic.setImageBitmap(mBmp_new1);
                        mPointNumber ++;
                    }
                }

                return false;
            }
        });
    }

    private Bitmap picTranslate() {
        int oldA, oldR, oldG, oldB,heightC, widthC;
        MyVector vector10 = new MyVector(mPoint[3].getX()-mPoint[0].getX(),mPoint[3].getY()-mPoint[0].getY());
        MyVector vector01 = new MyVector(mPoint[1].getX()-mPoint[0].getX(),mPoint[1].getY()-mPoint[0].getY());
        MyVector vector11 = new MyVector(mPoint[2].getX()-mPoint[0].getX(),mPoint[2].getY()-mPoint[0].getY());
        double a0, a1, x0, x1, FenMu, y0, y1;
        double[][] tmpa = new double[2][2];
        tmpa[0][0] = vector10.getX();
        tmpa[1][0] = vector10.getY();
        tmpa[0][1] = vector01.getX();
        tmpa[1][1] = vector01.getY();
        double[][] tmpb = new double[2][1];
        tmpb[0][0] = vector11.getX();
        tmpb[1][0] = vector11.getY();
        Jama.Matrix A = new Jama.Matrix(tmpa);
        Jama.Matrix B = new Jama.Matrix(tmpb);
        Jama.Matrix S = A.inverse().times(B);
        a0 = S.get(0,0);
        a1 = S.get(1,0);

        Bitmap curPic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmp.getPixels(oldArr,0,width,0,0,width,height);
        boolean hasP =false;

        for(int heightLoop = 0; heightLoop < height; heightLoop ++){
            for(int widthLoop = 0; widthLoop < width; widthLoop ++) {
                x0 = (double) heightLoop / height;
                x1 = (double) widthLoop / width;
                FenMu = a0 + a1 - 1 + (1-a1)*x0+(1-a0)*x1;
                y0 = a0*x0/FenMu;
                y1 = a1*x1/FenMu;
                heightC = (int)Math.round(y0*vector10.getX()+y1*vector01.getX()+mPoint[0].getX());
                widthC = (int)Math.round(y0*vector10.getY()+y1*vector01.getY()+mPoint[0].getY());
                if (heightC > width || heightC <= 0 || widthC >height || widthC <=0 )
                    continue;

                int j = widthC*width+heightC;
                int i = heightLoop*width+widthLoop;
                index = oldArr[j];
                oldA = Color.alpha(index);
                oldR = Color.red(index);
                oldG = Color.green(index);
                oldB = Color.blue(index);
                curArr[i] = Color.argb(oldA,oldR,oldG,oldB);
            }
        }

        curPic.setPixels(curArr,0,width,0,0,width,height);
        return curPic;
    }

    private Bitmap picNegative() {
        int oldA, oldR, oldG, oldB, newA, newR, newG, newB;
        Bitmap curPic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmp.getPixels(oldArr, 0, width, 0, 0, width, height);

        for (int i = 0; i < oldArr.length; i++) {
            index = oldArr[i];
            oldA = Color.alpha(index);
            oldR = Color.red(index);
            oldG = Color.green(index);
            oldB = Color.blue(index);

            newA = oldA;
            newR = 255 - oldR;
            newG = 255 - oldG;
            newB = 255 - oldB;

            newR = (newR > 255 ? 255 : newR) < 0 ? 0 : (newR > 255 ? 255 : newR);
            newG = (newG > 255 ? 255 : newG) < 0 ? 0 : (newG > 255 ? 255 : newG);
            newB = (newB > 255 ? 255 : newB) < 0 ? 0 : (newB > 255 ? 255 : newB);

            curArr[i] = Color.argb(newA, newR, newG, newB);
        }

        curPic.setPixels(curArr, 0, width, 0, 0, width, height);

        return curPic;
    }

    private Bitmap picOld() {
        int oldA, oldR, oldG, oldB, newA, newR, newG, newB;
        Bitmap curPic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmp.getPixels(oldArr, 0, width, 0, 0, width, height);

        for (int i = 0; i < oldArr.length; i++) {
            index = oldArr[i];
            oldA = Color.alpha(index);
            oldR = Color.red(index);
            oldG = Color.green(index);
            oldB = Color.blue(index);

            newA = oldA;
            newR = (int) (0.393 * oldR + 0.769 * oldG + 0.189 * oldB);
            newG = (int) (0.349 * oldR + 0.686 * oldG + 0.168 * oldB);
            newB = (int) (0.272 * oldR + 0.534 * oldG + 0.131 * oldB);

            newR = (newR > 255 ? 255 : newR) < 0 ? 0 : (newR > 255 ? 255 : newR);
            newG = (newG > 255 ? 255 : newG) < 0 ? 0 : (newG > 255 ? 255 : newG);
            newB = (newB > 255 ? 255 : newB) < 0 ? 0 : (newB > 255 ? 255 : newB);

            curArr[i] = Color.argb(newA, newR, newG, newB);
        }
        curPic.setPixels(curArr, 0, width, 0, 0, width, height);

        return curPic;
    }

    private Bitmap picRelief() {
        int oldA, oldR, oldG, oldB, newA, newR, newG, newB;
        Bitmap curPic = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmp.getPixels(oldArr, 0, width, 0, 0, width, height);

        for (int i = 1; i < oldArr.length; i++) {
            index = oldArr[i - 1];
            oldA = Color.alpha(index);
            oldR = Color.red(index);
            oldG = Color.green(index);
            oldB = Color.blue(index);

            index = oldArr[i];
            newR = Color.red(index);
            newG = Color.green(index);
            newB = Color.blue(index);

            newR = oldR - newR + 127;
            newG = oldG - newG + 127;
            newB = oldB - newB + 127;

            newR = (newR > 255 ? 255 : newR) < 0 ? 0 : (newR > 255 ? 255 : newR);
            newG = (newG > 255 ? 255 : newG) < 0 ? 0 : (newG > 255 ? 255 : newG);
            newB = (newB > 255 ? 255 : newB) < 0 ? 0 : (newB > 255 ? 255 : newB);

            curArr[i] = Color.argb(oldA, newR, newG, newB);
        }
        curPic.setPixels(curArr, 0, width, 0, 0, width, height);

        return curPic;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_select_all:
                imgPicChanged.setImageBitmap(mBmp);
                break;
            case R.id.btn_undo:
                mBmp_new1 = mBmp.copy(Bitmap.Config.ARGB_8888,true);
                canvas = new Canvas(mBmp_new1);
                imgPic.setImageBitmap(mBmp_new1);
                curArr = new int[width*height];
                imgPicChanged.setImageBitmap(null);
                mPointNumber = 0;
                break;
            case R.id.btn_confirm:
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("提示")
                        .setMessage("请点击转换进行修正").setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
                break;
            case R.id.btn_translate:
                if(mPointNumber == 4)
                    imgPicChanged.setImageBitmap(picTranslate());
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("提示")
                            .setMessage("请先选好四个顶点").setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder1.create().show();
                }
                break;
            case R.id.btn_negative:
                imgPicChanged.setImageBitmap(picNegative());
                break;
            case R.id.btn_old:
                imgPicChanged.setImageBitmap(picOld());
                break;
            case R.id.btn_relief:
                imgPicChanged.setImageBitmap(picRelief());
                break;
        }
    }













































































































































    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void takePhoto() {
        lqrPhotoSelectUtils.takePhoto();
    }
    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_SELECT_PHOTO)
    private void selectPhoto() {
        lqrPhotoSelectUtils.selectPhoto();
    }
    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void showTip1() {
        //        Toast.makeText(getApplicationContext(), "不给我权限是吧，那就别玩了", Toast.LENGTH_SHORT).show();
        showDialog();
    }
    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_SELECT_PHOTO)
    private void showTip2() {
        //        Toast.makeText(getApplicationContext(), "不给我权限是吧，那就别玩了", Toast.LENGTH_SHORT).show();
        showDialog();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 2、在Activity中的onActivityResult()方法里与LQRPhotoSelectUtils关联
        lqrPhotoSelectUtils.attachToActivityForResult(requestCode, resultCode, data);
    }

    public void showDialog() {
        //创建对话框创建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置对话框显示小图标
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        //设置标题
        builder.setTitle("权限申请");
        //设置正文
        builder.setMessage("在设置-应用-权限 中开启相机、存储权限，才能正常使用拍照或图片选择功能");

        //添加确定按钮点击事件
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {//点击完确定后，触发这个事件

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //这里用来跳到手机设置页，方便用户开启权限
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //添加取消按钮点击事件
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        //使用构建器创建出对话框对象
        AlertDialog dialog = builder.create();
        dialog.show();//显示对话框
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            return bitmap;
        }catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }


}
