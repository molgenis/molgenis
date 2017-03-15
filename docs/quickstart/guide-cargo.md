# # Deploy MOLGENIS with cargo

The fastest and easiest way to get MOLGENIS running on a machine, is using our cargo project. This is collection of files that you can use to deploy MOLGENIS for you. There are three steps you need to do before this will work: 

**Download the cargo project**

[Download](https://github.com/molgenis/molgenis-cargo) the entire project from GitHub.

*Setting up your PostGreSQL**  
If you are unfamiliar with PostGreSQL, follow one of their [PostGreSQL installation guides](https://www.postgresql.org/docs/9.6/static/index.html). Once you have a PostGreSQL server running, open up the included pgAdmin application that is supplied with most PostGreSQL installations, and perform the following actions:

- Add a database 'molgenis'
- Add a user 'molgenis' (password 'molgenis') under Login Roles
- Add 'can create databases' privilege to user 'molgenis'

```bash
mvn clean resources:resources org.codehaus.cargo:cargo-maven2-plugin:run
```

More details are included in the README of the cargo project.