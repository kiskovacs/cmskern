#! /usr/bin/env python

import shutil
import sys
import os
from stat import *

def mangle_extension(dir, ext, subst):
  if ext[:1] != '.':
    ext = '.'+ext
  thelen = -len(ext)
  for path, subdirs, files in os.walk(dir):
    for oldfile in files:
      if oldfile[thelen:] == ext:
        oldfile = os.path.join(path, oldfile)
        newfile =oldfile[:thelen]+subst
        #os.rename(oldfile, newfile)
        print "cp: " + oldfile + " => " + newfile
        shutil.copyfile(oldfile, newfile)

# buegel alle dateien mit gleichnamigen Dateien mit ".jessica"-Endung ueber
mangle_extension(".", "jessica", "")

# /config und /log erstellen, wenn nicht vorhanden

dirs =[]
for dirname in dirs:
  dir = "%s/%s" % (os.path.dirname(__file__), dirname)
  try:
    os.makedirs(dir)
  except OSError:
    pass
  os.chmod(dir,0777)
