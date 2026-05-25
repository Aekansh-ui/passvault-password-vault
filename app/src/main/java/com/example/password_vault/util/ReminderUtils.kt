package com.example.password_vault.util

import java.util.Calendar

const val UNIT_DAYS   = "DAYS"
const val UNIT_WEEKS  = "WEEKS"
const val UNIT_MONTHS = "MONTHS"

private const val FIVE_DAYS_MS = 5L * 24 * 60 * 60 * 1000

fun computeNextChangeMs(lastChangedAt: Long, unit: String, value: Int): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = lastChangedAt }
    when (unit) {
        UNIT_DAYS   -> cal.add(Calendar.DAY_OF_YEAR, value)
        UNIT_WEEKS  -> cal.add(Calendar.WEEK_OF_YEAR, value)
        UNIT_MONTHS -> cal.add(Calendar.MONTH, value)
    }
    return cal.timeInMillis
}

fun isDueSoon(lastChangedAt: Long, unit: String, value: Int): Boolean {
    val nextChange = computeNextChangeMs(lastChangedAt, unit, value)
    return System.currentTimeMillis() >= nextChange - FIVE_DAYS_MS
}

fun daysUntilChange(lastChangedAt: Long, unit: String, value: Int): Long {
    val nextChange = computeNextChangeMs(lastChangedAt, unit, value)
    return ((nextChange - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)
}
