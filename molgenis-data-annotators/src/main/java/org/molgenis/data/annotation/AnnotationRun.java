package org.molgenis.data.annotation;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;

import javax.annotation.Nullable;

public class AnnotationRun extends AbstractEntity{

    public String id;
    public String user;
    public String status;
    public String message;
    public String entity;

    public static final String ID = "id";
    public static final String USER = "user";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String ENTITY = "entityName";

    @Override
    public EntityMetaData getEntityMetaData() {
        return AnnotationRunMetadata.META;
    }

    @Override
    public Iterable<String> getAttributeNames() {
        return Iterables.transform(getEntityMetaData().getAttributes(), new Function<AttributeMetaData, String>() {
            @Nullable
            @Override
            public String apply(@Nullable AttributeMetaData attributeMetaData) {
                return attributeMetaData.getName();
            }
        });
    }

    @Override
    public Object getIdValue() {
        return getId();
    }

    @Override
    public Object get(String attributeName) {
        if (attributeName.equals(ID)) return getId();
        if (attributeName.equals(USER)) return getUser();
        if (attributeName.equals(STATUS)) return getStatus();
        if (attributeName.equals(MESSAGE)) return getMessage();
        if (attributeName.equals(ENTITY)) return getEntity();
        return null;
    }

    @Override
    public void set(String attributeName, Object value) {
        if (ID.equals(attributeName))
        {
            this.setId((String) value);
            return;
        }
        if (USER.equals(attributeName))
        {
            this.setUser((String) value);
            return;
        }
        if (STATUS.equals(attributeName))
        {
            this.setStatus((String) value);
            return;
        }
        if (MESSAGE.equals(attributeName))
        {
            this.setMessage((String) value);
            return;
        }
        if (ENTITY.equals(attributeName))
        {
            this.setEntity((String) value);
            return;
        }
    }

    @Override
    public void set(Entity entity) {
        if (entity.getString(ID) != null)
            this.setId(entity.getString(ID));
        if (entity.getString(USER) != null) this.setUser(entity.getString(USER));
        if (entity.getString(STATUS) != null)
            this.setStatus(entity.getString(STATUS));
        if (entity.getString(MESSAGE) != null) this.setMessage(entity.getString(MESSAGE));
        if (entity.getString(ENTITY) != null)
            this.setEntity(entity.getString(ENTITY));
    }


    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
