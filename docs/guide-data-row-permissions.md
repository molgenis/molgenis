# Set permissions on row level (RLS)

## Enabling Row Level security for your package

Assuming you have uploaded a new package, go to the permission manager plugin.
Select from the left 'Row-Level Security'. Find your package and check Row-Level Security Enabled.

## Manage permissions

Go to the Data Row Permissions plugin

Open your selected package (or use the filter option to easily find your package).
Click on the package.

> **Note:** if you don't see any entities in this screen, there are none with set permissions. Get the entity id from the data explorer, navigate back to this screen and add it to the end of the url e.g: http://....#/entityId/objectId

If you have manually added the Id to the URL the screen will start in addmode.

### Add

You can now add a new user / role to have access on that particular row / object
Start by selecting a type (role/user). Then on the left you have a dropdown with available options. Then you can set the correct permissions on the right dropdown.
If you are all set save. The permission should be succesfully added.

### Edit

Press the edit button. The fields are now ready to be changed.
Click either cancel (the edit button changed to cancel) or the save button to either
cancel or complete the operation.

> **Note:** You can bulk edit

### Delete

When pressing the delete button, you enter deletemode.
You can now delete them one at the time. This is immediate and **permanent**. 
Use with caution. Press cancel to stop delete mode.

### Change ownership

When you click on change ownership you get a modal with all possible owners.
Select one from the dropdown to save the new owner.

> **Note:** This is a one-way action and can only be reversed by the new owner!
