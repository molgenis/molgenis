/**
 * Returns a filtered list based on a case-in-sensitive match with the provided query
 *
 * @param array A list of objects with label and description parameters used to filter on
 * @param query A search query used to filter an array of objects
 * @return A filtered list
 */
export default function (array, query) {
  query = query.toLowerCase()
  return query ? array.filter(item => item.label.toLowerCase().indexOf(query) >= 0 || item.description.toLowerCase().indexOf(query) >= 0) : array
}