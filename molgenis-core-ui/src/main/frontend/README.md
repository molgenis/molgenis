## Bootstrap 4 themes with Sass

Sass code is located in /scss/bootstrap-molgenis-blue.scss. To compile the code for the first time:
```
yarn build-task:scss-compile
yarn dev:theme
```

After the first time just run:
```
yarn dev:theme
```

Currently te sass compiling steps are hardcoded based on the /scss/bootstrap-molgenis-blue.scss 
file. In future we need to do this dynamically to be able to support different themes using the same
pipeline. Matching the bootstrap 3 theme is done manually. 

