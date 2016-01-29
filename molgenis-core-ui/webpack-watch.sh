#!/bin/sh
cd $(dirname $0)
"./node/node" "./node_modules/.bin/webpack" -d --watch
