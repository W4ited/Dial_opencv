package com.lwd.dial_mgp;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_LIST;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

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
    private Button start, picture, camera;
    private TextView text_result;
    private RelativeLayout click;
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5, bitmap6;
    private List<MatOfPoint> contours = new ArrayList<>();  //原始轮廓列表

    private List<MatOfPoint> cntSet = new ArrayList<>();  //符合条件的刻度线轮廓列表
    private List<MatOfPoint> needleCnt = new ArrayList<>(); //符合条件的指针轮廓列表
    private List<Double> cntAreas = new ArrayList<>();      //符合条件的刻度线轮廓的面积列表
    private List<Double> needleAreas = new ArrayList<>();   //符合条件的指针轮廓的面积列表
    private List<Double> location = new ArrayList<>();     //符合条件的轮廓的位置列表

    private Point mark;
    private int count = 0;

    private double x1_needle,x2_needle;
    private int y1_needle,y2_needle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //导入Opencv opencv库的加载
        iniLoadOpencv();

        //组件初始化
        //init();
        //click = (RelativeLayout) findViewById(R.id.relative_click);
        //click.setOnTouchListener(this);
        test1 = (ImageView) findViewById(R.id.test_img1);
        test2 = (ImageView) findViewById(R.id.test_img2);
        test3 = (ImageView) findViewById(R.id.test_img3);
        test4 = (ImageView) findViewById(R.id.test_img4);
        test5 = (ImageView) findViewById(R.id.test_img5);
        test6 = (ImageView) findViewById(R.id.test_img6);
        //test6.setOnClickListener(this);
        test6.setOnTouchListener(this);

        start = (Button) findViewById(R.id.test_start);
        start.setOnClickListener(this);


        text_result = (TextView) findViewById(R.id.text_result);
        text_result.setOnClickListener(this);

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
                Mat average = new Mat();

                //均值模糊
                Imgproc.blur(mat2, average, new Size(3, 3), new Point(-1, -1), Core.BORDER_DEFAULT);

                //高斯模糊
                Imgproc.GaussianBlur(mat2, gauss, new Size(5, 5), 0);
                bitmap3 = Bitmap.createBitmap(gauss.width(), gauss.height(), Bitmap.Config.ARGB_8888);
                Log.d("gauss size", "width:" + gauss.width() + "height:" + gauss.height());
                Utils.matToBitmap(gauss, bitmap3);    //mat转为bitmap格式用于Android上显示图片
                test3.setImageBitmap(bitmap3);
                //end 高斯模糊

                //Utils.matToBitmap(gauss, bitmap2);    //mat转为bitmap格式用于Android上显示图片
                //test2.setImageBitmap(bitmap2);


                Mat canny = new Mat();

                //Canny边缘检测
                /** Canny边缘检测
                 * @param image 输入图像
                 * @param edges 表示输出的二值化边缘图像
                 * @param threshold1 表示低阈值T1
                 * @param threshold2 表示低阈值T2
                 * @param apertureSize 用于内部计算梯度Sobel
                 * @param L2gradient  计算图像梯度的计算方法
                 *                    Canny函数的第四个参数来指定连接方法（默认为True，即双阈值连接方法），
                 *                    将其设置为False可以使用基于单阈值的连接方法。
                 *
                 * canny输出的二值图
                 * Canny边缘检测算法首先会对输入图像进行高斯滤波，以减少噪声的影响。
                 * 您可以尝试增大高斯滤波器的大小，以获得更平滑的图像，从而提高边缘检测的准确性。
                 *
                 * 可以尝试减小低阈值，以检测更多的边缘。
                 */
                Imgproc.Canny(gauss, canny, 5, 90, 3, false);
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

                Utils.matToBitmap(result, bitmap2);
                test2.setImageBitmap(bitmap2);


                //因为mat对象使用的是BGR格式的 转为RGB格式才能显示正确颜色
                Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGB);
                Utils.matToBitmap(result, bitmap5);
                test5.setImageBitmap(bitmap5);
                //end 霍夫圆检测


                //刻度线
                Mat temp = new Mat();
                //Mat temp1 = new Mat();
                Mat gauss2 = new Mat();
                Mat gaussTest = new Mat();
                Utils.bitmapToMat(bitmap5, temp);

                //Utils.bitmapToMat(bitmap5,temp1);
                //temp1 = temp;

                Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY); //转灰度
                //高斯
                Imgproc.GaussianBlur(temp, gauss2, new Size(3, 3), 0);
                Imgproc.GaussianBlur(temp, gaussTest, new Size(3, 3), 0);

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
                //自动阈值法
                Imgproc.adaptiveThreshold(gauss2, gauss2, 255
                        , Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
                Imgproc.adaptiveThreshold(gaussTest, gaussTest, 255
                        , Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, -10);
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
                //Imgproc.drawContours(temp, Arrays.asList(largestContours), -1, new Scalar(255, 0, 0), 4);
                //Imgproc.circle(temp, center, 5, new Scalar(255, 0, 0), 2);
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
                    //Point rectCenter = new Point(box[1].x-box[3].x,box[1].y-box[3].y);
                    double distance = Distance.distanceOfPoint(center.x, center.y, rectCenter.x, rectCenter.y);

                    //确定两个阈值
                    if ((radius[0] * 0.609) < distance && (radius[0]) > distance) {
                        //在圆内部
                        location.add(distance); //记录位置 圆心和矩形中心的距离
                        if (h / w > 5 || w / h > 5) {
                            //如果高宽的比例大于5 则认为是刻度线
                            cntSet.add(contour);    //记录该符合条件的轮廓 --> 刻度线轮廓
                            cntAreas.add(w * h);    //添加面积 --> 刻度线面积 --> 面积的用处
                        }

                        //这里判断指针没有写 --> 看是否可以后面使用霍夫直线检测
//                        else { // 否则，将该轮廓添加到指针轮廓列表中
//
//                            //1.指针长度不会大于表盘
//                            //2.指针必定在表盘内
//                            //3.指针一定过圆心
//
//                            if (w > (radius[0] * 2) || h > (radius[0] * 2)) {
//                                // 如果宽度或高度大于圆半径的一半，则认为是指针
//                                needleCnt.add(contour); // 添加到指针轮廓列表
//                                needleAreas.add(w * h); //添加面积 --> 面积的用处
//                            }
//                        }

                    }
                }

                //是因为图像的原因？ 其实指针的直线不是特别的值 导致其实是一段一段的
                //Log.d("count", "needleSize:" + needleCnt.size());

                //下面有画出刻度线的另一种方式
                //画出刻度
//                for (int i = 0; i < needleCnt.size(); i++) {
//                    Imgproc.drawContours(temp, needleCnt, -1, new Scalar(0, 0, 255), 2);
//                }

                //刻度线拟合直线
                List<float[]> kb = new ArrayList<float[]>();


                /**
                 *
                 * 尝试使用背景差法
                 *
                 * 背景差法是一种用于减去背景信号的方法，通常用于视频或图像处理中的动态场景。
                 * 在这种情况下，我们可以采集两个图像或帧，一个是静态背景，另一个是包含动态对象的场景。
                 * 通过对这两个图像进行减法，我们可以得到仅包含动态对象的图像。
                 *
                 * 然而，如果您的应用场景是静态场景，背景差法可能不是最优的选择。
                 * 因为在静态场景中，背景信号并不会发生变化，因此我们可以直接使用原始图像或帧，而无需进行背景减法。
                 * 除非您需要检测静态场景中的运动目标，否则背景差法可能不适用于静态场景。
                 *
                 */
                Mat testCircleCnt = new Mat(canny.size(), canny.type(), Scalar.all(0));


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
                        //画出刻度线 --> 可显示
                        Imgproc.line(testCircleCnt, box[i], box[(i + 1) % 4], new Scalar(255, 0, 0), 2);
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
                    output.get(0, 0, arr);
                    float k_scale = arr[1] / arr[0]; // 计算直线的斜率
                    k_scale = Math.round(k_scale * 100) / 100f;  // 将斜率保留两位小数
                    float b_scale = arr[3] - k_scale * arr[2];  // 计算直线的截距
                    b_scale = Math.round(b_scale * 100) / 100f;  // 将截距保留两位小数

                    /**
                     * 把圆心作为拟合直线的起点 我只有右边有拟合直线
                     */
                    int x1 = 0;  // 直线起点的x坐标
                    //int x1 = (int) center.x;
                    int x2 = mat2.cols();  // 直线终点的x坐标
                    int y1 = Math.round(k_scale * x1 + b_scale);  // 计算直线起点的y坐标
                    //int y1 = (int) center.y;
                    int y2 = Math.round(k_scale * x2 + b_scale);  // 计算直线终点的y坐标

                    //拟合直线绘制 --> 可显示
                    //Imgproc.line(temp, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0), 1);  // 绘制直线

                    // 将直线的斜率和截距存储到kb列表中
                    kb.add(new float[]{k_scale, b_scale});


                    //temp1 = temp;
                }

                Utils.matToBitmap(testCircleCnt, bitmap4);
                test4.setImageBitmap(bitmap4);


                //霍夫直线检测 --> 找指针
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
                 *              -> 输入的二值化图像，必须为单通道、8 位或 32 位浮点数格式。
                 * @param lines 表示输出的每个直线的极坐标参数方程的两个参数
                 *              -> 输出的直线参数矩阵，每行包含四个整数，表示检测到的一条直线的起始点和终止点的坐标。
                 * @param rho 表示极坐标空间r值每次的步长 一般为1
                 *            -> 极径的分辨率，以像素为单位。
                 * @param theta 表示角度θ 每次移动1°即可
                 *              -> 极角的分辨率，以弧度为单位。
                 * @param threshold 表示极坐标中该点的累积数 越大则直线可能越长 通常30~50 单位像素
                 *        假设30时候 则表示大于30个像素长度的直线才会被检测到
                 *              -> 直线阈值，用于确定检测到的直线是否有效。
                 * @param minLineLength 表示可以检测的最小线段长度  ！！根据实际需要进行设置!!
                 *                      -> 直线的最小长度。
                 * @param maxLineGap 表示线段之间的最大间隔像素 假设5表示小于5个像素的两个相邻线段可以连接起来
                 *                   -> 直线的最大间隔。
                 */

                //直接用canny来霍夫直线检测 因为canny边缘检测 会包括仪表盘外
                //所以可以先使用掩膜 来保证 霍夫直线检测是检测 仪表盘之内的
                //Mat mask = new Mat(mat1.size(), mat1.type(), Scalar.all(0));

                //Imgproc.cvtColor(temp1,temp1,Imgproc.COLOR_BGR2RGB);

                //temp temp1
                Mat circleMask = new Mat(temp.size(), mat1.type(), Scalar.all(0));
                Imgproc.circle(circleMask, center, (int) radius[0], new Scalar(255, 255, 255), -1);
                Mat circle = new Mat();
                Core.bitwise_and(temp, circleMask, circle);

                Mat gray = new Mat();
                // 将掩码转换为灰度图像
                //Imgproc.cvtColor(circle, gray, Imgproc.COLOR_BGR2GRAY);

                //Canny边缘检测
                /** Canny边缘检测
                 * @param image 输入图像
                 * @param edges 表示输出的二值化边缘图像
                 * @param threshold1 表示低阈值T1
                 * @param threshold2 表示低阈值T2
                 * @param apertureSize 用于内部计算梯度Sobel
                 * @param L2gradient  计算图像梯度的计算方法
                 *                    Canny函数的第四个参数来指定连接方法（默认为True，即双阈值连接方法），
                 *                    将其设置为False可以使用基于单阈值的连接方法。
                 *
                 * canny输出的二值图
                 * Canny边缘检测算法首先会对输入图像进行高斯滤波，以减少噪声的影响。
                 * 您可以尝试增大高斯滤波器的大小，以获得更平滑的图像，从而提高边缘检测的准确性。
                 *
                 * 可以尝试减小低阈值，以检测更多的边缘。
                 */
                //20 150 当前效果较好
                Imgproc.Canny(circle, gray, 20, 150, 3, true);

                //整个表盘明显的canny边缘检测
                //Utils.matToBitmap(gray, bitmap4);
                //test4.setImageBitmap(bitmap4);

                /**
                 * 是否需要背景差法 直接去掉刻度？
                 *
                 *
                 *  没能实现
                 *
                 * 在图2 已经找到全部刻度
                 *
                 * 尝试使用减法 在图3只标绘指针
                 *
                 */

//                Mat diff = new Mat();
//                //Core.absdiff(testCircleCnt,gray,diff);
//                Core.subtract(gray,testCircleCnt,diff);
//                Utils.matToBitmap(diff,bitmap3);
//                test3.setImageBitmap(bitmap3);


                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                // 膨胀计算
                Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 1);
                //形态学膨胀操作 将掩膜图像中的白色区域扩大一点 --> 以避免直线检测时漏掉元素


                Utils.matToBitmap(gray, bitmap3);
                test3.setImageBitmap(bitmap3);

                //霍夫直线检测
                Mat lines = new Mat();
                //canny gauss2 gauss
                //使用gauss2 和 gaussTest 图像不一样  -----> 见上面275行的代码
                //参数 minLineLength 表示可以检测的最小线段长度 设置为半径的一半
                //因为指针比半径的一半长 以排除其他线
                Imgproc.HoughLinesP(gray, lines, 1, Math.PI / 180.0, 100, (radius[0] / 2), 2);


                Mat n_mask = new Mat(temp.size(), mat1.type(), Scalar.all(0));

                double[] axit = {0, 0}; //保存指针直线的终点
                for (int i = 0; i < lines.rows(); i++) {
//                    //HoughLines
//                    //double rho = lines.get(i, 0)[0], theta = lines.get(i, 0)[1];
//                    //double a = Math.cos(theta), b = Math.sin(theta);
//                    //double x0 = a * rho,y0 = b * rho;
//                    //Point pt1 = new Point(Math.round(x0 + 1000 * (-b)),Math.round(y0 + 1000*(a)));
//                    //Point pt2 = new Point(Math.round(x0 - 1000*(-b)),Math.round(y0 - 1000 * (a)));
//
//                    //HoughLinesP
//                    int[] oneLine = new int[4];
//                    lines.get(i, 0, oneLine);
//
                    double[] line = lines.get(i, 0);
                    double x1 = line[0];
                    double y1 = line[1];
                    double x2 = line[2];
                    double y2 = line[3];
                    //必须绘制这直线 不然下面指针细化时候没有轮廓
                    Imgproc.line(n_mask, new Point(x1, y1), new Point(x2, y2), new Scalar(100, 100, 100), 1, Imgproc.LINE_AA, 0);

                    Utils.matToBitmap(n_mask, bitmap3);
                    test3.setImageBitmap(bitmap3);

                    double d1 = Distance.distanceOfPoint(x1, y1, center.x, center.y);
                    double d2 = Distance.distanceOfPoint(x2, y2, center.x, center.y);
                    //为了找到直线的终点 用axit数组保存起来直线的终点
                    if (d1 > d2) {
                        axit[0] = x1;
                        axit[1] = y1;
                    } else {
                        axit[0] = x2;
                        axit[1] = y2;
                    }

                    Log.d("axit", "axit.x:" + axit[0] + "y:" + axit[1]);

                    //腐蚀操作 --> 可以缩小图像中的物体，使其变得更加细小 --> 看不到图像
                    //Imgproc.erode(n_mask,n_mask,kernel,new Point(-1,-1),1);
                    //膨胀操作 --> 线变粗
                    //Imgproc.dilate(n_mask, n_mask, kernel, new Point(-1, -1), 1);
//
//                    /**
//                     * 判断直线长度和半径的关系？
//                     *  -->
//                     *  以此去除干扰 找到指针
//                     *  --> 调用函数 计算欧几里得距离
//                     */
//                    double distance = Distance.distanceOfPoint(oneLine[0], oneLine[1], oneLine[2], oneLine[3]);
////                    if (distance < radius[0] && distance > (radius[0]/2)) {
////
////
////                        //绘制直线
////                        //Imgproc.line(result,pt1,pt2,new Scalar(0,0,255),3,Imgproc.LINE_AA,0);
////
////                        Imgproc.line(temp, new Point(oneLine[0], oneLine[1]), new Point(oneLine[2], oneLine[3]), new Scalar(0, 0, 255), 2, 8, 0);
////
////                        //从圆心开始画指针线
////                        //Imgproc.line(temp,center,new Point(oneLine[2],oneLine[3]),new Scalar(0,0,255),2,8,0);
////                    }
                    //Imgproc.line(gray, new Point(oneLine[0], oneLine[1]), new Point(oneLine[2], oneLine[3]), new Scalar(0, 0, 255), 2, 8, 0);
                }

                /**
                 * 指针细化
                 */
                //再查找直线轮廓,指针细化,找指针的骨架
                //找到图像中的轮廓
                //MatOfPoint2f needleContours = new MatOfPoint2f();
                Mat hierarchy = new Mat();
                Imgproc.cvtColor(n_mask, n_mask, Imgproc.COLOR_RGB2GRAY);
                Imgproc.GaussianBlur(n_mask, n_mask, new Size(3, 3), 0);
                Imgproc.adaptiveThreshold(n_mask, n_mask, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, -10);

                //bitmap1 = Bitmap.createBitmap(n_mask.width(), n_mask.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(n_mask, bitmap5);
                test5.setImageBitmap(bitmap5);

                //needleCnt 为空的 --> 上面的指针绘制必须画出来 因为需要以此图进行轮廓寻找并且使用最小二乘法
                //所以如果上面的指针不绘制 会导致图全黑 没有轮廓 所以找不到轮廓 下面get方法会报错
                Imgproc.findContours(n_mask, needleCnt, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);

                Log.d("axit", "needleSize:" + needleCnt.size());
                //计算每个轮廓的面积
                for (MatOfPoint mat : needleCnt) {
                    //计算轮廓面积
                    double area = Imgproc.contourArea(mat);
                    needleAreas.add(area);
                }

                Log.d("axit", "needleAreasSize:" + needleAreas.size());

                //找到具有最大面积的轮廓的索引
                int maxIndex = 0;       //开始索引为0 从开始找
//                if (needleAreas.size() != 0){
//                    double needleMaxArea = needleAreas.get(0); //开始最大面积为第一个 后面比较之后进行修改
//                    for (int i = 0; i < needleAreas.size(); i++) {
//                        if (needleAreas.get(i) > needleMaxArea) {
//                            maxIndex = i;
//                            needleMaxArea = needleAreas.get(i);
//                        }
//                    }
//
//                }
                double needleMaxArea = needleAreas.get(0); //开始最大面积为第一个 后面比较之后进行修改
                for (int i = 0; i < needleAreas.size(); i++) {
                    if (needleAreas.get(i) > needleMaxArea) {
                        maxIndex = i;
                        needleMaxArea = needleAreas.get(i);
                    }
                }

                //获得最大面积的轮廓
                //因为是按照轮廓列表 计算面积 所以索引也是对应位置
                MatOfPoint maxContour = needleCnt.get(maxIndex);

                Log.d("axit", "maxIndex:" + maxIndex);

                //使用最小二乘法拟合轮廓上的一条直线
                MatOfPoint2f maxContour2f = new MatOfPoint2f(maxContour.toArray());
                MatOfPoint2f lineParams = new MatOfPoint2f();
                Imgproc.fitLine(maxContour2f, lineParams, Imgproc.DIST_L2, 0, 0.001, 0.001);

                //计算直线的斜率和截距
                double k_needle = lineParams.get(1, 0)[0] / lineParams.get(0, 0)[0];
                double b_needle = lineParams.get(3, 0)[0] - k_needle * lineParams.get(2, 0)[0];

                //获取直线上的两个点的x坐标
                x1_needle = center.x;
                x2_needle = axit[0];

                //获取直线上的两个点的y坐标
                y1_needle = (int) Math.round(k_needle * x1_needle + b_needle);
                y2_needle = (int) Math.round(k_needle * x2_needle + b_needle);

                //在图像上画出直线
                Imgproc.line(temp, new Point(x1_needle, y1_needle), new Point(x2_needle, y2_needle), new Scalar(255, 0, 0), 4, Imgproc.LINE_AA);
                //Imgproc.line(temp,center,new Point(x2_needle,y2_needle),new Scalar(255,0,0),4,Imgproc.LINE_AA);
                Log.d("mark", "center.x:" + center.x + "center.y" + center.y);
                Log.d("axit", "needle.x:" + x1_needle + "needle.y" + y1_needle);

                //找到零刻度线 --> 显示读数
                //Imgproc.circle(temp, mark, 4, new Scalar(255, 0, 0), -1);
                //Log.d("mark","mark.x:" + mark.x + "mark.y:" + mark.y);
                //Imgproc.line(temp,center,mark,new Scalar(0,0,255),4,Imgproc.LINE_AA);


                //Imgproc.cvtColor(n_mask,n_mask,Imgproc.COLOR_GRAY2RGB);
                //temp
                bitmap6 = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(temp, bitmap6);
                test6.setImageBitmap(bitmap6);
                //end 霍夫直线检测

                Imgproc.circle(temp, mark, 4, new Scalar(255, 0, 0), -1);
                Log.d("mark", "mark.x:" + mark.x + "mark.y:" + mark.y);
                Imgproc.line(temp, center, mark, new Scalar(0, 0, 255), 4, Imgproc.LINE_AA);
                Utils.matToBitmap(temp, bitmap6);
                test6.setImageBitmap(bitmap6);

                Utils.matToBitmap(gaussTest, bitmap3);
                test3.setImageBitmap(bitmap3);

                //bitmap1 = Bitmap.createBitmap(gauss2.width(), gauss2.height(), Bitmap.Config.ARGB_8888);
                //Utils.matToBitmap(gauss2, bitmap1);
                //test1.setImageBitmap(bitmap1);


                //高斯背景重建？？ --> 动态 --> 背景差法


                //释放mat内存
                // ！！ 否则可能导致anr的问题？ ！！
                // --> 封装起来用一个release方法 -->但是需要传参

                mat1.release();
                mat2.release();
                gauss.release();
                average.release();
                canny.release();
                circles.release();
                mask.release();
                result.release();
                temp.release();
                gauss2.release();
                gaussTest.release();
                outMat.release();
                lines.release();
                circleMask.release();
                circle.release();
                kernel.release();
                gray.release();
                n_mask.release();
                hierarchy.release();
                //temp1.release();
                break;

            case R.id.text_result:
                double data = 0.0;

                //alpha为刻度起点和终点与圆心所成的夹角，beta为指针偏移刻度起点的角度，r为量程
                double r = 100;
                // ！ 感觉公式不对
                //double alpha = Math.atan((y1_needle-mark.y)/(x1_needle-mark.x)-(y1_needle-mark.y)/(x1_needle-mark.x));
                double alpha = 360;
                double beta = Math.atan(((y1_needle-y2_needle)/(x1_needle-x2_needle)-(y1_needle-mark.y)/(x1_needle-mark.x))/(1+(y1_needle-mark.y)/(x1_needle-mark.x)*(y1_needle-y2_needle)/(x1_needle-x2_needle)));
                double beta_degress = beta * (180 / Math.PI);

                data = r / alpha * beta_degress;
                //保证读数不是负数
                if (data < 0 ){
                    data = r + data;
                }
                String data_result = Double.toString(data);
                text_result.setText(data_result);

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mark = new Point(motionEvent.getX() * 2, motionEvent.getY() - 50);
                Log.d("mark", "markFist:" + mark.x + "y:" + mark.y);
        }

        /**
         *  注意返回值
         *  true：view继续响应Touch操作；
         *  false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
         */
        return false;
    }


    public static class Distance {
        /**
         * 计算两点之间的距离 --> 欧几里得距离
         * double
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
         * 计算两点之间的距离 --> 欧几里得距离
         * int
         *
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @return
         */
        public static int distanceOfPoint(int x1, int y1, int x2, int y2) {
            int distance = (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
            return distance;
        }
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