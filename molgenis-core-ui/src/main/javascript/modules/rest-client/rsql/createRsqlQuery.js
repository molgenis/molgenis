import $ from "jquery";

function toRsqlValue(value) {
    if (_.isString(value) === false || (value.indexOf('"') !== -1 || value.indexOf('\'') !== -1 || value.indexOf('(') !== -1 || value.indexOf(')') !== -1 || value.indexOf(';') !== -1
        || value.indexOf(',') !== -1 || value.indexOf('=') !== -1 || value.indexOf('!') !== -1 || value.indexOf('~') !== -1 || value.indexOf('<') !== -1
        || value.indexOf('>') !== -1 || value.indexOf(' ') !== -1)) {
        return '"' + encodeURIComponent(value) + '"';
    } else {
        return encodeURIComponent(value);
    }
}

export function createRsqlQuery(rules) {
    let rsql = '';

    for (let i = 0; i < rules.length; ++i) {
        let rule = rules[i];

        // simplify query
        while (rule.operator === 'NESTED' && rule.nestedRules.length === 1) {
            rule = rule.nestedRules[0];
        }

        switch (rule.operator) {
            case 'SEARCH':
                let field = rule.field !== undefined ? rule.field : '*';
                rsql += encodeURIComponent(field) + '=q=' + toRsqlValue(rule.value);
                break;
            case 'EQUALS':
                rsql += encodeURIComponent(rule.field) + '==' + toRsqlValue(rule.value);
                break;
            case 'IN':
                rsql += encodeURIComponent(rule.field) + '=in=' + '(' + $.map(rule.value, function (value) {
                        return toRsqlValue(value);
                    }).join(',') + ')';
                break;
            case 'LESS':
                rsql += encodeURIComponent(rule.field) + '=lt=' + toRsqlValue(rule.value);
                break;
            case 'LESS_EQUAL':
                rsql += encodeURIComponent(rule.field) + '=le=' + toRsqlValue(rule.value);
                break;
            case 'GREATER':
                rsql += encodeURIComponent(rule.field) + '=gt=' + toRsqlValue(rule.value);
                break;
            case 'GREATER_EQUAL':
                rsql += encodeURIComponent(rule.field) + '=ge=' + toRsqlValue(rule.value);
                break;
            case 'RANGE':
                rsql += encodeURIComponent(rule.field) + '=rng=' + '(' + toRsqlValue(rule.value[0]) + ',' + toRsqlValue(rule.value[1]) + ')';
                break;
            case 'LIKE':
                rsql += encodeURIComponent(rule.field) + '=like=' + toRsqlValue(rule.value);
                break;
            case 'NOT':
                rsql += encodeURIComponent(rule.field) + '!=' + toRsqlValue(rule.value);
                break;
            case 'AND':
                // ignore dangling AND rule
                if (i > 0 && i < rules.length - 1) {
                    rsql += ';';
                }
                break;
            case 'OR':
                // ignore dangling OR rule
                if (i > 0 && i < rules.length - 1) {
                    rsql += ',';
                }
                break;
            case 'NESTED':
                // ignore rule without nested rules
                if (rule.nestedRules.length > 0) {
                    rsql += '(' + createRsqlQuery(rule.nestedRules) + ')';
                }
                break;
            case 'SHOULD':
                throw 'unsupported query operator [' + rule.operator + ']';
            case 'DIS_MAX':
                throw 'unsupported query operator [' + rule.operator + ']';
            case 'FUZZY_MATCH':
                throw 'unsupported query operator [' + rule.operator + ']';
            default:
                throw 'unknown query operator [' + rule.operator + ']';
        }
    }
    return rsql;
}

export function createRsqlAggregateQuery(aggs) {
    let rsql = '';
    if (aggs.x) {
        rsql += 'x==' + toRsqlValue(aggs.x);
    }
    if (aggs.y) {
        if (rsql.length > 0) {
            rsql += ';';
        }
        rsql += 'y==' + toRsqlValue(aggs.y);
    }
    if (aggs.distinct) {
        if (rsql.length > 0) {
            rsql += ';';
        }
        rsql += 'distinct==' + toRsqlValue(aggs.distinct);
    }
    return rsql;
}