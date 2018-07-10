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
| plus     | Number     | `$('height').plus(100).value()`            | height + 100  |
| pow      | Number     | `$('height').pow(100).value()`          | height ^ 100  |
| times    | Number     | `$('height').times(100).value()`           | height * 100  |
| div      | Number     | `$('height').div(100).value()`             | height / 100  |
| gt       | Number     | `$('height').gt(100).value()`              | height > 100  |
| lt       | Number     | `$('height').lt(100).value()`             | height < 100  |
| ge       | Number     | `$('height').ge(100).value()`              | height >= 100 |
| le       | Number     | `$('height').le(100).value()`              | height <= 100 |

### Binary operations
| Operator | Parameters | Example                                  | Description            |
|----------|------------|------------------------------------------|------------------------|
| eq       | Number     | `$('height').eq(100).value()`              | height === 100         |
| matches  | Regex      | `$('name').matches(/^[a-z0-9]+$/i).value()`| name is alphanumerical |
| isNull   | -          | `$('height').isNull().value()`             | height === null        |
| not      | -          | `$('hasEars').not().value()`               | !hasEars               |
| or       | Expression | `$('male').or($('female')).value()`        | male || female         |
| and      | Expression | `$('female').and($('pregnant')).value()`   | female && pregnant     |

### Unit operations
| Operator | Parameters | Example                                    | Description                                             |
|----------|------------|--------------------------------------------|---------------------------------------------------------|
| unit     | Unit       | `$('height').unit('cm')`                     | Sets the current value unit to cm                       |
| toUnit   | Unit       | `$('height').unit('m').toUnit('cm').value()` | Converts the current value based on the change in units |

### Other
| Operator | Parameters | Example                                  | Description                                                           |
|----------|------------|------------------------------------------|-----------------------------------------------------------------------|
| age      | -          | `$('dateOfBirth').age().value()`           | Returns the age based on the date of birth and the current year       |
| map      | Object     | `$('data').map({0:1, 1:2}).value()`       | Maps categories to each other                                         |
| group    | Array      | `$('age').group([18, 35, 50, 75]).value()` | Produces left-inclusive ranges with the given boundaries, i.e. `(-∞, 18), [18, 35), [35, 50), [50, 75), [75, +∞)` and then returns the interval that the attribute value is in, i.el '18-35'                |
| attr     | String     | `$('person').attr('name').value()`      | Returns the value of an attribute of a reference

## Terminal operations
| Operator | Parameters | Example             | Description      |
|----------|------------|---------------------|------------------|
| value    | -          | `$('height').value()` | JavaScript value |

# Special case: reference types
If an attribute is a reference type (MREF, XREF, CATEGORICAL, CATEGORICAL_MREF) you can use the 'attr' operation, to access the values of different columns in the row being referenced. E.g. `$('cookie').attr('name').value()` gives you the value of the name column inside the table being referenced by the cookie column. See below for a more detailed example.

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
$('cookie').attr('id').value() // results in '1'
$('cookie').attr('name').value() // results in 'Chocolate chip'
$('cookie').attr('tastiness').value() // results in '9/10'
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

$('cookies').map(function (cookie) {
    // Pick one of the following
    return cookie.value()                   // results in ['1', '2', '3']
    return cookie.attr('id').value()        // results in ['1', '2', '3']
    return cookie.attr('name').value()      // results in ['Chocolate chip', 'Strawberry cookie', 'Banana cookie']
    return cookie.attr('tastiness').value() // results in ['9/10', '10/10', '7/10']
}).value()
```
