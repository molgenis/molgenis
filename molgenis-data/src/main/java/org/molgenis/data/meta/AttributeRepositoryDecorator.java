package org.molgenis.data.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decorator for the attribute repository:
 * - filters requested entities based on the entity permissions of the current user.
 * - applies attribute metadata updates to the backend
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeRepositoryDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final DataService dataService;

	public AttributeRepositoryDecorator(Repository<Attribute> delegateRepository, DataService dataService)
	{
		super(delegateRepository);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void update(Attribute attr)
	{
		updateBackend(attr);
		delegate().update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		delegate().update(attrs.filter(attr ->
		{
			updateBackend(attr);
			return true;
		}));
	}

	/**
	 * Updates an attribute's representation in the backend for each concrete {@link EntityType} that
	 * has the {@link Attribute}.
	 *
	 * @param attr        current version of the attribute
	 * @param updatedAttr new version of the attribute
	 */
	private void updateAttributeInBackend(Attribute attr, Attribute updatedAttr)
	{
		MetaDataService meta = dataService.getMeta();
		meta.getConcreteChildren(attr.getEntity())
			.forEach(entityType -> meta.getBackend(entityType).updateAttribute(entityType, attr, updatedAttr));
	}

	private void updateBackend(Attribute attr)
	{
		Attribute currentAttr = findOneById(attr.getIdentifier());
		updateAttributeInBackend(currentAttr, attr);
	}
}
