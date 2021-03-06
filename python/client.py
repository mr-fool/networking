import socket
import sys

HOST, PORT = 'localhost', 1234
data = " ".join(sys.argv[1:])


#Create a socket(SOCK_STREAM means a TCP socket)
sock = socket.socket()

try:
	#connect to server and send data
	sock.connect((HOST,PORT))
	sock.sendall(data + "\n")
	
	#receive data from the server and shut down
	received = sock.recv(4096)
	sock.sendall("received" + "\n")
	received2 = sock.recv(4096)
finally:
	sock.close()

print "Sent: {}".format(data)
print "Received: {}".format(received)
print "Received: {}".format(received2)
