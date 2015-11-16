import molgenis
import requests

c = molgenis.Session('http://localhost:8080/api/')
c.login('admin','secret')

print c.token

file_path = '/Users/fkelpin/Downloads/ninjas.jpeg'

'''requests.post('http://localhost:8080/api/v1/DeconvolutionPlot',
	files={'image': ('nindjas.jpg', open(file_path,'rb')), 'image2': ('nog meer ninjas.jpg', open(file_path, 'rb'))}, 
	data={'name':'ninjas', 'disease':'disease', 'gene':'gene', 'snp': 'snp'},
	headers=c._get_token_header())

c.add('DeconvolutionPlot',
	files={
		'image': ('nindjas.jpg', open(file_path,'rb')), 
		'image2': ('nog meer ninjas.jpg', open(file_path, 'rb'))
	}, 
	data={'name':'ninjas', 'disease':'disease', 'gene':'gene', 'snp': 'snp'})
'''

try:
	c.add('ScriptParameter', data={'name': 'blah'})
except requests.exceptions.HTTPError as e:
	print e.response.json()