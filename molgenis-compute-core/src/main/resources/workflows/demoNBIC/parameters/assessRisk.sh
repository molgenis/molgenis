#input sample
#input glucose
#output risk

if ((10 < $glucose));
  then risk+=("yes");
else risk+=("no"); fi
