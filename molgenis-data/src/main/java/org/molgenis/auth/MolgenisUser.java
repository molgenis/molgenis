package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class MolgenisUser  extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{
    public static final String ENTITY_NAME = "MolgenisUser";
    public static final String USERNAME = "username";
    public static final String SUPERUSER = "superuser";
    public static final String PASSWORD_ = "password_";
    public static final String EMAIL = "email";
    public static final String GOOGLEACCOUNTID = "googleaccountid";
    public static final String ACTIVATIONCODE = "activationcode";
    public static final String ACTIVE = "active";

    String id;
    String username;
    String password;
    String activationCode;
    Boolean active;
    Boolean superuser;
    String firstName;
    String middleNames;
    String lastName;
    String title;
    String affiliation;
    String department;
    String role;
    String address;
    String phone;
    String email;
    String fax;
    String tollFreePhone;
    String city;
    String country;
    Boolean changePassword;
    String languageCode;
    String googleAccountId;

    public static String getEntityName() {
        return ENTITY_NAME;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isSuperuser() {
        return superuser;
    }

    public void setSuperuser(Boolean superuser) {
        this.superuser = superuser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getTollFreePhone() {
        return tollFreePhone;
    }

    public void setTollFreePhone(String tollFreePhone) {
        this.tollFreePhone = tollFreePhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean isChangePassword() {
        return changePassword;
    }

    public void setChangePassword(Boolean changePassword) {
        this.changePassword = changePassword;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getGoogleAccountId() {
        return googleAccountId;
    }

    public void setGoogleAccountId(String googleAccountId) {
        this.googleAccountId = googleAccountId;
    }

    @Override
    public Object get(String name)
    {
        name = name.toLowerCase();
        if (name.equals("id"))
            return getId();
        if (name.equals(USERNAME))
            return getUsername();
        if (name.equals(PASSWORD_))
            return getPassword();
        if (name.equals(ACTIVATIONCODE))
            return getActivationCode();
        if (name.equals(ACTIVE))
            return isActive();
        if (name.equals(SUPERUSER))
            return isSuperuser();
        if (name.equals("firstname"))
            return getFirstName();
        if (name.equals("middlenames"))
            return getMiddleNames();
        if (name.equals("lastname"))
            return getLastName();
        if (name.equals("title"))
            return getTitle();
        if (name.equals("affiliation"))
            return getAffiliation();
        if (name.equals("department"))
            return getDepartment();
        if (name.equals("role"))
            return getRole();
        if (name.equals("address"))
            return getAddress();
        if (name.equals("phone"))
            return getPhone();
        if (name.equals(EMAIL))
            return getEmail();
        if (name.equals("fax"))
            return getFax();
        if (name.equals("tollfreephone"))
            return getTollFreePhone();
        if (name.equals("city"))
            return getCity();
        if (name.equals("country"))
            return getCountry();
        if (name.equals("changepassword"))
            return isChangePassword();
        if (name.equals("languagecode"))
            return getLanguageCode();
        if (name.equals(GOOGLEACCOUNTID))
            return getGoogleAccountId();
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
        if( entity.getString("molgenisuser_id") != null) this.setId(entity.getString("molgenisuser_id"));
        else if( entity.getString("MolgenisUser_id") != null) this.setId(entity.getString("MolgenisUser_id"));
        //set Username
        // query formal name, else lowercase name
        if(entity.getString("username") != null) this.setUsername(entity.getString("username"));
        else if(entity.getString("username") != null) this.setUsername(entity.getString("username"));
        else if(strict) this.setUsername(entity.getString("username")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_username") != null) this.setUsername(entity.getString("molgenisuser_username"));
        else if( entity.getString("MolgenisUser_username") != null) this.setUsername(entity.getString("MolgenisUser_username"));
        //set Password
        // query formal name, else lowercase name
        if(entity.getString("password_") != null) this.setPassword(entity.getString("password_"));
        else if(entity.getString("password_") != null) this.setPassword(entity.getString("password_"));
        else if(strict) this.setPassword(entity.getString("password_")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_password_") != null) this.setPassword(entity.getString("molgenisuser_password_"));
        else if( entity.getString("MolgenisUser_password_") != null) this.setPassword(entity.getString("MolgenisUser_password_"));
        //set ActivationCode
        // query formal name, else lowercase name
        if(entity.getString("activationcode") != null) this.setActivationCode(entity.getString("activationcode"));
        else if(entity.getString("activationCode") != null) this.setActivationCode(entity.getString("activationCode"));
        else if(strict) this.setActivationCode(entity.getString("activationcode")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_activationcode") != null) this.setActivationCode(entity.getString("molgenisuser_activationcode"));
        else if( entity.getString("MolgenisUser_activationCode") != null) this.setActivationCode(entity.getString("MolgenisUser_activationCode"));
        //set Active
        // query formal name, else lowercase name
        if(entity.getBoolean("active") != null) this.setActive(entity.getBoolean("active"));
        else if(entity.getBoolean("active") != null) this.setActive(entity.getBoolean("active"));
        else if(strict) this.setActive(entity.getBoolean("active")); // setting null is not an option due to function overloading
        if( entity.getBoolean("molgenisuser_active") != null) this.setActive(entity.getBoolean("molgenisuser_active"));
        else if( entity.getBoolean("MolgenisUser_active") != null) this.setActive(entity.getBoolean("MolgenisUser_active"));
        //set Superuser
        // query formal name, else lowercase name
        if(entity.getBoolean("superuser") != null) this.setSuperuser(entity.getBoolean("superuser"));
        else if(entity.getBoolean("superuser") != null) this.setSuperuser(entity.getBoolean("superuser"));
        else if(strict) this.setSuperuser(entity.getBoolean("superuser")); // setting null is not an option due to function overloading
        if( entity.getBoolean("molgenisuser_superuser") != null) this.setSuperuser(entity.getBoolean("molgenisuser_superuser"));
        else if( entity.getBoolean("MolgenisUser_superuser") != null) this.setSuperuser(entity.getBoolean("MolgenisUser_superuser"));
        //set FirstName
        // query formal name, else lowercase name
        if(entity.getString("firstname") != null) this.setFirstName(entity.getString("firstname"));
        else if(entity.getString("FirstName") != null) this.setFirstName(entity.getString("FirstName"));
        else if(strict) this.setFirstName(entity.getString("firstname")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_firstname") != null) this.setFirstName(entity.getString("molgenisuser_firstname"));
        else if( entity.getString("MolgenisUser_FirstName") != null) this.setFirstName(entity.getString("MolgenisUser_FirstName"));
        //set MiddleNames
        // query formal name, else lowercase name
        if(entity.getString("middlenames") != null) this.setMiddleNames(entity.getString("middlenames"));
        else if(entity.getString("MiddleNames") != null) this.setMiddleNames(entity.getString("MiddleNames"));
        else if(strict) this.setMiddleNames(entity.getString("middlenames")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_middlenames") != null) this.setMiddleNames(entity.getString("molgenisuser_middlenames"));
        else if( entity.getString("MolgenisUser_MiddleNames") != null) this.setMiddleNames(entity.getString("MolgenisUser_MiddleNames"));
        //set LastName
        // query formal name, else lowercase name
        if(entity.getString("lastname") != null) this.setLastName(entity.getString("lastname"));
        else if(entity.getString("LastName") != null) this.setLastName(entity.getString("LastName"));
        else if(strict) this.setLastName(entity.getString("lastname")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_lastname") != null) this.setLastName(entity.getString("molgenisuser_lastname"));
        else if( entity.getString("MolgenisUser_LastName") != null) this.setLastName(entity.getString("MolgenisUser_LastName"));
        //set Title
        // query formal name, else lowercase name
        if(entity.getString("title") != null) this.setTitle(entity.getString("title"));
        else if(entity.getString("Title") != null) this.setTitle(entity.getString("Title"));
        else if(strict) this.setTitle(entity.getString("title")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_title") != null) this.setTitle(entity.getString("molgenisuser_title"));
        else if( entity.getString("MolgenisUser_Title") != null) this.setTitle(entity.getString("MolgenisUser_Title"));
        //set Affiliation
        // query formal name, else lowercase name
        if(entity.getString("affiliation") != null) this.setAffiliation(entity.getString("affiliation"));
        else if(entity.getString("Affiliation") != null) this.setAffiliation(entity.getString("Affiliation"));
        else if(strict) this.setAffiliation(entity.getString("affiliation")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_affiliation") != null) this.setAffiliation(entity.getString("molgenisuser_affiliation"));
        else if( entity.getString("MolgenisUser_Affiliation") != null) this.setAffiliation(entity.getString("MolgenisUser_Affiliation"));
        //set Department
        // query formal name, else lowercase name
        if(entity.getString("department") != null) this.setDepartment(entity.getString("department"));
        else if(entity.getString("Department") != null) this.setDepartment(entity.getString("Department"));
        else if(strict) this.setDepartment(entity.getString("department")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_department") != null) this.setDepartment(entity.getString("molgenisuser_department"));
        else if( entity.getString("MolgenisUser_Department") != null) this.setDepartment(entity.getString("MolgenisUser_Department"));
        //set Role
        // query formal name, else lowercase name
        if(entity.getString("role") != null) this.setRole(entity.getString("role"));
        else if(entity.getString("Role") != null) this.setRole(entity.getString("Role"));
        else if(strict) this.setRole(entity.getString("role")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_role") != null) this.setRole(entity.getString("molgenisuser_role"));
        else if( entity.getString("MolgenisUser_Role") != null) this.setRole(entity.getString("MolgenisUser_Role"));
        //set Address
        // query formal name, else lowercase name
        if(entity.getString("address") != null) this.setAddress(entity.getString("address"));
        else if(entity.getString("Address") != null) this.setAddress(entity.getString("Address"));
        else if(strict) this.setAddress(entity.getString("address")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_address") != null) this.setAddress(entity.getString("molgenisuser_address"));
        else if( entity.getString("MolgenisUser_Address") != null) this.setAddress(entity.getString("MolgenisUser_Address"));
        //set Phone
        // query formal name, else lowercase name
        if(entity.getString("phone") != null) this.setPhone(entity.getString("phone"));
        else if(entity.getString("Phone") != null) this.setPhone(entity.getString("Phone"));
        else if(strict) this.setPhone(entity.getString("phone")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_phone") != null) this.setPhone(entity.getString("molgenisuser_phone"));
        else if( entity.getString("MolgenisUser_Phone") != null) this.setPhone(entity.getString("MolgenisUser_Phone"));
        //set Email
        // query formal name, else lowercase name
        if(entity.getString("email") != null) this.setEmail(entity.getString("email"));
        else if(entity.getString("Email") != null) this.setEmail(entity.getString("Email"));
        else if(strict) this.setEmail(entity.getString("email")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_email") != null) this.setEmail(entity.getString("molgenisuser_email"));
        else if( entity.getString("MolgenisUser_Email") != null) this.setEmail(entity.getString("MolgenisUser_Email"));
        //set Fax
        // query formal name, else lowercase name
        if(entity.getString("fax") != null) this.setFax(entity.getString("fax"));
        else if(entity.getString("Fax") != null) this.setFax(entity.getString("Fax"));
        else if(strict) this.setFax(entity.getString("fax")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_fax") != null) this.setFax(entity.getString("molgenisuser_fax"));
        else if( entity.getString("MolgenisUser_Fax") != null) this.setFax(entity.getString("MolgenisUser_Fax"));
        //set TollFreePhone
        // query formal name, else lowercase name
        if(entity.getString("tollfreephone") != null) this.setTollFreePhone(entity.getString("tollfreephone"));
        else if(entity.getString("tollFreePhone") != null) this.setTollFreePhone(entity.getString("tollFreePhone"));
        else if(strict) this.setTollFreePhone(entity.getString("tollfreephone")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_tollfreephone") != null) this.setTollFreePhone(entity.getString("molgenisuser_tollfreephone"));
        else if( entity.getString("MolgenisUser_tollFreePhone") != null) this.setTollFreePhone(entity.getString("MolgenisUser_tollFreePhone"));
        //set City
        // query formal name, else lowercase name
        if(entity.getString("city") != null) this.setCity(entity.getString("city"));
        else if(entity.getString("City") != null) this.setCity(entity.getString("City"));
        else if(strict) this.setCity(entity.getString("city")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_city") != null) this.setCity(entity.getString("molgenisuser_city"));
        else if( entity.getString("MolgenisUser_City") != null) this.setCity(entity.getString("MolgenisUser_City"));
        //set Country
        // query formal name, else lowercase name
        if(entity.getString("country") != null) this.setCountry(entity.getString("country"));
        else if(entity.getString("Country") != null) this.setCountry(entity.getString("Country"));
        else if(strict) this.setCountry(entity.getString("country")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_country") != null) this.setCountry(entity.getString("molgenisuser_country"));
        else if( entity.getString("MolgenisUser_Country") != null) this.setCountry(entity.getString("MolgenisUser_Country"));
        //set ChangePassword
        // query formal name, else lowercase name
        if(entity.getBoolean("changepassword") != null) this.setChangePassword(entity.getBoolean("changepassword"));
        else if(entity.getBoolean("changePassword") != null) this.setChangePassword(entity.getBoolean("changePassword"));
        else if(strict) this.setChangePassword(entity.getBoolean("changepassword")); // setting null is not an option due to function overloading
        if( entity.getBoolean("molgenisuser_changepassword") != null) this.setChangePassword(entity.getBoolean("molgenisuser_changepassword"));
        else if( entity.getBoolean("MolgenisUser_changePassword") != null) this.setChangePassword(entity.getBoolean("MolgenisUser_changePassword"));
        //set LanguageCode
        // query formal name, else lowercase name
        if(entity.getString("languagecode") != null) this.setLanguageCode(entity.getString("languagecode"));
        else if(entity.getString("languageCode") != null) this.setLanguageCode(entity.getString("languageCode"));
        else if(strict) this.setLanguageCode(entity.getString("languagecode")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_languagecode") != null) this.setLanguageCode(entity.getString("molgenisuser_languagecode"));
        else if( entity.getString("MolgenisUser_languageCode") != null) this.setLanguageCode(entity.getString("MolgenisUser_languageCode"));
        //set GoogleAccountId
        // query formal name, else lowercase name
        if(entity.getString("googleaccountid") != null) this.setGoogleAccountId(entity.getString("googleaccountid"));
        else if(entity.getString("googleAccountId") != null) this.setGoogleAccountId(entity.getString("googleAccountId"));
        else if(strict) this.setGoogleAccountId(entity.getString("googleaccountid")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisuser_googleaccountid") != null) this.setGoogleAccountId(entity.getString("molgenisuser_googleaccountid"));
        else if( entity.getString("MolgenisUser_googleAccountId") != null) this.setGoogleAccountId(entity.getString("MolgenisUser_googleAccountId"));
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("MolgenisUser(");
        sb.append("id='" + getId()+"' ");
        sb.append("username='" + getUsername()+"' ");
        sb.append("password_='" + getPassword()+"' ");
        sb.append("activationCode='" + getActivationCode()+"' ");
        sb.append("active='" + isActive()+"' ");
        sb.append("superuser='" + isSuperuser()+"' ");
        sb.append("firstName='" + getFirstName()+"' ");
        sb.append("middleNames='" + getMiddleNames()+"' ");
        sb.append("lastName='" + getLastName()+"' ");
        sb.append("title='" + getTitle()+"' ");
        sb.append("affiliation='" + getAffiliation()+"' ");
        sb.append("department='" + getDepartment()+"' ");
        sb.append("role='" + getRole()+"' ");
        sb.append("address='" + getAddress()+"' ");
        sb.append("phone='" + getPhone()+"' ");
        sb.append("email='" + getEmail()+"' ");
        sb.append("fax='" + getFax()+"' ");
        sb.append("tollFreePhone='" + getTollFreePhone()+"' ");
        sb.append("city='" + getCity()+"' ");
        sb.append("country='" + getCountry()+"' ");
        sb.append("changePassword='" + isChangePassword()+"' ");
        sb.append("languageCode='" + getLanguageCode()+"' ");
        sb.append("googleAccountId='" + getGoogleAccountId()+"'");
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
        for (AttributeMetaData attr : new MolgenisUserMetaData().getAttributes())
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
        if("username".equalsIgnoreCase(attributeName)) {
            this.setUsername((String)value);
            return;
        }
        if("password_".equalsIgnoreCase(attributeName)) {
            this.setPassword((String)value);
            return;
        }
        if("activationCode".equalsIgnoreCase(attributeName)) {
            this.setActivationCode((String)value);
            return;
        }
        if("active".equalsIgnoreCase(attributeName)) {
            this.setActive((Boolean)value);
            return;
        }
        if("superuser".equalsIgnoreCase(attributeName)) {
            this.setSuperuser((Boolean)value);
            return;
        }
        if("FirstName".equalsIgnoreCase(attributeName)) {
            this.setFirstName((String)value);
            return;
        }
        if("MiddleNames".equalsIgnoreCase(attributeName)) {
            this.setMiddleNames((String)value);
            return;
        }
        if("LastName".equalsIgnoreCase(attributeName)) {
            this.setLastName((String)value);
            return;
        }
        if("Title".equalsIgnoreCase(attributeName)) {
            this.setTitle((String)value);
            return;
        }
        if("Affiliation".equalsIgnoreCase(attributeName)) {
            this.setAffiliation((String)value);
            return;
        }
        if("Department".equalsIgnoreCase(attributeName)) {
            this.setDepartment((String)value);
            return;
        }
        if("Role".equalsIgnoreCase(attributeName)) {
            this.setRole((String)value);
            return;
        }
        if("Address".equalsIgnoreCase(attributeName)) {
            this.setAddress((String)value);
            return;
        }
        if("Phone".equalsIgnoreCase(attributeName)) {
            this.setPhone((String)value);
            return;
        }
        if("Email".equalsIgnoreCase(attributeName)) {
            this.setEmail((String)value);
            return;
        }
        if("Fax".equalsIgnoreCase(attributeName)) {
            this.setFax((String)value);
            return;
        }
        if("tollFreePhone".equalsIgnoreCase(attributeName)) {
            this.setTollFreePhone((String)value);
            return;
        }
        if("City".equalsIgnoreCase(attributeName)) {
            this.setCity((String)value);
            return;
        }
        if("Country".equalsIgnoreCase(attributeName)) {
            this.setCountry((String)value);
            return;
        }
        if("changePassword".equalsIgnoreCase(attributeName)) {
            this.setChangePassword((Boolean)value);
            return;
        }
        if("languageCode".equalsIgnoreCase(attributeName)) {
            this.setLanguageCode((String)value);
            return;
        }
        if("googleAccountId".equalsIgnoreCase(attributeName)) {
            this.setGoogleAccountId((String)value);
            return;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MolgenisUser other = (MolgenisUser) obj;
        if (username == null)
        {
            if (other.username != null) return false;
        }
        else if (!username.equals(other.username)) return false;
        if (email == null)
        {
            if (other.email != null) return false;
        }
        else if (!email.equals(other.email)) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public org.molgenis.data.EntityMetaData getEntityMetaData()
    {
        return new MolgenisUserMetaData();
    }
}
