function showTextInput(input, minHeight, maxHeight){
	var myInput = new TextInput(input,minHeight, maxHeight);
}

//constructor
function TextInput(input,minHeight,maxHeight) {
	//alert("constructor"); 
	this.init(input,minHeight,maxHeight);
}

//define class prototype
TextInput.prototype = {		
	init : function(input,minHeight,maxHeight) {
		//alert("init");
		this.input = input;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		
		//resize on load
		this.resizeInput();
	
		//add a handler to resize on change
		var _this = this;
		this.addEvent(this.input, "keyup", function(e) {
			_this.resizeInput();
		});
	},
	resizeInput : function(){
		//get current contents, wordwrap otherwise line count is wrong
		var lines = this.wordwrap(this.input.value, this.input.cols + 2, "\n", false).split("\n");
		this.input.rows = Math.min(lines.length,this.maxHeight);
		if(this.input.rows < this.minHeight) this.input.rows = this.minHeight;
		//alert(this.input.rows)
	},
	wordwrap : function(str, width, break_char, cut_long_words) {
		//http://phpjs.org/functions/wordwrap:581
		var i, j, l, s, r;
		var m = width;
		var b = break_char;
		var c = cut_long_words;
		
	    str += '';
	    if (m < 1) {
	        return str;
	    }
	    for (i = -1, l = (r = str.split(/\r\n|\n|\r/)).length; ++i < l; r[i] += s) {
	        for (s = r[i], r[i] = ""; s.length > m; r[i] += s.slice(0, j) + ((s = s.slice(j)).length ? b : "")){
	            j = c == 2 || (j = s.slice(0, m + 1).match(/\S*(\s)?$/))[1] ? m : j.input.length - j[0].length || c == 1 && m || j.input.length + (j = s.slice(m).match(/^\S*/)).input.length;
	        }
	    }
	    return r.join("\n");
	},
	wordwrap2 : function( str, width, brk, cut ) {
	    brk = brk || '\n';
	    width = width || 75;
	    cut = cut || false;
	 
	    if (!str) { return str; }
	 
	    var regex = '.{1,' +width+ '}(\\s|$)' + (cut ? '|.{' +width+ '}|.+$' : '|\\S+?(\\s|$)');
	 
	    return str.match( RegExp(regex, 'g') ).join( brk );
	},
	addEvent : function(obj, eventname, func) {
		//alert(eventname);
		if (navigator.userAgent.match(/MSIE/)) {
			obj.attachEvent("on" + eventname, func);
		} else {
			obj.addEventListener(eventname, func, true);
		}
	}
}