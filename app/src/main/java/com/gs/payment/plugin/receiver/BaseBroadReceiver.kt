package com.gs.payment.plugin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gs.payment.plugin.MainActivity

/**
 * @author chyi
 * @date 2025/9/29 8:18
 */
abstract class BaseBroadReceiver : BroadcastReceiver() {
    protected var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
    }

    protected fun log(msg: String) {
        context?.let {
            val intent = Intent(MainActivity.LOG_ACTION)
            intent.putExtra(MainActivity.LOG_MESSAGE_KEY, msg)
            it.sendBroadcast(intent)
        }
    }
}