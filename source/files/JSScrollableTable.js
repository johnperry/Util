//*****************************************************************************
// Filename: ScrollableTable.js
// Description: This javascript file can be applied to convert record tables
// in a HTML file to be scrollable.
// Version: 1.1
// Browser Compatibility: IE5.5+
//
// COPYRIGHT (C) 2002 WAGNER DOSANJOS
// THIS PROGRAM IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND/OR MODIFY IT
// UNDER THE TERMS OF THE GNU GENERAL PUBLIC LICENSE AS PUBLISHED BY THE FREE
// SOFTWARE FOUNDATION; EITHER VERSION 2 OF THE LICENSE, OR (AT YOUR OPTION)
// ANY LATER VERSION. THIS PROGRAM IS DISTRIBUTED IN THE HOPE THAT IT WILL BE
// USEFUL, BUT WITHOUT ANY WARRANTY; WITHOUT EVEN THE IMPLIED WARRANTY OF
// MERCHANTABILITY OF FITNESS FOR A PARTICULAR PURPOSE. SEE THE GNU GENERAL
// PUBLIC LICENSE FOR MORE DETAILS.
//
// YOU SHOULD HAVE RECEIVED A COPY OF THE GNU GENERAL PUBLIC LICENSE ALONG
// WITH THIS PROGRAM; IF NOT, WRITE TO:
//
// THE FREE SOFTWARE FOUNDATION, INC.,
// 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
//
// Bugs/Comments: wanjos@yahoo.com
//
// Change History:
// 12-09-2002:
//  o Release v1.0
// 12-12-2002:
//  o Fixed: tHead and tFoot attributes not properly set after changing parent node.
//  o Fixed: Tables with 'width' attribute not properly handled.
//  o Release v1.1
//
//*****************************************************************************
// ScrollableTable.js
//
// This script contains useful functions that can be used to convert ordinary
// tables into scrollable tables.
//
// Here is how one can do that. The following assumptions are required
// for the tables to be sorted.
//
// 1. The table header should be defined in a TBODY.
// 2. The table footer (if one exists) should be defined in a TFOOT.
// 3. TD's in TBODY should not contain the width attribute.
//
// To enable the sorting, simply include this javascript source file and
// add an onLoad event to the <body> like below:
//
// <body onLoad="makeScrollableTable('table1',false,'auto');makeScrollableTable('table2',false,100); ...">
//
// Parameters:
//      - Table ID
//      - Scroll Footer (true/false): Include footer in the scrollable area
//      - Scrollable Area Height (# of pixels or 'auto'): Only the first table called can be set to 'auto'.
//
// Note that all the tables that need to be scrolled MUST contain an ID tag.
// So, if they do not exist, you must create one for each table. Also, the
// table names/ids MUST BE UNIQUE.
//*****************************************************************************
// Global variables
var container = new Array();
var onResizeHandler;

function scrollbarWidth() {
    var w;

    if (! document.body.currentStyle)   document.body.currentStyle = document.body.style;

    if (document.body.currentStyle.overflowY == 'visible' || document.body.currentStyle.overflowY == 'scroll'){
        w = document.body.offsetWidth - document.body.clientLeft - document.body.clientWidth;
    }
    else {
        win = window.open("about:blank", "_blank", "top=0,left=0,width=100,height=100,scrollbars=yes");
        win.document.writeln('scrollbar');
        w = win.document.body.offsetWidth - win.document.body.clientLeft - win.document.body.clientWidth;
        win.close();
    }
    return w;
}

function getActualWidth(e){
    if (! e.currentStyle)   e.currentStyle = e.style;
    return  e.clientWidth - parseInt(e.currentStyle.paddingLeft) - parseInt(e.currentStyle.paddingRight);
}

function findRowWidth(r) {
    for (var i=0; i < r.length; i++){
        r[i].actualWidth = getActualWidth(r[i]);
    }
}

function setRowWidth(r) {
    for (var i=0; i < r.length; i++){
        r[i].width = r[i].actualWidth;
        r[i].innerHTML = '<span style="width:' + r[i].actualWidth + ';">' + r[i].innerHTML + '</span>';
    }
}

function fixTableWidth(tbl) {
    for (var i=0; i < tbl.tHead.rows.length; i++)   findRowWidth(tbl.tHead.rows[i].cells);
    findRowWidth(tbl.tBodies[0].rows[0].cells);
    if (tbl.tFoot)  for (var i=0; i < tbl.tFoot.rows.length; i++)   findRowWidth(tbl.tFoot.rows[i].cells);
    for (var i=0; i < tbl.tHead.rows.length; i++)   setRowWidth(tbl.tHead.rows[i].cells);
    setRowWidth(tbl.tBodies[0].rows[0].cells);
    if (tbl.tFoot)  for (var i=0; i < tbl.tFoot.rows.length; i++)   setRowWidth(tbl.tFoot.rows[i].cells);
}

function makeScrollableTable(tbl, scrollFooter, height) {
    var c, pNode, hdr, ftr, wrapper, rect;

    if (typeof tbl == 'string') tbl = document.getElementById(tbl);

    pNode = tbl.parentNode;
    fixTableWidth(tbl);

    c = container.length;
    container[c] = document.createElement('<SPAN style="height: 100; overflow: auto;">');
    container[c].id = tbl.id + "Container";
    pNode.insertBefore(container[c], tbl);
    container[c].appendChild(tbl);
    container[c].style.width = tbl.clientWidth + 2 * tbl.clientLeft + scrollbarWidth();

    hdr = tbl.cloneNode(false);
    hdr.id += 'Header';
    hdr.appendChild(tbl.tHead.cloneNode(true));
    tbl.tHead.style.display = 'none';

    if (!scrollFooter || !tbl.tFoot){
        ftr = document.createElement('<SPAN style="width:1;height:1;clip: rect(0 1 1 0);background-color:transparent;">');
        ftr.id = tbl.id + 'Footer';
        ftr.style.border = tbl.style.border;
        ftr.style.width = getActualWidth(tbl) + 2 * tbl.clientLeft;
        ftr.style.borderBottom = ftr.style.borderLeft = ftr.style.borderRight = 'none';
    }
    else {
        ftr = tbl.cloneNode(false);
        ftr.id += 'Footer';
        ftr.appendChild(tbl.tFoot.cloneNode(true));
        ftr.style.borderTop = 'none';
        tbl.tFoot.style.display = 'none';
    }

    wrapper = document.createElement('<table border=0 cellspacing=0 cellpadding=0>');
    wrapper.id = tbl.id + 'Wrapper';
    pNode.insertBefore(wrapper, container[c]);

    wrapper.insertRow(0).insertCell(0).appendChild(hdr);
    wrapper.insertRow(1).insertCell(0).appendChild(container[c]);
    wrapper.insertRow(2).insertCell(0).appendChild(ftr);

    wrapper.align = tbl.align;
    tbl.align = hdr.align = ftr.align = 'left';
    hdr.style.borderBottom = 'none';
    tbl.style.borderTop = tbl.style.borderBottom = 'none';

    // adjust page size
    if (c == 0 && height == 'auto') {
        onResizeAdjustTable();
        onResizeHandler = window.onresize;
        window.onresize = onResizeAdjustTable;
    }
    else {
        container[c].style.height = height;
    }
}

function onResizeAdjustTable() {
    if (onResizeHandler) onResizeHandler();

    var rect = container[0].getClientRects()(0);
    var h = document.body.clientHeight - (rect.top + (document.body.scrollHeight - rect.bottom));
    container[0].style.height = (h > 0) ? h : 1;
}

function printPage() {
    var tbs = document.getElementsByTagName('TABLE');
    for (var i=0; i < container.length; i++) container[i].style.overflow = '';
    window.print();
    for (var i=0; i < container.length; i++) container[i].style.overflow = 'auto';
}