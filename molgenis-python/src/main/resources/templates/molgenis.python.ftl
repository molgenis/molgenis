###################################################################
#
# Molgenis python api client. 
#
# TODO: Better error raising
####################################################################

import requests
import json
import urllib2
import warnings

class Connect_Molgenis():
    """Some simple methods for adding, updating and retrieving rows from Molgenis though the REST API

    Args:
          server_url (string): The url to the molgenis server
          user (string):       Login username
          password (string):   Login password

    Example:
        # connect to the server
        connection = Connect_Molgenis('https://molgenis39.target.rug.nl/', 'admin', '****')
        # add a row to an entity
        connection.add_entity_row('rnaseq_Per_tile_sequence_quality',{'tile':'20','base':'1', 'mean':4})
        # update a row of an entity
        connection.update_entity_row('rnaseq_Per_tile_sequence_quality',{'id':'AAAACTSOYOXT3RBMAMRIUPAAAE'}, {'base':'13434', 'mean':43141313})
        # get data from entity
        connection.get_entity_rows('rnaseq_Per_tile_sequence_quality',{'tile':'20','base':'1', 'mean':4})
        # use connections.headers() and connection.api_url() for making your own requests
        requests.patch(connection.api_url+'/SomePackage_SomeEntity',data = {'some':'data'}, headers=connection.headers)            
    """

    def __init__(self, server_url, user, password, verbose = True, give_warnings = True):
        '''Initialize Python api to talk to Molgenis Rest API
        
        Args:
            server_url (string): The url to the molgenis server (ex: https://molgenis39.target.rug.nl/)
            user (string):       Login username
            password (string):   Login password
            verbose (bool):      If True, print out extra info, if False no prints are used (def: True)
            warnings (bool):     If True, warnings are given for certain operations where something might have gone wrong, but not sure enough to raise exception.
        '''
        self.verbose = verbose
        self.api_url = server_url+'/api/v1'
        self.headers = self._construct_login_header(user, password)
        self.entity_meta_data = {}
        self.column_meta_data = {}
        self.give_warnings = give_warnings
    
    def set_verbosity(self, verbose):
        '''Set verbosity on or off
        
        Args:
            verbose (bool): True sets it on, False sets it off.
        '''
        self.verbose = verbose
    
    def set_give_warnings(self, give_warnings):
        '''Set give_warnings on or off
        
        Args:
            give_warnings (bool): True sets it on, False sets it off.
        '''
        self.give_warnings = give_warnings
    
    def _construct_login_header(self, user, password):
        '''Log in to the molgenis server and use the retrieve loginResponse token to construct the login header.
         
        Args:
            user (string): Login username
            password (string): Login password
    
        Returns:
            header (dict): Login header for molgenis server
        '''
        server_response = requests.post( self.api_url+'/login/',
                                       data=json.dumps({'username': user, 'password': password}),
                                       headers={'Content-type':'application/json'} )
        self.check_server_response(server_response, 'retrieve token')
        token = server_response.json()['token']
        headers = {'Content-type':'application/json', 'x-molgenis-token': token, 'Accept':'application/json'}
        return headers
    
    def check_server_response(self, server_response, type_of_request = ''):
        '''Retrieve error message from server response
        
        Args:
            server_response (server response object): Response from server
            type_of_request (string): Extra info to print with verbose print (def: '')
        Returns:
            True if response 200 or 201, False if other response, raises error if 400
            
        Raises:
            BaseException: if json object of server response contains error messages 
        '''
        def error(server_response):
            try:
                json = server_response.json()
                error_message = str(server_response)+'\n'+server_response.reason+'\n'
                if json.has_key('errors'):
                    for error in json['errors']:
                        error_message += error['message']+'\n'
                        raise BaseException(error_message.rstrip('\n'))
            except ValueError:
                pass # no json oobject in server_response
        if str(server_response) == '<Response [400]>' or str(server_response) == '<Response [404]>':
            error(server_response) 
        elif str(server_response) == '<Response [200]>' or str(server_response) == '<Response [201]>':
            if self.verbose:
                print type_of_request+' -> '+str(server_response)+' - '+server_response.reason
            return True
        else:
            error(server_response)
            # i didn't go through all response codes, so if it's a different response than expected I only want to raise exception if 
            # there are error messages in the response object, otherwise just warn that there is a different response than expected
            if self.give_warnings:
                warnings.warn('Expected <Response [200]> or <Response 201>, got '+str(server_response)+'\nReason: '+server_response.reason)
            return False
    
    def validate_data(self, entity_name, data):
        '''Validate that the right column names are given, since otherwise if wrong columns are used it will create empty rows. 
        Only the column names and auto ID get checked, not value type, as server will raise an error if wrong type is tried to be inserted
        
        Args:
            data (dict): Dictonary that will be used as json_data
            
        Raises:
            BaseException
        '''
        columns_to_insert = data.keys()
        columns_in_entity = self.get_column_names(entity_name)
        difference = set(columns_to_insert).difference(set(columns_in_entity))
        if len(difference) > 0:
            error_message = 'Provided data has columns which are not in the entity. The wrong columns are: '+', '.join(difference)+'\n'\
                           +'The provided data is: '+str(data)+'\n'\
                           +'The entity '+entity_name+' contains the columns: '+', '.join(columns_in_entity)
            raise BaseException(error_message)
        entity_id_attribute = self.get_id_attribute(entity_name)
        if entity_id_attribute in data and self.get_column_meta_data(entity_name,entity_id_attribute)['auto']:
            if self.give_warnings:
                warnings.warn('The ID attribute ('+entity_id_attribute+') of the entity ('+entity_name+') you are adding a row to is set to `auto`.\n'\
                             +'The value you gave for id ('+data[entity_id_attribute]+') will not be used. Instead, the ID will be a random string.')
        
    def add_entity_row(self, entity_name, data, validate_json=True):
        '''Add a row to an entity
        
        Args:
            entity_name (string): Name of the entity where row should be added
            json_data (dict): Key = column name, value = column value
            validate_json (bool): If True, check if the given data keys correspond with the column names of entity_name.
                              If adding entity rows seems slow, try setting to False (def: True)
                              
        Returns:
            server_response (Response object): Response from server
        '''
        # make a string of json data (dictionary) with key=column name and value=value you want (works for 1 individual, Jonatan is going to find out how to to it with multiple)
        # post to the entity with the json data
        self.validate_data(entity_name, data)
        server_response = requests.post(self.api_url+'/'+entity_name+'/', data=str(data), headers=self.headers)
        self.check_server_response(server_response, 'Add row to entity')
        return server_response
        
    def get_entity_rows(self, entity_name, query):
        '''Get row(s) from entity 
        
        Args:
            entity_name (string): Name of the entity where get query should be run on
            query (list): List of dictionaries with as keys:values -> [{'field':column name, 'operator':'EQUALS', 'value':value}]
             
        Returns:
            result (dict): json dictionary of retrieve data
        
        TODO:
            More difficult get queries
        '''
        json_query = json.dumps({'q':query})
        server_response = requests.post(self.api_url+'/'+entity_name+'?_method=GET', data = json_query, headers=self.headers)
        self.check_server_response(server_response, 'Get rows from entity')
        entity_data = server_response.json()
        if self.verbose:
            if entity_data['total'] >= entity_data['num']:
                if self.give_warnings:
                    warnings.warn(str(entity_data['total'])+' number of rows selected. Max number of rows to retrieve data for is set to '+str(entity_data['num'])+'.\n'
                                 +str(entity_data['num']-entity_data['total'])+' rows will not be in the results.')
            print 'Selected '+str(entity_data['total'])+' row(s).'
        return entity_data
    
    def update_entity_row(self, entity_name, query, data):
        '''Update an entity row
    
        Args:
            entity_name (string): Name of the entity to update
            query (list): List of dictionaries which contain query to select the row to update (see documentation of get_entity_rows)
            data (dict):  Key = column name, value = column value
        '''
        self.validate_data(entity_name, data)
        entity_data = self.get_entity_rows(entity_name, query)
        if len(entity_data['items']) == 0:
            raise BaseException('Query returned 0 results, no row to update.')
        elif len(entity_data['items']) > 1:
            raise BaseException('Query returned '+str(len(entity_data['items']))+' rows. Only updates on single rows supported.')
        id_attribute = self.get_id_attribute(entity_name)
        # select first element of items, because we know only one row can be selected for updating
        entity_items = entity_data['items'][0]
        # column values that are not given will be overwritten with null, so we need to add the existing column data into our dict
        for key in entity_items:
            if key != id_attribute and key not in data:
                data[key.encode('ascii')] = str(entity_items[key]).encode('ascii')
        id = entity_items[id_attribute]
        server_response = requests.put(self.api_url+'/'+entity_name+'/'+id+'/', data=str(data), headers=self.headers)
        self.check_server_response(server_response, 'Update entity row')
        return server_response
    
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
        self.check_server_response(server_response, 'Get meta data of entity')
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
        entity_meta_data = self.get_entity_meta_data(entity_name) 
        attributes = entity_meta_data['attributes']
        server_response = requests.get(self.api_url+'/'+entity_name+'/meta/'+column_name, headers=self.headers)
        self.check_server_response(server_response, 'Get meta data of column')
        column_meta_data = server_response.json()
        self.column_meta_data[entity_name+column_name] = column_meta_data
        return column_meta_data
    
    def get_column_type(self, entity_name, column_name):
        column_meta_data = self.get_column_meta_data(entity_name, column_name)
        return column_meta_data['fieldType']
    
connection = Connect_Molgenis('http://localhost:8080', 'admin', 'admin')
