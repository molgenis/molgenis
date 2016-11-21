package org.molgenis.data.annotation.core.entity.impl.gavin;

public class GavinEntry
{
	public String gene;
	public Category category;
	public String chromosome;
	public int start;
	public int end;
	public int NrOfPopulationVariants;
	public int NrOfPathogenicVariants;
	public int NrOfOverlappingVariants;
	public int NrOfFilteredPopVariants;
	public Double PathoMAFThreshold;
	public Double PopImpactHighPerc;
	public Double PopImpactModeratePerc;
	public Double PopImpactLowPerc;
	public Double PopImpactModifierPerc;
	public Double PathoImpactHighPerc;
	public Double PathoImpactModeratePerc;
	public Double PathoImpactLowPerc;
	public Double PathoImpactModifierPerc;
	public Double PopImpactHighEq;
	public Double PopImpactModerateEq;
	public Double PopImpactLowEq;
	public Double PopImpactModifierEq;
	public Integer NrOfCADDScoredPopulationVars;
	public Integer NrOfCADDScoredPathogenicVars;
	public Double MeanPopulationCADDScore;
	public Double MeanPathogenicCADDScore;
	public Double MeanDifference;
	public Double UTestPvalue;
	public Double Sens95thPerCADDThreshold;
	public Double Spec95thPerCADDThreshold;

	public enum Category
	{
		N1, N2, T1, T2, I1, I2, I3, C1, C2, C3, C4, C5
	}

	public GavinEntry(String lineFromFile) throws Exception
	{
		String[] split = lineFromFile.split("\t", -1);
		if (split.length != 30)
		{
			throw new Exception("not 30 elements");
		}

		this.gene = split[0];
		this.category = Category.valueOf(split[1]);
		this.chromosome = split[2];
		this.start = Integer.parseInt(split[3]);
		this.end = Integer.parseInt(split[4]);
		this.NrOfPopulationVariants = Integer.parseInt(split[5]);
		this.NrOfPathogenicVariants = Integer.parseInt(split[6]);
		this.NrOfOverlappingVariants = Integer.parseInt(split[7]);
		this.NrOfFilteredPopVariants = Integer.parseInt(split[8]);
		this.PathoMAFThreshold = split[9].isEmpty() ? null : Double.parseDouble(split[9]);

		this.PopImpactHighPerc = split[10].isEmpty() ? null : Double.parseDouble(split[10]);
		this.PopImpactModeratePerc = split[11].isEmpty() ? null : Double.parseDouble(split[11]);
		this.PopImpactLowPerc = split[12].isEmpty() ? null : Double.parseDouble(split[12]);
		this.PopImpactModifierPerc = split[13].isEmpty() ? null : Double.parseDouble(split[13]);

		this.PathoImpactHighPerc = split[14].isEmpty() ? null : Double.parseDouble(split[14]);
		this.PathoImpactModeratePerc = split[15].isEmpty() ? null : Double.parseDouble(split[15]);
		this.PathoImpactLowPerc = split[16].isEmpty() ? null : Double.parseDouble(split[16]);
		this.PathoImpactModifierPerc = split[17].isEmpty() ? null : Double.parseDouble(split[17]);

		this.PopImpactHighEq = split[18].isEmpty() ? null : Double.parseDouble(split[18]);
		this.PopImpactModerateEq = split[19].isEmpty() ? null : Double.parseDouble(split[19]);
		this.PopImpactLowEq = split[20].isEmpty() ? null : Double.parseDouble(split[20]);
		this.PopImpactModifierEq = split[21].isEmpty() ? null : Double.parseDouble(split[21]);

		this.NrOfCADDScoredPopulationVars = split[22].isEmpty() ? null : Integer.parseInt(split[22]);
		this.NrOfCADDScoredPathogenicVars = split[23].isEmpty() ? null : Integer.parseInt(split[23]);

		this.MeanPopulationCADDScore = split[24].isEmpty() ? null : Double.parseDouble(split[24]);
		this.MeanPathogenicCADDScore = split[25].isEmpty() ? null : Double.parseDouble(split[25]);
		this.MeanDifference = split[26].isEmpty() ? null : Double.parseDouble(split[26]);
		this.UTestPvalue = split[27].isEmpty() ? null : Double.parseDouble(split[27]);
		this.Sens95thPerCADDThreshold = split[28].isEmpty() ? null : Double.parseDouble(split[28]);
		this.Spec95thPerCADDThreshold = split[29].isEmpty() ? null : Double.parseDouble(split[29]);
	}

}
