<#-- @formatter:off -->
import requests
import json
import urllib
import os

try:
    from urllib.parse import quote_plus
except ImportError:
    # Python 2
    from urllib import quote_plus


class Session():
    '''Representation of a session with the MOLGENIS REST API.

    Usage:
    >>> session = molgenis.Session('http://localhost:8080/api/')
    >>> session.login('user', 'password')
    >>> session.get('Person')
    '''

    def __init__(self, url="http://localhost:8080/api/"):
        '''Constructs a new Session.
        Args:
        url -- URL of the REST API. Should be of form 'http[s]://<molgenis server>[:port]/api/'

        Examples:
        >>> connection = molgenis.Session('http://localhost:8080/api/')
        '''
        self.url = url

        self.session = requests.Session()

    def login(self, username, password):
        '''Logs in a user and stores the acquired session token in this Session object.

        Args:
        username -- username for a registered molgenis user
        password -- password for the user
        '''
        self.session.cookies.clear()
        response = self.session.post(self.url + "v1/login",
                                     data=json.dumps({"username": username, "password": password}),
                                     headers={"Content-Type": "application/json"})
        if response.status_code == 200:
            self.token = response.json()["token"]
        response.raise_for_status()
        return response

    def logout(self):
        '''Logs out the current session token.'''
        response = self.session.post(self.url + "v1/logout",
                                     headers=self._get_token_header())
        if response.status_code == 200:
            self.token = None
        response.raise_for_status()
        return response

    def getById(self, entity, id, attributes=None, expand=None):
        '''Retrieves a single entity row from an entity repository.

        Args:
        entity -- fully qualified name of the entity
        id -- the value for the idAttribute of the entity
        attributes -- The list of attributes to retrieve
        expand -- the attributes to expand

        Examples:
        session.get('Person', 'John')
        '''
        response = self.session.get(self.url + "v1/" + quote_plus(entity) + '/' + quote_plus(id),
                                    headers=self._get_token_header(),
                                    params={"attributes": attributes, "expand": expand})
        if response.status_code == 200:
            return response.json()
        response.raise_for_status()
        return response

    def get(self, entity, q=None, attributes=None, expand=None, num=100, start=0, sortColumn=None, sortOrder=None):
        '''Retrieves entity rows from an entity repository.

        Args:
        entity -- fully qualified name of the entity
        q -- query in json form, see the MOLGENIS REST API v1 documentation for details
        attributes -- The list of attributes to retrieve
        expand -- the attributes to expand
        num -- the amount of entity rows to retrieve
        start -- the index of the first row to retrieve (zero indexed)
        sortColumn -- the attribute to sort on
        sortOrder -- the order to sort in

        Examples:
        session.get('Person')
        '''
        if q:
            response = self.session.post(self.url + "v1/" + quote_plus(entity),
                                         headers=self._get_token_header_with_content_type(),
                                         params={"_method": "GET"},
                                         data=json.dumps(
                                             {"q": q, "attributes": attributes, "expand": expand, "num": num,
                                              "start": start,
                                              "sortColumn": sortColumn, "sortOrder": sortOrder}))
        else:
            response = self.session.get(self.url + "v1/" + quote_plus(entity),
                                        headers=self._get_token_header(),
                                        params={"attributes": attributes, "expand": expand, "num": num, "start": start,
                                                "sortColumn": sortColumn, "sortOrder":
                                                    sortOrder})
        if response.status_code == 200:
            return response.json()["items"]
        response.raise_for_status()
        return response

    def add(self, entity, data={}, files={}, **kwargs):
        '''Adds a single entity row to an entity repository.

        Args:
        entity -- fully qualified name of the entity
        files -- dictionary containing file attribute values for the entity row.
        The dictionary should for each file attribute map the attribute name to a tuple containing the file name and an
        input stream.
        data -- dictionary mapping attribute name to non-file attribute value for the entity row, gets merged with the
        kwargs argument
        **kwargs -- keyword arguments get merged with the data argument

        Examples:
        >>> session.add('Person', firstName='Jan', lastName='Klaassen')
        >>> session.add('Person', {'firstName': 'Jan', 'lastName':'Klaassen'})

        You can have multiple file type attributes.

        >>> session.add('Plot', files={'image': ('expression.jpg', open('~/first-plot.jpg','rb')),
        'image2': ('expression-large.jpg', open('/Users/me/second-plot.jpg', 'rb'))},
        data={'name':'IBD-plot'})
        '''
        response = self.session.post(self.url + "v1/" + quote_plus(entity),
                                     headers=self._get_token_header(),
                                     data=self._merge_two_dicts(data, kwargs),
                                     files=files)
        if response.status_code == 201:
            return response.headers["Location"].split("/")[-1]
        response.raise_for_status()
        return response

    def add_all(self, entity, entities):
        '''Adds multiple entity rows to an entity repository.'''
        response = self.session.post(self.url + "v2/" + quote_plus(entity),
                                     headers=self._get_token_header_with_content_type(),
                                     data=json.dumps({"entities": entities}))
        if response.status_code == 201:
            return [resource["href"].split("/")[-1] for resource in response.json()["resources"]]
        response.raise_for_status()
        return response

    def update_one(self, entity, id, attr, value):
        '''Updates one attribute of a given entity in a table with a given value'''
        response = self.session.put(self.url + "v1/" + quote_plus(entity) + "/" + id + "/" + attr,
                                    headers=self._get_token_header_with_content_type(),
                                    data=json.dumps(value))
        response.raise_for_status()
        return response

    def delete(self, entity, id):
        '''Deletes a single entity row from an entity repository.'''
        response = self.session.delete(self.url + "v1/" + quote_plus(entity) + "/" + quote_plus(id), headers=
        self._get_token_header())
        response.raise_for_status()
        return response

    def delete_list(self, entity, entities):
        '''Deletes multiple entity rows to an entity repository, given a list of id's.'''
        response = self.session.delete(self.url + "v2/" + quote_plus(entity),
                                       headers=self._get_token_header_with_content_type(),
                                       data=json.dumps({"entityIds": entities}))
        response.raise_for_status()
        return response

    def get_entity_meta_data(self, entity):
        '''Retrieves the metadata for an entity repository.'''
        response = self.session.get(self.url + "v1/" + quote_plus(entity) + "/meta?expand=attributes", headers=
        self._get_token_header())
        response.raise_for_status()
        return response.json()

    def get_attribute_meta_data(self, entity, attribute):
        '''Retrieves the metadata for a single attribute of an entity repository.'''
        response = self.session.get(self.url + "v1/" + quote_plus(entity) + "/meta/" + quote_plus(attribute), headers=
        self._get_token_header())
        response.raise_for_status()
        return response.json()

    def upload_zip(self, meta_data_zip):
        '''Uploads a given zip with data and metadata'''
        header = self._get_token_header()
        files = {'file': open(os.path.abspath(meta_data_zip), 'rb')}
        url = self.url.strip('/api/') + '/plugin/importwizard/importFile'
        response = requests.post(url, headers=header, files=files)
        if response.status_code == 201:
            return response.json()
        response.raise_for_status()
        return response

    def _get_token_header(self):
        '''Creates an 'x-molgenis-token' header for the current session.'''
        try:
            return {"x-molgenis-token": self.token}
        except AttributeError:
            return {}

    def _get_token_header_with_content_type(self):
        '''Creates an 'x-molgenis-token' header for the current session and a 'Content-Type: application/json' header'''
        headers = self._get_token_header()
        headers.update({"Content-Type": "application/json"})
        return headers

    @staticmethod
    def _merge_two_dicts(x, y):
        '''Given two dicts, merge them into a new dict as a shallow copy.'''
        z = x.copy()
        z.update(y)
        return z
<#-- @formatter:on -->