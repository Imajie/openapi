#!/bin/bash

sleep 5
ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=org" -w adminPass -f /tmp/account.ldif