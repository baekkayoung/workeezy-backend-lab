import pymysql

def get_conn():
    return pymysql.connect(
        host="workeezy.iptime.org",
        port=3306,
        user="workeezy",
        password="V9!tE4#sQp17%aDxmF8*Qz3P!uH6$YwRA#7pLk2!Zc9&vG0rTxS3$R0w!Bt8#Nq5LH@5vD1#tQm9%Wz4egK2!U7$rPq3#X5bNZ4&dM8!pS1#vC7kRfR9%W1#yT6!uB3hQnP7#L2$wC8!xV5gTJ8!qF4@vT1#kZ6rB",
        database="workeezy",
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=True,
    )
