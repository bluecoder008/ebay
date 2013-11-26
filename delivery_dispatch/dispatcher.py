from flask import Flask, Response
from flask import request

import MySQLdb

import dao

app = Flask(__name__)

myDAO = dao.DAO("localhost", "root", "macroot", "dddb")

@app.route('/user/<username>')
def show_user_profile(username):
    # show the user profile for that user
    return 'User %s' % username

@app.route('/post/<int:post_id>')
def show_post(post_id):
    # show the post with the given id, the id is an integer
    return 'Post %d' % post_id

@app.route('/')
def index():
    return 'Hello from - Smart dispatcher page'

def formatJSONResponse(str):
	return Response(str, mimetype='application/json')

@app.route('/accept/<courier>/<delivery>', methods=['POST','GET'])
def accept(courier,delivery):
    assign = myDAO.getAccepted(courier)
    print "assigned: " + str(assign)
    if assign != -1 :
	return "Courier {} already accepted another delivery[{}]".\
             format(courier,assign)
    myDAO.updateCarrierAccept(courier,delivery)
    return "Successfully acceplted delivery[{}] for {}" \
       .format(delivery, courier) 

@app.route('/complete/<courier>/<delivery>', methods=['PUT','GET','POST'])
def complete(courier,delivery):
    myDAO.updateCarrierComplete(courier,delivery)
    return formatJSONResponse(
               "{ \"OK\" : \"" +
               "Successfully completed delivery (id={}) for {}".format(delivery,courier) +
               "\"}")

@app.route('/switch/<courier>', methods=['POST'])
def switch(courier):
    assert( request.method == 'POST' )
    flag = request.json["flag"]
    myDAO = dao.DAO("localhost", "root", "macroot", "dddb")
    if flag == "on":
	myDAO.updateCarrierOnOff(courier, 1)
    elif flag == "off":
	myDAO.updateCarrierOnOff(courier, 0)
    else:
	return "{ \"ERROR\" : \"Invalid input\" } "
    respStr = "{ \"OK\" : \"" + "Successfully switching {} {}".format(courier,flag) + "\"}"
    return formatJSONResponse(respStr)

@app.route('/createDelivery', methods=['POST'])
def create():
    assert(request.method == 'POST')
    myDAO = dao.DAO("localhost", "root", "macroot", "dddb")

    pickupLat = request.json["pickup"]["lat"]
    pickupLong = request.json["pickup"]["long"]
    deliverLat = request.json["deliver"]["lat"]
    deliverLong = request.json["deliver"]["long"]
    deliverAddr = request.json["address"]

    frees = myDAO.getAvailableCarriers()
    min = -1
    minCarrier = ""
    for carrier in frees:
	#print carrier
	#print "carrier location: ({},{})".format(carrier[2], carrier[3])
	dx = (carrier[2] - pickupLat)
	dy = (carrier[3] - pickupLong)
	d = dx * dx + dy * dy
	if ( min > d or min < 0 ) :
		min = d
		minCarrier = carrier[1]
    #print "min distance : {}, from carrier: {}".format(min, minCarrier)
    deliverId = myDAO.createDelivery(pickupLat, pickupLong,
                                     deliverLat, deliverLong,deliverAddr)
    resp = "creating delivery (id=" + str(deliverId) + ") from: " + \
           "(" + str(pickupLat) + "," + str(pickupLong) + ") to " + \
           "(" + str(deliverLat) + "," + str(deliverLong) + \
           ") dispatching to carrier: " + minCarrier;
    return formatJSONResponse( "{ \"OK\" : \"" + resp + "\" } " )

@app.route('/loc/<courier>', methods=['POST','GET'])
def currentLoc(courier):
    assert( request.method == 'POST' )
    lat = request.json["lat"]
    long = request.json['long']
    myDAO = dao.DAO("localhost", "root", "macroot", "dddb")
    myConnection = myDAO.connect()
    myDAO.updateCarrierLoc( courier, lat, long )
    resp = "Stored location for {} is ({},{}) :".format(courier, lat, long) 
    print resp
    return formatJSONResponse( "{ \"status\" : \"OK\", \"carrier\" : \"" + courier + "\"," + \
           " \"location\" : " + \
           "{ \"lat\" : " + str(lat) + ", \"long\" :" + str(long) + "} }" )

if __name__ == '__main__':
    app.run(host='0.0.0.0')
