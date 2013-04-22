#string dis
#list risk

nRisk=0
for r in $risk; do if [[ "yes"="$r" ]]; then ((nRisk++)); fi; done

echo "Fraction of samples with $dis risk:"
echo "scale=2;$nRisk/${#risk[*]}" | bc
