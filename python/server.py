import socket

server_socket = socket.socket()


server_socket.blind( ('localhost' , 8888 ) )
