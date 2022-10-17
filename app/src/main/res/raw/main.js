function escapeHTML(str) {
  const p = document.createElement("div");
  p.appendChild(document.createTextNode(str));
  return p.innerHTML;
}

function addtxt(txt) {

    const el = document.getElementById('t');
    el.innerHTML = el.innerHTML + "" + escapeHTML(txt);
    el.scrollTo(0, el.scrollHeight);
}

function _onFocus(ev) {

    const el = document.getElementById('b');
    //ev.preventDefault();
   el.focus();
       addtxt("jml");

   true;
}

function _onBlur(ev) {

   true;
}

function _onKeyDown(ev) {
    if (ev.keyCode == 13) {
        window.Android.onKey(ev.keyCode);
        //ev.preventDefault();
        //return false;
    }
    //return true;
}

function _onKeyUp(ev) {
    if (ev.keyCode == 13) {
        //window.Android.onKey(ev.keyCode);
        //ev.preventDefault();
        //return false;
    }
    checktext();
    //return true;
}

function _onTextInput(ev) {
    //return true;
}

function checktext() {
    const el = document.getElementById('b');
    window.Android.onInput(el.innerText);
    el.innerText = "";
}
function _onInput(ev) {

    if (ev.data != null) {
        //window.Android.onInput(ev.data.toString());
    }
    checktext();
    //ev.preventDefault();
    //return false;
}

function _onBeforeInput(ev) {
    if (ev.data != null) {
        //window.Android.onInput(ev.data.toString());
    }
//ev.preventDefault();
    //return true;
}