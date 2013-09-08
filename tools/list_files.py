#!/usr/bin/env python

from sys import argv, stdout
import re

BEGIN_TAG = "--- Begin "
BEGIN_SUFFIX = " ---"

filename = argv[1]
occurances = {}
with open(filename, "r") as infile:
    for line in infile:
        if BEGIN_TAG in line:
            block_name = line.strip()
            block_name = block_name.replace(BEGIN_TAG, "")
            block_name = block_name.replace(BEGIN_SUFFIX, "")
            if not block_name in occurances:
                occurances[block_name] = 1
            else:
                occurances[block_name] += 1

for key in occurances:
    print('"{0}": {1}'.format(key, occurances[key]))