package org.molgenis.data.system;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.system.ImportRunMetaData;

@AutoValue
public class ImportRun extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{

    public static final String ENTITY_NAME = "ImportRun";

    private static final java.util.List<org.molgenis.util.ValueLabel> status_options;

    private String id;
    private Date startDate;
    private Date endDate;
    private String userName;
    private String status;
    private String message;
    private int progress;
    private String importedEntities;
    private String status_label;

    static {
        status_options = new java.util.ArrayList<org.molgenis.util.ValueLabel>();
        status_options.add(new org.molgenis.util.ValueLabel("RUNNING","RUNNING"));
        status_options.add(new org.molgenis.util.ValueLabel("FINISHED","FINISHED"));
        status_options.add(new org.molgenis.util.ValueLabel("FAILED","FAILED"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getImportedEntities() {
        return importedEntities;
    }

    public void setImportedEntities(String importedEntities) {
        this.importedEntities = importedEntities;
    }

    public String getStatusLabel()
    {
        return this.status_label;
    }
    /**
     * Status is enum. This method returns all available enum options.
     */
    public java.util.List<org.molgenis.util.ValueLabel> getStatusOptions()
    {
        return status_options;
    }



    @Override
    public Object get(String name)
    {
        name = name.toLowerCase();
        if (name.equals("id"))
            return getId();
        if (name.equals("startdate"))
            return getStartDate();
        if (name.equals("enddate"))
            return getEndDate();
        if (name.equals("username"))
            return getUserName();
        if (name.equals("status"))
            return getStatus();
        if(name.equals("status_label"))
            return getStatusLabel();
        if (name.equals("message"))
            return getMessage();
        if (name.equals("progress"))
            return getProgress();
        if (name.equals("importedentities"))
            return getImportedEntities();
        return null;
    }

    @Override
    public void set(Entity entity)
    {
        set(entity, true);
    }

    public void set(org.molgenis.data.Entity entity, boolean strict)
    {
        //set Id
        // query formal name, else lowercase name
        if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(strict) this.setId(entity.getString("id")); // setting null is not an option due to function overloading
        if( entity.getString("importrun_id") != null) this.setId(entity.getString("importrun_id"));
        else if( entity.getString("ImportRun_id") != null) this.setId(entity.getString("ImportRun_id"));
        //set StartDate
        // query formal name, else lowercase name
        if(entity.getTimestamp("startdate") != null) this.setStartDate(entity.getTimestamp("startdate"));
        else if(entity.getTimestamp("startDate") != null) this.setStartDate(entity.getTimestamp("startDate"));
        else if(strict) this.setStartDate(entity.getTimestamp("startdate")); // setting null is not an option due to function overloading
        if( entity.getTimestamp("importrun_startdate") != null) this.setStartDate(entity.getTimestamp("importrun_startdate"));
        else if( entity.getTimestamp("ImportRun_startDate") != null) this.setStartDate(entity.getTimestamp("ImportRun_startDate"));
        //set EndDate
        // query formal name, else lowercase name
        if(entity.getTimestamp("enddate") != null) this.setEndDate(entity.getTimestamp("enddate"));
        else if(entity.getTimestamp("endDate") != null) this.setEndDate(entity.getTimestamp("endDate"));
        else if(strict) this.setEndDate(entity.getTimestamp("enddate")); // setting null is not an option due to function overloading
        if( entity.getTimestamp("importrun_enddate") != null) this.setEndDate(entity.getTimestamp("importrun_enddate"));
        else if( entity.getTimestamp("ImportRun_endDate") != null) this.setEndDate(entity.getTimestamp("ImportRun_endDate"));
        //set UserName
        // query formal name, else lowercase name
        if(entity.getString("username") != null) this.setUserName(entity.getString("username"));
        else if(entity.getString("userName") != null) this.setUserName(entity.getString("userName"));
        else if(strict) this.setUserName(entity.getString("username")); // setting null is not an option due to function overloading
        if( entity.getString("importrun_username") != null) this.setUserName(entity.getString("importrun_username"));
        else if( entity.getString("ImportRun_userName") != null) this.setUserName(entity.getString("ImportRun_userName"));
        //set Status
        // query formal name, else lowercase name
        if(entity.getString("status") != null) this.setStatus(entity.getString("status"));
        else if(entity.getString("status") != null) this.setStatus(entity.getString("status"));
        else if(strict) this.setStatus(entity.getString("status")); // setting null is not an option due to function overloading
        if( entity.getString("importrun_status") != null) this.setStatus(entity.getString("importrun_status"));
        else if( entity.getString("ImportRun_status") != null) this.setStatus(entity.getString("ImportRun_status"));
        //set Message
        // query formal name, else lowercase name
        if(entity.getString("message") != null) this.setMessage(entity.getString("message"));
        else if(entity.getString("message") != null) this.setMessage(entity.getString("message"));
        else if(strict) this.setMessage(entity.getString("message")); // setting null is not an option due to function overloading
        if( entity.getString("importrun_message") != null) this.setMessage(entity.getString("importrun_message"));
        else if( entity.getString("ImportRun_message") != null) this.setMessage(entity.getString("ImportRun_message"));
        //set Progress
        // query formal name, else lowercase name
        if(entity.getInt("progress") != null) this.setProgress(entity.getInt("progress"));
        else if(entity.getInt("progress") != null) this.setProgress(entity.getInt("progress"));
        else if(strict) this.setProgress(entity.getInt("progress")); // setting null is not an option due to function overloading
        if( entity.getInt("importrun_progress") != null) this.setProgress(entity.getInt("importrun_progress"));
        else if( entity.getInt("ImportRun_progress") != null) this.setProgress(entity.getInt("ImportRun_progress"));
        //set ImportedEntities
        // query formal name, else lowercase name
        if(entity.getString("importedentities") != null) this.setImportedEntities(entity.getString("importedentities"));
        else if(entity.getString("importedEntities") != null) this.setImportedEntities(entity.getString("importedEntities"));
        else if(strict) this.setImportedEntities(entity.getString("importedentities")); // setting null is not an option due to function overloading
        if( entity.getString("importrun_importedentities") != null) this.setImportedEntities(entity.getString("importrun_importedentities"));
        else if( entity.getString("ImportRun_importedEntities") != null) this.setImportedEntities(entity.getString("ImportRun_importedEntities"));
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("ImportRun(");
        sb.append("id='" + getId()+"' ");
        sb.append("startDate='" + (getStartDate() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getStartDate()))+"' ");
        sb.append("endDate='" + (getEndDate() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getEndDate()))+"' ");
        sb.append("userName='" + getUserName()+"' ");
        sb.append("status='" + getStatus()+"' ");
        sb.append("message='" + getMessage()+"' ");
        sb.append("progress='" + getProgress()+"' ");
        sb.append("importedEntities='" + getImportedEntities()+"'");
        sb.append(");");
        return sb.toString();
    }

    @Override
    public String getIdValue()
    {
        return getId();
    }


    @Override
    public Iterable<String> getAttributeNames()
    {
        Set<String> attributeNames = new LinkedHashSet<String>();
        for (AttributeMetaData attr : new ImportRunMetaData().getAttributes())
        {
            attributeNames.add(attr.getName());
        }

        return attributeNames;
    }

    @Override
    public void set(String attributeName, Object value)
    {
        if("id".equalsIgnoreCase(attributeName)) {
            this.setId((String)value);
            return;
        }
        if("startDate".equalsIgnoreCase(attributeName)) {
            this.setStartDate((java.util.Date)value);
            return;
        }
        if("endDate".equalsIgnoreCase(attributeName)) {
            this.setEndDate((java.util.Date)value);
            return;
        }
        if("userName".equalsIgnoreCase(attributeName)) {
            this.setUserName((String)value);
            return;
        }
        if("status".equalsIgnoreCase(attributeName)) {
            this.setStatus((String)value);
            return;
        }
        if("message".equalsIgnoreCase(attributeName)) {
            this.setMessage((String)value);
            return;
        }
        if("progress".equalsIgnoreCase(attributeName)) {
            this.setProgress((Integer)value);
            return;
        }
        if("importedEntities".equalsIgnoreCase(attributeName)) {
            this.setImportedEntities((String)value);
            return;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        return result;
    }

    @Override
    public org.molgenis.data.EntityMetaData getEntityMetaData()
    {
        return new ImportRunMetaData();
    }
}
