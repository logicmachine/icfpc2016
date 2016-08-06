#!/usr/bin/env python

import sys
import os
import re
import copy
import subprocess
import threading
import datetime
import time
import heapq
import urllib
import urllib2
import logging
import json
import glob
import signal
import ssl
import shlex
import random
import shutil
import csv
import codecs
import pprint
import collections
import sqlite3
import cgi
import tempfile

#BASE_PATH = "/home/futatsugi/develop/contests/icfpc2016"
#BASE_PATH = ".."
PROBLEMS_PATH = "../problems"
RESEMBLANCE_CALCULATOR = "../approx/resemblance"
DB_FILE = "icfpc2016.sqlite3"
TIMEOUT = 180.0
NUM_RETRY = 5
MAX_RUNNING = 4
QUEUE_WAIT = 0.0

API_KEY = "56-c0d0425216599ecb557d45138c644174"
#API_URL = "http://2016sv.icfpcontest.org/api"

# Hello, world!
API_HELLO = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' 'http://2016sv.icfpcontest.org/api/hello'"

# Blob Lookup
# (hash)
API_BLOB = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' 'http://2016sv.icfpcontest.org/api/blob/%s'"

# Contest Status Snapshot Query
API_SNAPSHOT = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' 'http://2016sv.icfpcontest.org/api/snapshot/list'"

# Problem Submission
# (solution.txt, publish_time)
API_PROBLEM = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' -F 'solution_spec=@%s' -F 'publish_time=%d' 'http://2016sv.icfpcontest.org/api/problem/submit'"

# Solution Submission
# (problem_id, solution.txt)
#API_SOLUTION = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' -F 'problem_id=%d' -F 'solution_spec=@%s' 'http://2016sv.icfpcontest.org/api/solution/submit'"
API_SOLUTION = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' -F 'problem_id=%d' -F 'solution_spec=@-' 'http://2016sv.icfpcontest.org/api/solution/submit'"

class Command:
	def __init__(self, cmd):
		self.cmd = cmd
		self.process = None
		self.stdo = ""
		self.stde = ""
		self.returncode = 0

	def run(self, timeout):
		def target():
			#self.process = subprocess.Popen(shlex.split(self.cmd.encode("utf-8")), shell=False, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			self.process = subprocess.Popen(self.cmd.encode("utf-8"), shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			self.stdo, self.stde = self.process.communicate()
			self.returncode = self.process.returncode
		t = threading.Thread(target=target)
		t.start()
		t.join(timeout)
		if t.is_alive():
			self.process.terminate()
			t.join()
		return self.returncode, self.stdo, self.stde

def perform(command):
	try:
		p = subprocess.Popen(shlex.split(command.encode("utf-8")), shell=False, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	except:
		p = None
	stdo, stde = "", ""
	if p:
		stdo, stde = p.communicate()
		rc = p.returncode
	else:
		rc = 1
	return rc, stdo, stde

def main(args):
	#print "Reading snapshots..."
	#rc, o, e = perform(API_SNAPSHOT)
	rc, o, e = Command(API_SNAPSHOT).run(None)
	snapshots = json.loads(o)["snapshots"]
	snapshots = [(snapshot["snapshot_time"], snapshot["snapshot_hash"]) for snapshot in snapshots]
	snapshots.sort()
	last_snapshot = snapshots[-1][1]
	time.sleep(1.0)

	#print "Reading problems..."
	#rc, o, e = perform(API_BLOB % last_snapshot)
	rc, o, e = Command(API_BLOB % last_snapshot).run(None)

	data = []
	problems = json.loads(o)["problems"]
	hash_problem = collections.defaultdict(list)
	problem_hash = {}
	for problem in problems:
		#print problem["problem_id"], problem["problem_spec_hash"]
		cnt_perfect = 0
		for team in problem["ranking"]:
			if team["resemblance"] == 1.0:
				cnt_perfect += 1
		solution_size = problem["solution_size"]
		score = solution_size / (1.0 + cnt_perfect)
		data.append((score, problem["problem_id"], problem["problem_spec_hash"], solution_size, cnt_perfect, len(problem["ranking"])))
		hash_problem[problem["problem_spec_hash"]].append((problem["problem_id"], score))
		problem_hash[problem["problem_id"]] = problem["problem_spec_hash"]

	data.sort(reverse=True)
	memo = []
	ans = []
	for v in data:
		if v[1] in memo: continue
		score = 0.0
		problems = []
		for vv in hash_problem[problem_hash[v[1]]]:
			problems.append(vv[0])
			score += vv[1]
		ans.append((score, tuple(problems)))
		#print v[0], hash_problem[problem_hash[v[1]]]
		memo.extend(hash_problem[problem_hash[v[1]]])

	ans = list(set(ans))
	ans.sort(reverse=True)
	for xs in ans:
		print xs[0], xs[1]

if __name__ == "__main__": main(sys.argv)
