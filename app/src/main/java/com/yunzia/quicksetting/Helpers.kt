package com.yunzia.quicksetting

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import kotlin.also


object Helpers {

    fun isRoot() = getRootPermission() == 0


    fun String.sh(): String{
        val result = rootShell(this)
        Log.d("ggc","sh: $this $result")
        return rootShell(this)
    }

//    fun String.sh(): Int{
//        return rootShell(this) == "true"
//    }

    fun getRootPermission(): Int {
        var process: Process? = null
        val exitCode = -1
        try {
            process = Runtime.getRuntime().exec("su -c true")
            return process.waitFor()
        } catch (e: IOException) {
            return exitCode
        } catch (e: InterruptedException) {
            return exitCode
        } finally {
            process?.destroy()
        }
    }

    fun rootShell(cmd: String): String {
        val output = StringBuilder()
        var process: Process? = null

        try {
            // 启动 su 进程
            process = Runtime.getRuntime().exec("su")

            // 写入命令并退出
            process.outputStream.use { outputStream ->
                DataOutputStream(outputStream).use { dataStream ->
                    dataStream.writeBytes("$cmd\n")
                    dataStream.flush()
                    dataStream.writeBytes("exit\n")
                    dataStream.flush()
                }
            }

            // 读取命令输出
            process.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.appendLine(line)
                    }
                }
            }

            // 等待进程完成
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                return "Error: Command exited with non-zero code $exitCode"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error: IOException occurred - ${e.message}"
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return "Error: InterruptedException occurred - ${e.message}"
        } finally {
            // 确保销毁进程
            process?.destroy()
        }

        // 返回命令执行结果
        return output.toString().trim()
    }

}