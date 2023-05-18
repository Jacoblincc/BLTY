package com.first.vdemo.utils

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView


class CountDownUtil {
    companion object {
        private const val Default_num = 4 //计时数字
        private const val Final_txt = "开始！" //最后的文本
        private var Current_num = Default_num //当前计时

        //启动倒计时函数 供外部调用
        fun <T : TextView?> start(view: T, state: AnimateState) {
            //被倒计时的控件，倒计时时间
            start(view, Default_num, state)
        }
        //内部调用
        private fun <T : TextView?> start(view: T, num: Int, state: AnimateState) {
            // 设置计时
            Current_num = num - 1
            view!!.text = Current_num.toString()
            view.visibility = View.VISIBLE
            // 透明度改变
            val alphaAnimation = AlphaAnimation(1F, 0F)
            alphaAnimation.duration = 1000
            alphaAnimation.repeatCount = Current_num
            // 缩放
            val scaleAnimation = ScaleAnimation(
                0.1f, 1.3f, 0.1f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.duration = 1000
            scaleAnimation.repeatCount = Current_num
            //添加监听
            scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    state.start()
                }
                override fun onAnimationEnd(animation: Animation?) {
                    // 结束时设为不可见
                    view.visibility = View.GONE
                    state.end()
                }
                override fun onAnimationRepeat(animation: Animation?) {
                    //每次重复时
                    --Current_num
                    //改变文本
                    if (Current_num == 0) view.text = Final_txt
                    else view.text = Current_num.toString()
                    state.repeat()
                }
            })
            //同时播放
            val animationSet = AnimationSet(true)
            animationSet.addAnimation(scaleAnimation)
            animationSet.addAnimation(alphaAnimation)
            view.startAnimation(animationSet)
        }
    }
    //定义接口
    interface AnimateState {
        fun start()
        fun repeat()
        fun end()
    }
}