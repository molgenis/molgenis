# molgenis-frontend

Downloads npm modules and publishes their contents in the molgenis-frontend jar.
For the list of modules see `package.json`.

## Usage
When you install these packages, you first have to configure the registry. You can do that by 
running this command:
```
yarn config set registry https://registry.molgenis.org/repository/npm-group/
```
> Note: yarn.lock in .gitignore because of updating canary packages