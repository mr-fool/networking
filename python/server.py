import socket

server_socket = socket.socket()


server_socket.bind( ('localhost' , 8888 ) )
server_socket.listen(0)

try:
	while True:
		print ("Waiting for a client")
		
		client_socket, info = server_socket.accept()

except Exception as err:
	print("An Exception has occured: {}".format(err) )
	pass
    
server_socket.close()
