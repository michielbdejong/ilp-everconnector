#!/bin/bash

set -e
. ENVIRONMENT 
$CURL -H"ILP-Condition: ${CONDOK}" -H"ILP-Expiry: ${EXPIRE_10SECS}" -H"ILP-Destination: ${PAY1_DST}" -H"ILP-Amount: ${PAY1_AMT}" $URL_CONNECTOR1 

header "WRONG EXPIRE FORMAT"
$CURL -H"ILP-Condition: ${CONDOK}" -H"ILP-Expiry: ${EXPIRE__WRONG}" -H"ILP-Destination: ${PAY1_DST}" -H"ILP-Amount: ${PAY1_AMT}" $URL_CONNECTOR1 
exit
header "WRONG DESTINATION FORMAT FORMAT"
$CURL -H"ILP-Condition: ${CONDOK}" -H"ILP-Expiry: ${EXPIRE_10SECS}" -H"ILP-Destination: ${WRONG_DS}" -H"ILP-Amount: ${PAY1_AMT}" $URL_CONNECTOR1 

header "WRONG AMOUNT FORMAT"
$CURL -H"ILP-Condition: ${CONDOK}" -H"ILP-Expiry: ${EXPIRE_10SECS}" -H"ILP-Destination: ${PAY1_DST}" -H"ILP-Amount: ${WRNG_AMT}" $URL_CONNECTOR1 
