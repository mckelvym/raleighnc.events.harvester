#!/bin/bash

cd ~/Documents/raleighnc.events.rss
date=`date "+%Y-%m-%d_%H.%M.%S"`
size=`ls -lh events.xml | awk '{print $5}'`
items=`grep -c "<item>" events.xml`
git add events.xml && git commit -m "$date $size bytes, $items items" && git push
exit
