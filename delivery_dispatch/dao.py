#!/usr/bin/env python
import MySQLdb

class DAO :

   def __init__(self, host, user, passwd, db):
	self.host = host
	self.user = user
	self.passwd = passwd
	self.db = db

   def connect(self):
	self.dbConnection = MySQLdb.connect( host=self.host, 
            user=self.user, passwd=self.passwd, db=self.db)
	return self.dbConnection

   def query(self, query):
	self.dbConnection.query(query)
	rs = self.dbConnection.store_result()
	return rs

   def getAvailableCarriers(self):
	self.connect()
	rs = self.query("SELECT carrier_id, carrier_name, loc_lat, loc_long " +
                        "FROM carrier WHERE assign=-1" )
	carriers = []
	while True:
		row = rs.fetch_row()
		if row:
			carriers.append(row[0])
		else:
			break
	return carriers

   def getAccepted(self, carrier):
	self.connect()
	rs = self.query("SELECT assign FROM carrier " +
                        "WHERE carrier_name='" + carrier + "'")
	assign = None
 	try:
		assign = int(rs.fetch_row()[0][0])
	except:
		assign = None
	return assign

   def getCreatedId(self):
	self.connect()
	rs = self.query("SELECT MAX(delivery_id) FROM delivery;");
	id = -1 
 	try:
		id = int(rs.fetch_row()[0][0])
	except:
		id = -1
	return id

   def createDelivery(self, pLat, pLong, dLat, dLong, addr):
	self.connect()
	cur = self.dbConnection.cursor()
	sql = ("INSERT INTO delivery set start_loc_lat={}, start_loc_long={}, " + \
              "stop_loc_lat={}, stop_loc_long={}, delivery_addr='{}'").format(pLat, pLong, dLat, dLong,addr)
	print "sql: " + sql
	cur.execute(sql)
	id = -1
	if cur.rowcount:
		self.dbConnection.commit()
		id = self.getCreatedId()
	self.dbConnection.close()
	return id
 
   def updateCarrierLoc(self, carrier, lat, long):
	self.connect()
	cur = self.dbConnection.cursor()
	cur.execute("UPDATE CARRIER set loc_lat=" + str(lat) + 
                    ",loc_long=" + str(long) + 
                    " where carrier_name='" + carrier + "';")
	print "Number of rows updated:",  cur.rowcount
	if cur.rowcount:
		self.dbConnection.commit()
	self.dbConnection.close()

   def updateCarrierOnOff(self, carrier, flag):
	self.connect()
	cur = self.dbConnection.cursor()
	sql = "UPDATE CARRIER set online=" + str(flag) + \
              " where carrier_name='" + carrier + "';"
	print "sql: {} ".format(sql)
	cur.execute(sql)
	print "Number of rows updated:",  cur.rowcount
	if cur.rowcount:
		self.dbConnection.commit()
	self.dbConnection.close()

   def updateCarrierAccept(self, carrier, deliveryId):
	self.connect()
	cur = self.dbConnection.cursor()
	sql = "UPDATE CARRIER set assign=" + deliveryId + \
              " where carrier_name='" + carrier + "';"
	print "sql: {} ".format(sql)
	cur.execute(sql)
	print "Number of rows updated:",  cur.rowcount
	if cur.rowcount:
		self.dbConnection.commit()
	self.dbConnection.close()

   def updateCarrierComplete(self, carrier, delivery):
	self.connect()
	cur = self.dbConnection.cursor()
	sql = "UPDATE CARRIER set assign=-1 " + \
              " where carrier_name='" + carrier + "';"
	cur.execute(sql)
	updated = False
	if cur.rowcount:
		updated = True
	sql = "DELETE FROM delivery WHERE delivery_id= {};".format(delivery)
	cur.execute(sql)
	if cur.rowcount:
		updated = True
	if updated:
		self.dbConnection.commit()
	self.dbConnection.close()

