#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import time
import json
import time
import threading
import datetime
import subprocess
import shlex
import re
import Queue

from slackclient import SlackClient

#TOKEN        = "xoxb-51860207460-4JGySNRPwz2zttsKFpMrsQaJ"
TOKEN        = "xoxb-67001754224-INRWlX9sTIJYUEARyLfAOYpu"
CHANNEL      = "icfpc2016"

BASE_PATH = "/home/futatsugi/develop/contests/icfpc/icfpc2016/submitter"
SUBMIT_SOLUTION_EXE = "submit_solution_all.py"
VALUABLE_PROBLEMS_FILE = "valuable_problems.txt"
#VALUABLE_PROBLEMS_FILE = "valuable_problems_.txt"

WAIT_TIME    = 1
NUM_WORKERS  = 1
NUM_TRY      = 10

sc = None
q = Queue.Queue()
memo = []

def worker():
	global sc
	
	while True:
		fileinfo = q.get()
		if fileinfo:
			url = fileinfo["url_private_download"]
			userinfo = sc.api_call("users.info", user=fileinfo["user"])
			username = userinfo["user"]["name"]
			#text = u"Solution file url: %s" % url
			filename = os.path.basename(url)
			#if filename not in memo and re.search("^([\d]+\.txt)$", filename):
			#if not os.path.isfile(os.path.join(BASE_PATH, filename)):
			if True:
				print "Downloading solution file..."
				os.system('curl -H "Authorization: Bearer %s" %s > %s' % (TOKEN, url, os.path.join(BASE_PATH, "solutions", filename)))
				time.sleep(3.0)
				print "Running command..."
				command = "%s %s %s" % (os.path.join(BASE_PATH, SUBMIT_SOLUTION_EXE), os.path.join(BASE_PATH, "solutions", filename), os.path.join(BASE_PATH, VALUABLE_PROBLEMS_FILE))
				p = subprocess.Popen(command, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
				stdo, stde = p.communicate()
				#print "Error message: %s" % stde ###
				print "Sending message to slack..."
				sc.rtm_send_message(CHANNEL, "@%s\n%s" % (username, stdo))

def main(args):
	global sc
	
	for i in range(NUM_WORKERS):
		t = threading.Thread(target=worker)
		t.daemon = True
		t.start()

	for n in range(NUM_TRY):
		sc = SlackClient(TOKEN)
		if sc.rtm_connect():
			while True:
				try:
					records = sc.rtm_read()
				except:
					print "接続が切断されました。再接続します。試行回数: %d" % (n + 1)
					break
				for record in records:
					if "file" in record:
						fileinfo = record["file"]["id"]
						filedata = sc.api_call("files.info", file=fileinfo)
						if fileinfo not in memo:
							q.put(filedata["file"])
							memo.append(fileinfo)
				time.sleep(WAIT_TIME)
		else:
			print "接続に失敗しました。TOKENが間違っていませんか?"
			time.sleep(60)

if __name__ == "__main__": main(sys.argv)
