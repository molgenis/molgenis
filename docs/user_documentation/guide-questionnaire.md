**
MOLGENIS offers a questionnaire module which can present a questionnaire to users.
These questionnaires are defined in our [EMX format](./ref-emx.md). Each questionnaire can be filled in once, and generates one row
of data in a table used to collect the questionnaires that where submitted.
**

# Minimal example
[download](../data/simple-questionnaire.xlsx) simple questionnaire example. Import this xlsx file via the standard [import wizard](./guide-upload.md)

The above example showcases a very simple questionnaire.
It asks for your age, and if you are 18 or older the question "Do you have a drivers license" will appear.

To start creating your own questionnaire, you will need to have few fields specifically defined in your EMX in order for it to work

## EMX sheets
#### 'entities' sheet

| name               | description               | extends               | label               |
|--------------------|---------------------------|-----------------------|---------------------|
| questionnaire name | questionnaire description | __sys_Questionnaire__ | questionnaire label |

The questionnaire has to extend the sys_Questionnaire table. This is an indicator for MOLGENIS to show this questionnaire in the list of all
questionnaires.


## Visible expressions
The cool feature in the questionnaire module is that you can show certain questions based on the answer given on other questions.
You do this by using the __Javascript Magma syntax__ to create an expression inside the visible column (See driverslicence in the simple questionnaire example).

## Questionnaire timestamp on submit
MOLGENIS will automatically add a submit date for you when you submit a questionnaire. So keep in mind that you do not have to manually
add a question relating to submit dates.

# Using Questionnaire data
You can access the questionnaire results through the data explorer.
Go to the data explorer and look for the (in our case) Simple Questionnaire table.

This table contains the answers for all the users that filled in the questionnaire.

 
