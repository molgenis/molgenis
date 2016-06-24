package org.molgenis.data.annotation.datastructures;

/**
 * Created by jvelde on 1/30/14.
 */
public class Variant extends Locus
{

	// 1 78392446 rs1166698 G A . PASS AC=135;AN=240;GTC=0,105,15 4.442499 23.7

	String id;
	String ref;
	String alt;
	String info;

	public Variant(String chrom, Long pos, String id, String ref, String alt, String info)
	{
		super(chrom, pos);
		this.id = id;
		this.ref = ref;
		this.alt = alt;
		this.info = info;
	}

	public String getId()
	{
		return id;
	}

	public String getRef()
	{
		return ref;
	}

	public String getAlt()
	{
		return alt;
	}

	public String getInfo()
	{
		return info;
	}

	@Override
	public String toString()
	{
		return "Variant{" + "id='" + id + '\'' + ", ref='" + ref + '\'' + ", alt='" + alt + '\'' + ", info='" + info
				+ '\'' + '}';
	}
}
