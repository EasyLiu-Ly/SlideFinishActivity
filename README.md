# SlideFinishActivity
让Activity跟随者手指的滑动而滑动，当向右滑动到一定距离或者往右滑动到达一定速度就finsih，类似于微信滑动finish的效果。

## 达到的效果如下：
![演示动图](https://github.com/EasyLiu-Ly/SlideFinishActivity/blob/master/SlideFinishActivity.gif)

* 支持边界滑动和全屏滑动两种模式
* 解决了滑动冲突问题，例如上图中Activity当中就包含了ViewPager，解决了和ViewPager的滑动冲突问题

### 使用方式如下：
* 自定义的Activity继承自BaseSlideFinishActivity
* 自定义的Activity的主题需要包含以下两个属性
``` xml
 <item name="android:windowIsTranslucent">true</item>
 <item name="android:windowBackground">@android:color/transparent</item>
```
### 滑动动画
在滑动的时候，可以实现滑动动画效果，如下所示，类似于酷狗播放界面滑动Finish的效果。具体实现查看SlideFinishRelativeLayout中的IOnSlideFinishChangeListener接口，在BaseSlideFinishActivity当中实现了这个接口，在这个接口里面对顶层视图进行rotation操作即可。

![滑动动画](https://github.com/EasyLiu-Ly/SlideFinishActivity/blob/master/SlideFinishActivity2.gif)
