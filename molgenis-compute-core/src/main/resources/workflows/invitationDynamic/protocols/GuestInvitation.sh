#input guest
#input party
#output decision

echo "Hello ${guest},"
echo "We invite you for our ${party}."

# To go or not to go?
if [ "$(($RANDOM%2))" -eq "1" ];
	then decision="yes";
	else decision="no";
fi;
