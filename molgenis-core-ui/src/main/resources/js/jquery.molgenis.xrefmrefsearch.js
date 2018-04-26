/**
 * An autocomplete search dropdown for xref and mref values
 *
 * usage:
 *
 * $('#id_of_hidden_input)').xrefmrefsearch({attributeUri: 'api/v1/celiacsprue/meta/Celiac_Family'});
 *
 * or
 *
 * $('#id_of_hidden_input)').xrefmrefsearch({attribute: attribute});
 *
 * Depends on select2.js and molgenis.js
 *
 * @deprecated use EntitySelectBox.js
 */
(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();

    function getInexactQueryOperator(fieldType) {
        var operator = 'SEARCH';
        switch (fieldType) {
            case 'INT':
            case 'LONG':
            case 'BOOL':
            case 'DATE':
            case 'DATE_TIME':
            case 'DECIMAL':
                operator = 'EQUALS';
                break;
        }
        return operator;
    }

    function isCompatibleWith(fieldType, term) {
        var result = true
        switch (fieldType) {
            case 'INT':
            case 'LONG':
            case 'DECIMAL':
                return !isNaN(term);
            case 'BOOL':
                switch (term.toLowerCase()) {
                    case 'true':
                    case 'false':
                        return true;
                    default:
                        return false;
                }
            case 'DATE':
            case 'DATE_TIME':
                return false;
        }
        return result
    }

    function createQuery(lookupAttributes, terms, exactMatch, search) {
        var q = [];

        if (lookupAttributes.length) {
            $.each(lookupAttributes, function (index, attribute) {
                if (q.length > 0) {
                    q.push({operator: 'OR'});
                }

                if (terms.length > 0) {
                    var rule = {
                        operator: 'NESTED',
                        nestedRules: []
                    };

                    var operator = exactMatch ? 'EQUALS' : getInexactQueryOperator(attribute.fieldType);

                    $.each(terms, function (index) {
                        if (isCompatibleWith(attribute.fieldType, terms[index])) {
                            if (rule.nestedRules.length > 0) {
                                if (search) {
                                    rule.nestedRules.push({operator: 'AND'});
                                } else {
                                    rule.nestedRules.push({operator: 'OR'});
                                }
                            }

                            rule.nestedRules.push({
                                field: attribute.name,
                                operator: operator,
                                value: terms[index]
                            })
                        }
                    });

                    if (rule.nestedRules.length > 0) {
                        q.push(rule);
                    }
                }
            });
            return q;
        } else {
            return undefined;
        }
    }

    function getUniqueAttributes(entityMetaData) {
        var attributes = [];
        $.each(entityMetaData.attributes, function (attrName, attr) {
            if (attr.unique === true) {
                attributes.push(attr);
            }
        });
        return attributes;
    }

    function getLookupAttributes(entityMetaData) {
        var attributes = [];
        $.each(entityMetaData.attributes, function (attrName, attr) {
            if (attr.lookupAttribute === true) {
                attributes.push(attr);
            }
        });
        if (attributes.length === 0) {
            $.each(entityMetaData.attributes, function (attrName, attr) {
                if (attr.labelAttribute === true) {
                    attributes.push(attr);
                }
            });
        }
        return attributes;
    }

    function formatResult(entity, entityMetaData, lookupAttributeNames) {
        var items = [];
        items.push('<div class="row">');

        if (lookupAttributeNames.length > 0) {
            var width = Math.round(12 / lookupAttributeNames.length);// 12 is full width in px

            $.each(lookupAttributeNames, function (index, attrName) {
                var attrLabel = entityMetaData.attributes[attrName].label || attrName;
                var attrValue = entity[attrName] == undefined ? '' : entity[attrName];
                items.push('<div class="col-md-' + width + '">'); // FIXME wtf?! dit kan natuurlijk niet
                items.push(attrLabel + ': <b>' + htmlEscape(attrValue) + '</b>');
                items.push('</div>');
            });
        }

        items.push('</div>');

        return items.join('');
    }

    function formatSelection(entity, refEntityMetaData, t) {
        var result;
        if (entity instanceof Array && entity.length) {
            $.each(entity, function (index, value) {
                result = value[refEntityMetaData.labelAttribute];
            });
        }
        else {
            result = entity[refEntityMetaData.labelAttribute];
        }

        return result;
    }

    function createSelect2($container, attribute, options) {
        var refEntityMetaData = restApi.get(attribute.refEntity.href, {expand: ['attributes']});
        var lookupAttrNames = refEntityMetaData.lookupAttributes;

        if (lookupAttrNames.length == 0) {
            lookupAttrNames = [refEntityMetaData.labelAttribute];
        }
        var lookupAttributes = getLookupAttributes(refEntityMetaData);
        var uniqueAttributes = getUniqueAttributes(refEntityMetaData);

        var width = options.width ? options.width : 'resolve';
        var $hiddenInput = $(':input[type=hidden]', $container)
            .not('[data-filter=xrefmref-operator]')
            .not('[data-filter=ignore]');

        $hiddenInput.data('labels', options.labels);

        $hiddenInput.select2({
            width: width,
            minimumInputLength: 1,
            multiple: (attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL_MREF'
            || attribute.fieldType === 'XREF' || attribute.fieldType === 'FILE' || attribute.fieldType === 'ONE_TO_MANY'),
            closeOnSelect: false,
            query: function (options) {
                var query = createQuery(lookupAttributes, options.term.match(/[^ ]+/g), false, true);
                if (query) {
                    restApi.getAsync('/api/v1/' + refEntityMetaData.name, {
                        q: {num: 1000, q: query}, sort: {
                            orders: [{
                                direction: 'ASC',
                                property: lookupAttrNames[0]
                            }]
                        }
                    }, function (data) {
                        options.callback({results: data.items, more: false});
                    });
                }
            },
            initSelection: function (element, callback) {
                //Only called when the input has a value
                var query = createQuery(uniqueAttributes, element.val().split(','), true, false);
                if (query) {
                    restApi.getAsync('/api/v1/' + refEntityMetaData.name, {q: {q: query}}, function (data) {
                        callback(data.items);
                    });
                }
            },
            formatResult: function (entity) {
                return formatResult(entity, refEntityMetaData, lookupAttrNames);
            },
            formatSelection: function (entity) {
                if ($('.select2-choices .select2-search-choice', $container).length > 0 && !$('.dropdown-toggle-container', $container).is(':visible')) {
                    var $select2Container = $('.select2-container.select2-container-multi', $container);
                    $container.addClass('input-group');
                    $container.closest('.xrefmrefsearch').find('.dropdown-toggle-container').show();
                }
                return formatSelection(entity, refEntityMetaData);
            },
            id: function (entity) {
                return entity[refEntityMetaData.idAttribute];
            },
            separator: ',',
            dropdownCssClass: 'molgenis-xrefmrefsearch'
        }).change(function (event) {
            var labels = $hiddenInput.data('labels');
            if (!labels) labels = [];

            if (event.added) {
                labels.push(event.added[refEntityMetaData.labelAttribute]);
            }

            if (event.removed) {
                labels = labels.filter(function (label) {
                    return label !== event.removed[refEntityMetaData.labelAttribute];
                });
            }

            $hiddenInput.data('labels', labels);
        });


        $hiddenInput.on('select2-removed', function (e) {
            if ($('.select2-choices .select2-search-choice', $container).length < 2) {
                $container.removeClass('input-group');
                $container.closest('.xrefmrefsearch').find('.dropdown-toggle-container').hide();
            }
        });
        $container.removeClass('input-group');
        $container.closest('.xrefmrefsearch').find('.dropdown-toggle-container').hide();

        if (!lookupAttrNames.length) {
            $container.append($('<label>lookup attribute is not defined.</label>'));
        }
    }

    function addQueryPartSelect($container, attribute, options) {
        var attrs = {};

        if (options.autofocus) {
            attrs.autofocus = options.autofocus;
        }

        if (options.isfilter) {
            var $operatorInput = $('<input type="hidden" data-filter="xrefmref-operator" value="' + options.operator + '" />');

            if (attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL_MREF' || attribute.fieldType === 'ONE_TO_MANY') { // TODO remove CATEGORICAL_MREF when it is rendered like CATEGORICAL is rendered for XREF
                var $dropdown = $('<div class="input-group-addon dropdown dropdown-toggle-container">');
                var orValue = 'OR&nbsp;&nbsp;';
                var andValue = 'AND';
                $dropdown.append($operatorInput);
                $dropdown.append($('<button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">' + (options.operator === 'AND' ? andValue : orValue) + ' <b class="caret"></button>'));
                $dropdown.append($('<ul class="dropdown-menu" role="menu">'
                    + '<li role="presentation"><a  role="menuitem" data-value="OR" tabindex="-1" href="#">' + orValue + '</a></li>'
                    + '<li role="presentation"><a  role="menuitem" data-value="AND" tabindex="-1" href="#">' + andValue + '</a></li>'
                    + '</ul>'));

                $.each($dropdown.find('.dropdown-menu li a'), function (index, element) {
                    $(element).click(function () {
                        var dataValue = $(this).attr('data-value');
                        $operatorInput.val(dataValue);
                        $dropdown.find('button:first').html((dataValue === 'AND' ? andValue : orValue) + ' <b class="caret"></b>');
                        $dropdown.find('button:first').val(dataValue);
                    });
                });

                $dropdown.find('div:first').remove();//This is a workaround FIX

                $container.addClass("select2-bootstrap-prepend");
                $container.prepend($dropdown);
            }
            else if (attribute.fieldType === 'FILE' || attribute.fieldType === 'XREF') {
                $operatorInput.val('OR');
                $container.append($('<div class="input-group-addon dropdown-toggle-container"><button class="btn btn-default" type="button" disabled>OR</button></div>'));
                $container.append($operatorInput);
            }
        }

        var element = createInput(attribute, attrs, options.values);

        $container.addClass("input-group select2-bootstrap-prepend");
        $container.append(element);
    }

    /**
     * Creates a mref or xref select2 component
     *
     * options =    The options to create the filter
     *                {
	 * 					attribute 	 ==> the meta data of the attribute
	 * 					operator ==> AND or OR or undefined
	 * 					values 	 ==> the values that are concatenate with an operator
	 *						1. example one: a AND b AND c
	 *						2. example two: a OR b OR c
	 * 					
	 * 				}
     */
    $.fn.xrefmrefsearch = function (options) {//mref or xref select2
        var container = this;
        var attributeUri = options.attributeUri ? options.attributeUri : options.attribute.href;
        restApi.getAsync(attributeUri, {
            attributes: ['refEntity', 'fieldType'],
            expand: ['refEntity']
        }, function (attribute) {
            addQueryPartSelect(container, attribute, options);
            createSelect2(container, attribute, options);
        });

        return container;
    };

}($, window.top.molgenis = window.top.molgenis || {}));