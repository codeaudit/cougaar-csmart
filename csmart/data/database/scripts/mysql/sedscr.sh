#!/bin/sh
for x
do
sed s/\\r\\n/\\n/g $x | gzip | gunzip > $x.tmp
done
