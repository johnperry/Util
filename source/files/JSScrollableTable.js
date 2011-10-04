//A class to synchronize the widths of two tables.
//This class can be used to implement the equivalent
//of a single table with a non-scrolling header and
//a scrollable body.
//
//Variables:
//	head - the table containing a THEAD element with the column headings
//  body - the table containing a TBODY element whose column widths are
//			to be forced into the head table.
function ScrollableTable( head, body ) {
	this.headTable = head;
	this.bodyTable = body;
	this.thead = head.tHead;
	this.tbody = body.tBodies[0];
}

ScrollableTable.prototype.sync = function(debug) {
	this.headTable.style.width = this.bodyTable.clientWidth;
	var headRows = this.thead.rows;
	var bodyRows = this.tbody.rows;
	if ((headRows.length > 0) && (bodyRows.length > 0)) {
		var bodyRowCells = bodyRows[0].cells;
		for (var i=0; i < headRows.length; i++) {
			var headRowCells = headRows[i].cells;
			for (var k=0; (k<headRowCells.length) && (k<bodyRowCells.length); k++) {
				var hc = headRowCells[k];
				var bc = bodyRowCells[k];
				var style = (bc.currentStyle ? bc.currentStyle : bc.style);
				var pad = parseInt(style.paddingLeft) + parseInt(style.paddingRight);
				if (isNaN(pad)) pad = 4;
				var w = bc.clientWidth - pad;
				hc.width = ( (w>1) ? w : 1);
			}
		}
	}
}

