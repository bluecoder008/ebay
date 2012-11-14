This package contains the code for implementation of system resource monitoring web app.

System Configuration:
=====================

=> Linux version: Fedora release 16 (Verne)
=> JDK 1.6.0_31
=> ANT 1.8.4

It has two parts:
(1) endNode, which implements exposing the internal resource usage via an Http endpoint;
(2) webApp,  which accepts user inputs and fans out to each end node requested and later returns
    the results to end-users.

It has ANT build.xml as build files and should be self-explanatory.

notes:

1a. The 'endNode' may requires super user privildge, which can be done by 'sudo su' and source the relevant environment setup scripts

1b. The 'endNode' also requires the 'sigar' package, an open source pkg to provide uniform API for system resource monitoring and reporting, its description can be found at: http://support.hyperic.com/display/SIGAR/Home


Design Consideration:
=====================

o Use open source solution SIGAR (http://support.hyperic.com/display/SIGAR/Home) for uniform 
  cross-platform system resource monitoring API's

o Use JDK 6 light weight HTTP server for HTTP processing

o Use mySQL or MongoDB for data store of metrics persistence


Test Cases:
===========

=========
[endNode]
=========

==============
* UNIT TESTS *
==============

== test case 1 ==
# ant CpuUsageTest
Buildfile: /home/mongo/perfmon/endNode/build.xml

compile:

CpuUsageTest:
     [java] "Cpu" : {
     [java]     "Vender"        :       "Intel",
     [java]     "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
     [java]     "MHz"           :       3333,
     [java]     "Total CPUs"    :       4,
     [java]     "Usage" : {
     [java]             "core0" :       "4.0%",
     [java]             "core1" :       "2.0%",
     [java]             "core2" :       "32.6%",
     [java]             "core3" :       "49.0%"
     [java]     }
     [java] }
     [java]

BUILD SUCCESSFUL
Total time: 0 seconds

== test case 2==

# ant MemUsageTest
Buildfile: /home/mongo/perfmon/endNode/build.xml

compile:
    [javac] Compiling 1 source file to /home/mongo/perfmon/endNode/build/classes

MemUsageTest:
     [java] "Mem" : {
     [java]     "Total" :       "3760MB",
     [java]     "Free"  :       156274688,
     [java]     "Used"  :       3782610944
     [java] }
     [java]

BUILD SUCCESSFUL
Total time: 0 seconds

== test case 3==

# ant MetricTest
Buildfile: /home/mongo/perfmon/endNode/build.xml

compile:
    [javac] Compiling 1 source file to /home/mongo/perfmon/endNode/build/classes

MetricTest:
     [java] {
     [java] "Cpu" : {
     [java]     "Vender"        :       "Intel",
     [java]     "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
     [java]     "MHz"           :       3333,
     [java]     "Total CPUs"    :       4,
     [java]     "Usage" : {
     [java]             "core0" :       "48.9%",
     [java]             "core1" :       "0.0%",
     [java]             "core2" :       "2.0%",
     [java]             "core3" :       "51.0%"
     [java]     }
     [java] },
     [java] "Mem" : {
     [java]     "Total" :       "3760MB",
     [java]     "Free"  :       105848832,
     [java]     "Used"  :       3833036800
     [java] }
     [java]
     [java] }
     [java]

BUILD SUCCESSFUL
Total time: 1 second


===============
* SYSTEM TEST *
===============

step 1.
======
Start up Metric HTTP endpoint:

#ant run
Buildfile: /home/mongo/perfmon/endNode/build.xml

compile:

run:
     [java] Metric Server started listening on port 80

step 2.
=======
Query endpoint via HTTP GET: (or fire up a Chrome browser on the same URL)

# curl http://localhost/metric
{
"Cpu" : {
        "Vender"        :       "Intel",
        "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
        "MHz"           :       3333,
        "Total CPUs"    :       4,
        "Usage" : {
                "core0" :       "2.0%",
                "core1" :       "0.0%",
                "core2" :       "100.0%",
                "core3" :       "0.0%"
        }
},
"Mem" : {
        "Total" :       "3760MB",
        "Free"  :       154968064,
        "Used"  :       3783917568
}
}


=========
[webApp]
=========

==============
* UNIT TESTS *
==============

== test case 1==

(with end-node HTTP interface running)

$ ant SimpleHttpClientTest
Buildfile: /home/mongo/perfmon/webApp/build.xml

compile:

SimpleHttpClientTest:
     [java] {
     [java] "Cpu" : {
     [java]     "Vender"        :       "Intel",
     [java]     "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
     [java]     "MHz"           :       3333,
     [java]     "Total CPUs"    :       4,
     [java]     "Usage" : {
     [java]             "core0" :       "29.4%",
     [java]             "core1" :       "60.4%",
     [java]             "core2" :       "63.9%",
     [java]             "core3" :       "40.0%"
     [java]     }
     [java] },
     [java] "Mem" : {
     [java]     "Total" :       "3760MB",
     [java]     "Free"  :       157790208,
     [java]     "Used"  :       3781095424
     [java] }
     [java]
     [java] }
     [java]

BUILD SUCCESSFUL
Total time: 1 second


================
* SYSTEM TESTS *
================
step 1. Start up Resource monitor web app:

[mongo@w8splunk webApp]$ !ant
ant run
Buildfile: /home/mongo/perfmon/webApp/build.xml

compile:
    [javac] Compiling 1 source file to /home/mongo/perfmon/webApp/build/classes
    [javac] Note: /home/mongo/perfmon/webApp/src/WebApp.java uses or overrides a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.

run:
     [java] Metric Server started listening on port 8000
     [java]     Following URLs accepted:
     [java]
     [java]              /checkNodes
     [java]              /checkStats?seqNum=<n>

step 2. Submit user input query:

$  cat data.txt
nodes=w8splunk,10.100.153.165
&urlPat=http://<node>/metric

$ curl --data @data.txt w8splunk:8000/checkNodes
<html><body>OK<br/>
request received - session sequence number: 1<br/>
</body></html>

step 3. Check query status:

$ curl  w8splunk:8000/checkStats?seqNum=1
In progress ... 10% completed ..

$ curl  w8splunk:8000/checkStats?seqNum=1
In progress ... 20% completed ..

step 4. Return results for the query:
$ curl  w8splunk:8000/checkStats?seqNum=1
Query completed:
{
        "w8splunk" : {
"Cpu" : {
        "Vender"        :       "Intel",
        "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
        "MHz"           :       3333,
        "Total CPUs"    :       4,
        "Usage" : {
                "core0" :       "0.0%",
                "core1" :       "0.0%",
                "core2" :       "1.9%",
                "core3" :       "100.0%"
        }
},
"Mem" : {
        "Total" :       "3760MB",
        "Free"  :       186089472,
        "Used"  :       3752796160
}

}
        ,
        "10.100.153.165" : {
"Cpu" : {
        "Vender"        :       "Intel",
        "Model"         :       "Core(TM) i5 CPU         650  @ 3.20GHz",
        "MHz"           :       3333,
        "Total CPUs"    :       4,
        "Usage" : {
                "core0" :       "2.0%",
                "core1" :       "2.0%",
                "core2" :       "2.0%",
                "core3" :       "97.9%"
        }
},
"Mem" : {
        "Total" :       "3760MB",
        "Free"  :       186089472,
        "Used"  :       3752796160
}

}
}



