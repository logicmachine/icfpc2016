import glob
import re
import json
import os
import sys
import time
import subprocess
import os.path

r_list = glob.glob("results/*")

try:
    os.mkdir("submit_results")
except:
    pass

resemblance_list = open("submit_results/resemblance_list.txt","w")
fail_list = open("submit_results/fail_list.txt","w")

for r in r_list:
    pid = int(os.path.basename(r))

    cmd = "curl --compressed -L -H Expect: -H 'X-API-Key: 56-c0d0425216599ecb557d45138c644174' -F 'problem_id=%d' -F 'solution_spec=@%s' 'http://2016sv.icfpcontest.org/api/solution/submit'"%(pid,r)

    print cmd
    r = subprocess.check_output(cmd, shell=True)
    submit_json = json.loads(r)

    f = open('submit_results/%05d'%pid,"w")
    f.write(r)
    f.close()

    if submit_json['ok'] != True:
        fail_list.write("%d\n"%(pid))

    time.sleep(1)
