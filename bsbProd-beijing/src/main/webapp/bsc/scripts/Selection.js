function RangeInsert(field,txt)
{
    var fieldStartPos = -1;
    var fieldEndPos = -1;
    if (field.getPositionEl().dom!=null) {
        fieldStartPos = field.getPositionEl().dom.selectionStart;
        fieldEndPos = field.getPositionEl().dom.selectionEnd;
    }
	//IE support
	if (document.selection) {
		field.focus();
	    var sel = document.selection.createRange();
		sel.text = txt;

		field.focus();
	}
	//MOZILLA/NETSCAPE support
	// else if (field.selectionStart || field.selectionStart == '0') {
    else if (fieldStartPos >=0 && fieldEndPos>=0){
		// var startPos = field.selectionStart;
		// var endPos = field.selectionEnd;
        // var scrollTop = field.scrollTop;
        var startPos = fieldStartPos;
        var endPos = fieldEndPos;
		var cursorPos = endPos;
		var scrollTop = field.getPositionEl().dom.scrollTop;
		var scrollLeft = field.getPositionEl().dom.scrollLeft;
		if (startPos != endPos) {
			field.setValue( field.value.substring(0, startPos)
			              + txt
			              + field.value.substring(endPos, field.value.length));
			cursorPos += txt.length;
		}
		else {
				field.setValue (field.value.substring(0, startPos)
				              + txt
				              + field.value.substring(endPos, field.value.length));
				cursorPos = startPos + txt.length;
		}
        field.focus();
		field.selectionStart = startPos;
		field.selectionEnd = cursorPos;
		field.scrollTop = scrollTop;
		field.scrollLeft = scrollLeft;
	}
	else {
		//field.value += txt;
		field.setValue(field.getValue()+txt);
		field.focus();
	}
}