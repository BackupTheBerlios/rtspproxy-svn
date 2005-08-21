#!/bin/bash

export CLASSPATH=.:lib/mina.jar:lib/log4j.jar
java -Xshare:off rtspproxy.Main

