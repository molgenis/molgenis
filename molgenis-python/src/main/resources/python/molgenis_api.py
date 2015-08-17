###################################################################
#
# Molgenis python api client. 
#
####################################################################

import requests
import json
import re
import os.path
import security
import timeit
import time
import logging
from datetime import datetime
import copy

class Connect_Molgenis():
    """Some simple methods for adding, updating and retrieving rows from Molgenis though the REST API

    Args:
          server_url (string): The url to the molgenis server
          user (string):       Login username
          password (string):   Login password

    Example:
        from molgenis_api import molgenis
        # make a connection
        connection = connection = molgenis.Connect_Molgenis()('http://localhost:8080', 'admin', 'admin')
        # add a row to the entity public_rnaseq_Individuals
        connection.add_entity_row('public_rnaseq_Individuals',{'id':'John Doe','age':'26', 'gender':'Male'})
        # get the rows from public_rnaseq_Individuals where gender = Male
        print connection.query_entity_rows('public_rnaseq_Individuals',[{'field':'gender', 'operator':'EQUALS', 'value':'Male'}])['items'] 
        # update row in public_rnaseqIndivduals where id=John Doe -> set gender to Female
        connection.update_entity_row('public_rnaseq_Individuals',[{'field':'id', 'operator':'EQUALS', 'value':'John Doe'}], {'gender':'Female'})  
    """

    def __init__(self, server_url, new_pass_file = True,log_file = 'molgenis.log', logging_level='DEBUG', logfile_mode = 'w', only_warn_duplicates=False):
        '''Initialize Python api to talk to Molgenis Rest API
        
        Args:
            server_url (string):         The url to the molgenis server (ex: https://molgenis39.target.rug.nl/)
            user (string):               Login username
            password (string):           Login password
            log_file (string):           Path to write logfile with debug info etc to (def: molgenis.log)
            logging_level (string):      The level of logging to use. See Python's `logging` manual for info on levels (def: DEBUG)
            logfile_mode (string):       Mode of writing to logfile, e.g. w for overwrite or a for append, see `logging` manual for more details (def: w)
            only_warn_duplicates (bool): If set to true, throw warning instead of exception when trying to add duplicate values into unique column (def: False)
        '''
        logging.basicConfig(filename = log_file, filemode = logfile_mode)
        logging.getLogger().addHandler(logging.StreamHandler())
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(level=getattr(logging, logging_level))
        self.login_time = None 
        if new_pass_file:
            security.remove_secrets_file()
        security.require_username('Username')
        security.require_password('Password')
        self.api_url = server_url+'/api/v1'
        self.headers = self._construct_login_header()
        self.entity_meta_data = {}
        self.column_meta_data = {}
        self.added_rows = 0
        self.time = None
        self.only_warn_duplicates = only_warn_duplicates
        
    def _construct_login_header(self):
        '''Log in to the molgenis server and use the retrieve loginResponse token to construct the login header.
         
        Args:
            user (string): Login username
            password (string): Login password
    
        Returns:
            header (dict): Login header for molgenis server
        '''
        data = json.dumps({'username': security.retrieve('Username'), 'password': security.retrieve('Password')})
        self.logger.debug('Trying to log in to: '+str(self.api_url)+'/login/')
        server_response = requests.post( self.api_url+'/login/',
                                       data=data, headers={'Content-type':'application/json'} )
        self.check_server_response(server_response, 'retrieve token',data_used=data)
        headers = {'Content-type':'application/json', 'x-molgenis-token': server_response.json()['token'], 'Accept':'application/json'}
        self.login_time = timeit.default_timer()
        return headers
    
    def logout(self):
        server_response = requests.get(self.api_url+'/logout/', headers=self.headers)
        self.check_server_response(server_response, 'logout')
        return server_response
    
    def check_server_response(self, server_response, type_of_request, entity_used=None, data_used=None,query_used=None,column_used=None):
        '''Retrieve error message from server response
        
        Args:
            server_response (server response object): Response from server
            type_of_request (string): Extra info to print with verbose print (def: '')
        Returns:
            True if response 200 or 201, False if other response, raises error if 400
            
        Raises:
            Exception: if json object of server response contains error messages 
        '''
        def error(server_response):
            try:
                server_response_json = server_response.json()
                error_message = str(server_response)+' -> '+server_response.reason+'\n'
                if server_response_json.has_key('errors'):
                    if data_used:
                        error_message += 'Used data: '+str(data_used)+'\n'
                    if entity_used:
                        error_message += 'Used Entity: '+str(entity_used)+'\n'
                    if query_used:
                        error_message += 'Used Query: '+str(query_used)+'\n'
                    if column_used:
                        error_message += 'Used column: '+str(column_used)+'\n'
                    for error in server_response_json['errors']:
                        error_message += error['message']+'\n'
                        # below commented problems gives recursion depth exceeded error, have to fix that before uncommenting
                        #if 'Not Found' in error_message:
                            #if entity_used: 
                                #error_message += 'Available columns for entity \''+entity_used+'\': '+', '.join(self.get_column_names(entity_used))
                        # bug in error response when wrong enum value. Remove wrong part of message and add sensible one 
                        # This should be obsolete as wrong error message has been fixed in the api, can be removed after testing
                        if 'Invalid enum value' in error_message:
                            column_name = re.search('for attribute \'(.+?)\'', error_message).group(1)
                            entity_name = re.search('of entity \'(.+?)\'', error_message).group(1)
                            column_meta = self.get_column_meta_data(entity_name,column_name)
                            enum_options = ', '.join(column_meta['enumOptions'])
                            error_message = error_message.replace('Value must be less than or equal to 255 characters',
                                                  ' The enum options are: '+enum_options)
                        self.logger.error(error_message.rstrip('\n'))
                        raise Exception(error_message.rstrip('\n'))
                return server_response_json
            except ValueError:
                pass # no json oobject in server_response
        if str(server_response) == '<Response [400]>' or str(server_response) == '<Response [404]>':
            error(server_response) 
        elif str(server_response) == '<Response [401]>':
            self.logger.error(type_of_request+' -> '+str(server_response)+' - '+server_response.reason +' (Wrong username - password combination)')
            raise Exception(type_of_request+' -> '+str(server_response)+' - '+server_response.reason +' (Wrong username - password combination)')
        elif str(server_response) == '<Response [200]>' or str(server_response) == '<Response [201]>' or str(server_response) == '<Response [204]>':
            message = type_of_request+' -> '+str(server_response)+' - '+server_response.reason 
            if 'Add row to entity' in type_of_request:
                message += '. Total added rows this session: '+str(self.added_rows)
            self.logger.debug(message)
            return True
        else:
            error(server_response)
            # i didn't go through all response codes, so if it's a different response than expected I only want to raise exception if 
            # there are error messages in the response object, otherwise just warn that there is a different response than expected
            self.logger.warning('Expected <Response [200]>, <Response 201> or <Response 204>, got '+str(server_response)+'\nReason: '+server_response.reason)
            return False
    
    def validate_data(self, entity_name, data):
        '''Validate that the right column names are given, since otherwise if wrong columns are used it will create empty rows. 
        Only the column names and auto ID get checked, not value type, as server will raise an error if wrong type is tried to be inserted
        
        Args:
            data (dict): Dictonary that will be used as json_data
            
        Raises:
            Exception
        '''
        columns_to_insert = data.keys()
        columns_in_entity = self.get_column_names(entity_name)
        difference = set(columns_to_insert).difference(set(columns_in_entity))
        if len(difference) > 0:
            error_message = 'Provided data has columns which are not in the entity. The wrong columns are: '+', '.join(difference)+'\n'\
                           +'The provided data is: '+str(data)+'\n'\
                           +'The entity '+entity_name+' contains the columns: '+', '.join(columns_in_entity)
            self.logger.error(error_message)
            raise Exception(error_message)
        entity_id_attribute = self.get_id_attribute(entity_name)
        if entity_id_attribute in data and self.get_column_meta_data(entity_name,entity_id_attribute)['auto']:
            self.logger.warning('The ID attribute ('+entity_id_attribute+') of the entity ('+entity_name+') you are adding a row to is set to `auto`.\n'\
                         +'The value you gave for id ('+str(data[entity_id_attribute])+') will not be used. Instead, the ID will be a random string.')

    def _sanitize_data(self, data, add_datetime, datetime_column, added_by, added_by_column):
        if add_datetime:
            data[datetime_column] = str(datetime.now())
        if added_by:
            data[added_by_column] = security.retrieve('Username')
        # make all values str and remove if value is None or empty string
        data = {k: v for k, v in data.items() if v!=None}
        data = dict([a, str(x)] for a, x in data.iteritems() if len(str(x).strip())>0)
        return data
    
    def add_entity_row_or_file_server_response(self, entity_name, data, server_response):
        try:
            self.check_server_response(server_response, time.strftime('%H:%M:%S', time.gmtime(timeit.default_timer()-self.login_time))+ ' - Add row to entity '+entity_name, entity_used=entity_name, data_used=json.dumps(data))
            added_id = server_response.headers['location'].split('/')[-1]
        except Exception as e:
            if self.only_warn_duplicates:
                if 'Duplicate value' in str(e):
                    message = 'Duplicate value not added, instead return id of already existing row\n'
                    message += 'Tried to insert into '+str(entity_name)+' with data:\n'+str(data)
                    self.logger.debug(message)
                    unqiue_att = re.search("Duplicate value '(\S+?)' for unique attribute '(\S+?)'", str(e))
                    row = self.query_entity_rows(entity_name, query = [{'field':unqiue_att.group(2), 'operator':'EQUALS', 'value':unqiue_att.group(1)}])['items'][0]
                    try:
                        added_id = row[self.get_id_attribute(entity_name)]
                    except KeyError:
                        print row
                        raise
                    self.logger.debug('id found for row with duplicate value: '+str(added_id))
                else:
                    raise
        return added_id  
      
    _add_datetime_default = False
    _added_by_default = False
    def add_entity_row(self, entity_name, data, validate_json=False, add_datetime=None, datetime_column='datetime_added', added_by=None, added_by_column='added_by'):
        '''Add a row to an entity
        
        Args:
            entity_name (string): Name of the entity where row should be added
            json_data (dict): Key = column name, value = column value
            validate_json (bool): If True, check if the given data keys correspond with the column names of entity_name.
                              If adding entity rows seems slow, try setting to False (def: False)
            add_datetime (bool): If True, add a datetime to the column <datetime_column> (def: False)
            datetime_column (str): column name where to add datetime
            added_by (bool): If true, add the login name of the person that updated the record
            added_by_column (string): column name where to add name of person that updated record
            
        Returns:
            added_id (string): Id of the row that got added
        '''
        if not add_datetime:
            add_datetime = self._add_datetime_default
        if not added_by:
            added_by = self._added_by_default
        if timeit.default_timer()-self.login_time > 30*60:
            # molgenis login head times out after a certain time, so after 30 minutes resend login request
            self.headers = self._construct_login_header()
        # make a string of json data (dictionary) with key=column name and value=value you want (works for 1 individual, Jonatan is going to find out how to to it with multiple)
        # post to the entity with the json data
        if validate_json:
            self.validate_data(entity_name, data)
        data = self._sanitize_data(data, add_datetime, datetime_column, added_by, added_by_column)
        request_url = self.api_url+'/'+entity_name+'/'
        server_response = requests.post(request_url, data=json.dumps(data), headers=self.headers)
        self.added_rows += 1
        added_id = self.add_entity_row_or_file_server_response(entity_name, data, server_response)
        return added_id
    
    def add_file(self, file_path, description, entity_name, extra_data=None, file_name=None, add_datetime=False, datetime_column='datetime_added', added_by=None, added_by_column='added_by'):
        '''Add a file to entity File.
        
        Args:
            file_path (string): Path to the file to be uploaded
            description (description): Description of the file
            entity (string): Name of the entity to add the files to
            data (dict): If extra columns have to be added, provide a dict with key column name, value value (def: None)
            file_name (string): Name of the file. If None is set to basename of filepath (def: None)
            added_by (bool): If true, add the login name of the person that updated the record (def: False)
            added_by_column (string): column name where to add name of person that updated record (def: added_by)
            add_datetime (bool): If true, add the datetime that the file was added (def: False)
            add_datetime_column (string): column name where to add datetime (def: datetime_added)
            
        Returns:
            file_id (string): ID if the file that got uploaded (for xref)
            
        Example:
            >>> from molgenis_api import molgenis
            >>> connection = molgenis.Connect_Molgenis('http://localhost:8080', 'admin', 'admin')
            >>> print connection.add_file('/Users/Niek/UMCG/test/data/ATACseq/rundir/QC/FastQC_0.sh')
            AAAACTWVCYDZ6YBTJMJDWXQAAE
        '''
        if not add_datetime:
            add_datetime = self._add_datetime_default
        if not added_by:
            added_by = self._added_by_default
        if not file_name:
            file_name = os.path.basename(file_path)
        if not os.path.isfile(file_path):
            self.logger.error('File not found: '+str(file_path))
            raise IOError('File not found: '+str(file_path))
        file_post_header = copy.deepcopy(self.headers)
        del(file_post_header['Accept'])
        del(file_post_header['Content-type'])
        data = {'description': description}
        if extra_data:
            data.update(extra_data)
        data = self._sanitize_data(data, add_datetime, datetime_column, added_by, added_by_column)
        server_response = requests.post(self.api_url+'/'+entity_name, 
                                        files={'attachment':(os.path.basename(file_path), open(file_path,'rb'))},
                                        data=data,
                                        headers = file_post_header)
        added_id = self.add_entity_row_or_file_server_response(entity_name, data, server_response)
        return added_id
        
    def query_entity_rows(self, entity_name, query):
        '''Get row(s) from entity with a query
        
        Args:
            entity_name (string): Name of the entity where get query should be run on
            query (list): List of dictionaries with as keys:values -> [{'field':column name, 'operator':'EQUALS', 'value':value}]
             
        Returns:
            result (dict): json dictionary of retrieve data
        
        TODO:
            More difficult get queries
        '''
        if len(query) == 0:
            self.logger.error('Can\'t search with empty query')
            raise ValueError('Can\'t search with empty query')
        json_query = json.dumps({'q':query})
        server_response = requests.post(self.api_url+'/'+entity_name+'?_method=GET', data = json_query, headers=self.headers)
        server_response_json = server_response.json()
        self.check_server_response(server_response, 'Get rows from entity',entity_used=entity_name, query_used=json_query)
        if server_response_json['total'] >= server_response_json['num']:
            self.logger.warning(str(server_response_json['total'])+' number of rows selected. Max number of rows to retrieve data for is set to '+str(server_response_json['num'])+'.\n'
                        +str(server_response_json['num']-server_response_json['total'])+' rows will not be in the results.')
            self.logger.info('Selected '+str(server_response_json['total'])+' row(s).')
        return server_response_json
  
    def get_entity(self, entity_name):
        '''Get all data of entity_name
        
        Args:
            entity_name (string): Name of the entity where get query should be run on
            query (list): List of dictionaries with as keys:values -> [{'field':column name, 'operator':'EQUALS', 'value':value}]
             
        Returns:
            result (dict): json dictionary of retrieve data
        
        TODO:
            More difficult get queries
        '''
        server_response = requests.get(self.api_url+'/'+entity_name, headers=self.headers)
        server_response_json = server_response.json()
        self.check_server_response(server_response, 'Get rows from entity',entity_used=entity_name)
        if server_response_json['total'] >= server_response_json['num']:
            self.logger.warning(str(server_response_json['total'])+' number of rows selected. Max number of rows to retrieve data for is set to '+str(server_response_json['num'])+'.\n'
                        +str(int(server_response_json['num'])-int(server_response_json['total']))+' rows will not be in the results.')
            self.logger.info('Selected '+str(server_response_json['total'])+' row(s).')
        return server_response_json
    _updated_by_default = False
    def update_entity_rows(self, entity_name, query, data, add_datetime=None, datetime_column='datetime_last_updated', updated_by = None, updated_by_column='updated_by'):
        '''Update an entity row
    
        Args:
            entity_name (string): Name of the entity to update
            query (list): List of dictionaries which contain query to select the row to update (see documentation of query_entity_rows)
            data (dict):  Key = column name, value = column value
            updated_by (bool): If true, add the login name of the person that updated the record
            updated_by_column (string): column name where to add name of person that updated record
        '''
        if not add_datetime:
            add_datetime = self._add_datetime_default
        if not updated_by:
            updated_by = self._updated_by_default
        self.validate_data(entity_name, data)
        entity_data = self.query_entity_rows(entity_name, query)
        if len(entity_data['items']) == 0:
            self.logger.error('Query returned 0 results, no row to update.')
            raise Exception('Query returned 0 results, no row to update.')
        data = self._sanitize_data(data, add_datetime, datetime_column, updated_by, updated_by_column)
        id_attribute = self.get_id_attribute(entity_name)
        server_response_list = [] 
        for entity_items in entity_data['items']:
            row_id = entity_items[id_attribute]
            for key in data:
                server_response = requests.put(self.api_url+'/'+entity_name+'/'+str(row_id)+'/'+key, data='"'+str(data[key])+'"', headers=self.headers)
                server_response_list.append(server_response)
                self.check_server_response(server_response, 'Update entity row', query_used=query,data_used=[self.api_url+'/'+entity_name+'/'+str(row_id)+'/'+key, '"'+str(data[key])+'"'],entity_used=entity_name)                
            # BELOW IS LEGACY CODE
            #else:
            #    self.logger.error('Updating multiple values at the same time not implemented yet'
            #                     +'Trying to update following data:\n'+str(data))
            #    raise NotImplementedError('Updating multiple values at the same time not implemented yet'
            #                             +'Trying to update following data:\n'+str(data))
                # if trying to update multiple columns, column values that are not given will be overwritten with null, so we need to add the existing column data into our dict
                # DOES NOT WORK FOR X/MREFS!!!
            #   for key in entity_items:
            #        if key != id_attribute and key not in data and key!='previous_individuals':
            #            data[key.encode('ascii')] = str(entity_items[key]).encode('ascii')
            #    server_response = requests.put(self.api_url+'/'+entity_name+'/'+row_id+'/', data=json.dumps(data), headers=self.headers)
            #    server_response_list.append(server_response)
            #    self.check_server_response(server_response, 'Update entity row (multiple values)', query_used=query,data_used=data,entity_used=entity_name)
        return server_response_list

    def get_entity_meta_data(self, entity_name):
        '''Get metadata from entity
        
        Args:
            entity_name (string): Name of the entity to get meta data of
        
        Returns:
            result (dict): json dictionary of retrieve data
        '''
        if entity_name in self.entity_meta_data:
            return self.entity_meta_data[entity_name]
        server_response = requests.get(self.api_url+'/'+entity_name+'/meta', headers=self.headers)
        self.check_server_response(server_response, 'Get meta data of entity',entity_used=entity_name)
        entity_meta_data = server_response.json()
        self.entity_meta_data[entity_name] = entity_meta_data
        return entity_meta_data

    def get_column_names(self, entity_name):
        '''Get the column names from the entity
        
        Args:
            entity_name (string): Name of the entity to get column names of
        Returns:
            meta_data(list): List with all the column names of entity_name
        '''
        entity_meta_data = self.get_entity_meta_data(entity_name)
        attributes = entity_meta_data['attributes']
        return attributes.keys()
    
    def get_id_attribute(self, entity_name):
        '''Get the id attribute name'''
        entity_meta_data = self.get_entity_meta_data(entity_name)
        return entity_meta_data['idAttribute']
    
    def get_column_meta_data(self, entity_name, column_name):
        '''Get the meta data for column_name of entity_name
        
        Args:
            entity_name (string): Name of the entity 
            column_name (string): Name of the column
        Returns:
            List with all the column names of entity_name
        '''
        if entity_name+column_name in self.column_meta_data:
            return self.column_meta_data[entity_name+column_name]
        server_response = requests.get(self.api_url+'/'+entity_name+'/meta/'+column_name, headers=self.headers)
        self.check_server_response(server_response, 'Get meta data of column',entity_used=entity_name,column_used=column_name)
        column_meta_data = server_response.json()
        self.column_meta_data[entity_name+column_name] = column_meta_data
        return column_meta_data
    
    def get_column_type(self, entity_name, column_name):
        column_meta_data = self.get_column_meta_data(entity_name, column_name)
        return column_meta_data['fieldType']

    def get_all_entity_data(self):
        '''Get info of all entities 
        '''
        raise NotImplementedError('Not implemented yet, returns a max number (~450ish) entities, so if more entities are present (e.g. many packages available), not all entities are returned')
        server_response = requests.get(self.api_url+'/entities/', headers=self.headers)
        self.check_server_response(server_response, 'Get info from all entities')
        return server_response
    
    def delete_all_rows_of_all_entities(self, package):
        '''Delete all entities of package
        
        Args:
            package (string): Package for which to delete all entities. (def: None)
        '''
        if not package:
            self.logger.error('package can\'t be None, is '+str(package))
            raise AttributeError('package can\'t be None, is '+str(package))
        server_response = self.get_all_entity_data()
        for entity in server_response.json()['items']:
            entity_name = entity['fullName']
            if package in entity_name and not bool(entity['abstract']):
                self.logger.info('Deleting all rows from',entity_name)
                try:
                    self.delete_all_entity_rows(entity_name)
                except Exception as e:
                    self.logger.warning(str(e))
    
    def delete_all_entity_rows(self,entity_name):
        '''delete all entity rows'''
        entity_data = self.get_entity(entity_name)
        # because I can only select 100 rows, have to select untill table is empty. <<<<< TODO: figure out how to change num
        server_response_list = []
        while len(entity_data['items']) > 0:
            server_response_list.extend(self.delete_entity_data(entity_data,entity_name))
            entity_data = self.get_entity(entity_name)
        return server_response_list
    
    def delete_entity_rows(self, entity_name, query):
        '''delete entity rows
    
        Args:
            entity_name (string): Name of the entity to update
            query (list): List of dictionaries which contain query to select the row to update (see documentation of query_entity_rows)
        '''
        entity_data = self.query_entity_rows(entity_name, query)
        if len(entity_data['items']) == 0:
            self.logger.error('Query returned 0 results, no row to delete.')
            raise Exception('Query returned 0 results, no row to delete.')
        return self.delete_entity_data(entity_data, entity_name, query_used=query)

    def delete_entity_data(self, entity_data,entity_name,query_used=None):
        '''delete entity data
        
        Args:
            entity_data (dict): A dictionary with at least key:"items", value:<dict with column IDs>. All items in this dict will be deleted
            entity_name (string): Name of entity to delete from
            query_used (string): Incase entity_data was made with a query statement, the query used can be given for more detailed error prints (def: None)
        '''
        server_response_list = []
        id_attribute = self.get_id_attribute(entity_name)
        for rows in entity_data['items']:
            row_id = rows[id_attribute]
            server_response = requests.delete(self.api_url+'/'+entity_name+'/'+str(row_id)+'/', headers=self.headers)
            self.check_server_response(server_response, 'Delete entity row',entity_used=entity_name,query_used=query_used)
            server_response_list.append(server_response)
        return server_response_list
 
