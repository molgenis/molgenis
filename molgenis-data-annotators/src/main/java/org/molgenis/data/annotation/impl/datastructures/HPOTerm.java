package org.molgenis.data.annotation.impl.datastructures;

/**
 * Created by jvelde on 2/12/14.
 */
public class HPOTerm {

    String id;
    String description;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public HPOTerm(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String toString() {
        return "HPOTerm{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
