BeaconAlleleRequest = {
    'referenceName': u'',  # required
    'start': 0,  # required
    'referenceBases': u'',  # required
    'alternateBases': u'',  # required
    'assemblyId': '',  # required
    'datasetIds': [],  # optional
    'includeDatasetResponses': False,  # optional
}

BeaconDataset = {
    'id': u'',  # required
    'name': u'',  # required
    'description': u'',  # optional
    'assemblyId': u'',  # required
    'createDateTime': u'',  # required
    'updateDateTime': u'',  # required
    'version': u'',  # optional
    'variantCount': 1,  # optional
    'callCount': 1,  # optional
    'sampleCount': 1,  # optional
    'externalUrl': u'',  # optional
    'info': {}  # optional
}

BeaconOrganization = {
    'id': u'',  # required
    'name': u'',  # required
    'description': u'',  # optional
    'address': u'',  # optional
    'welcomeUrl': u'',  # optional
    'contactUrl': u'',  # optional
    'logoUrl': u'',  # optional
    'info': {}  # optional
}

Beacon = {
    'id': u'',  # required
    'name': u'',  # required
    'apiVersion': u'0.3.0',  # required
    'organization': BeaconOrganization,  # required
    'description': u'',  # optional
    'version': u'',  # optional
    'welcomeUrl': u'',  # optional
    'alternativeUrl': u'',  # optional
    'createDateTime': u'',  # optional
    'updateDateTime': u'',  # optional
    'datasets': [  # optional
        BeaconDataset
    ],
    'sampleAlleleRequests': [  # optional
        BeaconAlleleRequest  # Examples of interesting queries
    ],
    'info': {}
}

BeaconError = {
    'errorCode': 400,  # required
    'message': u''  # optional
}

BeaconDatasetAlleleResponse = {
    'datasetId': u'',  # required
    'exists': True,  # optional (required in no-error cases)
    'error': None,  # optional (required in case of an error)
    'frequency': 1.0,  # optional
    'variantCount': 1,  # optional
    'callCount': 1,  # optional
    'sampleCount': 1,  # optional
    'note': u'',  # optional
    'externalUrl': u'',  # optional
    'info': {}  # optional
}

BeaconAlleleResponse = {
    'beaconId': u'',  # required
    'exists': True,  # optional (required in no-error cases)
    'error': None,  # optional (required in case of an error)
    'alleleRequest': BeaconAlleleRequest,  # optional
    'datasetAlleleResponses': [  # optional
        BeaconDatasetAlleleResponse
    ]
}
