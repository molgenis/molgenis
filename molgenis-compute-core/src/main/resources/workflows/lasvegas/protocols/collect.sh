#input payout
#list profit

sum=0
for i in "${profit[@]}"
do
  sum=$(echo "$sum + ${profit[$i]}" | bc)
done

echo "RULES OF THE GAME"
echo "Each round costs 1 coin"
echo "Each time you win, you get ${payout}"

echo "You've paid: ${#profit[@]} coins"
echo "You've won: $sum"