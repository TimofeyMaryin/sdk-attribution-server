package org.example

object Database {
    private val installs = mutableMapOf<String, MutableList<InstallData>>()

    fun saveInstall(installData: InstallData) {
        val installs = installs.getOrPut(installData.bundleId) { mutableListOf() }

        // Проверяем, нет ли уже установки с таким `deviceId`
        if (installs.none { it.deviceId == installData.deviceId }) {
            installs.add(installData)
            println("✅ Сохранена новая установка: $installData")
        } else {
            println("⚠️ Установка уже существует для deviceId=${installData.deviceId}")
        }
    }


    fun getInstallsByBundleId(bundleId: String): List<InstallData> {
        return installs[bundleId] ?: emptyList()
    }

    fun getInstallsByAppName(appName: String): List<InstallData> {
        return installs.values.flatten().filter { it.appName == appName }
    }


    fun getAllApps(): Map<String, List<InstallData>> {
        return installs
    }
}