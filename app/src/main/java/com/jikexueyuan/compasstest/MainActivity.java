package com.jikexueyuan.compasstest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    //1 传感管理器对象
    private SensorManager sensorManager;

    //ImageView对象
    private ImageView compassImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //引用view控件,ImageView实例
        compassImg = (ImageView) findViewById(R.id.compass_img);

        //取得传感服务
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //地磁和加速实例
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//地磁传感
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速

        //注册监听
        sensorManager.registerListener(listener,
                magneticSensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME);

    }

    /**
     * 销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null){
            sensorManager.unregisterListener(listener);
        }
    }

    /**
     *传感器事件监听器
     */
    private SensorEventListener listener = new SensorEventListener() {

        //长度为3的两个浮点数组对象
        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        //最后旋转角度对象
        private float lastRotateDegree;

        /**
         * 数据改变后， 使用旋转动画
         * @param event
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            //判断是加速还是地磁传感器
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                //赋值时需要调用clone()方法
                accelerometerValues = event.values.clone();
            }else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                magneticValues = event.values.clone();
            }
            float[] R = new float[9]; //长度为9的浮点数组
            float[] values = new float[3];
            //RotationMatrix 旋转模型方法为R数组赋值
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
           //getOrientation 方向方法为values数组赋值
            SensorManager.getOrientation(R, values);

            //将计算出的旋转角度取反，用于旋转指南针背景图
            float rotateDegree = -(float) Math.toDegrees(values[0]);
            if (Math.abs(rotateDegree - lastRotateDegree) > 1){
                //旋转动画实例
                RotateAnimation animation = new RotateAnimation(
                        lastRotateDegree, //旋转起始角度
                        rotateDegree,       //旋转的终止角度
                        Animation.RELATIVE_TO_SELF, //X轴
                        0.5f, //X轴中心点
                        Animation.RELATIVE_TO_SELF, //Y轴
                        0.5f); //Y轴中心点
                animation.setFillAfter(true); //动画终止时停留在最后一帧
                compassImg.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }

            /**
             * values[0] z轴弧度，
             *          取值范围-180到+180，
             *          正负180表示正南方向，
             *          0表示正北方，
             *          -90正西方，
             *          90正东方
             * Math.toDegrees（）方法把弧度转换成方向
             *
             */
            Log.d("MainActivity", "values[0] is " + Math.toDegrees(values[0]));

        }

        /**
         * 经度改变
         * @param sensor
         * @param accuracy
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
