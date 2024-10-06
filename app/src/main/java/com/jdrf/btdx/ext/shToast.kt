package com.jdrf.btdx.ext

import android.content.Context
import android.widget.Toast

fun Context.shToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()