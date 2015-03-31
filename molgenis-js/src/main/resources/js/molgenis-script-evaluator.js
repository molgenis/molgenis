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
				if (_isNull(this.val)) {
					this.val = undefined;
				} else {
					if (typeof this.val === 'string') {
						this.val = new Date(this.val);
					}
					this.val = Math.floor((new Date() - this.val)/(365.2425 * 24 * 60 * 60 * 1000));
				}
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
			eq: function(other) {
				if (_isNull(this.val) && _isNull(other)) {
					this.val = false;
				} else if (_isNull(this.val) && !_isNull(other)) {
					this.val = false;
				} else {
					this.val = (this.val === other);
				}
				return this;
			},
			isNull: function() {
				this.val = _isNull(this.val);
				return this;
			},
			not: function() {
				this.val = !this.val;
				return this;
			},
			or: function(other) {
				this.val = (this.val || other.value());
				return this;
			},
			gt: function(value) {
				this.val = _isNull(this.val) ? false : (this.val > value);
				return this;
			},
			lt: function(value) {
				this.val = _isNull(this.val) ? false : (this.val < value);
				return this;
			},
			ge: function(value) {
				this.val = _isNull(this.val) ? false : (this.val >= value);
				return this;
			},
			le: function(value) {
				this.val = _isNull(this.val) ? false : (this.val <= value);
				return this;
			}
		}
		
		function _isNull(value) {
			if (value === null || value === undefined) return true;
			if ((typeof value === 'string') && (value.length == 0)) return true;
			return false;
		}
		
		attribute.val = this[attr];
		return attribute
	}
	
	$ = $.bind(entity);
	return eval(script);
}
