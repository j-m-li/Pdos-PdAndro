/*
 * The authors disclam copyright to this source code
 */
package com.cod5.pdos_pdandro

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.system.Os
import android.util.DisplayMetrics
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.cod5.pdos_pdandro.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

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
    private var escape_state = 0
    private var escape_sb = StringBuilder()
    private var escape_args = arrayListOf<Int>()

    private lateinit var proc: Process
    lateinit var wri: OutputStreamWriter
    var input_buf = ""

    class ScreenChar(str: String) {
        public var txt = str
        public var color = Color.BLACK
        public var bgcolor = Color.WHITE
    }

    class ScreenRow (nbcol: Int) {
        public var col = arrayListOf<ScreenChar>()
        init {
            var i = 0;
            while (i < nbcol) {
                col.add(ScreenChar(" "))
                i++
            }
        }
        operator fun get(j: Int): ScreenChar {
            return col[j]
        }
    }

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
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        var dw = dm.widthPixels - 2 * margin
        var dh = dm.heightPixels - 2 * margin

        bitmap = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)

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
                    add = 0.5F
                } else if (add == 0.5F) {
                    add = 0.125F
                } else {
                    h = dh.toFloat() + 1.0F
                }
            }
        }

        binding.imageView?.setImageBitmap(bitmap)
        line_height = paint.fontMetrics.descent - paint.fontMetrics.ascent
        glyph_width = paint.measureText("_");

        var i = 0
        while (i < 25) {
            rows.add(ScreenRow(80));
            i++
        }
    }

    fun draw(draw_all: Boolean) {
        var i = 0
        var y = margin.toFloat() - paint.fontMetrics.ascent
        var ascent = paint.fontMetrics.ascent
        /*paint.color = Color.YELLOW
        canvas.drawRect(0F, 0F,
            (bitmap.width).toFloat(),
            (bitmap.height).toFloat(), paint)*/

        paint.color = Color.BLACK
        while (i < 25) {
            var j = 0;
            val r = rows[i]
            var x = margin.toFloat()
            if (draw_all || i == cur_row) {
                while (j < 80) {
                    val col = r[j]
                    paint.color = col.bgcolor
                    canvas.drawRect(RectF(x, y + ascent, x + glyph_width, ceil(y + ascent + line_height)), paint)
                    x += glyph_width
                    j++
                }
            }
            y += line_height;
            i++
        }
        i = 0
        y = margin.toFloat() - paint.fontMetrics.ascent
        while (i < 25) {
            var j = 0
            val r = rows[i]
            var x = margin.toFloat()
            if (draw_all || i == cur_row) {
                while (j < 80) {
                    val col = r[j]
                    paint.color = col.color
                    canvas.drawText(col.txt, x, y, paint)
                    x += glyph_width
                    j++
                }
            }
            y += line_height;
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
            rows.add(ScreenRow(80));
            i++
        }
    }

    fun vt100(c: Int) {
        // https://notes.burke.libbey.me/ansi-escape-codes/
        if (c == 'A'.code) {
            if (escape_args.size > 0) {
                cur_row -= escape_args[0]
            }
        } else if (c == 'B'.code) {
            if (escape_args.size > 0) {
                cur_row += escape_args[0]
            }
        } else if (c == 'C'.code) {
            if (escape_args.size > 0) {
                cur_col += escape_args[0]
            }
        } else if (c == 'D'.code) {
            if (escape_args.size > 0) {
                cur_col -= escape_args[0]
            }
        } else if (c == 'E'.code) {
            if (escape_args.size > 0) {
                cur_row += escape_args[0]
                cur_col = 0;
            }
        } else if (c == 'F'.code) {
            if (escape_args.size > 0) {
                cur_row -= escape_args[0]
                cur_col = 0;
            }
        } else if (c == 'G'.code || c == 'f'.code) {
            if (escape_args.size > 0) {
                cur_col = escape_args[0] - 1
            }
        } else if (c == 'H'.code) {
            if (escape_args.size > 1) {
                cur_row = escape_args[0] - 1
                cur_col = escape_args[1] - 1
            }
        } else if (c == 'J'.code) {
        } else if (c == 'K'.code) {
        } else if (c == 'S'.code) {
        } else if (c == 'T'.code) {
        } else if (c == 's'.code) {
        } else if (c == 'u'.code) {
        } else if (c == 'm'.code) {

        }
    }

    fun addtxt(txt: String) {
        var i = 0
        var all = false
        val nbcp = txt.codePointCount(0, txt.length)
        var row = rows[cur_row]
        var col = row[cur_col]
        var last_row = cur_row
        var last_col = cur_col
        while (i < nbcp) {
            var c = txt.codePointAt(i)
            if (escape_state == 1) {
                // https://notes.burke.libbey.me/ansi-escape-codes/
                if (c == '['.code) {
                    escape_state = 2;
                    escape_sb.clear()
                    escape_args.clear()
                } else {
                    escape_state = 0;
                    i-- // rescan the current character
                }
            } else if (escape_state == 2) {
                if (c >= '0'.code && c <= '9'.code) {
                    escape_sb.appendCodePoint(c)
                } else if (c == ';'.code) {
                    try {
                        escape_args.add(escape_sb.toString().toInt())
                    } catch(e: Exception) {
                        escape_args.add(0)
                    }
                    escape_sb.clear()
                } else {
                    try {
                        escape_args.add(escape_sb.toString().toInt())
                        escape_sb.clear()
                    } catch(e: Exception) {
                    }
                    vt100(c)
                    all = true
                    escape_state = 0
                }
            } else if (c == 10) { // \n
                cur_row++;
                cur_col = 0;
            } else if (c == 13) { // \r
                //el.cur_col = 0;
            } else if (c == 8) {
                if (cur_col > 0) {
                    cur_col--;
                    col.txt = ""
                }
            } else if (c == 0x1B) { // ESC
                escape_state = 1;
            } else {
                val sb = StringBuilder()
                sb.appendCodePoint(c)
                col.txt = sb.toString()
                cur_col++;
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
                all = true
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
        draw(all);
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
