package com.example.core.utils

import android.icu.util.Calendar
import android.icu.util.ULocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersianCalendarHelper {
    private val persianLocale = ULocale("fa_IR@calendar=persian")

    val monthNames = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    val monthAbbreviations = arrayOf(
        "FAR", "ORD", "KHO", "TIR", "MOR", "SHA",
        "MEH", "ABA", "AZA", "DEY", "BAH", "ESF"
    )

    fun getCurrentPersianYear(): Int = getPersianDateParts(
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    ).first

    fun getCurrentPersianMonth(): Int = getPersianDateParts(
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    ).second

    fun getGregorianDateRange(persianYear: Int, persianMonth: Int): Pair<String, String> {
        val startDate = getGregorianDateString(persianYear, persianMonth, 1)
        val (nextYear, nextMonth) = getOffsetPersianMonth(persianYear, persianMonth, 1)
        val endDate = getGregorianDateString(nextYear, nextMonth, 1)
        return Pair(startDate, endDate)
    }

    fun getOffsetPersianMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        var m = month + delta
        var y = year
        while (m < 1) { m += 12; y -= 1 }
        while (m > 12) { m -= 12; y += 1 }
        return Pair(y, m)
    }
    
    fun getPersianDateString(gregorianDateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(gregorianDateStr) ?: return ""
            return getPersianDateString(date)
        } catch (e: Exception) {
            return ""
        }
    }

    fun getPersianDateString(date: Date): String {
        try {
            val calendar = Calendar.getInstance(persianLocale)
            calendar.time = date
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val monthName = monthNames.getOrNull(month - 1) ?: month.toString()
            
            return "$day $monthName $year"
        } catch (e: Exception) {
            return ""
        }
    }
    
    fun getPersianDateParts(gregorianDateStr: String): Triple<Int, Int, Int> {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(gregorianDateStr) ?: return getCurrentPersianDateParts()
            
            val calendar = Calendar.getInstance(persianLocale)
            calendar.time = date
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            return Triple(year, month, day)
        } catch (e: Exception) {
            return getCurrentPersianDateParts()
        }
    }

    private fun getCurrentPersianDateParts(): Triple<Int, Int, Int> {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(now) ?: return Triple(1400, 1, 1)
            val calendar = Calendar.getInstance(persianLocale)
            calendar.time = date
            return Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            return Triple(1400, 1, 1)
        }
    }

    fun getGregorianDateString(persianYear: Int, persianMonth: Int, persianDay: Int): String {
        try {
            val calendar = Calendar.getInstance(persianLocale)
            calendar.clear()
            calendar.set(Calendar.YEAR, persianYear)
            calendar.set(Calendar.MONTH, persianMonth - 1)
            calendar.set(Calendar.DAY_OF_MONTH, persianDay)
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(calendar.time)
        } catch (e: Exception) {
            return ""
        }
    }
    
    fun getPersianDayOfWeekName(gregorianDateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(gregorianDateStr) ?: return ""
            
            val calendar = Calendar.getInstance(persianLocale)
            calendar.time = date
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            // In Android's Persian calendar via ULocale, Calendar.SUNDAY is 1, Calendar.MONDAY is 2...
            // Let's map it safely.
            return when (dayOfWeek) {
                Calendar.SATURDAY -> "Shanbeh"
                Calendar.SUNDAY -> "Yekshanbeh"
                Calendar.MONDAY -> "Doshanbeh"
                Calendar.TUESDAY -> "Seshanbeh"
                Calendar.WEDNESDAY -> "Chaharshanbeh"
                Calendar.THURSDAY -> "Panjshanbeh"
                Calendar.FRIDAY -> "Jomeh"
                else -> ""
            }
        } catch (e: Exception) {
            return ""
        }
    }
    
    fun getPersianNumericString(gregorianDateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(gregorianDateStr) ?: return ""
            
            val calendar = Calendar.getInstance(persianLocale)
            calendar.time = date
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            return String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
        } catch (e: Exception) {
            return ""
        }
    }
}
