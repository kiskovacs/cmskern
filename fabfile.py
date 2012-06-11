from fabric.api import *
from fabric.state import output
import shutil
import os
import re
import time

output['debug'] = True

env.re_tag = r'\d{5}' # Assumes tags are in the form 00123

env.rsyncparam = "-r -v -u --delete -l --exclude '.DS_Store' --exclude '/playapp/orig_images' --exclude '/playapp/eclipse/' --exclude '/playapp/data/' --exclude '/playapp/attachments/' --exclude '/playapp/public/images/cache/' --exclude '.hg' --exclude '.git' --exclude '.svn' --exclude '*.swp' --exclude '*.swo' --exclude '/playapp/logs' --exclude '/playapp/server.pid' --exclude '/f3-modules/*.pyc' --exclude '/playapp/tmp' --exclude '/uploads' --exclude '/playapp/cache' --exclude '/playapp/modules'" 

"""
********************************
*** ZIEL-UMGEBUNGEN ***
********************************
"""

def prod():
    env.fab_hosts = ['guj@cmskern.de']
    env.www_dir = '/var/httpd/servers/www.cmskern.de'
    env.play_dir = '/var/play/shop.cmskern.de/cmskern-shop'
    env.extension = 'prod'
    env.task = "prod_task.py"

def mv_jessica():
    env.fab_hosts = ['marielle@jessica']
    env.www_dir = '/home/marielle/playprojects/cmskern'
    env.play_dir = '/home/marielle/playprojects/cmskern'
    env.extension = 'jessica'
    env.task = "jessica_task.py"
    print local(
        "rsync " + env.rsyncparam + " --exclude htdocs . jessica:" + env.play_dir
      )
    print local(
        "rsync " + env.rsyncparam + " --exclude playapp . jessica:" + env.www_dir
      )

    print("remote ausfuehren: ./%s" % env.task)
    print www_run("./%s" % env.task)

def annette():
    env.fab_hosts = ['kang@annette']
    env.play_dir = '/home/kang/playprojects/cmskern'
    env.extension = 'annette'
    print local(
        "rsync " + env.rsyncparam + " . annette:" + env.play_dir
      )

def kang_jessica():
    env.fab_hosts = ['kang@jessica']
    env.play_dir = '/home/kang/playprojects/cmskern'
    env.extension = 'jessica'
    env.task = "jessica_task.py"
    print local(
        "rsync " + env.rsyncparam + " . jessica:" + env.play_dir
      )

    print("remote ausfuehren: ./%s" % env.task)
    print playapp_run("./%s" % env.task)

def f_jessica():
    env.fab_hosts = ['friederike@jessica']
    env.www_dir = '/home/friederike/projects/cmskern'
    env.play_dir = '/home/friederike/playprojects/cmskern'
    env.extension = 'jessica'
    env.task = "jessica_task.py"
    print local(
        "rsync " + env.rsyncparam + " --exclude htdocs . friederike@jessica:" + env.play_dir
      )

    print("remote ausfuehren: ./%s" % env.task)
    print playapp_run("./%s" % env.task)



def v_jessica():
    env.fab_hosts = ['estrin@jessica']
    env.www_dir = '/home/estrin/projects/cmskern'
    env.play_dir = '/home/estrin/playprojects/cmskern'
    env.extension = 'jessica'
    env.task = "jessica_task.py"
    print local(
        "rsync " + env.rsyncparam + " --exclude htdocs . estrin@jessica:" + env.play_dir
      )
    print local(
        "rsync " + env.rsyncparam + " --exclude playapp . estrin@jessica:" + env.www_dir
      )

    print("remote ausfuehren: ./%s" % env.task)
    print www_run("./%s" % env.task)


def dev():
  print('dummy')


"""
********************************
*** ZIEL-UMGEBUNGEN ENDE ***
********************************
""" 

# COMMANDS
def info():
    require('fab_hosts', provided_by = [dev, prod, kang_jessica, mv_jessica])
    require('www_dir', provided_by = [dev, prod, kang_jessica, mv_jessica])

    hginfo = www_run("hg parents")
    print hginfo
    
def tag():
    print ('######## Der aktuelle Mercurial-Tip wird getagged ###########')
    print (' ... vorher wird ein "hg push" ausgefuehrt ')
    print (' ... in der Regel, macht dieser Task nur Sinn, wenn vorher ein "hg ci" ausgefuehrt wurde')
    print (' ...')

    env.tag = get_next_tag()
    local('hg tag %s' % env.tag)
    local('hg push')
    local('echo *** Created Tag  %s' % env.tag)

def deploy():
    require('fab_hosts', provided_by = [dev, prod, kang_jessica, mv_jessica])
    require('www_dir', provided_by = [dev, prod, kang_jessica, mv_jessica])
    require('play_dir', provided_by = [dev, prod, kang_jessica, mv_jessica])

    print www_run('hg pull')
    prompt('What tag should we deploy?', 'tag', default=get_current_tag(), validate=env.re_tag)
    print("hg up -C %s" % env.tag)
    print www_run("hg up -C %s" % env.tag)

    if env.task:
      print("remote ausfuehren: ./%s" % env.task)
      print www_run("./%s" % env.task)

    print playapp_run('hg pull')
    print("hg up -C %s" % env.tag)
    print playapp_run("hg up -C %s" % env.tag)

    if env.task:
      print("remote ausfuehren: ./%s" % env.task)
      print playapp_run("./%s" % env.task)

    local('echo *** Deployed Tag %s' % env.tag)

def tip():
    print ('######## Der aktuelle Mercurial-Tip wird deployed ###########')
    print (' ... vorher wird ein "hg push" ausgefuehrt ')

    require('fab_hosts', provided_by = [dev, prod, kang_jessica, mv_jessica])
    require('www_dir', provided_by = [dev, prod,kang_jessica, mv_jessica])
    require('play_dir', provided_by = [dev, prod, kang_jessica, mv_jessica])

    local('hg push')

    print www_run('hg pull')
    print("hg up -C")
    print www_run("hg up -C")

    if env.task:
      print("remote ausfuehren: ./%s" % env.task)
      print www_run("./%s" % env.task)

    print playapp_run('hg pull')
    print("hg up -C")
    print playapp_run("hg up -C")

    if env.task:
      print("remote ausfuehren: ./%s" % env.task)
      print playapp_run("./%s" % env.task)
######################################################
# HELPERS
######################################################
def format_tag(tag):
    return str(tag).zfill(5)

def get_current_tag():
    s = open('.hgtags').read()
    matches = re.findall(r'\s(%s)' % env.re_tag, s, re.MULTILINE)
    if len(matches) == 0:
        local('echo "No tagged builds found"')
        build = '1'
    else:
        build = matches[-1]
    return format_tag(build)


def sshagent_run(cmd):
    """
    Helper function.
    Runs a command with SSH agent forwarding enabled.
    
    Note:: Fabric (and paramiko) can't forward your SSH agent. 
    This helper uses your system's ssh to do so.
    """
    for h in env.fab_hosts:
        try:
            # catch the port number to pass to ssh
            host, port = h.split(':')
            ret=local('ssh -p %s -A %s "%s"' % (port, host, cmd))
        except ValueError:
            ret=local('ssh -A %s "%s"' % (h, cmd))
    return ret

def get_next_tag():
    build = get_current_tag()
    return format_tag(int(build)+1)

def www_run(cmd):
    return sshagent_run("cd %s; %s" % (env.www_dir, cmd))

def playapp_run(cmd):
    return sshagent_run("cd %s; %s" % (env.play_dir, cmd))

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
        shutil.copyfile(oldfile, newfile)

