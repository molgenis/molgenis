# Auditing

With auditing you can keep track of who did what and when. Think of login/logout events, data access
events, failed authentications, etc. Below you will read how to configure auditing in your MOLGENIS.

## Configuring the log file
If you want to log auditing information to a log file, you'll need to set the `audit.log.path` 
environment variable and point it to where you want to store the log file. For developers using 
IntelliJ, see the [Tomcat guide](guide-using-an-ide-for-backend#deploy-run-in-tomcat-server).

## Auditing settings
The settings for auditing can be found in the Settings plugin.

### Audit system entity types
Audit logging for system entity types can be enabled or disabled. When enabled, all *changes* to 
system entity types will be logged: create, update and delete. Read events won't be logged.

### Audit non-system entity types
Audit logging for non-system entity types (like imported datasets) can be configured with this 
setting. Your options are:
- **None** - no entity types will be audited
- **Tagged** - only entity types with the `audit-usage` tag will be audited (see below)
- **All** - all entity types will be audited

When enabled, all interactions with these entity types will be logged, including read events.

## Tagging an entity type
With tags, you have fine-grained control over which entity types are audited. Here's how you can tag
your entity types:

### In MOLGENIS
1. Go to the Data Explorer
2. Navigate to the `Entity type` table
3. Look for the entity type you want to audit
4. Edit it, and find the `Tags` field
5. Here, add the `audit-usage` tag

### In EMX
1. Go to the `entities` sheet
2. Add `audit-usage` in the `tags` column for every entity type that should be audited
3. Import the EMX

