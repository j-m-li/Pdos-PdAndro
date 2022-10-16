package com.cod5.pdos_pdandro

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.system.Os
import android.system.Os.*
import android.system.OsConstants
import android.view.InputEvent
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.cod5.pdos_pdandro.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var proc: Process
    public lateinit var wri: OutputStreamWriter
    fun timer() {
        object : CountDownTimer(50, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {

                    //val t = proc.inputStream.bufferedReader().readText()
                    val b = ByteArray(proc.inputStream.available())
                    val t = proc.inputStream.read(b)

                    val cmd = "javascript:addtxt("
                    val s = String(b)
                    val cm = "$cmd '$s');"
                    if (t > 0) {
                        binding.gui.loadUrl(cm)
                    }
                    //wri.append("jml")
                    //wri.flush()

                } catch (e: Exception) {
                    binding.gui.loadData("Error reading stream", "text/html", "UTF-8")
                }
            }
            override fun onFinish() {
                start()
            }
        }.start()
    }


    class MyJavascriptInterface(private val self: MainActivity) {

        @JavascriptInterface
        fun onInput(mes: String?): Boolean  {
            self.wri.append(mes)
            //self.wri.append("----")
            self.wri.flush()
            return true
        }

        @JavascriptInterface
        fun onKey(mes: Int): Boolean  {
            if (mes == 13) {
                self.wri.append("\n")
            }
            //self.wri.append("----")
            self.wri.flush()
            return true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //supportActionBar?.hide()
       binding.gui.requestFocus()
        run()
        timer();

        binding.gui.getSettings().setJavaScriptEnabled(true);

        binding.gui.addJavascriptInterface(MyJavascriptInterface(this), "Android")

        // val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
       // imm.showSoftInput(binding.root,1)
        //imm.hideSoftInputFromWindow(binding.gui.getWindowToken(), 0);
    }

    fun run() {
        //val opath = Os.getenv("PATH");
        //Os.setenv("PATH", path, true)
        val s = applicationContext.applicationInfo.nativeLibraryDir
        val p = applicationContext.applicationInfo.dataDir

        val c = "$s/libpdos.so"
        val dir = File(p);
        c.runCommand(dir)
    }

    fun String.runCommand(workingDir: File): String? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                proc = ProcessBuilder("sh", "-c", this) //*parts.toTypedArray())
                    .directory(workingDir)
                    .redirectErrorStream(true)
                    //.redirectOutput(ProcessBuilder.Redirect.PIPE)
                    //.redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()
                binding.gui.loadUrl("file:///android_res/raw/index.html");
                wri = proc.outputStream.writer()
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            //proc.waitFor(1, TimeUnit.SECONDS)
            return ""
        } catch(e: IOException) {
            e.printStackTrace()
            return "ERROR"
        }
    }
}
