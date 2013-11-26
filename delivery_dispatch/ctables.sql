
DROP database IF EXISTS dddb;
create database dddb;

use dddb;

Drop Table IF EXISTS CARRIER;
Create table CARRIER 
( 
carrier_id int not null auto_increment,
carrier_name varchar(42) not null, 
loc_lat float,
loc_long float,
assign int,
online tinyint,
primary key (carrier_id)
) ENGINE=InnoDB;

INSERT INTO CARRIER VALUES ( 1, "carrier1", 1.0, -1.0, -1, 1);
INSERT INTO CARRIER VALUES ( 2, "carrier2", 2.0, -2.0, -1, 1);
INSERT INTO CARRIER VALUES ( 3, "carrier3", 3.0, -3.0, -1, 1);

Drop Table IF EXISTS DELIVERY;
Create table DELIVERY
( 
delivery_id int not null auto_increment,
delivery_addr varchar(42) not null, 
start_loc_lat float,
start_loc_long float,
stop_loc_lat float,
stop_loc_long float,
primary key (delivery_id)
) ENGINE=InnoDB;

INSERT INTO DELIVERY VALUES ( 1, "pizza delivery", 37.10, -120.12, -37.12, -120.11);

commit;




									
