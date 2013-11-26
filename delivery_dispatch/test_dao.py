#!/usr/bin/env python
from dao import DAO

# test driver
if __name__ == "__main__" :
	myDAO = DAO("localhost", "root", "macroot", "dddb")
	lat = -2.0
	long = -2.5
	print "updating carrier1 -> ({},{})".format(lat,long)
	myDAO.updateCarrierLoc( "carrier1", lat, long)

	myDAO = DAO("localhost", "root", "macroot", "dddb")
	print "updating carrier2 -> offline"
	myDAO.updateCarrierOnOff( "carrier2", 0)

	myDAO = DAO("localhost", "root", "macroot", "dddb")
	print "carrier1 accepting delivery 1"
	myDAO.updateCarrierAccept( "carrier1", "1" )

	myDAO = DAO("localhost", "root", "macroot", "dddb")
	print "completing carrier2"
	myDAO.updateCarrierComplete( "carrier2", 1 )

	print "getAccepted('carrier1'): {}".format( myDAO.getAccepted('carrier1') )

	print "Get all free carriers:"
	frees = myDAO.getAvailableCarriers()
	for carrier in frees:
		print carrier

	newDelivery = myDAO.createDelivery( 1.2, 1.2,
    	                                  2.4, 2.4, "12345 main street, san jose")
	print "created delivery: {}".format(newDelivery)
