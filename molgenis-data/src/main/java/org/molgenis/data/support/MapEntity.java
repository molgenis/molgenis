package org.molgenis.data.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

/**
 * Simple Entity implementation based on a Map
 */
public class MapEntity extends AbstractEntity
{
	private static final long serialVersionUID = -8283375007931769373L;
    private EntityMetaData entityMetaData;
    private Map<String, Object> values = new CaseInsensitiveLinkedHashMap<Object>();
	private String idAttributeName = null;

	public MapEntity()
	{
	}

	public MapEntity(Entity other)
	{
		set(other);
	}

	public MapEntity(String idAttributeName)
	{
		this.idAttributeName = idAttributeName;
	}

	public MapEntity(Map<String, Object> values)
	{
		this.values = values;
	}

	public MapEntity(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

    public MapEntity(EntityMetaData metaData)
    {
        this.entityMetaData = metaData;
        this.idAttributeName = entityMetaData.getIdAttribute().getName();
    }

	@Override
	public Object get(String attributeName)
	{
		return values.get(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity other, boolean strict)
	{
		for (String attributeName : other.getAttributeNames())
		{
			set(attributeName, other.get(attributeName));
		}
	}

	@Override
	public Object getIdValue()
	{
		if (getIdAttributeName() == null)
		{
			return null;
		}

		return get(getIdAttributeName());
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	public String getIdAttributeName()
	{
		return idAttributeName;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
        if(entityMetaData != null){
            return Iterables.transform(entityMetaData.getAttributes(), new Function<AttributeMetaData, String>() {
                @Override
                public String apply(AttributeMetaData input) {
                    return input.getName();
                }
            });
        }
		return values.keySet();
	}

    @Override
    public List<String> getLabelAttributeNames()
    {
        return Collections.singletonList(entityMetaData.getLabelAttribute().getName());
    }

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

}
