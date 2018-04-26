/**
 * RSQL grammar, producing a simplified syntax tree
 * The backslashes are doubly escaped cause the string is inlined as a template.
 */
export default `
or = head:and tail:("," and)* {
  if(tail.length == 0) return head;
  var result = [head], i;
  for (i = 0; i < tail.length; i++) {
  result.push(tail[i][1]);
  }
  return { operator: "OR", operands: result};
}

and = head:constraint tail:(";" constraint)* {
  if(tail.length == 0) return head;
  var result = [head], i;
  for (i = 0; i < tail.length; i++) {
  result.push(tail[i][1]);
  }
  return { operator: "AND", operands: result};
}

constraint = group / comparison
group = "(" o:or ")" { return o;}

comparison = s:selector c:comparisonop a:arguments { 
	return {selector: s, comparison: c, arguments: a} 
}

selector = unreservedstr

comparisonop  = $compfiql / $compalt
compfiql      = ( "=" alpha* / "!" ) "="
compalt       = ( ">" / "<" ) "="

alpha = [a-z] / [A-Z]

arguments = "(" _ head:value _ tail:("," _ value _)* ")"  {
var result = [head], i;
for (i = 0; i < tail.length; i++) {
result.push(tail[i][2]);
        }
return result;} / value

value = unreservedstr / doublequoted / singlequoted

unreservedstr = $unreserved+
singlequoted = [\\'] v:(escaped / [^'\\\\])* [\\'] {return v.join("")}
doublequoted = [\\"] v:(escaped / [^"\\\\])* [\\"] {return v.join("")}

reserved = $["'();,=!~<>]
unreserved = $[^"'();,=!~<> ]
escaped = "\\\\" c:allchars { return c; }
allchars = $.
_ "whitespace" = [ \\t]*
`