import requests
import unittest

import molgenis


class TestStringMethods(unittest.TestCase):
    api_url = "https://molgenis62.gcc.rug.nl/api/"

    no_count_permission_user_msg = 'No [COUNT] permission on entity type [User] with id [sys_sec_User]'
    user_entity = 'sys_sec_User'
    ref_entity = 'it_emx_datatypes_TypeTestRef'

    def test_login_and_get_superuser_username(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        response = s.get(self.user_entity, q=[{"field": "superuser", "operator": "EQUALS", "value": "true"}])
        self.assertEqual('admin', response[0]['username'])

    def test_login_logout_and_get_MolgenisUser(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        response = s.get(self.user_entity)
        s.logout()
        try:
            response = s.get(self.user_entity)
        except requests.exceptions.HTTPError as e:
            self.assertEqual(e.response.status_code, 401)
            self.assertEqual(e.response.json()['errors'][0]['message'], self.no_count_permission_user_msg)

    def test_no_login_and_get_MolgenisUser(self):
        s = molgenis.Session(self.api_url)
        try:
            s.get(self.user_entity)
        except requests.exceptions.HTTPError as e:
            self.assertEqual(e.response.status_code, 401)
            self.assertEqual(e.response.json()['errors'][0]['message'], self.no_count_permission_user_msg)

    def test_add_all(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        try:
            s.delete(self.ref_entity, 'ref55')
            s.delete(self.ref_entity, 'ref57')
        except Exception as e:
            print(str(e))
        response = s.add_all(self.ref_entity,
                             [{"value": "ref55", "label": "label55"}, {"value": "ref57", "label": "label57"}])
        self.assertEqual(['ref55', 'ref57'], response)
        item55 = s.get(self.ref_entity, q=[{"field": "value", "operator": "EQUALS", "value": "ref55"}])[0]
        self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/" + self.ref_entity + "/ref55"},
                         item55)
        s.delete(self.ref_entity, 'ref55')
        s.delete(self.ref_entity, 'ref57')

    def test_add_dict(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        try:
            s.delete(self.ref_entity, 'ref55')
        except Exception as e:
            print(str(e))
        self.assertEqual('ref55', s.add(self.ref_entity, {"value": "ref55", "label": "label55"}))
        s.delete(self.ref_entity, 'ref55')

    def test_update_one(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        try:
            s.delete(self.ref_entity, 'ref55')
        except Exception as e:
            print(str(e))
        self.assertEqual('ref55', s.add(self.ref_entity, {"value": "ref55", "label": "label55"}))
        try:
            s.update_one(self.ref_entity, 'ref55', 'label', 'updated-label55');
        except Exception as e:
            print(str(e))
        item55 = s.getById(self.ref_entity, "ref55", ["label"])
        self.assertEqual("updated-label55", item55["label"])
        s.delete(self.ref_entity, 'ref55')

    def test_add_kwargs(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        try:
            s.delete(self.ref_entity, 'ref55')
        except Exception as e:
            print(str(e))
        self.assertEqual('ref55', s.add(self.ref_entity, value="ref55", label="label55"))
        item55 = s.get(self.ref_entity, q=[{"field": "value", "operator": "EQUALS", "value": "ref55"}])[0]
        self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/" + self.ref_entity + "/ref55"},
                         item55)
        s.delete(self.ref_entity, 'ref55')

    def test_add_merge_dict_kwargs(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        try:
            s.delete(self.ref_entity, 'ref55')
        except Exception as e:
            print(str(e))
        self.assertEqual('ref55', s.add(self.ref_entity, {"value": "ref55"}, label="label55"))
        item55 = s.get(self.ref_entity, q=[{"field": "value", "operator": "EQUALS", "value": "ref55"}])[0]
        self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/" + self.ref_entity + "/ref55"},
                         item55)
        s.delete(self.ref_entity, 'ref55')

    def test_get_meta(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        meta = s.get_entity_meta_data(self.user_entity)
        self.assertEqual('username', meta['labelAttribute'])

    def test_get_attribute_meta(self):
        s = molgenis.Session(self.api_url)
        s.login('admin', 'admin')
        meta = s.get_attribute_meta_data(self.user_entity, 'username')
        self.assertEqual({'isAggregatable': False, 'attributes': [], 'auto': False,
                          'fieldType': 'STRING', 'href': '/api/v1/' + self.user_entity + '/meta/username',
                          'label': 'Username',
                          'labelAttribute': True, 'lookupAttribute': True, 'maxLength': 255, 'nillable': False,
                          'readOnly': False, 'unique': True, 'visible': True, 'name': 'username', 'enumOptions': []},
                         meta)

if __name__ == '__main__':
    unittest.main()
