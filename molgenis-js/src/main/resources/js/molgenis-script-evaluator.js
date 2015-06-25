function evalScript(script, entity) {
	var table = {};
	(function () {

	    function addUnit (baseUnit, actualUnit, multiplier) {
	        table[actualUnit] = { base: baseUnit, actual: actualUnit, multiplier: multiplier };
	    };

	    var prefixes = ['Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'k', 'h', 'da', '', 'd', 'c', 'm', 'u', 'n', 'p', 'f', 'a', 'z', 'y'];
	    var factors = [24, 21, 18, 15, 12, 9, 6, 3, 2, 1, 0, -1, -2, -3, -6, -9, -12, -15, -18, -21, -24];
	    // SI units only, that follow the mg/kg/dg/cg type of format
	    var units = ['g', 'b', 'l', 'm'];

	    for (var j = 0; j < units.length; j++) {
	        var base = units[j];
	        for (var i = 0; i < prefixes.length; i++) {
	            addUnit(base, prefixes[i] + base, Math.pow(10, factors[i]));
	        }
	    }

	    // we use the SI gram unit as the base; this allows
	    // us to convert between SI and English units
	    addUnit('g', 'ounce', 28.3495231);
	    addUnit('g', 'oz', 28.3495231);
	    addUnit('g', 'pound', 453.59237);
	    addUnit('g', 'lb', 453.59237);
	})();

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
			map: function(categoryMapping, defaultValue, nullValue) {
				if( this.val in categoryMapping ) {
					this.val = categoryMapping[this.val];
				} else {
					if (nullValue !== undefined && ((this.val === undefined) || (this.val === null))) {
						this.val = nullValue;
					} else {
						this.val = defaultValue;
					}
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
			},
			unit: function(newUnit) {
				if(!newUnit){
					return this.unit;
				}
				this.unit = newUnit;
				return this;
			},
			toUnit: function(targetUnit) {
				var target = table[targetUnit];
		        var current = table[this.unit];
		        if (target.base != current.base) {
		            throw new Error('Incompatible units; cannot convert from "' + this.unit + '" to "' + targetUnit + '"');
		        }
		        this.val = this.val * (current.multiplier / target.multiplier);
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
