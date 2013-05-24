#input round
#input payout
#output profit

symbols=(@ $ ^)

s1=${symbols[$(($RANDOM%3))]}
s2=${symbols[$(($RANDOM%3))]}
s3=${symbols[$(($RANDOM%3))]}

if [ "$s1" == "$s2" -a "$s1" == "$s3" ];
	then profit=${payout};
	else profit=0;
fi;

echo "Round $round: ($s1 $s2 $s3) --> profit = $profit"