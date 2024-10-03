package com.jdrf.btdx.ext

import android.util.Log

fun eLog(e: Throwable) =
    Log.e("BTDX", e.message, e)
