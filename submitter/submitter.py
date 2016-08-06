#!/usr/bin/env python

import sys
import os
import re
import copy
import subprocess
#import subprocess32 as subprocess
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
#TIMEOUT = 180.0
TIMEOUT = 15.0
#TIMEOUT = None
NUM_RETRY = 5
MAX_RUNNING = 4
#MAX_RUNNING = 8
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


CUR_CREATE_JOBS = """
CREATE TABLE IF NOT EXISTS jobs (job_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, status TEXT, task_type TEXT, submit_time TEXT, start_time TEXT, end_time TEXT, content TEXT)
"""
CUR_CREATE_PROBLEMS = """
CREATE TABLE IF NOT EXISTS problems (problem_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, publish_time INTEGER, solution_size INTEGER, owner TEXT, problem_size INTEGER, problem_spec_hash TEXT, content TEXT)
"""
#CUR_CREATE_SOLUTIONS = """
#CREATE TABLE IF NOT EXISTS solutions (solution_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, content TEXT)
#"""
CUR_CREATE_SOLVES = """
CREATE TABLE IF NOT EXISTS solves (solve_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, job_id INTEGER NOT NULL, solver TEXT, problem_id INTEGER, solution TEXT, size TEXT, resemblance REAL, solution_spec_hash TEXT)
"""

INSERT_JOBS = "INSERT INTO jobs VALUES(NULL,?,?,?,?,?,?)"
INSERT_PROBLEMS = "INSERT INTO problems VALUES(?,?,?,?,?,?,?)"
#INSERT_SOLUTIONS = "INSERT INTO solutions VALUES(NULL,?,?)"
INSERT_SOLVES = "INSERT INTO solves VALUES(NULL,?,?,?,?,?,?,?)"

#con = sqlite3.connect(os.path.join(BASE_PATH, DB_FILE), check_same_thread=False)
con = sqlite3.connect(DB_FILE, check_same_thread=False)
cur = con.cursor()
cur_lock = threading.Lock()
cur.execute(CUR_CREATE_JOBS)
cur.execute(CUR_CREATE_PROBLEMS)
#cur.execute(CUR_CREATE_SOLUTIONS)
cur.execute(CUR_CREATE_SOLVES)
#cur.execute("CREATE INDEX IF NOT EXISTS job_id_index ON solves(job_id)")

class QueueThread(threading.Thread):
	def __init__(self):
		self.queue = []
		self.is_running = True
		self.lock = threading.Lock()
		self.threads = {}
		threading.Thread.__init__(self)

	def run(self):
		while self.is_running:
			self.lock.acquire()
			while len(self.queue) > 0 and self.size() < MAX_RUNNING:
				[priority, job_id, commands] = heapq.heappop(self.queue)
				t = run_job(job_id,commands)
				self.threads[job_id] = t
				time.sleep(QUEUE_WAIT)
			self.lock.release()
			time.sleep(0.1)

	def finish(self):
		self.is_running = False

	def push(self, job_id, commands, priority=10):
		self.lock.acquire()
		entry = [priority, job_id, commands]
		heapq.heappush(self.queue, entry)
		self.lock.release()

	def size(self):
		return len(self.threads)

	def remove(self, job_id):
		if job_id in self.threads:
			self.lock.acquire()
			del self.threads[job_id]
			self.lock.release()

	def kill(self, job_id):
		if job_id in self.threads:
			self.lock.acquire()
			self.threads[job_id].process.kill()
			del self.threads[job_id]
			self.lock.release()

pool = threading.Semaphore(MAX_RUNNING)
queue_thread = QueueThread()
queue_thread.start()

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
			self.process = subprocess.Popen(self.cmd.encode("utf-8"), shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, preexec_fn=os.setsid)
			self.stdo, self.stde = self.process.communicate()
			"""
			# using subprocess32
			try:
				self.stdo, self.stde = self.process.communicate(timeout=timeout)
			except subprocess.TimeoutExpired:
				self.process.kill()
				self.stdo, self.stde = self.process.communicate()
			"""
			self.returncode = self.process.returncode
		t = threading.Thread(target=target)
		t.start()
		t.join(timeout)
		if t.is_alive():
			self.process.terminate()
			#os.killpg(os.getpgid(self.process.pid), signal.SIGTERM)
			os.killpg(self.process.pid, signal.SIGTERM)
			#self.process.pid
			#os.killpg(os.getpgid(self.process.pid), signal.SIGINT)
			#os.kill(self.process.pid, signal.SIGINT)
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
		
def run_job(job_id, commands):
	class ExecThread(threading.Thread):
		def __init__(self, commands):
			self.job_id = job_id
			self.commands = commands
			self.response = []
			#self.process = None
			threading.Thread.__init__(self)

		def run(self):
			with pool:
				dt = datetime.datetime.utcnow().isoformat().split(".")[0] + "Z"
				with cur_lock:
					#cur.execute("SELECT status FROM jobs WHERE job_id='%s'" % self.job_id)
					cur.execute("SELECT status, task_type FROM jobs WHERE job_id=?", (self.job_id,))
					row = cur.fetchone()
					task_type = row[1]
					if row and row[0] == "submitted":
						cur.execute("UPDATE jobs SET start_time=?, status='running' WHERE job_id=?", (dt, self.job_id))
						con.commit()
				rc = 0 # returncode
				for command in self.commands:
					#returncode, stdo, stde = perform(command)
					returncode, stdo, stde = Command(command).run(TIMEOUT)
					#print command ##### debug_nf
					#print stdo, stde ##### debug_nf
					rc |= returncode
					self.response.append((stdo.rstrip(), stde.rstrip()))
				job_finish(self.job_id)

		def get_response(self):
			return self.response

	t = ExecThread(commands)
	t.daemon = True
	t.start()
	return t

def job_finish(job_id):
	data = queue_thread.threads[job_id].get_response()
	content = ""
	for o, e in data:
		#print o
		#content = json.loads(o)
		content += o
	queue_thread.remove(job_id)

	dt = datetime.datetime.utcnow().isoformat().split(".")[0] + "Z"
	cur.execute("UPDATE jobs SET end_time=?, status=?, content=? WHERE job_id=?", (dt, "complete", content, job_id))
	con.commit()

	solution_tempfile = tempfile.NamedTemporaryFile()
	f = open(solution_tempfile.name, "wb")
	f.write(content)
	f.close()
	is_solved = False
	with cur_lock:
		cur.execute("SELECT content FROM problems WHERE problem_id=?", (problem_id,))
		problem = cur.fetchone()[0]
		cur.execute("SELECT task_type FROM jobs WHERE job_id=?", (job_id,))
		row = cur.fetchone()
		if row:
			task_type = row[0]
			data = task_type.split(":")
			if len(data) == 3 and data[0] == "solver":
				solver = data[1]
				problem_id = int(data[2])
				solution_size = len(content.replace(" ", "").replace("\n", ""))
				is_solved = True

	if not is_solved: return
		
	problem_tempfile = tempfile.NamedTemporaryFile()
	f2 = open(problem_tempfile.name, "wb")
	f2.write(problem)
	f2.close()
	rc, o, e = Command("%s %s %s" % (RESEMBLANCE_CALCULATOR, problem_tempfile.name, solution_tempfile.name)).run(None)
	calc_resemblance = float(o.strip()) if (o.strip().count(".") <= 1 and o.strip().replace(".", "").isdigit()) else 0.0

	with cur_lock:
		"""
		cur.execute("SELECT task_type FROM jobs WHERE job_id=?", (job_id,))
		row = cur.fetchone()
		if row:
			task_type = row[0]
			data = task_type.split(":")
			if len(data) == 3 and data[0] == "solver":
				solver = data[1]
				problem_id = int(data[2])
				solution_size = len(content.replace(" ", "").replace("\n", ""))
		"""

		"""
				solution_tempfile = tempfile.NamedTemporaryFile()
				f = open(solution_tempfile.name, "wb")
				f.write(content)
				f.close()
				cur.execute("SELECT content FROM problems WHERE problem_id=?", (problem_id,))
				problem = cur.fetchone()[0]
				problem_tempfile = tempfile.NamedTemporaryFile()
				f2 = open(problem_tempfile.name, "wb")
				f2.write(problem)
				f2.close()
				rc, o, e = Command("%s %s %s" % (RESEMBLANCE_CALCULATOR, problem_tempfile.name, solution_tempfile.name)).run(None)
				calc_resemblance = float(o.strip()) if (o.strip().count(".") <= 1 and o.strip().replace(".", "").isdigit()) else 0.0
		"""

		if is_solved:
			if True:
				cur.execute("SELECT resemblance, size FROM solves WHERE problem_id=?", (problem_id,))
				do_calc = True
				for row in cur:
					prev_resemblance = row[0]
					if prev_resemblance == 1.0 and int(row[1]) <= int(solution_size):
						do_calc = False
						break
					if row[0] + 0.001 > calc_resemblance and row[0] != 1.0:
						do_calc = False
						break

				if do_calc and solution_size <= 5000:
					cur.execute(INSERT_SOLVES, (job_id, solver, problem_id, content, solution_size, 0.0, ""))
					con.commit()
	
					for n in range(NUM_RETRY):
						#rc, o, e = Command(API_SOLUTION % (problem_id, solution_file)).run(None)
						rc, o, e = Command("%s <<EOF\n%s\nEOF\n" % (API_SOLUTION % problem_id, content)).run(None)
						#run_job_task("solution_submit", ["%s <<EOF\n%s\nEOF\n" % (API_SOLUTION % problem_id, content)])
						try:
							data = json.loads(o)
							if not data["ok"]:
								if re.search("Can not submit a solution to an own problem", data["error"]):
									print >>sys.stderr, "%d %s: skipped." % (problem_id, data["error"])
									break
								print >>sys.stderr, "%d %s: retry %d" % (problem_id, data["error"], (n + 1))
								time.sleep(1.0)
								continue
							cur.execute("UPDATE solves SET size=?, resemblance=?, solution_spec_hash=? WHERE problem_id=?", (data["solution_size"], data["resemblance"], data["solution_spec_hash"], data["problem_id"]))
							con.commit()
							break
						except:
							print >>sys.stderr, "%d solution JSON error: retry %d" % (problem_id, (n + 1))
							time.sleep(1.0)
				else:
					if not do_calc:
						print >>sys.stderr, "%d prev_resemblance = %f, calc_resemblance = %f." % (problem_id, prev_resemblance, calc_resemblance)
					elif solution_size > 5000:
						print >>sys.stderr, "%d solution size is >5000." % problem_id
		"""
		dt = datetime.datetime.utcnow().isoformat().split(".")[0] + "Z"
		cur.execute("UPDATE jobs SET end_time=?, status=?, content=? WHERE job_id=?", (dt, "complete", content, job_id))
		con.commit()
		"""

def run_job_task(task_type, commands):
	with cur_lock:
		dt = datetime.datetime.utcnow().isoformat().split(".")[0] + "Z"
		cur.execute(INSERT_JOBS, ("submitted", task_type, dt, "", "", ""))
		con.commit()
	job_id = cur.lastrowid
	queue_thread.push(job_id, commands)

def solve_problems(solver):
	with cur_lock:
		cur.execute("SELECT problem_id, content FROM problems")
		problems = [(row[0], row[1]) for row in cur]

	for problem_id, problem in problems:
		run_job_task("solver:%s:%d" %(solver, problem_id), ["%s <<EOF\n%s\nEOF\n" % (solver, problem)])

def cleanup_db():
	with cur_lock:
		cur.execute("UPDATE jobs SET status='canceled', content='canceled' WHERE status='running' OR status='submitted'")
		con.commit()

def signal_handler(signum, frame):
	print("Called signal handler with signal %d" % signum)
	queue_thread.finish()
	queue_thread.join()
	sys.exit()

def main(args):
	if len(args) < 2:
		print >>sys.stderr, "Usage: %s solver" % os.path.basename(args[0])
		print >>sys.stderr, "  To quit, CTRL+\\"
		sys.exit(1)

	solver = args[1]

	signal.signal(signal.SIGINT, signal_handler)
	#signal.signal(signal.SIGQUIT, signal_handler)
	cleanup_db()

	print "Reading snapshots..."
	#rc, o, e = perform(API_SNAPSHOT)
	rc, o, e = Command(API_SNAPSHOT).run(None)
	snapshots = json.loads(o)["snapshots"]
	snapshots = [(snapshot["snapshot_time"], snapshot["snapshot_hash"]) for snapshot in snapshots]
	snapshots.sort()
	last_snapshot = snapshots[-1][1]
	time.sleep(1.0) # for wait to avoid time-limit

	print "Reading problems..."
	#rc, o, e = perform(API_BLOB % last_snapshot)
	rc, o, e = Command(API_BLOB % last_snapshot).run(None)
	problems = json.loads(o)["problems"]
	for problem in problems:
		print problem["problem_id"], problem["problem_spec_hash"]
		filename = "problem_%05d_%05d_%05d.txt" % (problem["problem_id"], problem["problem_size"], problem["solution_size"])
		#filename = os.path.join(BASE_PATH, "problems", filename)
		filename = os.path.join(PROBLEMS_PATH, filename)
		if not os.path.isfile(filename):
			for n in range(NUM_RETRY):
				#rc, o, e = perform(API_BLOB % problem["problem_spec_hash"])
				rc, o, e = Command(API_BLOB % problem["problem_spec_hash"]).run(None)
				if re.search("html", o):
					print >>sys.stderr, "Error downloading problem: retry %d" % (n + 1)
					time.sleep(1.0) # for wait to avoid time-limit
					continue
				break
			if n < 4:
				f = open(filename, "wb")
				f.write(o)
				f.close()
		f = open(filename)
		content = f.read()
		with cur_lock:
			cur.execute("SELECT * FROM problems WHERE problem_id=%d" % problem["problem_id"])
			row = cur.fetchone()
			if not row:
				cur.execute(INSERT_PROBLEMS, (problem["problem_id"], problem["publish_time"], problem["solution_size"], problem["owner"], problem["problem_size"], problem["problem_spec_hash"], content))
				con.commit()
		f.close()

	print "Running program..."
	#run_job_task("test", ["ls"])
	#solver = "/home/futatsugi/develop/contests/icfpc2016/approx/approximate_solver"
	#solve_problems("wc -l")
	solve_problems(solver)

	print "Completed!"

if __name__ == "__main__": main(sys.argv)
