import socket
import os

sk = socket.socket()

sk.bind(("10.135.20.165", 5000))
sk.listen(5)

while True:
	conn, address = sk.accept()

	#获得指令确认
	order = conn.recv(1024).decode('utf-8')
	print('recv order:' + order)


	#send分隔
	conn.send(b'div 1')

	if order == 'Register':
		name = conn.recv(1024)
		# 装模作样的查询名字是否存在
		print('Register name :' + str(name))
		conn.send(b'0')

		psw = conn.recv(1024)
		# 装模作样的实现注册
		print('Register psw :' + str(psw))
		conn.send(b'1')


	elif order == 'Login':
		name = conn.recv(1024)
		# 装模作样的查询名字是否存在
		print('Login name :' + str(name))
		conn.send(b'0')

		psw = conn.recv(1024)
		# 装模作样的实现登录
		print('Login psw :' + str(psw))
		conn.send(b'2')
	else:
		# 获得文件大小
		size = conn.recv(1024)
		size_str = str(size, "utf-8")
		file_size = int(size_str)
		print('Get file size is:' + str(file_size))
		has_size = 0

		# 接收用分隔符
		conn.send(b'div 2')

		f = open("getImg.png", "wb")
		while True:
			if file_size <= has_size:
				break
			data = conn.recv(1024)
			f.write(data)
			has_size += len(data)
		f.close()

		file_size = os.stat("psb.gif").st_size
		has_sent = 0

		conn.send(b'3')