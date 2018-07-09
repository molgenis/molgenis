**
When the system adminstrator has setup the global definition of groups and users you can now manage your own *group*. Assuming you are a group-manager in MOLGENIS.
**

# Configure MOLGENIS
When are using MOLGENIS you have to understand some basic concepts.

**Structure:**
* **Groups**
  * **Members / Users**
    * **Roles**
      * Managers
      * Editors
      * Viewers
  
**Resources:**
* Folders
* Tables

**Plugins**
Plugins are specific modules within MOLGENIS that offers certain functionality. For example the DataExplorer. 

## Basic setup
You can start performing these steps to configure MOLGENIS.

- **Step 1: Add a group**
  
  You can add groups to MOLGENIS to structure user groups and data you want to manage. When you a group is added you get a folder to put your resources in.
  
- **Step 2: Add members**
  
  When you have defined your group, you can add members to the group. You can add new user by reading this: [user management guide](guide-user-management.md).

- **Step 3: Add members to groups** 

  You can now determine how does what in your group. There are standard roles that you can use enable members to edit, view or manage your group content.
  The available roles are:
  - **Manager**
    
    Can manage members from a group and all resources in a group (tables, scripts and files)
    
  - **Editor**
  
    Can manage all resources in a group (folders, tables)
    
  - **Viewer**
  
    Can view all resources in a group (folders, tables)

When you have setup this basic configuration you can start using MOLGENIS. When you want a detailed description to setup groups click [here](admin-features/guide-ref-security.md).

