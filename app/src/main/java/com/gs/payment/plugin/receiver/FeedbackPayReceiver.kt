package com.gs.payment.plugin.receiver

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.innohi.YNHAPI

class FeedbackPayReceiver : BaseBroadReceiver() {

    companion object {
        private const val TAG = "PaymentPlugin.FeedbackPayReceiver"

        const val ACTION = "com.coffeeji.payment.plugin.MAKE_STATE_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.i(TAG, "Received intent action: ${intent.action}")
        log("Received intent action: ${intent.action}")
        if (intent.action != ACTION) return

        val orderId = intent.getStringExtra("ORDER_ID")
        val orderMoney = intent.getStringExtra("ORDER_MONEY")
        val state = intent.getStringExtra("STATE")
        Log.i(TAG, "MAKE_STATE_ACTION received. ORDER_ID=${orderId}")
        Log.i(TAG, "MAKE_STATE_ACTION received. ORDER_MONEY=${orderMoney}")
        Log.i(TAG, "MAKE_STATE_ACTION received. STATE=${state}")

        log("MAKE_STATE_ACTION received. ORDER_ID=${orderId}")
        log("MAKE_STATE_ACTION received. ORDER_MONEY=${orderMoney}")
        log("MAKE_STATE_ACTION received. STATE=${state}")

        if ("SUCCESS".equals(state, true)) {
            val isSetModel = YNHAPI.getInstance().setGpioMode(YNHAPI.GPIO_4, YNHAPI.GpioMode.OUTPUT)
            Log.i(TAG, "MAKE_STATE_ACTION setGpioMode. isSetModel=${isSetModel}")
            log("MAKE_STATE_ACTION setGpioMode. isSetModel=${isSetModel}")
            if (isSetModel) {
                val gpioState = YNHAPI.getInstance().getGpioState(YNHAPI.GPIO_4)
                Log.i(TAG, "MAKE_STATE_ACTION getGpioState. gpioState=${gpioState.mValue}")
                log("MAKE_STATE_ACTION getGpioState. gpioState=${gpioState.mValue}")
                if (gpioState.mValue != 0) {
                    return
                }
                val setHigh =
                    YNHAPI.getInstance().setGpioState(YNHAPI.GPIO_4, YNHAPI.GpioState.HIGH)
                val timeHigh = System.currentTimeMillis()
                Log.i(TAG, "MAKE_STATE_ACTION setGpioState to HIGH. setHigh=${setHigh}")
                Log.i(TAG, "MAKE_STATE_ACTION setGpioState to HIGH. time=${timeHigh}")
                log("MAKE_STATE_ACTION setGpioState to HIGH. setHigh=${setHigh}")
                log("MAKE_STATE_ACTION setGpioState to HIGH. time=${timeHigh}")
                if (!setHigh) {
                    return
                }
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val setLow =
                        YNHAPI.getInstance().setGpioState(YNHAPI.GPIO_4, YNHAPI.GpioState.LOW)
                    val timeLow = System.currentTimeMillis()
                    Log.i(TAG, "MAKE_STATE_ACTION setGpioState to LOW. setHigh=${setLow}")
                    Log.i(TAG, "MAKE_STATE_ACTION setGpioState to LOW. time=${timeLow}")
                    log("MAKE_STATE_ACTION setGpioState to LOW. setHigh=${setLow}")
                    log("MAKE_STATE_ACTION setGpioState to LOW. time=${timeLow}")
                }, 1000)
            }
        }
    }
}
