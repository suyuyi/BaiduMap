from socket import *
import pymysql,threading,math
EARTH_REDIUS = 6378.137
global login_request,regi_request,send_request,scan_request,read_request,comment_request,comment_list_request
global like_request,dislike_request,delete_request
login_request=1
regi_request=2
send_request=3
scan_request=4
read_request=5
comment_request=6
comment_list_request=7
like_request=8
dislike_request=9
delete_request=10

def rad(d):
    return d * math.pi / 180.0

def getDistance(lat1, lng1, lat2, lng2):
    radLat1 = rad(lat1)
    radLat2 = rad(lat2)
    a = radLat1 - radLat2
    b = rad(lng1) - rad(lng2)
    s = 2 * math.asin(math.sqrt(math.pow(math.sin(a/2), 2) + math.cos(radLat1) * math.cos(radLat2) * math.pow(math.sin(b/2), 2)))
    s = s * EARTH_REDIUS
    return s

def test(tcpClientSocket,addr):
    print("thread start!")
    print('addr:=',addr)
    data = tcpClientSocket.recv(BUFFER_SIZE).decode()
    if not data:
        return 0;
    op=data.split('-')
    print(op)
    #
    if int(op[0])==login_request:
        #login request
        userid=op[1]
        password=op[2]
        operation1="SELECT ID FROM UserInfo WHERE name=\'"+userid+"\'"
        operation2="SELECT ID FROM UserInfo WHERE name=\'"+userid+"\'and password=\'"+password+"\'"
        #print(operation)
        cursor=db.cursor()
        cursor.execute(operation1)
        db.commit()
        result1=cursor.fetchone()
        cursor.execute(operation2)
        db.commit()
        result2=cursor.fetchone()
        if("<class 'tuple'>"==str(type(result1))):
            if("<class 'tuple'>"==str(type(result2))):
                msg="1/"+str(result2[0])
                print(msg)
                tcpClientSocket.send(msg.encode())
            else:
                msg="0/-2"
                print(msg)
                tcpClientSocket.send(msg.encode())
        else:
            msg="0/-1"
            print(msg)
            tcpClientSocket.send(msg.encode())
    elif int(op[0])==regi_request:
        username=op[1]
        password=op[2]
        operation1="SELECT ID FROM UserInfo WHERE name=\'"+username+"\'"
        cursor=db.cursor()
        cursor.execute(operation1)
        db.commit()
        result1=cursor.fetchone()
        if("<class 'tuple'>"!=str(type(result1))):#OK
            operation2="INSERT INTO UserInfo(name,password) VALUES (\'"+username+"\',\'"+password+"\')"
            print(operation2)
            cursor.execute(operation2)
            db.commit()
            msg="1/1"
            print(msg)
            tcpClientSocket.send(msg.encode())
        else:
            msg="0/-1"
            print(msg)
            tcpClientSocket.send(msg.encode())
    elif int(op[0])==send_request:
        #userid-lon-lat-title-content
        userid=op[1]
        lon=op[2]
        lat=op[3]
        title=op[4]
        content=op[5]
        '''
      `UserID` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
      `Content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
      `ID` int(255) NOT NULL AUTO_INCREMENT,
      `like` int(255) DEFAULT '0',
      `dislike` int(255) DEFAULT '0',
      `lat` double(255,0) NOT NULL,
      `lon` double(255,0) NOT NULL,
      `title`
        '''
        #此处的ID指的是UserInfo中给每个用户名分配的ID
        operation="INSERT INTO FloatingContent(UserID,lon,lat,title,Content) VALUES (\'"+userid+"\',\'"+lon+"\',\'"+lat+"\',\'"+title+"\',\'"+content+"\')"
        cursor=db.cursor()
        cursor.execute(operation)
        db.commit()
        msg="1/1"
        print(msg)
        tcpClientSocket.send(msg.encode())
    elif int(op[0])==scan_request:
        lat=float(op[1])
        lon=float(op[2])
        operation="SELECT ID,lat,lon,title,Content,isdeleted FROM FloatingContent"
        cursor=db.cursor()
        cursor.execute(operation)
        result=cursor.fetchall()
        msg=""
        for i in range(len(result)):
            if getDistance(lat,lon,float(result[i][1]),float(result[i][2]))<1.0 and int(result[i][5])!=1:
                msg=msg+str(result[i][0])+"~"+str(result[i][1])+"~"+str(result[i][2])+"~"+str(result[i][3])+"~"+str(result[i][4])+"/"
        print(msg)
        tcpClientSocket.send(msg.encode())
    #read isn't finished!
    elif int(op[0])==read_request:
        targetID=op[1]
        operation="SELECT ID,title,Content,isdeleted FROM FloatingContent WHERE UserID="+targetID
        cursor=db.cursor()
        cursor.execute(operation)
        db.commit()
        result=cursor.fetchall()
        print(result)
        msg=""
        for i in range(len(result)):
            if int(result[i][3])==0:
                id=str(result[i][0])
                title=str(result[i][1])
                content=str(result[i][2])
                msg=msg+id+'~'+title+'~'+content+'/'
        print(msg)
        tcpClientSocket.send(msg.encode())
    elif int(op[0])==comment_request:
        targetID=op[1]
        comment=op[2]
        operation="INSERT INTO Comment (targetID,comment) VALUES(\'"+targetID+"\',\'"+comment+"\')"
        cursor=db.cursor()
        cursor.execute(operation)
        db.commit()
        msg="1/1"
        tcpClientSocket.send(msg.encode())
    elif int(op[0])==comment_list_request:
        targetID=op[1]
        operation="SELECT comment FROM Comment WHERE targetID=\'"+targetID+"\'"
        cursor=db.cursor()
        cursor.execute(operation)
        result=cursor.fetchall()
        db.commit()
        msg=""
        for i in range(len(result)):
            msg=msg+str(result[i][0])+"/"
        print(msg)
        tcpClientSocket.send(msg.encode())
    elif int(op[0])==like_request:
        targetID=op[1]
        operation="UPDATE FloatingContent set love=love+1 WHERE ID="+targetID
        cursor=db.cursor()
        cursor.execute(operation)
        res=cursor.fetchone()

        db.commit()
    elif int(op[0])==dislike_request:
        targetID=op[1]
        operation="UPDATE FloatingContent set hate=hate+1 WHERE ID="+targetID
        cursor=db.cursor()
        cursor.execute(operation)
        db.commit()
    elif int(op[0])==delete_request:
        targetID=op[1]
        operation="UPDATE FloatingContent set isdeleted=1 WHERE ID="+targetID
        cursor=db.cursor()
        cursor.execute(operation)
        db.commit()
    tcpClientSocket.close()
    print("connection closed!")

global db
db=pymysql.connect("localhost","root","********","testdb1")#you can create your onw sql db
HOST="45.76.196.92"
PORT=8088
BUFFER_SIZE=1024
ADDR=(HOST,PORT)
tcpServerSocket = socket(AF_INET,SOCK_STREAM)
tcpServerSocket.bind(ADDR)
ser_listen=tcpServerSocket.listen(8088)
if ser_listen==-1:
    print("connection failed! exit")
    sys.exit()
while True:
    tcpClientSocket,addr = tcpServerSocket.accept()
    tcpClientSocket.settimeout(5)
    print("connection succeed")
    threading.Thread(target=test,args=(tcpClientSocket,addr)).start()
tcpServerSocket.close()
