package com.iyxan23.ecjexample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

class ECJViewModel : ViewModel() {

    private val ecjOutputMutable = MutableLiveData<String>().apply { value = "== START ==" }

    val ecjOutput: LiveData<String> = ecjOutputMutable

    private lateinit var ecjPath: String
    private lateinit var androidPath: String

    private fun l(text: String) {
        ecjOutputMutable.value += "\n$text"
    }

    fun checkAndExtractJars(context: Context) {
        val storage: File = context.filesDir

        androidPath = File("${storage.absolutePath}/android.jar").absolutePath
        ecjPath = File("${storage.absolutePath}/ecj.jar").absolutePath

        if (File("${storage.absolutePath}/android.jar").exists()) return

        viewModelScope.launch { extractAndroidJar(context) }
    }

    private suspend fun extractAndroidJar(context: Context) {
        withContext(Dispatchers.IO) {
            runCatching {
                val storage: File = context.filesDir

                for (entry in mapOf("android.jar" to R.raw.android, "ecj.jar" to R.raw.ecj)) {
                    val dexWriter: OutputStream
                    val bufSize = 8 * 1024

                    val bis = BufferedInputStream(context.resources.openRawResource(entry.value))
                    dexWriter = BufferedOutputStream(FileOutputStream("${storage.absolutePath}/${entry.key}"))
                    val buf = ByteArray(bufSize)
                    var len: Int

                    while (bis.read(buf, 0, bufSize).also { len = it } > 0) { dexWriter.write(buf, 0, len) }

                    dexWriter.close()
                    bis.close()
                }

            }.onFailure {
                it.printStackTrace()
                l("Error occured whilst trying to extract ecj.jar and android.jar: $it")
            }
        }
    }

    fun runCommand(command: String) {
        val process = Runtime.getRuntime().exec(
            "dalvikvm -Xmx256m -Xcompiler-option --compiler-filter=speed -cp $ecjPath org.eclipse.jdt.internal.compiler.batch.Main -proc:none -7 -cp $androidPath $command"
        )

        viewModelScope.launch {
            runCatching {
                val buffer = ByteArray(1024)
                while (process.inputStream.read(buffer) != -1) {
                    l("[ECJ] ${String(buffer)}")
                }
            }.onFailure {
                it.printStackTrace()
                l("An error occurred whilst trying to read ecj's output: $it")
            }
        }

        viewModelScope.launch {
            runCatching {
                val buffer = ByteArray(1024)
                while (process.errorStream.read(buffer) != -1) {
                    l("[ECJ ERR] ${String(buffer)}")
                }
            }.onFailure {
                it.printStackTrace()
                l("An error occurred whilst trying to read ecj's error stream: $it")
            }
        }
    }
}