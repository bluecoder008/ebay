#!/usr/bin/env python
import os
import flask
import unittest
import tempfile

import dispatcher

class FlaskrTestCase(unittest.TestCase):

    def setUp(self):
        self.app = dispatcher.app.test_client()
	os.system("mysql -u root -pmacroot < ctables.sql")

    def tearDown(self):
	pass

    def test_empty_db(self):
        #rv = self.app.get('/')
        #assert 'No entries here so far' in rv.data
	app = flask.Flask(__name__)
	with app.test_request_context('/accept/carrier1/1'):
		assert flask.request.path == '/accept/carrier1/1'

    def test_carrier_loc(self):
	rv = self.app.post('/loc/carrier1',
			   data='{ "lat" : 37.123, "long" : -120.12 }',
                           content_type='application/json' )
	assert '"status" : "OK"' in rv.data

    def test_switch_online(self):
	rv = self.app.post('/switch/carrier1',
			   data='{ "flag" : "off" }',
                           content_type='application/json' )
	assert "Successfully switching" in rv.data

    def test_create_delivery(self):
	textData = '{ "pickup" : { "lat" : 1.1, "long" : 1.2 }, "deliver" : { "lat" : 2.1, "long" : 2.2} , "address" : "12345 main street, san jose, california"}'
	rv = self.app.post('/createDelivery', data=textData, content_type='application/json' )
	assert "dispatching to carrier" in rv.data

    def test_accept_delivery(self):
	rv = self.app.get('/accept/carrier1/1')
	assert "Successfully acceplted" in rv.data

    def test_complete_delivery(self):
	rv = self.app.get('/complete/carrier1/1')
	print "rv.data:" + rv.data
	assert "Successfully completed delivery" in rv.data

if __name__ == '__main__':
    unittest.main()

