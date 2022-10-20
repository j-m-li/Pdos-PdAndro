/*
 * The authors disclam copyright to this source code
 */
package com.cod5.pdos_pdandro

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.system.Os
import android.system.Os.*
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.cod5.pdos_pdandro.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.Base64.*

class ScreenChar(str: String) {
    public var txt = str
}

class ScreenRow () {
    public var col = arrayListOf<ScreenChar>()
    init {
        var i = 0;
        while (i < 80) {
            col.add(ScreenChar(" "))
            i++
        }
    }
    operator fun get(j: Int): ScreenChar {
        return col[j]
    }
}
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private var margin = 10
    private var line_height = 1F
    private var glyph_width = 1F
    private var rows = arrayListOf<ScreenRow>()
    private var cur_col = 0
    private var cur_row = 0

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
    fun read_data() {
        try {
            val b = ByteArray(proc.inputStream.available())
            val t = proc.inputStream.read(b)
            if (t < 1) {
                return;
            }
            addtxt(String(b))
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
            System.exit(0)
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

    fun run_timer() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).postDelayed(
                    {
                        //write_buf()
                        read_data()
                    }, 0.toLong())

            }
        }, 1000, 20)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        run()
        run_timer()
        init_display()
    }

    fun init_display()
    {
        // Getting the current window dimensions

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        var dw = dm.widthPixels - 2 * margin
        var dh = dm.heightPixels - 2 * margin

        // Creating a bitmap with fetched dimensions
        bitmap = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888)

        // Storing the canvas on the bitmap
        canvas = Canvas(bitmap)

        // Initializing Paint to determine
        // stoke attributes like color and size
        paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 0F
        paint.typeface = Typeface.create("monospace", Typeface.NORMAL)

        var h = 0F
        paint.textSize = 8F
        var add = 1.0F
        while (h < dh) {
            h = (paint.fontMetrics.descent - paint.fontMetrics.ascent) * 25F
            if (h < dh) {
                paint.textSize += add
            } else {
                paint.textSize -= add
                if (add == 1.0F) {
                    add = 0.1F
                } else {
                    h = dh.toFloat() + 1.0F
                }
            }
        }

        // Setting the bitmap on ImageView
        binding.imageView?.setImageBitmap(bitmap)
        line_height = paint.fontMetrics.descent - paint.fontMetrics.ascent
        glyph_width = paint.measureText("_");

        var i = 0
        while (i < 25) {
            rows.add(ScreenRow());
            i++
        }
    }

    fun draw() {
        var i = 0
        var y = margin.toFloat() - paint.fontMetrics.descent
        paint.color = Color.WHITE
        canvas.drawRect(margin.toFloat(), margin.toFloat(),
            (margin + bitmap.width).toFloat(),
            (margin + bitmap.height).toFloat(), paint)
        paint.color = Color.BLACK
        while (i < 25) {
            var j = 0;
            val r = rows[i]
            var x = margin.toFloat()
            y += line_height;
            while (j < 80) {
                val col = r[j]
                canvas.drawText(col.txt, x, y, paint)
                x += glyph_width
                j++
            }
            i++
        }
        binding.imageView?.invalidate()
    }

    fun scroll(amount: Int) {
        var i = 0;
        while (i < amount) {
            rows.removeAt(0)
            i++
        }
        i = 0
        while (i < amount) {
            rows.add(ScreenRow());
            i++
        }
    }

    fun addtxt(txt: String) {
        var i = 0
        val nbcp = txt.codePointCount(0, txt.length)
        var row = rows[cur_row]
        var col = row[cur_col]
        var last_row = cur_row
        var last_col = cur_col
        while (i < nbcp) {
            var c = txt.codePointAt(i)
            if (c == 10) {
                cur_row++;
                cur_col = 0;
            } else if (c == 13) {
                //el.cur_col = 0;
            } else if (c == 8) {
                if (cur_col > 0) {
                    cur_col--;
                }
            } else if (c == 0x1B) {
                // https://notes.burke.libbey.me/ansi-escape-codes/
                /* TODO */
            } else {
                if (c == 32) {
                    c = 0xA0;
                }
                val sb = StringBuilder()
                sb.appendCodePoint(c)
                col.txt = sb.toString()
                cur_col++;
                //vt100(el, c, col);
            }
            if (cur_col >= 80) {
                cur_col = 0;
                cur_row++;
            }
            if (cur_row >= 25) {
                scroll(cur_row - 24);
                cur_row = 24;
                last_row = -1;
            }
            if (last_row != cur_row) {
                row = rows[cur_row];
                col = row[cur_col];
                last_row = cur_row;
                last_col = cur_col;
            } else if (last_col != cur_col) {
                col = row[cur_col];
                last_col = cur_col;
            }

            i++;
        }
        draw();
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
            wri = proc.outputStream.writer()

            //proc.waitFor(1, TimeUnit.SECONDS)
            return ""
        } catch(e: IOException) {
            e.printStackTrace()
            return "ERROR"
        }
    }
}
