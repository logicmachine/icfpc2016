import sys
import json
import os
import time
import subprocess

sn_str = subprocess.check_output("curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' 'http://2016sv.icfpcontest.org/api/snapshot/list'", shell=True)
snapshot = json.loads(sn_str)

sn_list = []

class X:
    def __init__(self):
        pass

for s in snapshot["snapshots"]:
    a = X()
    a.time = int(s['snapshot_time'])
    a.hash = s['snapshot_hash']

    sn_list.append( a )

sn_list.sort(key=lambda a: a.time)
last_hash = sn_list[-1].hash

time.sleep(1)

blob_str = subprocess.check_output("curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' 'http://2016sv.icfpcontest.org/api/blob/%s'"%(last_hash), shell=True)

open("snapshot.json","w").write(blob_str)

