from __future__ import unicode_literals
import requests
import json
import argparse

parser = argparse.ArgumentParser(description="""Retrieves pull request numbers and titles from the GitHub REST API.
                                                Only merged pull requests will be listed.""")
parser.add_argument('-t', '--token', required=True,
                    help="""Generate an OAUTH token on your GitHub personal settings page, under Personal access tokens,
                            and provide it as an argument here. You need not give it any scopes.""")
parser.add_argument('-b', '--branch', default='master', help='The branch that you want to generate pull requests for')
parser.add_argument('-p', '--pages', type=int, help="""Number of pull request pages you want to retrieve.
                                                       A page contains 30 items.""")
args = parser.parse_args()

page = 1
more = True
while more and (args.pages is None or page < args.pages):
    url = 'https://api.github.com/repos/molgenis/molgenis/pulls?state=closed&base={branch}&page={page}' \
        .format(page=page,
                branch=args.branch)
    response = requests.get(url, headers={'Authorization': 'token ' + args.token})
    response.raise_for_status()
    pulls = json.loads(response.text, encoding='utf-8')
    if not pulls:
        more = False
    else:
        for pull in pulls:
            pullUrl = 'https://api.github.com/repos/molgenis/molgenis/pulls/{number}/merge'.format(**pull)
            mergeResponse = requests.get(pullUrl, headers={'Authorization': 'token ' + args.token})
            if mergeResponse.status_code == 204:
                print('#{number} | {title}'.format(**pull))
        page += 1