package org.molgenis.annotation.cmd.exception;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.exception.AnnotationException;
import org.molgenis.data.vcf.model.VcfAttributes;

import static org.molgenis.data.annotation.core.effects.EffectsMetaData.VARIANT;

public class CmdLineAnnotationException extends AnnotationException
{
	public CmdLineAnnotationException(AnnotationException ae)
	{
		super(ae);
	}

	@Override
	public String getMessage()
	{
		Entity variantEntity = getFailedEntity();
		String message = "Annotation failed at variant " + getEntityNumber();

		if (variantEntity != null)
		{
			if (variantEntity.getEntity(VARIANT) != null)
			{
				variantEntity = variantEntity.getEntity(VARIANT);
			}

			message += " with [" + "CHROM=" + variantEntity.get(VcfAttributes.CHROM) + ", " + "POS=" + variantEntity
					.get(VcfAttributes.POS) + ", " + "REF=" + variantEntity.get(VcfAttributes.REF) + ", " + "ALT="
					+ variantEntity.get(VcfAttributes.ALT) + "]";
		}

		message += " Cause: " + super.getCause();

		return message;
	}
}
