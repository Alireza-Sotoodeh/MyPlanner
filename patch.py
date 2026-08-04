with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'r') as f:
    content = f.read()

old_code = """            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            val packageManager = context.packageManager
            val items = stats.filter { it.totalTimeInForeground > 0 }
                .map { stat ->
                    val label = try {
                        val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName.substringAfterLast('.')
                    }
                    AppUsageItem(
                        appName = label,
                        packageName = stat.packageName,
                        durationMinutes = stat.totalTimeInForeground / (1000 * 60)
                    )
                }
                .sortedByDescending { it.durationMinutes }
                .take(6)"""

new_code = """            val statsMap = usageStatsManager.queryAndAggregateUsageStats(
                startTime,
                endTime
            )
            
            val packageManager = context.packageManager
            val items = statsMap.values
                .filter { stat -> 
                    stat.totalTimeInForeground > 0 && 
                    packageManager.getLaunchIntentForPackage(stat.packageName) != null 
                }
                .map { stat ->
                    val label = try {
                        val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName.substringAfterLast('.')
                    }
                    AppUsageItem(
                        appName = label,
                        packageName = stat.packageName,
                        durationMinutes = stat.totalTimeInForeground / (1000 * 60)
                    )
                }
                .filter { it.durationMinutes > 0 }
                .sortedByDescending { it.durationMinutes }
                .take(6)"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'w') as f:
    f.write(content)
