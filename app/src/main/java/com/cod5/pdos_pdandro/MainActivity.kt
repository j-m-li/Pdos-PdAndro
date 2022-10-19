/*
 * The authors disclam copyright to this source code
 */
package com.cod5.pdos_pdandro

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.system.Os
import android.system.Os.*
import android.system.OsConstants
import android.view.InputEvent
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.cod5.pdos_pdandro.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var proc: Process
    lateinit var wri: OutputStreamWriter
    var input_buf = ""

    fun write_buf() {
        try {
            if (input_buf.length > 0) {
                wri.append(input_buf)
                input_buf = ""
                wri.flush()
            }
        } catch (e: Exception)
        {

        }
    }
    class MyJavascriptInterface(private val self: MainActivity) {

        @JavascriptInterface
        fun onInput(mes: String?): Boolean  {
            try {
                self.wri.append(mes)
                self.wri.flush()
            } catch (e: Exception) {
            }
            return true
        }
        @JavascriptInterface
        fun getText(): String  {
            /*
            self.binding.root.hasFocus()?.let {
                val imm = self.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(self.binding.root.windowToken, 0)
            }*/

            try {
                val b = ByteArray(self.proc.inputStream.available())
                val t = self.proc.inputStream.read(b)
                if (t < 1) {
                    return "";
                }
                return String(b);
            } catch (e: Exception) {
                self.finish();
                System.exit(0);
            }
            return ""
        }
        @JavascriptInterface
        fun onKey(mes: Int): Boolean  {
            if (mes == 13) {
                // self.wri.append("\n")
            } else {
                //self.wri.append(mes.toString())
                return false
            }
            self.wri.flush()
            return true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val c = event?.unicodeChar;
        if (c != null) {
            val s =  when (c) {
                10 -> "\n"
                13 -> ""
                8 -> "\b"
                0 -> ""
                else -> event.unicodeChar.toChar().toString()
                //else -> c.toString()
            }

            if (s.length > 0) {
                val sb = StringBuilder()
                sb.append(input_buf).append(s)
                input_buf = sb.toString();
                write_buf()
            }


        }

        return super.onKeyDown(keyCode, event)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.gui.getSettings().setJavaScriptEnabled(true);
        binding.gui.addJavascriptInterface(MyJavascriptInterface(this), "Android")
        binding.gui.loadData("INIT","text/html", "UTF-8")

        run()
       // binding.gui.requestFocus()

       //  val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
       // imm.showSoftInput(binding.root,1)
       // imm.hideSoftInputFromWindow(binding.gui.getWindowToken(), 0);
    }

    fun run() {
        val s = applicationContext.applicationInfo.nativeLibraryDir
        val p = applicationContext.applicationInfo.dataDir

        try {
            val bin = "$p/bin"
            Os.symlink(s, bin);
        } catch (e:Exception)
        {

        }
        val c = "$s/libpdos.so"
        val dir = File(p);
        c.runCommand(dir)
    }

    fun String.runCommand(workingDir: File): String? {
        try {
            val own = applicationContext.applicationInfo.nativeLibraryDir
            proc = Runtime.getRuntime().exec(arrayOf<String> (this, "$own/lib%s.so"), Os.environ(), workingDir)

            binding.gui.loadUrl("file:///android_res/raw/index.html");
            wri = proc.outputStream.writer()

            //proc.waitFor(1, TimeUnit.SECONDS)
            return ""
        } catch(e: IOException) {
            e.printStackTrace()
            return "ERROR"
        }
    }
}
