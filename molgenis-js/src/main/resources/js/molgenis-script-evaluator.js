function evalScript(script, entity) {
	function $(attr) { 
		var attribute = {
			value: function() {
				return this.val;
			},
			pow: function(exp) {
				if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
					exp = exp.value();
				}
				this.val = Math.pow(this.val, exp);
				return this;
			},
			div: function(denominator) {
				if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
					denominator = denominator.value();
				}
				this.val = (this.val / denominator);
				return this;
			},
			age: function() {
				if (typeof this.val === 'string') {
					this.val = new Date(this.val);
				}
				this.val = Math.floor((new Date() - this.val)/(365.2425 * 24 * 60 * 60 * 1000));
				return this;
			},
			map: function(categoryMapping) {
				var MISSING_VALUE = 9999;
				
				var categoryMappingReversed = {};//Keys and values reversed
				for (var key in categoryMapping) {
					categoryMappingReversed[categoryMapping[key]] = key;
				}
				this.val = categoryMappingReversed[this.val];
				if ((this.val === undefined) || (this.val === null)) {
					this.val = MISSING_VALUE;
				}
				return this;
			},
			contains: function(value) {
				if ((this.val === undefined) || (this.val === null)) {
					return false;
				}
				
				if (typeof this.val === 'string') {
					this.val = this.val.split(',');
				}
				
				if (this.val.constructor !== Array) {
					return false
				}
				
				for (var i = 0; i < this.val.length; i++) {
					if (this.val[i] === value) {
						return true;
					}
				}
				
				return false;
			}
		}
		
		attribute.val = this[attr];
		return attribute
	}
	
	$ = $.bind(entity);
	return eval(script);
}
