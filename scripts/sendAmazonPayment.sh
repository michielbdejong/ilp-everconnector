#!/bin/bash

AMAZON_DST="g.usd.com.amazon.webshop.account1.adfaf"
set -e
. ENVIRONMENT 
$CURL -H"ILP-Condition: ${CONDOK}" -H"ILP-Expiry: ${EXPIRE_10SECS}" -H"ILP-Destination: ${AMAZON_DST}" -H"ILP-Amount: ${PAY1_AMT}" $URL_CONNECTOR1 


