#!/bin/bash

set -e
. ENVIRONMENT 
$CURL \
  -H"ILP-Condition: c" \
  -H"ILP-Expiry: ${EXPIRE_10SECS}" \
  -H"ILP-Destination: ${PAY1_DST}" \
  -H"ILP-Amount: ${PAY1_AMT}" \
  $URL_CONNECTOR1 
