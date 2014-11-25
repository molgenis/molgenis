var queryString = "";
var operators= {};
operators['AND'] = ";";
operators['OR'] = ",";
operators['GREATER_EQUAL'] = ">=";
operators['LESS_EQUAL'] = "<=";
operators['GREATER'] = ">";
operators['LESS'] = "<";
operators['EQUALS'] = "==";

var nestedRuleToString = function(rule){
    if(rule.operator === "NESTED"){
        queryString += "("
        for(nestedRule in rule.nestedRules){
            nestedRuleToString(rule.nestedRules[nestedRule]);
        }
        queryString += ")"
    }
    else{
        if(rule.field !== undefined) queryString += rule.field;
        if(rule.operator !== undefined) queryString += operatorStringToOperator(rule.operator);
        if(rule.value !== undefined) queryString += rule.value;
    }
}

var operatorStringToOperator = function(operatorString){
    var operator = "";
    operator = operators[operatorString];
    if(operator === undefined) operator = operatorString;
    return operator;
}

var queryObjectToQueryString = function(queryObject){
    if(queryObject.length > 0){
        for(nestedRule in queryObject){
            nestedRuleToString(queryObject[nestedRule]);
        }
    }
}
