/**
 * Attribute filter modal
 *
 * Dependencies: dataexplorer.js
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};

    self.createFilter = function createFilter(attribute, filter, wizard) {
        switch (attribute.fieldType) {
            case 'BOOL':
            case 'CATEGORICAL':
            case 'CATEGORICAL_MREF':
                return self.createSimpleFilter(attribute, filter, wizard, false);
            case 'XREF':
            case 'FILE':
                return self.createSimpleFilter(attribute, filter, wizard, true);
            case 'DATE':
            case 'DATE_TIME':
            case 'DECIMAL':
            case 'LONG':
            case 'EMAIL':
            case 'HTML':
            case 'HYPERLINK':
            case 'STRING':
            case 'ENUM':
            case 'INT':
            case 'TEXT':
            case 'SCRIPT':
                return self.createComplexFilter(attribute, filter, wizard, 'OR');
            case 'MREF':
            case 'ONE_TO_MANY':
                return self.createComplexFilter(attribute, filter, wizard, null);
            case 'COMPOUND' :
                throw 'Unsupported data type: ' + attribute.fieldType;
            default:
                throw 'Unknown data type: ' + attribute.fieldType;
        }
    };

    /**
     * Create filters JavaScript components from a form
     */
    self.createFilters = function createFilters(form) {
        var filters = {};
        var filter;

        $('.complex-filter-container', form).each(function () {
            filter = new self.ComplexFilter($(this).data('attribute'));
            filter.update($(this));
            filters[filter.attribute.href] = filter;
        });

        $('.simple-filter-container', form).each(function () {
            filter = new self.SimpleFilter($(this).data('attribute'));
            filter.update($(this));
            filters[filter.attribute.href] = filter;
        });

        return filters;
    };

    /**
     * Create the filter
     */
    self.createFilterQueryUserReadableList = function (attributeFilters) {
        var items = [];
        $.each(attributeFilters, function (attributeUri, filter) {
            var attributeLabel = molgenis.getAttributeLabel(filter.attribute);
            items.push('<p><a class="feature-filter-edit" data-href="' + attributeUri + '" href="#">'
                + attributeLabel + ': ' + self.createFilterQueryUserReadable(filter)
                + '</a><a class="feature-filter-remove" data-href="' + attributeUri + '" href="#" title="Remove '
                + attributeLabel + ' filter" ><span class="glyphicon glyphicon-remove"></span></a></p>');
        });
        items.push('</div>');
        $('#feature-filters').html(items.join(''));
    };

    /**
     * Create the user simple representation of the query
     */
    self.createFilterQueryUserReadable = function (filter) {
        if (filter.isType('complex')) {
            var complexFilterElements = filter.getComplexFilterElements();
            var addBracket = true;
            if (complexFilterElements) {
                var items = [];
                var elementHasAndOperator = false;
                $.each(complexFilterElements, function (index, complexFilterElement) {
                    var s = '';
                    if (index > 0) {
                        if (complexFilterElement.operator === 'AND') {
                            if (!elementHasAndOperator) {
                                elementHasAndOperator = true;
                                items[items.length - 1] = '(' + items[items.length - 1];
                            }
                            addBracket = false;
                        } else {
                            // complexFilterElement.operator === 'OR'
                            if (elementHasAndOperator) {
                                elementHasAndOperator = false;
                                s += ') ';
                            }
                        }

                        items.push(' ' + complexFilterElement.operator.toLowerCase() + ' ');
                    }

                    items.push(self.createSimpleFilterValuesRepresentation(complexFilterElement.simpleFilter));
                });

                if (elementHasAndOperator) {
                    items.push(')');
                }

                if (items.length < 2) {
                    addBracket = false;
                }
            }
            if (addBracket) {
                return '(' + items.join('') + ')';
            } else {
                return items.join('');
            }
        }
        else if (filter.isType('simple')) {
            return self.createSimpleFilterValuesRepresentation(filter);
        }
    }

    /**
     * Create the simple filter user simple representation of the query
     */
    self.createSimpleFilterValuesRepresentation = function (filter) {
        var values = filter.getValues();
        switch (filter.attribute.fieldType) {
            case 'DATE':
            case 'DATE_TIME':
            case 'DECIMAL':
            case 'INT':
            case 'LONG':
                if (filter.fromValue && filter.toValue) {
                    return '(' + htmlEscape(filter.fromValue) + ' &le; x &le; ' + htmlEscape(filter.toValue) + ')';
                } else if (filter.fromValue) {
                    return '(' + htmlEscape(filter.fromValue) + ' &le; x)';
                } else if (filter.toValue) {
                    return '(x &le; ' + htmlEscape(filter.toValue) + ')';
                } else {
                    return '';
                }
            case 'EMAIL':
            case 'HTML':
            case 'HYPERLINK':
            case 'STRING':
            case 'TEXT':
            case 'BOOL':
            case 'ENUM':
            case 'SCRIPT':
                return htmlEscape(values[0] ? values[0] : '');
            case 'CATEGORICAL':
            case 'CATEGORICAL_MREF':
            case 'MREF':
            case 'XREF':
            case 'FILE':
            case 'ONE_TO_MANY':
                var operator = (filter.operator ? filter.operator.toLocaleLowerCase() : 'or');
                var array = [];
                $.each(filter.getLabels(), function (key, value) {
                    array.push('\'' + value + '\'');
                });
                return htmlEscape('(' + array.join(' ' + operator + ' ') + ')');
            case 'COMPOUND' :
                throw 'Unsupported data type: ' + filter.attribute.fieldType;
            default:
                throw 'Unknown data type: ' + filter.attribute.fieldType;
        }
    }

    /**
     * Create complex filter
     */
    self.createComplexFilter = function (attribute, filter, wizard, fixedOperator) {
        var $container = $('<div class="complex-filter-container form-group"></div>').data('attribute', attribute);
        var useFixedOperator = (fixedOperator !== undefined && fixedOperator !== null ? true : false);
        var filterElementOperator = null;
        var $addButton = null;

        if (filter) {
            if (filter.isType('complex')) {
                $.each(filter.getComplexFilterElements(), function (index, complexFilterElement) {
                    filterElementOperator = (fixedOperator ? fixedOperator : complexFilterElement.operator);
                    self.addComplexFilterElementToContainer(
                        $container,
                        attribute,
                        filterElementOperator,
                        complexFilterElement.simpleFilter,
                        wizard,
                        (index > 0 ? false : true),
                        filter.getComplexFilterElements().length,
                        useFixedOperator);
                });
            }
        } else {
            filterElementOperator = (fixedOperator ? fixedOperator : null);
            self.addComplexFilterElementToContainer(
                $container,
                attribute,
                filterElementOperator,
                null,
                wizard,
                true,
                null,
                useFixedOperator);
        }

        $addButton = self.createComplexFilterAddButton($container, attribute, filterElementOperator, wizard, useFixedOperator);
        self.addComplexFilterAddButton($container, $addButton);

        return $container;
    }

    /**
     * Add a complex filter element to the complex filter container
     */
    self.addComplexFilterElementToContainer = function ($container, attribute, complexFilterOperator, simpleFilter, wizard, isFirstElement, totalNumberElements, useFixedOperator) {
        // The complex filter element container
        var $complexElementContainer = $('<div class="form-group complex-element-container" data-filter="complex-element-container"></div>');

        if (wizard) {
            // Complex element containing the simple filter and the operator
            var $complexElement = $('<div class="controls complex-element col-md-9" data-filter="complex-element"></div>');
        } else {
            // if its for a single attribute
            var $complexElement = $('<div class="controls complex-element col-md-12" data-filter="complex-element"></div>');
        }

        // Make complex filter element label
        var $complexElementLabel = self.createFilterLabel(attribute, isFirstElement, wizard);

        // Add label
        $complexElementContainer.append($complexElementLabel);

        // Simple filter
        var $controlGroupSimpleFilter = self.createSimpleFilterControls(attribute, simpleFilter, wizard);
        $controlGroupSimpleFilter.attr('data-filter', 'complex-simplefilter');
        $controlGroupSimpleFilter.addClass('complex-simplefilter');
        $controlGroupSimpleFilter.css('display', 'inline-block');

        // Remove complex filter element button container
        var $removeButtonContainer = $('<div class="controls complex-removebutton-container" data-filter="complex-removebutton-container"></div>');

        // Add complex filter element button container
        var $plusButtonContainer = $('<div class="controls complex-addbutton-container" data-filter="complex-addbutton-container"></div>');


        if (isFirstElement) {
            // Add simple filter
            $complexElement.append($controlGroupSimpleFilter);

            if (totalNumberElements > 1) {
                // Add remove
                $removeButtonContainer.append(self.createRemoveButtonFirstComplexElement($container));
            }
        } else {
            // Add select complex filter operator
            var $complexOperatorControlGroup = self.createComplexFilterSelectOperator(complexFilterOperator, useFixedOperator, wizard);

            // Add operator
            $complexElement.append($complexOperatorControlGroup);

            // Add simple filter
            $complexElement.append($controlGroupSimpleFilter);

            // Add remove
            $removeButtonContainer.append(self.createRemoveButtonComplexElementFilter($complexElementContainer));
        }

        $complexElement.append($plusButtonContainer);
        $complexElement.append($removeButtonContainer);
        $complexElementContainer.append($complexElement)

        $container.append($complexElementContainer);

        return $complexElementContainer;
    }

    /**
     * Create the complex filter select operator component
     *
     * Options: OR, AND
     */
    self.createComplexFilterSelectOperator = function (complexOperator, useFixedOperator, wizard) {
        var $controlGroup = $('<div class="' + (wizard ? 'col-md-9' : 'col-md-10') + '">');
        var operator = (complexOperator === 'AND' ? 'AND' : 'OR');
        var orLabel = 'OR&nbsp;&nbsp;';
        var andLabel = 'AND';
        var operatorLabel = operator === 'AND' ? andLabel : orLabel;
        var $operatorInput = $('<input type="hidden" data-filter="complex-operator" value="' + operator + '"/>');
        $controlGroup.append($operatorInput);
        var $dropdown;
        if (useFixedOperator === false) {
            $dropdown = $('<div class="btn-group" data-filter="complex-operator-container" style="margin-left:45%;"><div>');
            $dropdown.append($('<a class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown" href="#">' + operatorLabel + ' <b class="caret"></a>'));
            $dropdown.append($('<ul class="dropdown-menu"><li><a data-value="OR">' + orLabel + '</a></li><li><a data-value="AND">' + andLabel + '</a></li></ul>'));
            $.each($dropdown.find('.dropdown-menu li a'), function (index, element) {
                $(element).click(function () {
                    var dataValue = $(this).attr('data-value');
                    $operatorInput.val(dataValue);
                    $dropdown.find('a:first').html((dataValue === 'AND' ? andLabel : orLabel) + ' <b class="caret"></b>');
                    $dropdown.find('a:first').val(dataValue);
                });
            });
            $dropdown.find('div:first').remove();//This is a workaround FIX

        } else {
            $dropdown = $('<div data-filter="complex-operator-container" style="width:5%; margin: 0 auto;">' + operator + '<div>');
        }

        return $('<div class="form-group">').append($controlGroup.append($dropdown));
    }

    /**
     * Create filter label
     */
    self.createFilterLabel = function (attribute, isFirstElement, wizard) {
        var label = attribute.label || attribute.name;
        if (isFirstElement && wizard) {
            var labelHtml = $('<label class="col-md-3 control-label" data-placement="right" data-title="' + attribute.description + '">' + label + '</label>');
            if (attribute.description !== undefined) {
                labelHtml.tooltip()
            }
            return labelHtml;
        }
        else if (!isFirstElement && wizard) {
            return $('<label class="col-md-3 control-label"></label>');
        }
        else {
            return null;
        }
    }

    /**
     * add complex filter add-button
     */
    self.addComplexFilterAddButton = function ($container, $addButton) {
        $('[data-filter=complex-addbutton-container]', $container).last().append($addButton);
    }

    /**
     * Create complex filter add-button
     */
    self.createComplexFilterAddButton = function ($container, attribute, complexFilterOperator, wizard, useFixedOperator) {
        return ($('<button class="btn btn-default btn-xs" type="button" data-filter=complex-addbutton><i class="glyphicon glyphicon-plus"></i></button>').click(function () {
            if ($('[data-filter=complex-removebutton]', $container).length === 0) {
                $('[data-filter=complex-removebutton-container]', $container).append(self.createRemoveButtonFirstComplexElement($container));
            }
            self.addComplexFilterElementToContainer($container, attribute, complexFilterOperator, null, wizard, false, null, useFixedOperator);
            self.addComplexFilterAddButton($container, $('[data-filter=complex-addbutton]', $container));
        }));
    }

    /**
     * Create remove button to remove complex elements that are not the first
     */
    self.createRemoveButtonComplexElementFilter = function ($complexElementContainer) {
        return $('<button class="btn btn-default btn-xs" type="button" data-filter=complex-removebutton><i class="glyphicon glyphicon-minus"></i></button>').click(function () {
            var $container = $complexElementContainer.parent();
            var $addButton = $('[data-filter=complex-addbutton]', $container);

            if ($('[data-filter=complex-removebutton]', $container).length === 2) {
                $('[data-filter=complex-removebutton]', $container).remove();
            }

            if ($('[data-filter=complex-addbutton]', $complexElementContainer).length) {
                var $prev = $('[data-filter=complex-addbutton-container]', $container).eq(-2);
                $prev.append($addButton);
            }

            $complexElementContainer.remove();
        });
    }

    /**
     * Create remove button to remove the first element in a complex filter
     */
    self.createRemoveButtonFirstComplexElement = function ($container) {
        return $('<button class="btn btn-default btn-xs" type="button" data-filter=complex-removebutton><i class="glyphicon glyphicon-minus"></i></button>').click(function () {
            var $firstElement = $($('[data-filter=complex-element]', $container)[0]);
            var $secondElement = $($('[data-filter=complex-element]', $container)[1]);
            var $simpleFilterFirstElement = $('[data-filter=complex-simplefilter]', $firstElement);
            var $simpleFilterSecondElement = $('[data-filter=complex-simplefilter]', $secondElement);
            var $simpleFilterSecondElementButton = $('[data-filter=complex-addbutton]', $secondElement);
            var $simpleFilterSecondElementContainer = $('[data-filter=complex-element-container]', $container).eq(1);

            $simpleFilterFirstElement.remove();
            $firstElement.append($simpleFilterSecondElement);
            $('[data-filter=complex-addbutton-container]', $firstElement).append($simpleFilterSecondElementButton);

            $simpleFilterSecondElementContainer.remove();
            if ($('[data-filter=complex-removebutton]', $container.parent()).length === 1) {
                $('[data-filter=complex-removebutton]', $container.parent()).remove();
            }
        });
    }

    /**
     * Create simple filter
     */
    self.createSimpleFilter = function (attribute, filter, wizard, wrap) {
        var $container = $('<div class="simple-filter-container form-group"></div>');
        var $label = self.createFilterLabel(attribute, true, wizard);
        $container.append($label);
        $container.append(self.createSimpleFilterControls(attribute, filter, wizard));
        $container.data('attribute', attribute);
        if (wrap) {
            var $wrapper = $('<div>').addClass((wizard ? 'col-md-9' : 'col-md-10'));
            $container.children('.col-md-9').wrap($wrapper);
        }
        return $('<div class="form-group">').append($container);
    }

    /**
     * Create simple filter controls
     */
    self.createSimpleFilterControls = function (attribute, simpleFilter, wizard) {
        var $controls = $('<div>');
        $controls.addClass((wizard ? 'col-md-9' : 'col-md-10'));
        var name = 'input-' + attribute.name + '-' + new Date().getTime();
        var values = simpleFilter ? simpleFilter.getValues() : null;
        var fromValue = simpleFilter ? simpleFilter.fromValue : null;
        var toValue = simpleFilter ? simpleFilter.toValue : null;
        switch (attribute.fieldType) {
            case 'BOOL':
                var attrs = {'name': name};
                var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
                var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
                var inputTrue = createInput(attribute, attrsTrue, true);
                var inputFalse = createInput(attribute, attrsFalse, false);
                $controls.append($('<div class="filter-radio-inline-container">').append(inputTrue.addClass('radio-inline')).append(inputFalse.addClass('radio-inline')));
                break;
            case 'CATEGORICAL':
            case 'CATEGORICAL_MREF':
                var restApi = new molgenis.RestClient();
                var entityMeta = restApi.get(attribute.refEntity.href);
                var entitiesUri = entityMeta.href.replace(new RegExp('/meta[^/]*$'), ''); // TODO do not manipulate uri
                var entities = restApi.get(entitiesUri, {
                    q: {
                        sort: {
                            orders: [{
                                direction: 'ASC',
                                property: entityMeta.labelAttribute
                            }]
                        }
                    }
                });
                $.each(entities.items, function () {
                    var attrs = {'name': name, 'id': name};
                    if (values && $.inArray(this[entityMeta.idAttribute], values) > -1)
                        attrs.checked = 'checked';
                    $controls.append(createInput(attribute, attrs, this[entityMeta.idAttribute], this[entityMeta.labelAttribute]));
                });
                break;
            case 'DATE':
            case 'DATE_TIME':
                var nameFrom = name + '-from', nameTo = name + '-to';
                var valFrom = fromValue ? fromValue : undefined;
                var valTo = toValue ? toValue : undefined;
                var inputFrom = createInput(attribute, {'name': nameFrom, 'placeholder': 'Start date'}, valFrom);
                var inputTo = createInput(attribute, {'name': nameTo, 'placeholder': 'End date'}, valTo);
                $controls.append($('<div class="form-group">').append(inputFrom)).append($('<div class="form-group">').append(inputTo));
                break;
            case 'DECIMAL':
            case 'INT':
            case 'LONG':
                if (attribute.range) {
                    var slider = $('<div id="slider" class="form-group"></div>');
                    var min = fromValue ? fromValue : attribute.range.min;
                    var max = toValue ? toValue : attribute.range.max;
                    slider.editRangeSlider({
                        symmetricPositionning: true,
                        bounds: {min: attribute.range.min, max: attribute.range.max},
                        defaultValues: {min: min, max: max},
                        type: 'number'
                    });
                    $controls.addClass('range-container');
                    if (fromValue || toValue) {
                        // Values differ from range min and max
                        $controls.data('dirty', true);
                    }
                    slider.bind("userValuesChanged", function (e, data) {
                        // User changed slider values
                        $controls.data('dirty', true);
                    });
                    $controls.append(slider);
                } else {
                    var nameFrom = name + '-from', nameTo = name + '-to';
                    var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
                    var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
                    var inputFrom = createInput(attribute, {
                        'name': nameFrom,
                        'id': nameFrom,
                        'style': 'width: 189px'
                    }, values ? fromValue : undefined).addClass('input-small');
                    var inputTo = createInput(attribute, {
                        'name': nameTo,
                        'id': nameTo,
                        'style': 'width: 189px'
                    }, values ? toValue : undefined).addClass('input-small');
                    $controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
                }
                break;
            case 'EMAIL':
            case 'HTML':
            case 'HYPERLINK':
            case 'STRING':
            case 'TEXT':
            case 'ENUM':
            case 'SCRIPT':
                $controls.append(createInput(attribute, {
                    'name': name,
                    'id': name
                }, values ? values[0] : undefined));
                break;
            case 'XREF':
            case 'MREF':
            case 'FILE':
            case 'ONE_TO_MANY':
                var operator = simpleFilter ? simpleFilter.operator : 'OR';
                var container = $('<div class="xrefmrefsearch">');
                $controls.append(container);
                container.xrefmrefsearch({
                    width: '100%',
                    attribute: attribute,
                    values: values,
                    labels: simpleFilter ? simpleFilter.getLabels() : null,
                    operator: operator,
                    autofocus: 'autofocus',
                    isfilter: true
                });
                break;
            case 'COMPOUND' :
                throw 'Unsupported data type: ' + attribute.fieldType;
            default:
                throw 'Unknown data type: ' + attribute.fieldType;
        }
        return $controls;
    }

    /**
     * JavaScript filter representation as an interface for filters
     */
    self.Filter = function () {
        this.operators = {'OR': 'OR', 'AND': 'AND'};
        this.types = {'simple': 'simple', 'complex': 'complex'};
        this.type = undefined;
        this.operator = this.operators['OR'];
        this.attribute = undefined;

        /**
         * this.update = function ($domElement)
         * {
		 * 		Implement this method in subclass filters
		 * }
         */

        /**
         * this.isEmpty = function ()
         * {
		 * 		Implement this method in subclass filters
		 * }
         */

        /**
         * this.createQueryRule = function (){
		 * {
		 * 		Implement this method in subclass filters
		 * }
		 */

        this.isType = function (type) {
            return type && this.types[type] && this.type === type;
        };

        this.formatOperator = function (operator) {
            return this.operators[operator];
        }

        return this;
    }

    /**
     * JavaScript representation of a simple filter
     */
    self.SimpleFilter = function (attribute, fromValue, toValue, value) {
        this.fromValue = fromValue;
        this.toValue = toValue;
        var values = [];
        var labels = [];
        this.type = 'simple';
        this.attribute = attribute;

        if (value !== undefined) values.push(value);

        this.isEmpty = function () {
            return !(values.length || this.fromValue || this.toValue);
        };

        this.getValues = function () {
            return values;
        };

        this.getLabels = function () {
            return labels;
        };

        this.update = function ($domElement) {
            var fromValue = this.fromValue;
            var toValue = this.toValue;
            var operator = this.operator;

            // Add operator
            var operator = $(':input[data-filter=xrefmref-operator]', $domElement).val();

            $(':input', $domElement).not('[type=radio]:not(:checked)')
                .not('[type=checkbox]:not(:checked)')
                .not('[data-filter=complex-operator]')
                .not('[data-filter=xrefmref-operator]')
                .not('button.btn.btn-default.dropdown-toggle')
                .not('.select2-input')
                .not('.exclude').each(function () {
                var value = $(this).val();
                var name = $(this).attr('name');

                if (value) {
                    // Add values
                    if (attribute.fieldType === 'MREF' ||
                        attribute.fieldType === 'XREF' || attribute.fieldType === 'FILE' || attribute.fieldType == 'ONE_TO_MANY') {
                        var mrefValues = value.split(',');
                        $(mrefValues).each(function (i) {
                            values.push(mrefValues[i]);
                        });

                        labels = $(this).data('labels');
                    }
                    else if (attribute.fieldType == 'CATEGORICAL' || attribute.fieldType === 'CATEGORICAL_MREF') {
                        labels.push($(this).parent().text());
                        values[values.length] = value;
                    }
                    else if (attribute.fieldType === 'INT'
                        || attribute.fieldType === 'LONG'
                        || attribute.fieldType === 'DECIMAL'
                        || attribute.fieldType === 'DATE'
                        || attribute.fieldType === 'DATE_TIME'
                    ) {

                        if ($domElement.closest('.range-container').data('dirty') || !attribute.range) {
                            // Add toValue
                            if (name && (name.match(/-to$/g) || name === 'sliderright')) {
                                toValue = value;
                            }

                            // Add fromValue
                            if (name && (name.match(/-from$/g) || name === 'sliderleft')) {
                                fromValue = value;
                            }
                        }

                        // Validate that to > from
                        if (attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG') {
                            if (parseFloat(toValue) < parseFloat(fromValue)) {
                                toValue = undefined;
                            }
                        }
                    }
                    else {
                        values[values.length] = value;
                        labels[values.length] = value;
                    }
                }
            });
            this.fromValue = fromValue;
            this.toValue = toValue;
            this.operator = operator;
            return this;
        };

        this.createQueryRule = function () {
            var attribute = this.attribute;
            var fromValue = this.fromValue;
            var toValue = this.toValue;
            var operator = this.operator;
            var rule;
            var rangeQuery = attribute.fieldType === 'DATE' || attribute.fieldType === 'DATE_TIME' || attribute.fieldType === 'DECIMAL' || attribute.fieldType === 'INT' || attribute.fieldType === 'LONG';
            var queryRuleField = attribute.parent ? attribute.parent.name + '.' + attribute.name : attribute.name;

            if (rangeQuery) {
                if (attribute.fieldType === 'DATE_TIME') {
                    if (fromValue) {
                        fromValue = fromValue.replace("'T'", "T");
                    }
                    if (toValue) {
                        toValue = toValue.replace("'T'", "T");
                    }
                }

                // add range fromValue / toValue
                if (fromValue && toValue) {
                    rule = {
                        operator: 'NESTED',
                        nestedRules: [
                            {
                                field: queryRuleField,
                                operator: 'GREATER_EQUAL',
                                value: fromValue
                            },
                            {
                                operator: 'AND'
                            },
                            {
                                field: queryRuleField,
                                operator: 'LESS_EQUAL',
                                value: toValue
                            }]
                    };
                } else if (fromValue) {
                    rule = {
                        field: queryRuleField,
                        operator: 'GREATER_EQUAL',
                        value: fromValue
                    };
                } else if (toValue) {
                    rule = {
                        field: queryRuleField,
                        operator: 'LESS_EQUAL',
                        value: toValue
                    };
                }
            } else {
                if (values) {
                    // determine query operator for attribute type
                    var attrOperator;
                    switch (attribute.fieldType) {
                        case 'BOOL':
                        case 'CATEGORICAL':
                        case 'CATEGORICAL_MREF':
                        case 'DATE':
                        case 'DATE_TIME':
                        case 'DECIMAL':
                        case 'ENUM':
                        case 'INT':
                        case 'LONG':
                        case 'MREF':
                        case 'XREF':
                        case 'ONE_TO_MANY':
                        case 'FILE':
                            attrOperator = 'EQUALS';
                            break;
                        case 'EMAIL':
                        case 'HTML':
                        case 'HYPERLINK':
                        case 'SCRIPT':
                        case 'STRING':
                        case 'TEXT':
                            attrOperator = 'SEARCH';
                            break;
                        case 'COMPOUND':
                            throw 'Unsupported data type: ' + attribute.fieldType;
                        default:
                            throw 'Unknown data type: ' + attribute.fieldType;
                    }

                    if (values.length > 1) {
                        operator = operator && operator !== 'undefined' ? operator : 'OR'
                        if (attrOperator === 'EQUALS' && operator === 'OR') {
                            rule = {
                                field: queryRuleField,
                                operator: 'IN',
                                value: values
                            };
                        } else {
                            var nestedRule = {
                                operator: 'NESTED',
                                nestedRules: []
                            };

                            $.each(values, function (index, value) {
                                if (index > 0) {
                                    nestedRule.nestedRules.push({
                                        operator: operator
                                    });
                                }

                                nestedRule.nestedRules.push({
                                    field: queryRuleField,
                                    operator: attrOperator,
                                    value: value
                                });
                            });
                            rule = nestedRule;
                        }
                    } else {
                        rule = {
                            field: queryRuleField,
                            operator: attrOperator,
                            value: values[0]
                        };
                    }
                }
            }

            return rule;
        };

        return this;
    };
    self.SimpleFilter.prototype = new self.Filter();

    /**
     * JavaScript representation of een element in a complex filter
     */
    self.ComplexFilterElement = function (attribute) {
        this.simpleFilter = null;
        this.type = 'complex-element';
        this.attribute = attribute;

        this.update = function ($domElement) {
            this.operator = this.formatOperator($(':input[data-filter=complex-operator]', $domElement).val());
            this.simpleFilter = (new self.SimpleFilter(attribute)).update($('[data-filter=complex-simplefilter]', $domElement));
            return this;
        };
    };
    self.ComplexFilterElement.prototype = new self.Filter();

    /**
     * JavaScript representation of a complex filter
     */
    self.ComplexFilter = function (attribute) {
        var complexFilterElements = [];
        this.type = 'complex';
        this.attribute = attribute;

        this.update = function ($domElement) {
            $('[data-filter=complex-element]', $domElement).each(function () {
                var complexFilterElement = (new self.ComplexFilterElement(attribute)).update($(this));
                if (!complexFilterElement.simpleFilter.isEmpty()) {
                    complexFilterElements.push(complexFilterElement);
                }
            });
            return this;
        };

        this.addComplexFilterElement = function (complexFilterElement) {
            if (!complexFilterElement !== null) {
                complexFilterElements.push(complexFilterElement);
            }
            return this;
        };

        this.isEmpty = function () {
            for (var i = 0; i < complexFilterElements.length; i++) {
                if (!complexFilterElements[i].simpleFilter.isEmpty()) {
                    return false;
                }
            }
            return true;
        };

        this.getComplexFilterElements = function () {
            return complexFilterElements;
        };

        /**
         * Implements the SQL Logic Operator Precedence: And and Or
         *
         * And has precedence over Or
         *
         * Example: (A and B and C) or (D) or (E and F)
         */
        this.createQueryRule = function () {
            var nestedRules = [];
            var lastOperator, rule, lastNestedRule = null;

            $.each(complexFilterElements, function (index, complexFilterElement) {
                if (index > 0) {
                    lastOperator = complexFilterElement.operator;

                    if (lastOperator === 'AND') {
                        lastNestedRule.nestedRules.push({
                            operator: lastOperator
                        });
                    }
                    else if (lastOperator === 'OR') {
                        nestedRules.push(lastNestedRule);
                        lastNestedRule = null;
                        nestedRules.push({
                            operator: lastOperator
                        });
                    }
                }

                if (lastNestedRule === null) {
                    lastNestedRule = {
                        operator: 'NESTED',
                        nestedRules: []
                    };
                }

                lastNestedRule.nestedRules.push(complexFilterElement.simpleFilter.createQueryRule());

            });

            if (lastNestedRule !== null) {
                nestedRules.push(lastNestedRule);
            }

            rule = {
                operator: 'NESTED',
                nestedRules: nestedRules
            };

            return rule;
        };

        return this;
    };
    self.ComplexFilter.prototype = new self.Filter();
}($, window.top.molgenis = window.top.molgenis || {}));