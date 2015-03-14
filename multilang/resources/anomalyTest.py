__author__ = 'fil'

#For testing open another terminal and type "nc hostname 9999"

import time
import socket

sock = socket.socket()
host = socket.gethostname()
port = 9999
sock.bind((host, port))
sock.listen(5)

try:
    while True:
        conn, addr = sock.accept()
        print 'Got connection from', addr
        payload = []
        timeVar = 0
        delay = 0
        #with open('/home/fil/Desktop/Python Server/timestampedDumps/dumpcsv-timestamped.20141103', 'r') as dump:
        while True:
            if timeVar < 20:
                path = '/home/fil/Desktop/Python Server/timestampedDumps/noAnomalies'
            elif timeVar < 30:
                path = '/home/fil/Desktop/Python Server/timestampedDumps/subnetDownSpike'
            else:
                path = '/home/fil/Desktop/Python Server/timestampedDumps/noAnomalies'
            with open(path, 'r') as dump:
                for line in dump:
                    conn.send(line)
            print "total seconds:" + str(timeVar) + "    " + path + "  " + str(time.time())
            timeVar += 1
            dump.close()
            time.sleep(1)
        conn.close()
        sock.close()
        print "Finished"
except (KeyboardInterrupt, SystemExit):
    sock.close()
    print " --- Socket Closed --- "
    raise



