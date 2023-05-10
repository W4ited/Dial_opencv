# Dial_opencv

## Android opencv 毕业设计
基于Android平台的指针式仪表读数识别

1.灰度图 二值化（手动阈值、自适应阈值）

2.高斯模糊

3.Canny边缘检测

4.霍夫圆检测

5.mask掩膜

6.mat对象 --> BGR格式 --> 正常显示图片 --> RGB格式 --> cvtColor

7.自动阈值法

8.轮廓识别

9.approxPolyDP()函数 --> 使得原始顶点数降低

10.手动计算距离 --> 找刻度线

11.刻度线拟合直线

12.霍夫直线检测

13.指针细化

14.显示读数

## 误差：
由于圆形表盘截取和0刻度线截取有误差 导致最后读数会产生误差。
     大致已经完成 可以改进了进行函数的封装。

## 可识别图片
ps：two、three、eight、nine、ten、sixteen、nineteen、twenty_eight、thirty_three、forty_two 不造成闪退
以上除two、three、twenty_eight之外均可以基本识别读出
 -->  仍可以进行优化改进

