#string group
#string organizer
#list guests

echo "Dear ${organizer},"
echo "Please organize activities for the party '${group}' group."
echo "List of your guests:"
<#list guests as g>
echo "* ${g}"
</#list>