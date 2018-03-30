/**
 * Evaluates a script.
 *
 * @param script
 *            the script
 * @param entity
 *            the entity
 * @returns the evaluated script result
 */
function evalScript (script, entity) {
  function Attribute (value) {

    var attribute = {
      /**
       *
       * Gives you the value of the attribute specified between $('')
       * notation
       *
       * Example: $('Height').value() returns the values of the height
       * attribute
       *
       * If the value is an object, return the idValue
       *
       * @memberof $
       * @method value
       *
       */
      value: function () {
        if (this.val !== null && typeof this.val === 'object') {
          if (this.val['_idValue'] !== undefined) {

            // Map a list of entities to a list of ID's
            if (Array.isArray(this.val)) {
              return this.val.map(function(value) {
                return newValue(value).value()
              })
            }
            return this.val['_idValue']
          }
        }
        return this.val
      },

      /**
       * Returns true if your value is valid JSON
       * Returns false if not
       */
      isValidJson: function () {
        try {
          JSON.parse(this.val)
          this.val = true
        } catch (e) {
          this.val = false
        }
        return this
      },

      /**
       * returns the result of the first value plus the second value
       *
       * @param value: the number you want to add to the current value
       */
      plus: function (value) {
        if (!_isNull(value)) {
          if (typeof value === 'object' || typeof value === 'function') {
            this.val = this.val + value.value()
          } else {
            this.val = this.val + value
          }
        }
        return this
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
      pow: function (exp) {
        if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
          exp = exp.value()
        }
        this.val = Math.pow(this.val, exp)
        return this
      },
      /**
       * Gives you the multiplication value of the attribute specified between
       * $('') notation
       *
       * Example: $('Height').times(2).value() returns the result of
       * height_value * 2
       *
       * @param factor :
        *            The number you multiply by
       *
       * @memberof $
       * @method div
       */
      times: function (factor) {
        if ((typeof factor === 'object') && (typeof factor.value === 'function')) {
          factor = factor.value()
        }
        this.val = (this.val * factor)
        return this
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
      div: function (denominator) {
        if ((typeof denominator === 'object') && (typeof denominator.value === 'function')) {
          denominator = denominator.value()
        }
        this.val = (this.val / denominator)
        return this
      },
      /**
       * Returns the age based on the date of birth and the current year
       *
       * Example: $('Date_Of_Birth').age().value()
       *
       * @memberof $
       * @method age
       */
      age: function () {
        if (_isNull(this.val)) {
          this.val = undefined
        } else {
          this.val = Math.floor((Date.now() - this.val) / (365.2425 * 24 * 60 * 60 * 1000))
        }
        return this
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
      map: function (categoryMapping, defaultValue, nullValue) {
        if (typeof categoryMapping === 'function') {
          this.val = this.val.map(newValue).map(categoryMapping)
        } else {
          this.val = this.value()
          if (this.val in categoryMapping) {
            this.val = categoryMapping[this.val]
          } else {
            if (nullValue !== undefined && ((this.val === undefined) || (this.val === null))) {
              this.val = nullValue
            } else {
              this.val = defaultValue
            }
          }
        }
        return this
      },
      /**
       * Group values into defined ranges
       *
       * Example: age -> 19, 39, 50, 75
       * $('age').group({18, 35, 50, 75}).value() produces the following ranges which are left inclusive, (-∞, 18), [18, 35), [35, 50), [50, 75), [75, +∞)
       * the text representations are '-18','18-35','35-50','50-75','75+'
       *
       * @param arrayOfBounds :
        *            a list of values that will be the bounds of the ranges.
       * @param arrayOfOutliers :
        *            a list of outlier values that will be not be grouped and will be returned as is.
       * @param nullValue :
        *            a value to use for null instances
       *
       *
       * @memberof $
       * @method group
       */
      group: function (arrayOfBounds, arrayOfOutliers, nullValue) {

        // Check if the the value is an outlier value
        if (arrayOfOutliers && arrayOfOutliers.length > 0) {
          for (var i = 0; i < arrayOfOutliers.length; i++) {
            if (this.val === arrayOfOutliers[i]) {
              return this
            }
          }
        }
        // find the ranges that the value fits into
        if (arrayOfBounds && arrayOfBounds.length > 0) {
          var originalValue = this.val
          if (originalValue < arrayOfBounds[0]) {
            this.val = '-' + arrayOfBounds[0]
          } else if (originalValue >= arrayOfBounds[arrayOfBounds.length - 1]) {
            this.val = arrayOfBounds[arrayOfBounds.length - 1] + '+'
          }
          if (arrayOfBounds.length > 1) {
            for (var i = 1; i < arrayOfBounds.length; i++) {
              var lowerBound = arrayOfBounds[i - 1]
              var upperBound = arrayOfBounds[i]
              //If lowerBound is bigger than upperBound, restore the original value and stop the function
              if (lowerBound > upperBound) {
                this.val = nullValue ? nullValue : null
                break
              }
              if (originalValue >= lowerBound && originalValue < upperBound) {
                this.val = lowerBound + '-' + upperBound
                break
              }
            }
          }
          return this
        }

        this.val = nullValue ? nullValue : null
        return this
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
      eq: function (other) {
        if (_isNull(this.val) && _isNull(other)) {
          this.val = false
        } else if (_isNull(this.val) && !_isNull(other)) {
          this.val = false
        } else {
          this.val = (this.value() === other)
        }
        return this
      },
      /**
       * Check if a value matches a regular expression
       *
       * Example: $('Username').matches(/^[a-z0-9_-]{6,18}$/).value()
       */
      matches: function (regex) {
        this.val = regex.test(this.val)
        return this
      },
      /**
       * Check if a value is null
       *
       * Example: $('Height').isNull().value()
       *
       * @memberof $
       * @method isNull
       */
      isNull: function () {
        this.val = _isNull(this.val)
        return this
      },
      /**
       * Checks if a boolean is not
       *
       * Example: $('Has_Ears').not().value()
       *
       * @memberof $
       * @method not
       */
      not: function () {
        this.val = !this.val
        return this
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
      or: function (other) {
        this.val = (this.val || other.value())
        return this
      },
      /**
       * Checks if something is one value and the other
       *
       * Example: $('female').and($('pregnant')).value()
       *
       * @param other :
        *            Another value
       *
       * @memberof $
       * @method and
       */
      and: function (other) {
        this.val = (this.val && other.value())
        return this
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
      gt: function (value) {
        this.val = _isNull(this.val) ? false : (this.value() > value)
        return this
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
      lt: function (value) {
        this.val = _isNull(this.val) ? false : (this.value() < value)
        return this
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
      ge: function (value) {
        this.val = _isNull(this.val) ? false : (this.value() >= value)
        return this
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
      le: function (value) {
        this.val = _isNull(this.val) ? false : (this.value() <= value)
        return this
      },
      /**
       * Sets the measurement unit of the current value to the specified
       * unit. Returns the current unit when no argument is supplied.
       *
       * @memberof $
       * @method unit
       */
      unit: function (newUnit) {
        if (!newUnit) {
          return this.unit
        }
        this.unit = newUnit
        return this
      },
      /**
       * Measurement unit conversion: converts the current value into a
       * different measurement unit.
       *
       * @memberof $
       * @method toUnit
       */
      toUnit: function (targetUnit) {
        var unit = math.unit(this.val, this.unit)
        this.val = unit.toNumber(targetUnit)
        return this
      }
    }

    function _isNull (value) {
      if (value === null || value === undefined)
        return true
      if ((typeof value === 'string') && (value.length == 0))
        return true
      return false
    }

    attribute.val = value
    return attribute
  }

  /**
   * Stores the computed attribute value after applying on of the mathematical
   * functions listed below
   *
   * In case of deep referencing attribute e.g. xref.xref2.label, we loop through these attributes and only return
   * new Attribute(label)
   *
   * @version 1.0
   * @namespace $
   */
  function $ (attr) {
    var attributes = attr.split('\.')
    var result = this

    for (var i = 0; i < attributes.length && result !== null; i++) {
      result = result[attributes[i]]
    }
    return new Attribute(result)
  }

  function newValue (value) {
    return new Attribute(value)
  }

  $ = $.bind(entity)
  return eval(script)
}