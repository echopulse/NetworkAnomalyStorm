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
        currtime = 0
        delay = 0
        with open('/home/fil/Desktop/Python Server/timestampedDumps/dumpcsv-timestamped.20141103', 'r') as dump:
            for line in dump:
                arr = line.split(',')
                timestamp = long(arr[1])
                #initialize
                if currtime == 0:
                    currtime = timestamp
                #normal operation
                if currtime == timestamp:
                    conn.send(line)
                #wait then resume
                else:
                    delay = timestamp - currtime
                    print "delay: " + str(delay)
                    time.sleep(delay)
                    currtime = timestamp
                    conn.send(line)
            conn.close()
            dump.close()
            sock.close()
            print "Finished"
except (KeyboardInterrupt, SystemExit):
    sock.close()
    print " --- Socket Closed --- "
    raise


