export const generateError = (error, spec, test) => {
  return new Error(error + '\nspec file: ' + spec + '\ntest: ' + test)
}
