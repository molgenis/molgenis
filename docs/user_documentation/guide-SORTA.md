# SORTA
**
The System for Ontology-based Re-coding and Technical Annotation (SORTA) is able to semi-automatically match data values with standard codes such as ontologies or local terminologies. For each data value, SORTA provides a list of the most relevant standard codes based on the lexical similarity in percentage. Users can then pick the correct matches from the suggested list.
**

For a video tutorial, see the [MOLGENIS YouTube page](https://www.youtube.com/watch?v=Wq81S-jR3l8).

For the full paper, see [Pang et al, Database(Oxford)](http://database.oxfordjournals.org/content/2015/bav089.full). 

## Usage
We implemented the following three steps. First, coding systems or ontologies are uploaded and indexed in Lucene to enable fast searches (once for each ontology). Second, users create their own coding/recoding project by uploading a list of data values. What users get back is a shortlist of matching concepts for each value that has been retrieved from the selected coding system based on their lexical relevance.


![SORTA example](../images/sorta_example.jpg?raw=true, "sorta/example")

Above is an example of matching the input value ‘external auditory canal defect’ with HPO ontology terms. A list of candidate HPO ontology terms was retrieved from the index and sorted based on similarity scores. Users can select a mapping by clicking the ‘v’ button. If none of the candidate mappings are suitable, users can choose the ‘No match’ option.

## Technical design

SORTA is built based on Lucene in combination with the N-gram string matching algorithm to achieve high performance and accuracy. Lucene matching scores are too abstract for users to understand and they are not comparable between each other. Therefore we use the N-gram algorithm to re-calculate the similarity scores (in percentages) between data values and the concepts retrieved by Lucene. The new similarity scores are more clear and comparable, enabling us to explore the uniform cut-off value.

Step 1 - Index the standard concepts in Lucene to establish a knowledge base.
Step 2 - Lucene retrieves the most relevant concepts for data values from the knowledge base.
Step 3 - The N-gram algorithm is applied to re-calculate the similarity scores between data values and concepts retrieved by Lucene.
Step 4 - Users can pick the correct matches from the list of concepts sorted based on N-gram similarity scores.
 

![SORTA design](../images/sorta_design.png?raw=true, "sorta/design")
 

## Ontology model

Standard codes (ontologies) can be imported using the [EMX format](ref-emx) , the model can be browsed and viewed as a UML diagram as well as a flat list in the webbrowser. 
