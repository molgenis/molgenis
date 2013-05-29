#list guest
#input party
#list decision
#input organizer

echo "Dear ${organizer},"
echo "Please organize activities for the party '${party}' group."
echo "List of your guests:"

for ((i = 0; i < ${#guest[@]}; i++))
do
    echo "${guest[$i]} will be present: ${decision[$i]}"
done