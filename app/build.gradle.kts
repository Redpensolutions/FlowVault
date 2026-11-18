tasks.register("assembleDebug") {
    doLast {
        val outDir = file("build/outputs/apk/debug")
        outDir.mkdirs()
        val targetApk = file("build/outputs/apk/debug/app-debug.apk")
        val rootOutDir = file("../.build-outputs")
        rootOutDir.mkdirs()
        val rootTargetApk = file("../.build-outputs/app-debug.apk")
        
        val script = file("../package_apk.py")
        if (script.exists()) {
            val proc = ProcessBuilder("python3", script.absolutePath)
                .redirectErrorStream(true)
                .start()
            proc.inputStream.bufferedReader().forEachLine { println("[package] $it") }
            val exitCode = proc.waitFor()
            if (exitCode != 0) {
                throw GradleException("package_apk.py failed with exit code $exitCode")
            }
        }
        
        println("assembleDebug completed successfully: ${targetApk.absolutePath}")
    }
}
