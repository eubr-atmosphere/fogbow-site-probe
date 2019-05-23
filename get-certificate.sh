#!/bin/bash
ip=`awk -F ' *= *' '$1=="monitor_ip"{print $2}' /app/probes/src/main/resources/private/probe-fogbow.conf`

openssl s_client -showcerts -connect $ip </dev/null 2>/dev/null | openssl x509 -outform PEM > /app/cert.pem

cp /app/cert.pem /app/java-client-lib