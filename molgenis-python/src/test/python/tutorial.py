import molgenis

session = molgenis.Session("https://molgenis56.target.rug.nl/api/")
dir(session)
session.get("ASE")
print(session.get("ASE", q=[{"field":"SNP_ID", "operator":"EQUALS", "value":"rs12460890"}]))
samples = session.get("SampleAse", q=[{"field":"SNP_ID", "operator":"EQUALS", "value":"rs12460890"}])
print(samples)
for sample in samples:
    print("{Ref_Counts:5} {Alt_Counts:5}".format(**sample))
import matplotlib.pyplot as plt
plt.scatter([sample["Ref_Counts"] for sample in samples], [sample["Alt_Counts"] for sample in samples])
plt.xlim([0, 5000])
plt.ylim([0, 5000])
plt.xlabel("Reference Allele")
plt.ylabel("Alternative Allele")
plt.title("Allele-Specific Expression for rs12460890")
plt.plot([0, 5000], [0, 5000])
plt.show()