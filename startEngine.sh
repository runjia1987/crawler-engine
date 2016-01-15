#!/bin/bash

if [[ ! $# -eq 2 ]]; then
	echo "[zipPath] [clientName] parameters are required!";
	exit 1;
fi
zipPath=$1;clientName=$2

if [[ ! -f $zipPath ]]; then
	echo "${zipPath} does not exist";
	exit 1;
fi

# path=`dirname $zipPath`
unzip -o $zipPath -d .

nohup java -jar engineClient.jar ${clientName} > ${clientName}.log &