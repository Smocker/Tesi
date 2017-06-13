#!/usr/bin/env python

import sys
import os
import time
import subprocess
import commands
from datetime import datetime


if len(sys.argv) == 2:
    pid = sys.argv[1]
else:
    print('No PID specified. Usage: %s <PID>' % os.path.basename(__file__))
    sys.exit(1)


def get_cpumem(pid):
	command = 'ps aux  | grep %s ' % pid 
	d = commands.getoutput(command ).split("\n")
	return ((d[0].split()[2]), (d[0].split()[3]))

f = True
flag = False
ff = False
i=0
while True:
	d = get_cpumem(pid)[0]
	n = d.replace(",", ".")
	if float(n) > 75 and f:
		f=False
		flag = True
		start = datetime.now()
	if float(n) < 75 and flag:
		flag=False
		ff = True
	if ff:
		i+=1
		ff = False
		dt = datetime.now() - start
		exe = (dt.days * 24 * 60 * 60 + dt.seconds) * 1000 + dt.microseconds / 1000.0
		print str(i) +") "+str(exe)
	
