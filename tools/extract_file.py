#!/usr/bin/env python

from sys import argv, stdout
import re

BEGIN_FORMAT = "--- Begin {0} ---"
END_FORMAT = "--- End {0} ---"

filename = argv[1]
sought_file = argv[2]
begin_tag = BEGIN_FORMAT.format(sought_file)
end_tag = END_FORMAT.format(sought_file)

output = stdout
found_file = False
with open(filename, "r") as infile:
    for line in infile:
        if begin_tag in line:
            found_file = True
            continue
        if end_tag in line:
            found_file = False
            continue

        if found_file:
            output.write(line)