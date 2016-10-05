package org.molgenis.genetics.diag.genenetwork.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class GeneNetworkScore extends StaticEntity
{
	public String ensemblId;
	public String hpo;
	public Double score;
	public String id;
	public String hugo;

	public GeneNetworkScore(Entity entity)
	{
		super(entity);
	}

	public GeneNetworkScore(EntityType entityType)
	{
		super(entityType);
	}

	public GeneNetworkScore(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getEnsemblId()
	{
		return ensemblId;
	}

	public void setEnsemblId(String ensemblId)
	{
		this.ensemblId = ensemblId;
	}

	public String getHpo()
	{
		return hpo;
	}

	public void setHpo(String hpo)
	{
		this.hpo = hpo;
	}

	public Double getScore()
	{
		return score;
	}

	public void setScore(Double score)
	{
		this.score = score;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getHugo()
	{
		return hugo;
	}

	public void setHugo(String hugo)
	{
		this.hugo = hugo;
	}
}
