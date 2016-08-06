import glob
import re
import os
import sys

cmd = sys.argv[1]
try:
    os.mkdir("results")
except:
    pass

plist = glob.glob("../problems/problem_*.txt")

list_txt = open("exec_list.txt","w")

for p in plist:
    m = re.match('../problems/problem_([0-9]*)_.*.txt', p)
    pid = int(m.groups()[0])

    list_txt.write("%s < %s > results/%05d\n"%(cmd, p, pid))

list_txt.close()

os.system("parallel < exec_list.txt")