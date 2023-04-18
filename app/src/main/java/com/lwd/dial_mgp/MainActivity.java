package com.lwd.dial_mgp;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.approxPolyDP;

import androidx.annotation.LongDef;
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
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfFloat6;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 会有情况 出现ANR问题 UI not response --> 好像是因为没有release mat的问题
     * <p>
     * 可以将功能修改为函数形式 传参bitmap 在里面new Mat 在里面release释放空间
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
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5, bitmap6;
    private List<MatOfPoint> contours = new ArrayList<>();  //原始轮廓列表

    private List<MatOfPoint> cntSet = new ArrayList<>();  //符合条件的刻度线轮廓列表
    private List<MatOfPoint> needleCnt = new ArrayList<>(); //符合条件的指针轮廓列表
    private List<Double> cntAreas = new ArrayList<>();      //符合条件的刻度线轮廓的面积列表
    private List<Double> needleAreas = new ArrayList<>();   //符合条件的指针轮廓的面积列表
    private List<Double> location = new ArrayList<>();     //符合条件的轮廓的位置列表

    private int count = 0;


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

        //在外面new 则再次点击按钮时候会出错 因为再次点击时候没有new开空间 因为最后时候release释放了
        //mat1 = new Mat();
        //mat2 = new Mat();

        //会导致anr问题？ 还没找到问题

        //用mat读入图片
//        try {
//            mat1 = Utils.loadResource(this,R.drawable.meter);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

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

                //Mat mat1 = new Mat();
                Mat mat1;
                Mat mat2 = new Mat();

                //用mat读入图片
                try {
                    mat1 = Utils.loadResource(this, R.drawable.meter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //mat2为灰度图
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

                Mat gauss = new Mat();

                //均值模糊
                //Imgproc.blur(mat2,gau,new Size(3,3),new Point(-1,-1), Core.BORDER_DEFAULT);

                //高斯模糊
                Imgproc.GaussianBlur(mat2, gauss, new Size(5, 5), 0);
                bitmap3 = Bitmap.createBitmap(gauss.width(), gauss.height(), Bitmap.Config.ARGB_8888);
                Log.d("gauss size", "width:" + gauss.width() + "height:" + gauss.height());
                Utils.matToBitmap(gauss, bitmap3);    //mat转为bitmap格式用于Android上显示图片
                test3.setImageBitmap(bitmap3);
                //end 高斯模糊

                Mat canny = new Mat();

                //Canny边缘检测
                Imgproc.Canny(gauss, canny, 40, 150);
                bitmap4 = Bitmap.createBitmap(canny.width(), canny.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(canny, bitmap4);    //mat转为bitmap格式用于Android上显示图片
                test4.setImageBitmap(bitmap4);
                //end Canny边缘检测


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
                Imgproc.HoughCircles(canny, circles, Imgproc.HOUGH_GRADIENT, 1,
                        1200, 100, 30, 200, 500);

                //创建一个掩膜
                Mat mask = new Mat(mat1.size(), mat1.type(), Scalar.all(0));

                for (int i = 0; i < circles.cols(); i++) {
                    //float[] info = new float[3];
                    //circles.get(0, i, info);
                    double[] info = circles.get(0, i);
                    Point center = new Point((int) info[0], (int) info[1]);
                    int radius = (int) info[2];

                    //绘制圆时候 thickness参数为-1时候 为填充
                    //当第一个参数为mat1 当前图片绘制在原图上
                    Imgproc.circle(mask, center, radius, new Scalar(255, 255, 255), -1, 8, 0);
                    Log.d("radius", "radius:" + radius);
                    Log.d("center", "center:" + (int) info[0] + "  " + (int) info[1]);
                }

                //用掩膜之后剩余的结果result
                Mat result = new Mat();
                Core.bitwise_and(mat1, mask, result);

                bitmap5 = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                Log.d("gauss size", "width:" + result.width() + "height:" + result.height());


                //因为mat对象使用的是BGR格式的 转为RGB格式才能显示正确颜色
                Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(result, bitmap5);
                test5.setImageBitmap(bitmap5);
                //end 霍夫圆检测


                //刻度线
                Mat temp = new Mat();
                Mat gauss2 = new Mat();
                Utils.bitmapToMat(bitmap5, temp);
                Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY); //转灰度
                Imgproc.GaussianBlur(temp, gauss2, new Size(3, 3), 0);  //高斯

                /**自动阈值法 adaptiveThreshold
                 * src : Mat 输入图像
                 * dst : Mat 输出图像 阈值操作结果填充在此图像
                 * maxValue : double 分配给满足条件的像素的非零值
                 * adaptiveMethod : int 自定义使用的阈值算法，ADAPTIVE_THRESH_MEAN_C、ADAPTIVE_THRESH_GAUSSIAN_C
                 *    ADAPTIVE_THRESH_MEAN_C 时，T(x,y) = blockSize * blockSize【b】
                 *     blockSize【b】= 邻域内(x,y) - C
                 *    ADAPTIVE_THRESH_GAUSSIAN_C 时，T(x,y) = blockSize * blockSize【b】
                 *     blockSize【b】= 邻域内(x,y) - C与高斯窗交叉相关的加权总和
                 *
                 * thresholdType : int 阈值类型，只能是THRESH_BINARY 、 THRESH_BINARY_INV
                 * blockSize : int 用来计算阈值的邻域尺寸 3，5，7等等，奇数
                 * C : double 减去平均值或加权平均值的常数，通常情况下，它是正的，但也可能是零或负。
                 */
                Imgproc.adaptiveThreshold(gauss2, gauss2, 255
                        , Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
                //自适应阈值这里 最后C常数的改变 5时候 gauss2的图像显示 背景全白 -10时候背景全黑 并且改了之后 刻度线的寻找出现误差


                //轮廓识别
                Mat outMat = new Mat();
                Imgproc.findContours(gauss2, contours, outMat, RETR_LIST, CHAIN_APPROX_SIMPLE);
                count = contours.size();
                Log.d("count", "count:" + count);  // Log --> 300+

                //Imgproc.cvtColor(gauss2,gauss2,Imgproc.COLOR_GRAY2RGB);

                // --> 最大的轮廓的中心是否是圆心呢？
                double maxArea = 0;     //最大轮廓面积
                MatOfPoint largestContours = null;  //最大轮廓
                MatOfPoint2f contour2f;
                MatOfPoint2f approxCurve;
                double epsilon;

                //approxPolyDP()函数 使得原始顶点数降低
                //epsilon拟合的精度 --> 阈值
                for (int i = 0; i < count; i++) {
                    contour2f = new MatOfPoint2f(contours.get(i).toArray());
                    epsilon = 0.04 * Imgproc.arcLength(contour2f, true);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
                    if (approxCurve.rows() > 4) {
                        //顶点大于4 所以保证是圆
                        double area = Imgproc.contourArea(contours.get(i));
                        if (area > maxArea) {
                            maxArea = area;
                            largestContours = contours.get(i);
                        }
                    }
                }

                Log.d("count", "maxArea:" + maxArea);  //最大面积

                //获取轮廓的中心坐标
                //Moments moments = Imgproc.moments(largestContours);
                //将中心坐标作为圆心
                //Point center = new Point(moments.m10/moments.m00,moments.m01/moments.m00);
                //圆的半径怎么获取呢？

                // ------>

                //圆心坐标
                Point center = new Point();
                //半径
                float[] radius = new float[1];
                //获取圆的圆心坐标和半径
                Imgproc.minEnclosingCircle(new MatOfPoint2f(largestContours.toArray()), center, radius);

                //灰度图像转为RGB
                Imgproc.cvtColor(temp, temp, Imgproc.COLOR_GRAY2RGB);

                //drawContours 用于测试是否找到最大的圆轮廓 --> 即为表盘 画出表盘
                //circle 用于测试是否找到圆心 --> 画出圆心
                Imgproc.drawContours(temp, Arrays.asList(largestContours), -1, new Scalar(255, 0, 0), 4);
                Imgproc.circle(temp, center, 5, new Scalar(255, 0, 0), 2);
                //感觉圆心有瑕疵 因为最外面表盘的圆轮廓不是很标准 所以导致 获取到的圆心也不是特别的标准
                Log.d("count", "radius:" + radius[0]);

                //找到表盘 最外面的圆
                for (MatOfPoint contour : contours) {
                    //计算最小外接矩形
                    RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
                    Point[] box = new Point[4];
                    rect.points(box);   //得到矩形四个角的坐标

                    double w = rect.size.width;     //矩形宽度
                    double h = rect.size.height;    //矩形高度

                    // 如果宽度或高度为0，则跳过该矩形
                    if (h == 0 || w == 0) {
                        continue;
                    }

                    //计算矩形中心点与圆心的距离
                    /**旋转矩形的四个角点的坐标存储在 points 数组中，并按照顺序存储。
                     **具体来说，它们按照从左上角开始，顺时针方向依次存储。
                     * 假设 points 数组的长度为 4  --> 上面使用的是Point[] box ！
                     * points[0]：左上角点的坐标；
                     * points[1]：右上角点的坐标；
                     * points[2]：右下角点的坐标；
                     * points[3]：左下角点的坐标。
                     */
                    //找到矩形的中心点坐标
                    Point rectCenter = new Point(box[0].x + Math.abs((box[0].x - box[1].x) / 2),
                            box[3].y + Math.abs((box[0].y - box[3].y) / 2));
                    double distance = distanceOfPoint(center.x, center.y, rectCenter.x, rectCenter.y);

                    //确定两个阈值
                    if ((radius[0] * 0.609) < distance && (radius[0] )> distance) {
                        //在圆内部
                        location.add(distance); //记录位置 圆心和矩形中心的距离
                        if (h / w > 5 || w / h > 5) {
                            //如果高宽的比例大于5 则认为是刻度线
                            cntSet.add(contour);    //记录该符合条件的轮廓 --> 刻度线轮廓
                            cntAreas.add(w * h);    //添加面积 --> 刻度线面积 --> 面积的用处
                        }

                        //这里判断指针没有写 --> 看是否可以后面使用霍夫直线检测
                        else { // 否则，将该轮廓添加到指针轮廓列表中

                            //1.指针长度不会大于表盘
                            //2.指针必定在表盘内
                            //3.指针一定过圆心

                            if (w > (radius[0] * 2) || h > (radius[0] * 2)) {
                                // 如果宽度或高度大于圆半径的一半，则认为是指针
                                needleCnt.add(contour); // 添加到指针轮廓列表
                                needleAreas.add(w * h); //添加面积 --> 面积的用处
                            }
                        }

                    }
                }

                //是因为图像的原因？ 其实指针的直线不是特别的值 导致其实是一段一段的
                Log.d("count","needleSize:" + needleCnt.size());

                //下面有画出刻度线的另一种方式
                //画出刻度
//                for (int i = 0; i < needleCnt.size(); i++) {
//                    Imgproc.drawContours(temp, needleCnt, -1, new Scalar(0, 0, 255), 2);
//                }

                //刻度线拟合直线
                List<float[]> kb = new ArrayList<float[]>();

                for (MatOfPoint mat : cntSet) {
                    MatOfPoint2f xx = new MatOfPoint2f(mat.toArray());
                    // 计算轮廓点集的最小外接矩形
                    RotatedRect rect = Imgproc.minAreaRect(xx);

//                    // 调整最小外接矩形的角度，使其垂直于直线
//                    if (rect.size.width > rect.size.height) {
//                        double tmp = rect.size.width;
//                        rect.size.width = rect.size.height;
//                        rect.size.height = tmp;
//                        rect.angle += 90.0f;
//                    }


                    // 构造一个Point数组，用于存储最小外接矩形的四个顶点
                    Point[] box = new Point[4];
                    // 将最小外接矩形的四个顶点存储到Point数组中
                    rect.points(box);
                    for (int i = 0; i < 4; i++) {
                        // 绘制最小外接矩形的 !(四条边) --> %4 保证找到全部四条边
                        //画出刻度线
                        Imgproc.line(temp, box[i], box[(i + 1) % 4], new Scalar(255, 0, 0), 2);
                    }

                    // 构造一个MatOfFloat对象，用于存储拟合得到的直线参数
                    MatOfFloat output = new MatOfFloat();

                    /**  对轮廓点集进行直线拟合
                     * @param xx 输入的二维点集，类型为MatOfPoint2f，它是OpenCV中用于存储二维点坐标的数据结构，
                     *           其中每个元素表示一个二维点的坐标，包含两个浮点型数值表示点的x和y坐标
                     * @param output 输出参数，用于存储拟合得到的直线参数，类型为MatOfFloat6，
                     *               它是OpenCV中用于存储一组浮点型数值的数据结构，
                     *               其中包含6个浮点型数值，前两个数值表示直线的方向向量，
                     *               后两个数值表示直线上的一点，最后两个数值表示直线的长度。
                     * @param Imgproc.DIST_L2 距离类型，指定拟合直线时使用的距离度量类型，此处使用的是欧几里得距离。
                     * @param param 距离度量类型的参数，用于指定混合距离的权重系数，对于欧几里得距离无效。
                     * @param reps 拟合直线的精度，指定当点到直线距离的平均值小于该值时停止迭代。
                     * @param aeps 拟合直线的精度，指定当直线方向向量的变化量小于该值时停止迭代。
                     */
                    Imgproc.fitLine(xx, output, Imgproc.DIST_L2, 0, 0.001, 0.001);

                    // 将拟合得到的直线参数存储到一个float数组中
                    //float[] arr = output.toArray();
                    float[] arr = new float[output.rows() * output.cols()];
                    output.get(0,0,arr);
                    float k = arr[1] / arr[0]; // 计算直线的斜率
                    k = Math.round(k * 100) / 100f;  // 将斜率保留两位小数
                    float b = arr[3] - k * arr[2];  // 计算直线的截距
                    b = Math.round(b * 100) / 100f;  // 将截距保留两位小数

                    /**
                     * 把圆心作为拟合直线的起点 我只有右边有拟合直线
                     */
                    int x1 = 0;  // 直线起点的x坐标
                    //int x1 = (int) center.x;
                    int x2 = mat2.cols();  // 直线终点的x坐标
                    int y1 = Math.round(k * x1 + b);  // 计算直线起点的y坐标
                    //int y1 = (int) center.y;
                    int y2 = Math.round(k * x2 + b);  // 计算直线终点的y坐标
                    Imgproc.line(temp, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0), 1);  // 绘制直线
                    kb.add(new float[]{k, b});  // 将直线的斜率和截距存储到kb列表中

                }


                //霍夫直线检测
                /**
                 * HoughLines
                 * @param image 表示输入的图像 8位单通道图像 一般为二值图像
                 * @param lines 表示输出的每个直线的极坐标参数方程的两个参数
                 * @param rho 表示极坐标空间r值每次的步长 一般为1
                 * @param theta 表示角度θ 每次移动1°即可
                 * @param threshold 表示极坐标中该点的累积数 越大则直线可能越长 通常30~50 单位像素
                 *        假设30时候 则表示大于30个像素长度的直线才会被检测到
                 */

                /**
                 * HoughLinesP
                 * @param image 表示输入的图像 8位单通道图像 一般为二值图像
                 * @param lines 表示输出的每个直线的极坐标参数方程的两个参数
                 * @param rho 表示极坐标空间r值每次的步长 一般为1
                 * @param theta 表示角度θ 每次移动1°即可
                 * @param threshold 表示极坐标中该点的累积数 越大则直线可能越长 通常30~50 单位像素
                 *        假设30时候 则表示大于30个像素长度的直线才会被检测到
                 * @param minLineLength 表示可以检测的最小线段长度  ！！根据实际需要进行设置!!
                 * @param maxLineGap 表示线段之间的最大间隔像素 假设5表示小于5个像素的两个相邻线段可以连接起来
                 */
                Mat lines = new Mat();
                Imgproc.HoughLinesP(gauss2, lines,1,Math.PI/180.0,100,15,2);

                for (int i = 0; i < lines.rows(); i++) {
                    //HoughLines
                    //double rho = lines.get(i, 0)[0], theta = lines.get(i, 0)[1];
                    //double a = Math.cos(theta), b = Math.sin(theta);
                    //double x0 = a * rho,y0 = b * rho;
                    //Point pt1 = new Point(Math.round(x0 + 1000 * (-b)),Math.round(y0 + 1000*(a)));
                    //Point pt2 = new Point(Math.round(x0 - 1000*(-b)),Math.round(y0 - 1000 * (a)));

                    //HoughLinesP
                    int [] oneLine = new int[4];
                    lines.get(i,0,oneLine);

                    //绘制直线
                    //Imgproc.line(result,pt1,pt2,new Scalar(0,0,255),3,Imgproc.LINE_AA,0);
                    //Imgproc.line(temp,new Point(oneLine[0],oneLine[1]),new Point(oneLine[2],oneLine[3]),new Scalar(0,0,255),2,8,0);
                }


                //gauss2
                bitmap6 = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888);

                Utils.matToBitmap(temp, bitmap6);
                test6.setImageBitmap(bitmap6);
                //end 霍夫直线检测


                Utils.matToBitmap(gauss2,bitmap5);
                test5.setImageBitmap(bitmap5);

                //高斯背景重建


                //释放mat内存
                // ！！ 否则可能导致anr的问题？ ！！
                // --> 封装起来用一个release方法
                mat1.release();
                mat2.release();
                gauss.release();
                canny.release();
                circles.release();
                mask.release();
                result.release();
                temp.release();
                gauss2.release();
                outMat.release();
                lines.release();
                break;

            default:
                break;
        }
    }

    /**
     * 计算两点之间的距离 --> 欧几里得距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distanceOfPoint(double x1, double y1, double x2, double y2) {
        //两点之间距离 欧几里得距离
        int distance = (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
        return distance;
    }

    /**
     * 导入opencv
     * Toast提示是否Opencv初始化成功
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