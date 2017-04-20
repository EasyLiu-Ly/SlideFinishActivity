# SlideFinishActivity
让Activity跟随者手指的滑动而滑动，当向右滑动到一定距离或者往右滑动到达一定速度就finsih，类似于微信滑动finish的效果。

## 达到的效果如下,有点卡顿，将就着看^_^：
![演示动图](https://github.com/EasyLiu-Ly/SlideFinishActivity/blob/master/SlideFinishActivity.gif)

* 支持边界滑动和全屏滑动两种模式
* 解决了滑动冲突问题，例如上图中Activity当中就包含了ViewPager，解决了和ViewPager的滑动冲突问题

### 使用方式如下：
* 自定义的Activity继承自BaseAbsActivity
* 自定义的Activity的主题需要包含以下两个属性
``` xml
 <item name="android:windowIsTranslucent">true</item>
 <item name="android:windowBackground">@android:color/transparent</item>
```
