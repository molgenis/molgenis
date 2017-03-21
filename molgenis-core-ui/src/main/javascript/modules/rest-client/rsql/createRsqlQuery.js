import {encode} from "mdurl";

export function containsRsqlReservedCharacter(value) {
    return /["'();,=!~<> ]/.test(value);
}

/**
 * Escapes an rsql value by putting it between quotes.
 */
export function rsqlEscape(value) {
    const doubleQuotes = (value.match(/["]/g) || []).length
    const singleQuotes = (value.match(/[']/g) || []).length

    const quoteChar = (doubleQuotes >= singleQuotes) ? "'" : "\""
    return quoteChar + value.split(quoteChar).join("\\" + quoteChar) + quoteChar
}

export function toRsqlValue(value) {
    return containsRsqlReservedCharacter(value) ? rsqlEscape(value) : value;
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
                rsql += toRsqlValue(field) + '=q=' + toRsqlValue(rule.value);
                break;
            case 'EQUALS':
                rsql += toRsqlValue(rule.field) + '==' + toRsqlValue(rule.value);
                break;
            case 'IN':
                rsql += toRsqlValue(rule.field) + '=in=' + '(' + rule.value.map(toRsqlValue).join(',') + ')';
                break;
            case 'LESS':
                rsql += toRsqlValue(rule.field) + '=lt=' + toRsqlValue(rule.value);
                break;
            case 'LESS_EQUAL':
                rsql += toRsqlValue(rule.field) + '=le=' + toRsqlValue(rule.value);
                break;
            case 'GREATER':
                rsql += toRsqlValue(rule.field) + '=gt=' + toRsqlValue(rule.value);
                break;
            case 'GREATER_EQUAL':
                rsql += toRsqlValue(rule.field) + '=ge=' + toRsqlValue(rule.value);
                break;
            case 'RANGE':
                rsql += toRsqlValue(rule.field) + '=rng=' + '(' + toRsqlValue(rule.value[0]) + ',' + toRsqlValue(rule.value[1]) + ')';
                break;
            case 'LIKE':
                rsql += toRsqlValue(rule.field) + '=like=' + toRsqlValue(rule.value);
                break;
            case 'NOT':
                rsql += toRsqlValue(rule.field) + '!=' + toRsqlValue(rule.value);
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

/**
 * URLEncodes an rsql value, leaving as many rsql-relevant characters as possible unencoded
 */
export function encodeRsqlValue(str) {
    return encode(str, encode.componentChars + "=:,;\"'<>", false)
}