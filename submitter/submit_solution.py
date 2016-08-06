#!/usr/bin/env python

import sys
import os
import re
import copy
#import subprocess
import subprocess32 as subprocess
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
#TIMEOUT = 10.0
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


CUR_CREATE_PROBLEMS = """
CREATE TABLE IF NOT EXISTS problems (problem_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, publish_time INTEGER, solution_size INTEGER, owner TEXT, problem_size INTEGER, problem_spec_hash TEXT, content TEXT)
"""
CUR_CREATE_SOLVES = """
CREATE TABLE IF NOT EXISTS solves (solve_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, job_id INTEGER NOT NULL, solver TEXT, problem_id INTEGER, solution TEXT, size TEXT, resemblance REAL, solution_spec_hash TEXT)
"""

INSERT_PROBLEMS = "INSERT INTO problems VALUES(?,?,?,?,?,?,?)"
INSERT_SOLVES = "INSERT INTO solves VALUES(NULL,?,?,?,?,?,?,?)"

con = sqlite3.connect(DB_FILE, check_same_thread=False)
cur = con.cursor()
cur_lock = threading.Lock()
#cur.execute(CUR_CREATE_JOBS)
cur.execute(CUR_CREATE_PROBLEMS)
cur.execute(CUR_CREATE_SOLVES)

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
			#self.stdo, self.stde = self.process.communicate()
			try:
				self.stdo, self.stde = self.process.communicate(timeout=timeout)
			except subprocess.TimeoutExpired:
				self.process.kill()
				self.stdo, self.stde = self.process.communicate()
			self.returncode = self.process.returncode
		t = threading.Thread(target=target)
		t.start()
		t.join(timeout)
		if t.is_alive():
			self.process.terminate()
			#self.process.kill()
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

def submit_solution(problem_id, content):
	with cur_lock:
		rc, o, e = Command("%s <<EOF\n%s\nEOF\n" % (API_SOLUTION % problem_id, content)).run(None)
		try:
			data = json.loads(o)
			if not data["ok"]:
				if re.search("Can not submit a solution to an own problem", data["error"]):
					print >>sys.stderr, "%d %s: skipped." % (problem_id, data["error"])
				else:
					print >>sys.stderr, "%d %s: retry %d" % (problem_id, data["error"], (n + 1))
				return
			cur.execute("UPDATE solves SET size=?, resemblance=?, solution_spec_hash=? WHERE problem_id=?", (data["solution_size"], data["resemblance"], data["solution_spec_hash"], data["problem_id"]))
			con.commit()
		except:
			print >>sys.stderr, "%d solution JSON error: retry %d" % (problem_id, (n + 1))

def main(args):
	if len(args) < 3:
		print >>sys.stderr, "Usage: %s problem_id solution_file" % os.path.basename(args[0])
		sys.exit(1)

	problem_id = int(args[1])
	content = open(args[2]).read()
	submit_solution(problem_id, content)

if __name__ == "__main__": main(sys.argv)





