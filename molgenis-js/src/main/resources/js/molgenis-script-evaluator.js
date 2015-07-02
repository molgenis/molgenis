/**
 * Evaluates a script.
 * 
 * @param script
 *            the script
 * @param entity
 *            the entity
 * @returns the evaluated script result
 */
function evalScript(script, entity) {

	var table = {};
	(function() {

		function addUnit(baseUnit, actualUnit, multiplier) {
			table[actualUnit] = {
				base : baseUnit,
				actual : actualUnit,
				multiplier : multiplier
			};
		};

		var prefixes = [ 'Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'k', 'h', 'da', '', 'd', 'c', 'm', 'u', 'n', 'p', 'f', 'a', 'z', 'y' ];
		var factors = [ 24, 21, 18, 15, 12, 9, 6, 3, 2, 1, 0, -1, -2, -3, -6, -9, -12, -15, -18, -21, -24 ];
		// SI units only, that follow the mg/kg/dg/cg type of format
		var units = [ 'g', 'b', 'l', 'm' ];

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

	/**
	 * Stores the computed attribute value after applying on of the mathematical
	 * functions listed below
	 * 
	 * @version 1.0
	 * @namespace $
	 */
	function $(attr) {
		var attribute = {
			/**
			 * 
			 * Gives you the value of the attribute specified between $('')
			 * notation
			 * 
			 * Example: $('Height').value() returns the values of the height
			 * attribute
			 * 
			 * @memberof $
			 * @method value
			 * 
			 */
			value : function() {
				return this.val;
			},
			/**
			 * Gives you the exponent value of the attribute specified between
			 * $('') notation
			 * 
			 * Example: $('Height').pow(2).value() returns the result of
			 * height_value ^ 2
			 * 
			 * @param exp :
			 *            The number you use to execute the power function
			 * 
			 * @memberof $
			 * @method pow
			 */
			pow : function(exp) {
				if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
					exp = exp.value();
				}
				this.val = Math.pow(this.val, exp);
				return this;
			},
			/**
			 * Gives you the division value of the attribute specified between
			 * $('') notation
			 * 
			 * Example: $('Height').div(2).value() returns the result of
			 * height_value / 2
			 * 
			 * @param denominator :
			 *            The number you divide by
			 * 
			 * @memberof $
			 * @method div
			 */
			div : function(denominator) {
				if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
					denominator = denominator.value();
				}
				this.val = (this.val / denominator);
				return this;
			},
			/**
			 * Returns the age based on the date of birth and the current year
			 * 
			 * Example: $('Date_Of_Birth').age().value()
			 * 
			 * @memberof $
			 * @method age
			 */
			age : function() {
				if (_isNull(this.val)) {
					this.val = undefined;
				} else {
					if (typeof this.val === 'string') {
						this.val = new Date(this.val);
					}
					this.val = Math.floor((new Date() - this.val) / (365.2425 * 24 * 60 * 60 * 1000));
				}
				return this;
			},
			/**
			 * Maps categories to eachother.
			 * 
			 * Example: Dataset1 -> Male = 1, Female = 2 Dataset2 -> Male = 0,
			 * Female = 1 $('Dataset2').map({0:1, 1:2}).value()
			 * 
			 * @param categoryMapping :
			 *            The mapping in JSON format to apply
			 * @param defaultValue :
			 *            a value to use for categories that are not mentioned
			 *            in the categoryMappign
			 * @param nullValue :
			 *            a value to use for null instances
			 * 
			 * @memberof $
			 * @method map
			 */
			map : function(categoryMapping, defaultValue, nullValue) {
				if (this.val in categoryMapping) {
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
			/**
			 * Compares two values and returns true or false
			 * 
			 * Example: $('Height').eq(100).value()
			 * 
			 * @param other :
			 *            the value you wish to compare with
			 * 
			 * @memberof $
			 * @method eq
			 */
			eq : function(other) {
				if (_isNull(this.val) && _isNull(other)) {
					this.val = false;
				} else if (_isNull(this.val) && !_isNull(other)) {
					this.val = false;
				} else {
					this.val = (this.val === other);
				}
				return this;
			},
			/**
			 * Check if a value is null
			 * 
			 * Example: $('Height').isNull().value()
			 * 
			 * @memberof $
			 * @method isNull
			 */
			isNull : function() {
				this.val = _isNull(this.val);
				return this;
			},
			/**
			 * Checks if a boolean is not
			 * 
			 * Example: $('Has_Ears').not().value()
			 * 
			 * @memberof $
			 * @method not
			 */
			not : function() {
				this.val = !this.val;
				return this;
			},
			/**
			 * Checks if something is one value or the other
			 * 
			 * Example: $('male').or($('female')).value()
			 * 
			 * @param other :
			 *            Another value
			 * 
			 * @memberof $
			 * @method or
			 */
			or : function(other) {
				this.val = (this.val || other.value());
				return this;
			},
			/**
			 * Returns true or false if Greater then the submitted value
			 * 
			 * Example: $('Height').gt(100).value()
			 * 
			 * @param value :
			 *            The value you compare with
			 * 
			 * @memberof $
			 * @method gt
			 */
			gt : function(value) {
				this.val = _isNull(this.val) ? false : (this.val > value);
				return this;
			},
			/**
			 * Returns true or false if Less then the submitted value
			 * 
			 * Example: $('Height').lt(100).value()
			 * 
			 * @param value :
			 *            The value you compare with
			 * 
			 * @memberof $
			 * @method lt
			 */
			lt : function(value) {
				this.val = _isNull(this.val) ? false : (this.val < value);
				return this;
			},
			/**
			 * Returns true or false if Greater or equal then the submitted
			 * value
			 * 
			 * Example: $('Height').ge(100).value()
			 * 
			 * @param value :
			 *            The value you compare with
			 * 
			 * @memberof $
			 * @method ge
			 */
			ge : function(value) {
				this.val = _isNull(this.val) ? false : (this.val >= value);
				return this;
			},
			/**
			 * Returns true or false if Less or equal then the submitted value
			 * 
			 * Example: $('Height').le(100).value()
			 * 
			 * @param value :
			 *            The value you compare with
			 * 
			 * @memberof $
			 * @method le
			 */
			le : function(value) {
				this.val = _isNull(this.val) ? false : (this.val <= value);
				return this;
			},
			/**
			 * Sets the measurement unit of the current value to the specified
			 * unit. Returns the current unit when no argument is supplied.
			 * 
			 * @memberof $
			 * @method unit
			 */
			unit : function(newUnit) {
				if (!newUnit) {
					return this.unit;
				}
				this.unit = newUnit;
				return this;
			},
			/**
			 * Measurement unit conversion: converts the current value into a
			 * different measurement unit.
			 * 
			 * @memberof $
			 * @method toUnit
			 */
			toUnit : function(targetUnit) {
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
			if (value === null || value === undefined)
				return true;
			if ((typeof value === 'string') && (value.length == 0))
				return true;
			return false;
		}

		attribute.val = this[attr];
		return attribute
	}

	$ = $.bind(entity);
	return eval(script);
}
