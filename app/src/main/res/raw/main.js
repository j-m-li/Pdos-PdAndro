// The authors disclam copyright to this source code

var theApp = Array();

function _onLoad()
{
    theApp.el = document.getElementById('t');
    initdoc(theApp.el);
    fit(theApp.el);
    window.setTimeout(_onTimer, 100);
}

function escapeHTML(str) {
  const p = document.createElement("div");
  p.appendChild(document.createTextNode(str));
  return p.innerHTML;
}

function fit(el)
{
    var z = 7.0;
    while ((el.clientHeight < window.innerHeight - 10) /* &&
        (el.clientWidth < window.innerWidth)*/)
    {
        z = z + 0.5;
        el.style.fontSize = z + "pt";
        if (z > 50.0) {
            return;
        }
    }
    z = z - .5;
    el.style.fontSize = z + "pt";
}

function scroll(el, nbl)
{
    var i = 0;
    var ch = el.children;
    while (i < nbl) {
        el.removeChild(el.firstChild);
        ch = el.children;
        i++;
    }

    while (el.children.length < 25) {
            el.appendChild(theApp.ref_row.cloneNode(true));
    }

}

function initdoc(el)
{
    var i = 0;
    var di = document.createElement("div");
    var spa = document.createElement("span");
    spa.innerHTML = "&nbsp";
    spa.innerText = spa.innerText;
    while (i < 80) {
        di.appendChild(spa.cloneNode(true))
        i++;
    }
    while (el.children.length > 0) {
        el.removeChild(el.firstChild);
    }
    i = 0;
    while (i < 25) {

        el.appendChild(di.cloneNode(true))
        i++;
    }
    theApp.ref_row = di;
    theApp.cur_col = 0;
    theApp.cur_row = 0;

}

function addtxt(txt)
{

    const el = theApp.el;
    var i = 0;
    var row = el.children[theApp.cur_row];
    var col = row.children[theApp.cur_col];
    var last_row = theApp.cur_row;
    var last_col = theApp.cur_col;
    var c = txt.codePointAt(i);
    while (c !== undefined) {
        if (c == 10) {
            theApp.cur_row++;
            theApp.cur_col = 0;
        } else if (c == 13) {
            //el.cur_col = 0;
        } else if (c == 8) {
            if (theApp.cur_col > 0) {
                theApp.cur_col--;
            }
        } else if (c == 0x1B) {
        // https://notes.burke.libbey.me/ansi-escape-codes/
        /* TODO */
        } else {
            if (c == 32) {
                    c = 0xA0;
            }
            col.innerText = String.fromCodePoint(c);
            theApp.cur_col++;
            //vt100(el, c, col);
        }
        if (theApp.cur_col >= 80) {
            theApp.cur_col = 0;
            theApp.cur_row++;
        }
        if (theApp.cur_row >= 25) {
                    scroll(el, theApp.cur_row - 24);
                    theApp.cur_row = 24;
                    last_row = -1;
        }
        if (last_row != theApp.cur_row) {
                    row = el.children[theApp.cur_row];
                    col = row.children[theApp.cur_col];
                    last_row = theApp.cur_row;
                    last_col = theApp.cur_col;
        } else if (last_col != theApp.cur_col) {
                    col = row.children[theApp.cur_col];
                    last_col = theApp.cur_col;
        }

        i++;
        c = txt.codePointAt(i);
    }
}
function _onTimer()  {
    const t = window.Android.getText();
    if (t.length > 0) {
        addtxt(t);
        window.setTimeout(_onTimer, 1);
    } else {
        window.setTimeout(_onTimer, 20);
    }
}

