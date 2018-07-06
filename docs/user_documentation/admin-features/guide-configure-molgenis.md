**
Once you have a server running, you are probably eager to share your data with the world. However, you might want to only show your data to a select few. In the following paragraphs we explain how to define groups and permissions.
**

# Configure MOLGENIS

When you start with MOLGENIS you need to define your environment.

We define some basic concepts in MOLGENIS:

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
* Scripts
* Files

## Basic setup
When you start using MOLGENIS 

- **Step 1: Define your own group** 
  
  So if you want to configure MOLGENIS you start to define your own group.
  
- **Step 2: Add members**
  
  When you have defined your group, you can add members to the group. You can add new user by reading this: [user management guide](guide-user-management.md).

- **Step 3: Assign roles to the members** 

  You can now determine how does what in your group. There are standard roles that you can use enable members to edit, view or manage your group content.
  The available roles are:
  - **Manager**
    
    Can manage members from a group and manage all resources in a group (tables, scripts and files)
    
  - **Editor**
  
    Can manage all resources in a group (folders, tables, scripts and files)
    
  - **Viewer**
  
    Can view all resources in a group (folders, tables, scripts and files)
   
When you have setup this basic configuration you can start importing data and use MOLGENIS.
