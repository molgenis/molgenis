# Using simple expressions

## Introduction
For some entity type attributes an expression can be set that determines whether or not a condition is true based on the
attribute values to determine whether values are valid, visible or required.

### Examples
The following expression returns true when the value of attribute 'myAttributeName' of an entity only contains
alphanumberic characters:
```js
regex('^[a-zA-Z0-9]+$',{myStringAttributeName})
```
In this example:
- `{myStringAttributeName}` looks up the value of the `myStringAttributeName` variable
- `regex` is a function

You can combine expressions to perform more complex logic:
```
{myIntAttributeName} > 3 and {myIntAttributeName} < 6
```
### Reference
See [molgenis/molgenis-expressions](https://github.com/molgenis/molgenis-expressions#readme) for a
complete description of the syntax.