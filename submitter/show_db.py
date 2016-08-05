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

BASE_PATH = "/home/futatsugi/develop/contests/icfpc2016"

CUR_CREATE_JOBS = """
CREATE TABLE IF NOT EXISTS jobs (job_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, task_type TEXT, submit_time TEXT, start_time TEXT, end_time TEXT, content TEXT)
"""
CUR_CREATE_PROBLEMS = """
CREATE TABLE IF NOT EXISTS problems (problem_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, publish_time INTEGER, solution_size INTEGER, owner INTEGER, problem_size INTEGER, problem_spec_hash TEXT)
"""
#CUR_CREATE_SOLUTIONS = """
#CREATE TABLE IF NOT EXISTS solutions (solution_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, content TEXT)
#"""
CUR_CREATE_SOLVES = """
CREATE TABLE IF NOT EXISTS solves (solve_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, job_id INTEGER NOT NULL, solver TEXT, solution TEXT, size TEXT, score REAL)
"""

INSERT_JOBS = "INSERT INTO jobs VALUES(NULL,?,?,?,?,?,?,?,?,NULL)"
INSERT_PROBLEMS = "INSERT INTO problems VALUES(?,?,?,?,?,?)"
#INSERT_SOLUTIONS = "INSERT INTO solutions VALUES(NULL,?,?)"
INSERT_SOLVES = "INSERT INTO solves VALUES(NULL,?,?,?,?,?)"

con = sqlite3.connect(os.path.join(BASE_PATH, "icfpc2016.sqlite3"), check_same_thread=False)
cur = con.cursor()
cur_lock = threading.Lock()
cur.execute(CUR_CREATE_JOBS)
cur.execute(CUR_CREATE_PROBLEMS)
#cur.execute(CUR_CREATE_SOLUTIONS)
cur.execute(CUR_CREATE_SOLVES)
#cur.execute("CREATE INDEX IF NOT EXISTS job_id_index ON solves(job_id)")

def show_jobs(job_id=None):
	with cur_lock:
		cur.execute("PRAGMA TABLE_INFO(jobs)")
		print ",".join(map(str, [row[1] for row in cur]))
		if job_id:
			cur.execute("SELECT * FROM jobs WHERE jobs_id=%d" % jobs_id)
			row = cur.fetchone()
			if row:
				print ",".join(map(str, row))
			else:
				print "No job_id: %d" % job_id
		else:
			cur.execute("SELECT * FROM jobs")
			for row in cur:
				print ",".join(map(str, row))
	

def show_problems(problem_id=None):
	with cur_lock:
		cur.execute("PRAGMA TABLE_INFO(problems)")
		print ",".join(map(str, [row[1] for row in cur]))
		if problem_id:
			cur.execute("SELECT * FROM problems WHERE problem_id=%d" % problem_id)
			row = cur.fetchone()
			if row:
				print ",".join(map(str, row))
			else:
				print "No problem_id: %d" % problem_id
		else:
			cur.execute("SELECT * FROM problems")
			for row in cur:
				print ",".join(map(str, row[:-1]))

def show_solves(problem_id=None):
	with cur_lock:
		cur.execute("PRAGMA TABLE_INFO(solves)")
		print ",".join(map(str, [row[1] for row in cur]))
		if problem_id:
			cur.execute("SELECT * FROM solves WHERE problem_id=%d" % problem_id)
			for row in cur:
				print ",".join(map(str, row))
			else:
				print "No problem_id: %d" % problem_id
		else:
			cur.execute("SELECT * FROM solves")
			for row in cur:
				print ",".join(map(str, row))

def main(args):
	if len(args) < 2:
		print "Usage: %s [jobs|problems]" % os.path.basename(args[0])
		sys.exit(1)

	target = args[1]
	uid = int(args[2]) if len(args) > 2 else None

	if target == "jobs":
		show_jobs(uid)
	elif target == "problems":
		show_problems(uid)
	elif target == "solves":
		show_solves(uid)

if __name__ == "__main__": main(sys.argv)
