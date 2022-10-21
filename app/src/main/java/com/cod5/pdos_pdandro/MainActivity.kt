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
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private var margin = 10
    private var line_height = 1F
    private var glyph_width = 1F
    private var rows = arrayListOf<ScreenRow>()
    private var cur_col = 0
    private var cur_row = 0
    private var cur_show = true
    private var cur_timer = 0
    private var escape_state = 0
    private var escape_val = 0
    private var escape_args = arrayListOf<Int>()
    private var escape_save = arrayListOf<Int>()
    private var escape_gfx = ScreenChar("")
    private lateinit var proc: Process
    private lateinit var wri: OutputStreamWriter
    private var input_buf = ""

    companion object {
        var normal = Typeface.create("monospace", Typeface.NORMAL)
        var bold = Typeface.create("monospace", Typeface.BOLD)
        var italic = Typeface.create("monospace", Typeface.ITALIC)
        var bolditalic = Typeface.create("monospace", Typeface.BOLD_ITALIC)
    }

    class ScreenChar(str: String) {
        public var txt = str
        public var decoration = ""
        public var color = Color.WHITE
        public var bgcolor = Color.BLACK
        public var typeface = MainActivity.normal

        fun set(model: ScreenChar)
        {
            typeface = model.typeface
            color = model.color
            bgcolor = model.bgcolor
            decoration = model.decoration
        }
        fun reset() {
            color = Color.BLACK
            bgcolor = Color.WHITE
            typeface = MainActivity.normal
            decoration = ""
        }
    }

    class ScreenRow (nbcol: Int) {
        var col = arrayListOf<ScreenChar>()
        init {
            var i = 0;
            while (i < nbcol) {
                col.add(ScreenChar(" "))
                i++
            }
        }
        fun set(model: ScreenChar) {
            var i = 0;
            while (i < col.size) {
                col[i].set(model)
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
        } catch (e: Exception) {
        }
    }
    fun read_data() {
        try {
            var x = proc.exitValue();
            finishAffinity()
            exitProcess(0)
            System.exit(0)
        } catch (e: Exception) {
        }
        try {
            val b = ByteArray(proc.inputStream.available())
            val t = proc.inputStream.read(b)
            if (t < 1) {
                draw_cursor()
                return;
            }
            addtxt(String(b))
        } catch (e: Exception) {
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var s = when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> "\n"
            KeyEvent.KEYCODE_NUMPAD_ENTER -> "\n"
            KeyEvent.KEYCODE_ESCAPE -> "\u001b\u001b"
            KeyEvent.KEYCODE_DEL -> "\b"
            // https://sourceforge.net/p/pdos/gitcode/ci/master/tree/src/pdos.c#l1764
            KeyEvent.KEYCODE_DPAD_UP -> "\u001b[A"
            KeyEvent.KEYCODE_DPAD_DOWN -> "\u001b[B"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "\u001b[C"
            KeyEvent.KEYCODE_DPAD_LEFT -> "\u001b[D"
            KeyEvent.KEYCODE_CTRL_LEFT -> "\u001b[1;5;D"
            KeyEvent.KEYCODE_CTRL_RIGHT -> "\u001b[1;5;C"
            KeyEvent.KEYCODE_INSERT -> "\u001b[2~"
            KeyEvent.KEYCODE_FORWARD_DEL -> "\u001b[3~"
            KeyEvent.KEYCODE_MOVE_HOME -> "\u001b[1~"
            KeyEvent.KEYCODE_MOVE_END -> "\u001b[4~"
            KeyEvent.KEYCODE_PAGE_DOWN -> "\u001b[6~" // FIXME CTRL "\u001b[6;5~"
            KeyEvent.KEYCODE_PAGE_UP -> "\u001b[5~" // FIXME CTRL "\u001b[5;5~"
            else -> "" //keyCode.toString()
        }
        if (s.length == 0) {
                val c = event?.unicodeChar;
                if (c != null) {
                    if (c >= ' '.code) {
                        s = c.toChar().toString()
                    }
                }
        }
        if (s.length > 0) {
                val sb = StringBuilder()
                sb.append(input_buf).append(s)
                input_buf = sb.toString();
                write_buf()
                return true
        } else {
                return super.onKeyDown(keyCode, event)
        }
    }

    fun run_timer() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).postDelayed(
                    { read_data() }, 0.toLong())
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
        paint.color = Color.WHITE
        paint.strokeWidth = 0F
        paint.typeface = normal

        // find the optimal text size for the screen
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
                    paint.typeface = col.typeface
                    canvas.drawText(col.txt, x, y, paint)
                    if (col.decoration.length > 0) {
                        canvas.drawText(col.decoration, x, y, paint)
                    }
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

        if (amount < 0) {
            while (i > amount) {
                rows.removeLast()
                i--
            }
            i = 0
            while (i > amount) {
                rows.add(0, ScreenRow(80));
                rows[0].set(escape_gfx)
                i--
            }
        } else {
            while (i < amount) {
                rows.removeAt(0)
                i++
            }
            i = 0
            while (i < amount) {
                rows.add(ScreenRow(80));
                rows[rows.size-1].set(escape_gfx)
                i++
            }
        }
    }

    fun earse(start_col : Int, start_row :Int, end_col: Int, end_row:Int) {
        var r = start_row;
        while (r <= end_row) {
            var row = rows[r]
            var c = 0
            var ec = 79
            if (r == start_row ) {
                c = start_col
            }
            if (r == end_row) {
                c = end_col
            }
            while (c <= ec) {
                row[c].txt = "" // TODO
                c++
            }
            r++
        }
    }

    fun get_ansi_color(index: Int) : Int {
        return when(index) {
            0 -> Color.BLACK
            1 -> Color.RED
            2 -> Color.GREEN
            3 -> Color.YELLOW
            4 -> Color.BLUE
            5 -> Color.MAGENTA
            6 -> Color.CYAN
            7 -> Color.WHITE
            else -> Color.GRAY
        }
    }

    // Select Graphics Rendition
    // https://en.wikipedia.org/wiki/ANSI_escape_code
    fun sgr() {
        var f = 0
        if (escape_args.size > 1) {
            f = escape_args[0]
        }
        if (f == 0) {
            escape_gfx.reset();
        } else if (f == 1) {
            if (escape_gfx.typeface == italic) {
                escape_gfx.typeface = bolditalic
            } else {
                escape_gfx.typeface = bold
            }
        } else if (f == 2) {
        } else if (f == 3) {
            if (escape_gfx.typeface == bold) {
                escape_gfx.typeface = bolditalic
            } else {
                escape_gfx.typeface = italic
            }
        } else if (f == 4) {
            escape_gfx.decoration = "_"
        } else if (f >= 30 && f <= 37) {
            escape_gfx.color = get_ansi_color(f - 30)
        } else if (f >= 40 && f <= 47) {
            escape_gfx.bgcolor = get_ansi_color(f - 40)
        } else if (f >= 90 && f <= 97) {
            escape_gfx.color = get_ansi_color(f - 90) // FIXME brighter
        } else if (f >= 100 && f <= 107) {
            escape_gfx.bgcolor = get_ansi_color(f - 100) // FIXME brighter
        }
    }

    fun draw_cursor() {
        if (!cur_show) {
            return;
        }
        cur_timer++;
        if (cur_timer == 25) {
            var bg = rows[cur_row][cur_col].bgcolor
            var fg = rows[cur_row][cur_col].color
            rows[cur_row][cur_col].bgcolor = fg
            rows[cur_row][cur_col].color = bg
            draw(false)
            rows[cur_row][cur_col].bgcolor = bg
            rows[cur_row][cur_col].color = fg
        } else if (cur_timer == 50) {
            draw(false)
            cur_timer = 0
        }
    }

    fun ansi_private(c : Int) {
        if (c == 'h'.code) {    // show caret
            if (escape_args.size > 1) {
                val cmd = escape_args[1]
                if (cmd == 25) {
                    cur_show = true
                }
            }
        } else if (c == 'l'.code) { // hide caret
            if (escape_args.size > 1) {
                val cmd = escape_args[1]
                if (cmd == 25) {
                    cur_show = false
                }
            }
        }
    }

    fun ansi_term(c: Int) {
        // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
        // https://sourceforge.net/p/pdos/gitcode/ci/master/tree/src/pdos.c#l5493
        var n = 1
        if (escape_args.size > 0) {
            if (escape_args[0] == -'?'.code) {
                ansi_private(c)
                return
            }
            n = escape_args[0]
        }
        if (c == 'A'.code) {
            cur_row -= n
        } else if (c == 'B'.code) {
            cur_row += n
        } else if (c == 'C'.code) {
            cur_col += n
        } else if (c == 'D'.code) {
            cur_col -= n
        } else if (c == 'E'.code) {
            cur_row += n
            cur_col = 0;
        } else if (c == 'F'.code) {
            cur_row -= n
            cur_col = 0;
        } else if (c == 'G'.code || c == 'f'.code) {
            if (escape_args.size > 0) {
                cur_col = escape_args[0] - 1
            }
        } else if (c == 'H'.code) { // set cursor position
            if (escape_args.size > 0) {
                cur_row = escape_args[0] - 1
                if (escape_args.size > 1) {
                    cur_col = escape_args[1] - 1
                } else {
                    cur_col = 0
                }
            }
        } else if (c == 'J'.code) { // clear screen
            if (escape_args.size == 0) {
                n = 0
            }
            if (n == 0) {
                earse(cur_col, cur_row, 79, 24)
            } else if (n == 1) {
                earse(0, 0, cur_col, cur_row)
            } else if (n == 2) {
                earse(0, 0, 79, 24)
            } else if (n == 3) {
            }
        } else if (c == 'K'.code) { // clear line
            if (escape_args.size == 0) {
               n = 0
            }
            if (n == 0) {
                earse(cur_col, cur_row, 79, cur_row)
            } else if (n == 1) {
                earse(0, cur_row, cur_col, cur_row)
            } else if (n == 2) {
                earse(0, cur_row, 79, cur_row)
            }
        } else if (c == 'S'.code) {
            scroll(n)
        } else if (c == 'T'.code) {
            scroll(-n)
        } else if (c == 's'.code) {
            escape_save.add(cur_row)
            escape_save.add(cur_col)
        } else if (c == 'u'.code) {
            if (escape_save.size > 1) {
                cur_col = escape_save.removeLast()
                cur_row = escape_save.removeLast()
            }
        } else if (c == 'm'.code) {
            sgr();
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
            if (escape_state == 3) {
                escape_state = 0;
            }
            if (escape_state == 1) {
                // https://notes.burke.libbey.me/ansi-escape-codes/
                if (c == '['.code) {
                    escape_state = 2
                    escape_val = -1
                    escape_args.clear()
                } else {
                    escape_state = 0
                    i-- // rescan the current character
                }
            } else if (escape_state == 2) {
                if (c >= '0'.code && c <= '9'.code) {
                    if (escape_val == -1) {
                        escape_val = 0
                    }
                    escape_val *= 10
                    escape_val += c - '0'.code
                } else if (c == ';'.code) {
                    escape_args.add(escape_val)
                    escape_val = -1
                } else if (c == '='.code) {
                    escape_args.add(-'='.code)
                    escape_val = -1
                } else if (c == '?'.code) {
                    escape_args.add(-'?'.code)
                    escape_val = -1
                } else {
                    if (escape_val >= 0) {
                        escape_args.add(escape_val)
                    }
                    ansi_term(c)
                    all = true
                    escape_state = 3
                }
            } else if (c == 10) { // \n
                cur_row++;
                cur_col = 0;
            } else if (c == 13) { // \r
            } else if (c == 8) {
                if (cur_col > 0) {
                    cur_col--;
                    col = row[cur_col]
                    col.txt = " "
                }
            } else if (c == 0x1B) { // ESC
                escape_state = 1;
            } else {
                val sb = StringBuilder()
                sb.appendCodePoint(c)
                col.set(escape_gfx)
                col.txt = sb.toString()
                cur_col++;
            }
            if (cur_col < 0) {
                cur_col = 0;
            }
            if (cur_row < 0) {
                cur_row = 0;
            }
            if (cur_col >= 80) {
                if (escape_state == 0) {
                    cur_row++;
                    cur_col = 0;
                } else {
                    cur_col = 79;
                }
            }
            if (cur_row >= 25) {
                if (escape_state == 0) {
                    scroll(cur_row - 24);
                    last_row = -1;
                }
                cur_row = 24;
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
        } catch (e:Exception) {
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
            return ""
        } catch(e: IOException) {
            e.printStackTrace()
            return "ERROR"
        }
    }
}
