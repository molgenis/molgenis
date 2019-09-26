# Migration guide
We only specify functional migration steps in this guide.

## From 7.x to 8.x
Functional migration steps from MOLGENIS 7.x to 8.x

### EMX models and other configuration
The following changes require manual actions (if applicable for your application):

- MREF as labels are not allowed anymore. You should use template expressions. Please check: [template expressions](guide-emx.md#template).
    - You can migrate the database first and if you encounter the following error 
    
      ```
      Error:
        Conversion failure in entity type [#name#] attribute [#name#]; No converter found capable of converting from type [org.molgenis.data.support.DynamicEntity] to type [java.lang.String]
      ```  
    
      You can fix these attributes in the Metadata Manager by editing the expression of the mentioned attribute.
- If users or groups were giving special permissions on the _Settings Manager_ Plugin, these permissions should be set again for the replacement _Settings_ Plugin. The plugin itself is replaced automatically.
   

