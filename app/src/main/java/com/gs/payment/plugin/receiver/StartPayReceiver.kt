package com.gs.payment.plugin.receiver

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.innohi.YNHAPI

class StartPayReceiver : BaseBroadReceiver() {
    companion object {
        private const val TAG = "PaymentPlugin.StartPayReceiver"

        const val ACTION = "com.coffeeji.payment.plugin.PAY_ACTON"

        const val RESULT_ACTION = "com.coffeeji.payment.plugin.PAY_STATE_ACTION"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable: Runnable = Runnable {
        context?.let {
            Log.i(TAG, "PAY_ACTON pay timeout.")
            log("PAY_ACTON pay timeout.")
            sendResult(it, false, "pay timeout", "")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.i(TAG, "Received intent action: ${intent.action}")
        log("Received intent action: ${intent.action}")
        if (intent.action != ACTION) return

        val orderId = intent.getStringExtra("ORDER_ID")
        val orderMoney = intent.getStringExtra("ORDER_MONEY")
        val productId = intent.getStringExtra("PRODUCT_ID")
        val productName = intent.getStringExtra("PRODUCT_NAME")
        val scanCode = intent.getStringExtra("SCAN_CODE")
        Log.i(TAG, "PAY_ACTON received. ORDER_ID=${orderId}")
        Log.i(TAG, "PAY_ACTON received. ORDER_MONEY=${orderMoney}")
        Log.i(TAG, "PAY_ACTON received. PRODUCT_ID=${productId}")
        Log.i(TAG, "PAY_ACTON received. PRODUCT_NAME=${productName}")
        Log.i(TAG, "PAY_ACTON received. SCAN_CODE=${scanCode}")

        log("PAY_ACTON received. ORDER_ID=${orderId}")
        log("PAY_ACTON received. ORDER_MONEY=${orderMoney}")
        log("PAY_ACTON received. PRODUCT_ID=${productId}")
        log("PAY_ACTON received. PRODUCT_NAME=${productName}")
        log("PAY_ACTON received. SCAN_CODE=${scanCode}")

        if (orderId.isNullOrBlank()) {
            sendResult(context, false, "invalid orderId", "")
            return
        }

        val gpioState = YNHAPI.getInstance().getGpioState(YNHAPI.GPIO_1)
        Log.i(TAG, "PAY_ACTON getGpioState. gpioState=${gpioState.mValue}")
        log("PAY_ACTON getGpioState. gpioState=${gpioState.mValue}")
        if (gpioState.mValue != 0) {
            sendResult(context, false, "the current initial level is not low！", "")
            return
        }
        val isSetModel = YNHAPI.getInstance().setGpioMode(YNHAPI.GPIO_1, YNHAPI.GpioMode.INPUT)
        Log.i(TAG, "PAY_ACTON setGpioMode. isSetModel=${isSetModel}")
        log("PAY_ACTON setGpioMode. isSetModel=${isSetModel}")
        if (!isSetModel) {
            sendResult(context, false, "setting gpio_1 input failed！", "")
            return
        }
        var isStart = false
        var startTimestamp = 0L
        YNHAPI.getInstance().listenGpio(YNHAPI.GPIO_1, object : YNHAPI.GpioListenerCallback {
            override fun onChanged(index: Int, oldValue: Int, newValue: Int) {
                if (oldValue == 0 && newValue == 1) {
                    // 开始
                    isStart = true
                    // 计时
                    startTimestamp = System.currentTimeMillis()
                    Log.i(
                        TAG,
                        "PAY_ACTON listenGpio. oldValue=${oldValue},newValue=${newValue},startTimestamp=${startTimestamp}"
                    )
                    log("PAY_ACTON listenGpio. oldValue=${oldValue},newValue=${newValue},startTimestamp=${startTimestamp}")
                    return
                }
                if (!isStart) {
                    return
                }
                if (oldValue == 1 && newValue == 0) {
                    val currentTimestamp = System.currentTimeMillis()
                    Log.i(
                        TAG,
                        "PAY_ACTON listenGpio. oldValue=${oldValue},newValue=${newValue},startTimestamp=${startTimestamp},startTimestamp=${currentTimestamp}"
                    )
                    log("PAY_ACTON listenGpio. oldValue=${oldValue},newValue=${newValue},startTimestamp=${startTimestamp},startTimestamp=${currentTimestamp}")
                    if (currentTimestamp - startTimestamp > 800) {
                        Log.i(TAG, "PAY_ACTON listenGpio. The payment is successful, and the successful broadcast is sent")
                        log("PAY_ACTON listenGpio. The payment is successful, and the successful broadcast is sent")
                        sendResult(context, true, "The payment was successful！", orderMoney!!)
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }
            }
        })

        handler.postDelayed(timeoutRunnable, 50 * 1000)
    }

    private fun sendResult(
        ctx: Context,
        success: Boolean,
        message: String,
        money: String
    ) {
        val out = Intent(RESULT_ACTION)
            .putExtra("STATE", if (success) "success" else "fail")
            .putExtra("MESSAGE", message)
            .putExtra("MONEY", money)
        Log.i(
            TAG,
            "Sending PAY_STATE_ACTION: status=${if (success) "success" else "fail"}, message=$message, money=$money"
        )
        log("Sending PAY_STATE_ACTION: status=${if (success) "success" else "fail"}, message=$message, money=$money")
        ctx.sendBroadcast(out)
    }
}
