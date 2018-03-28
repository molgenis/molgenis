# Expressions

## Introduction
For some entity type attributes an expression can be set that determines whether or not a condition is true based on the 
attribute values e.g. to determine whether values are valid. The expression format is based on the 
[Magma JavaScript API](http://wiki.obiba.org/display/OPALDOC/Magma+Javascript+API).

### Examples
The following expression returns true when the value of attribute 'myAttributeName' of an entity only contains
alphanumberic characters: 
```js
$('myStringAttributeName').matches(/^[a-z0-9]+$/i).value()
```
In this example:
- '$(...)' is the selector
- 'myStringAttributeName' is an attribute name
- 'matches' is a chaining operation
- 'value' is a terminal operation

Chaining operations allows you to express complex expressions:
```js
$('myIntAttributeName').gt(3).and($('myIntAttributeName').lt(6)).value()
```

## Chaining operations

### Numerical operations
| Operator | Parameters | Example                                  | Description   |
|----------|------------|------------------------------------------|---------------|
| plus     | Number     | $('height').plus(100)                    | height + 100  |
| pow      | Number     | $('height').pow(100)                     | height ^ 100  |
| times    | Number     | $('height').times(100                    | height * 100  |
| div      | Number     | $('height').div(100)                     | height / 100  |
| gt       | Number     | $('height').gt(100).value()              | height > 100  |
| lt       | Number     | $('height').lt(100).value()              | height < 100  |
| ge       | Number     | $('height').ge(100).value()              | height >= 100 |
| le       | Number     | $('height').le(100).value()              | height <= 100 |

### Binary operations
| Operator | Parameters | Example                                  | Description            |
|----------|------------|------------------------------------------|------------------------|
| eq       | Number     | $('height').eq(100).value()              | height === 100         |
| matches  | Regex      | $('name').matches(/^[a-z0-9]+$/i).value()| name is alphanumerical |
| isNull   | -          | $('height').isNull().value()             | height === null        |
| not      | -          | $('hasEars').not().value()               | !hasEars               |
| or       | Expression | $('male').or($('female')).value()        | male || female         |
| and      | Expression | $('female').and($('pregnant')).value()   | female && pregnant     |

### Unit operations
| Operator | Parameters | Example                                  | Description                                             |
|----------|------------|------------------------------------------|---------------------------------------------------------|
| unit     | Unit       | $('height').unit('cm')                   | Sets the current value unit to cm                       |
| toUnit   | Unit       | $('height').unit('m').toUnit('cm')       | Converts the current value based on the change in units |

### Other
| Operator | Parameters | Example                                  | Description                                                                                |
|----------|------------|------------------------------------------|--------------------------------------------------------------------------------------------|
| age      | -          | $('dateOfBirth').age()                   | Returns the age based on the date of birth and the current year                            |
| map      | Object     | $('data').map({0:1, 1:2}).value()        | Maps categories to eachother                                                               |
| group    | Array      | $('age').group([18, 35, 50, 75]).value() | Produces ranges which are left inclusive, (-∞, 18), [18, 35), [35, 50), [50, 75), [75, +∞)|                                                                |

## Terminal operations
| Operator | Parameters | Example             | Description      |
|----------|------------|---------------------|------------------|
| value    | -          | $('Height').value() | JavaScript value |

# Special case: reference types
If an attribute is a reference type (MREF, XREF, CATEGORICAL, CATEGORICAL_MREF) you can use dot notation, to access the values of different columns in the row being referenced. E.g. `$('cookie.name').value()` gives you the value of the name column inside the table being referenced by the cookie column. See below for a more detailed example.

Imagine __table A__ referencing __table B__. 

__Table A__ has 2 columns: id, cookie.
__Table B__ has 3 columns: id, name, tastiness.

The _cookie_ column in __table A__ references __table B__.  

__Table A__

| id | cookie |
|----|--------|
| A  | 1      |

__Table B__

| id | name | tastiness |
|----|--------|-----------|
| 1  | Chocolate chip | 9/10 |

Expressions allow you to do the following

```js
// Expressions are based on table A

$('cookie').value() // results in '1'
$('cookie.id').value() // results in '1'
$('cookie.name').value() // results in 'Chocolate chip'
$('cookie.tastiness').value() // results in '9/10'
```

In the following case, we have Table A which has multiple references to Table B e.g. an MREF

__Table A__

| id | cookie |
|----|--------|
| A  | 1,2,3  |

__Table B__

| id | name | tastiness |
|----|--------|-----------|
| 1  | Chocolate chip | 9/10 |
| 2  | Strawberry cookie | 10/10 |
| 3  | Banana cookie | 7/10 |

```js
// Expressions are based on table A

var cookies = []
$('cookies').map(function (cookie) {
    // You should use one of the following
    cookies.push(cookie.value()) // results in ['1', '2', '3']
    cookies.push(cookie.val.id) // results in ['1', '2', '3']
    cookies.push(cookie.val.name) // results in ['Chocolate chip', 'Strawberry cookie', 'Banana cookie']
    cookies.push(cookie.val.tastiness) // results in ['9/10', '10/10', '7/10']
});
cookies
```
