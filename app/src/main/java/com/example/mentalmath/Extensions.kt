package com.example.mentalmath

import android.content.Context

fun Context.getThemeColor(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    return try {
        ta.getColor(0, 0)
    } finally {
        ta.recycle()
    }
}
