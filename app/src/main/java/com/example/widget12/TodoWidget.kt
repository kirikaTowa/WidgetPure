package com.example.widget12

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.example.widget12.TodoWidget.Companion.count

const val TAG = "TodoWidget"


class TodoWidget : AppWidgetProvider() {

    companion object{
        var count=5
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "TodoWidget onReceive: ")
        Log.d(TAG, "TodoWidget onReceive: ")
        context?.let {
            val action = intent?.action

            if ("CountingService.COUNT_UPDATE" == action) {
                // “更新”广播
                Log.d(TAG, "TodoWidget 触发计数器 将当前count显示出来: ")

                // 增加计数器
                count++

                // 更新 Widget UI
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }else if ("CountingService.COUNT_CLEAN" == action){
                count=0

                // 更新 Widget UI
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, TodoWidget::class.java))

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d(TAG, "onAppWidgetOptionsChanged: 组件大小发生变化")
    }

    override fun onEnabled(context: Context) {
        /*首次创建组件*/
        Log.d(TAG, "onEnabled")
    }

    override fun onDisabled(context: Context) {
        /*最后一个组件被删除*/
        Log.d(TAG, "onDisabled")
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted")
    }
}

@SuppressLint("RemoteViewLayout")
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val remoteViewsMin = RemoteViews(
        context.packageName,
        R.layout.todo_widget_min
    )

    val remoteViewsNormal = RemoteViews(
        context.packageName,
        R.layout.todo_widget_normal
    )

    val remoteViewsMax = RemoteViews(
        context.packageName,
        R.layout.todo_widget_max
    ).apply {
        //打开App
        setOnClickPendingIntent(R.id.add, openAppPendingIntent(context, R.id.add))

        setOnClickPendingIntent(R.id.tvStart, countUp(context, R.id.tvStart))
        setOnClickPendingIntent(R.id.tvClean, cleanUp(context, R.id.tvClean))
    }

    /*不同大小控件对应不同布局*/
    val viewMapping: MutableMap<SizeF, RemoteViews> = mutableMapOf()
    viewMapping[SizeF(180.0f, 110.0f)] = remoteViewsMin
    viewMapping[SizeF(230.0f, 180.0f)] = remoteViewsNormal
    viewMapping[SizeF(270.0f, 300.0f)] = remoteViewsMax

    // 设置 TextView 的文本为计数器的值
    remoteViewsMax.setTextViewText(R.id.tvContent, count.toString())



    /*只有在Android12以上版本才支持*/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(viewMapping))
    }else{
        appWidgetManager.updateAppWidget(appWidgetId, remoteViewsNormal)
    }


}

private fun openAppPendingIntent(context: Context,@IdRes id:Int):PendingIntent{
    /*打开APP intent*/
    val activityIntent = Intent(context, MainActivity::class.java).apply {
        setData(Uri.parse("harvic:$id"))
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val appOpenIntent = PendingIntent.getActivity(
        context,
        1,
        activityIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return appOpenIntent
}

private fun countUp(context: Context,@IdRes id:Int):PendingIntent{
    val intent = Intent(context, TodoWidget::class.java).apply {
        action = "CountingService.COUNT_UPDATE"
    }
    return PendingIntent.getBroadcast(
        context,
        2,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

//requestCode不能一样 不然UI会异常
private fun cleanUp(context: Context,@IdRes id:Int):PendingIntent{
    val intent = Intent(context, TodoWidget::class.java).apply {
        action = "CountingService.COUNT_CLEAN"
    }
    return PendingIntent.getBroadcast(
        context,
        3,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}