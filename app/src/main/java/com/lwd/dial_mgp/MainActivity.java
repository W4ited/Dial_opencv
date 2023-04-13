package com.lwd.dial_mgp;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lwd.dial_mgp.Picture.PictureActivity;
import com.lwd.dial_mgp.Result.ResultActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 会有情况 出现ANR问题 UI not response
     */

    //碎片切换
    //private TabLayout tabLayout;
    //private ViewPager2 viewPager2;

    //碎片
    //private List<Fragment> fragmentList = new ArrayList<>();
    //final String[] titleArray = new String[]{"1", "2"};
    //final int[] titleItem = new int[]{R.drawable.fragment_operation, R.drawable.fragment_picture};

    private ImageView test1, test2, test3, test4, test5, test6;
    private Button start;
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4,bitmap5;
    private Mat mat1, mat2;
    private List<MatOfPoint> contours = new ArrayList<>();  //轮廓列表
    private int count = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mat1.release();
        mat2.release();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //导入Opencv opencv库的加载
        iniLoadOpencv();

        //组件初始化
        //init();

        test1 = (ImageView) findViewById(R.id.test_img1);
        test2 = (ImageView) findViewById(R.id.test_img2);
        test3 = (ImageView) findViewById(R.id.test_img3);
        test4 = (ImageView) findViewById(R.id.test_img4);
        test5 = (ImageView) findViewById(R.id.test_img5);
        test6 = (ImageView) findViewById(R.id.test_img6);

        start = (Button) findViewById(R.id.test_start);
        start.setOnClickListener(this);

        mat1 = new Mat();
        mat2 = new Mat();

        //会导致anr问题？ 还没找到问题


        //碎片代码
        /**
         * //tabLayout = findViewById(R.id.home_tabLayout);
         *         //viewPager2 = findViewById(R.id.home_viewPager2);
         *         //fragmentList.add(new PictureActivity());
         *         //fragmentList.add(new ResultActivity());
         *         //viewPager2.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList));
         * //        TabLayoutMediator tab = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
         * //            @Override
         * //            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
         * //                tab.setText(titleArray[position]);
         * //                tab.setIcon(titleItem[position]);
         * //            }
         * //        });
         * //        tab.attach();
         */

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_start:

                //用mat读入图片
                try {
                    mat1 = Utils.loadResource(this,R.drawable.meter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.meter);
                //test1.setImageBitmap(bitmap1);

                //Utils.bitmapToMat(bitmap1, mat1);  //转换为mat格式的

                //画线
                //Imgproc.line(mat1,new Point(0,mat1.height()),new Point(mat1.width(),0),new Scalar(255,255,0),4);

                //Utils.matToBitmap(mat1,bitmap1);
                //test1.setImageBitmap(bitmap1);

                Imgproc.cvtColor(mat1, mat2, Imgproc.COLOR_BGR2GRAY); //转灰度
                //Imgproc.cvtColor(mat1,mat1,Imgproc.COLOR_BGR2RGB);  //bgr转rgb

                //二值化 阈值 手动阈值
                Imgproc.threshold(mat2, mat2, 125, 255, Imgproc.THRESH_BINARY);

                //自适应阈值 可以描绘更多细节
                //Imgproc.adaptiveThreshold(mat2,mat2,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,13,5);

                //Imgproc.cvtColor(mat1,mat1,Imgproc.COLOR_RGB2HSV);  //RGB转hsv

                //颜色识别时候用？
                //Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
                //Imgproc.morphologyEx(mat1,mat1,Imgproc.MORPH_OPEN,kernel);  //开运算
                //Imgproc.morphologyEx(mat1,mat1,Imgproc.MORPH_CLOSE,kernel);  //闭运算


                //要使用bitmap展示图片
                //要先创建bitmap的窗口大小 否则直接用mat转换为bitmap会因为没有图片窗口大小而导致闪退
                bitmap2 = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);


                Utils.matToBitmap(mat2, bitmap2);    //mat转为bitmap格式用于Android上显示图片
                test2.setImageBitmap(bitmap2);

                //找轮廓
//                Mat output = new Mat();
//                Imgproc.findContours(mat2,contours,output,Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
//                count = contours.size();    //轮廓数量
//                Log.d("size" , "size:" + count);

                //画轮廓
//                Imgproc.drawContours(mat1,contours,-1,new Scalar(255,0,0),8);
//                bitmap3 = Bitmap.createBitmap(mat1.width(), mat1.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(mat1,bitmap3);
//                test3.setImageBitmap(bitmap3);

                Mat gauss = new Mat();

                //均值模糊
                //Imgproc.blur(mat2,gau,new Size(3,3),new Point(-1,-1), Core.BORDER_DEFAULT);

                //高斯模糊
                Imgproc.GaussianBlur(mat2, gauss, new Size(5, 5), 0);
                bitmap3 = Bitmap.createBitmap(gauss.width(), gauss.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(gauss, bitmap3);    //mat转为bitmap格式用于Android上显示图片
                test3.setImageBitmap(bitmap3);

                Mat gray = new Mat();
                //Imgproc.cvtColor(gau,gray,Imgproc.COLOR_BGR2GRAY);

                //Canny边缘检测
                Imgproc.Canny(gauss, gray, 40, 150);
                bitmap4 = Bitmap.createBitmap(gray.width(), gray.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(gray, bitmap4);    //mat转为bitmap格式用于Android上显示图片
                test4.setImageBitmap(bitmap4);

                //霍夫圆检测
                Mat circles = new Mat();

                /**
                 * ！！！修改参数
                 * @param image 输入的图像
                 * @param circles 输出的三个向量数组 圆心与半径x y r
                 * @param method 唯一支持Imgproc.HOUGH_GRADIENT
                 * @param dp 图像分辨率 越大相应减少分辨率 等于1的时候跟原图大小一致
                 * @param minDist 表示区分两个圆的圆心之间的最小的距离 如果两个圆之间的距离小于给定的minDist 则认为是同一个圆
                 *                可以帮助降低噪声的影响
                 * @param param1  边缘检测Canny算法中使用的高阈值
                 * @param param2  累加器阈值 越大则说明越有可能是圆
                 * @param minRadius 检测的最小圆半径 单位像素
                 * @param maxRadius 检测的最大圆半径 单位像素
                 */
                Imgproc.HoughCircles(gauss, circles, Imgproc.HOUGH_GRADIENT, 1,
                        1200, 100, 30, 200, 500);

                for (int i = 0; i < circles.cols(); i++) {
                    //float[] info = new float[3];
                    //circles.get(0, i, info);
                    double [] info = circles.get(0,i);
                    Point center = new Point((int) info[0], (int) info[1]);
                    int radius = (int) info[2];

                    //绘制圆
                    //Imgproc.circle(mat1, new Point((int) info[0], (int) info[1]), (int) info[2]
                           // ,new Scalar(255,255,255),4,8,0);
                    Imgproc.circle(mat1,center,radius,new Scalar(255,0,0),8,8,0);
                }

                bitmap5 = Bitmap.createBitmap(mat1.width(), mat1.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat1,bitmap5);
                test5.setImageBitmap(bitmap5);
                break;

            default:
                break;
        }
    }

    /**
     * 导入opencv
     */
    private void iniLoadOpencv() {
        boolean success = OpenCVLoader.initDebug();     //opencv初始化
        if (success) {
            Toast.makeText(MainActivity.this, "Loading Opencv Libraries..", Toast.LENGTH_LONG).show();
            Log.d("666", "Loading Opencv Libraries..");
        } else {
            Toast.makeText(MainActivity.this, "WARNING: Could not load Opencv Libraries !", Toast.LENGTH_LONG).show();
            Log.d("666", "WARNING");
        }
    }

}