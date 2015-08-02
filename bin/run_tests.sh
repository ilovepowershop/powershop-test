#! /bin/sh
java -cp .:junit-4.10.jar:hamcrest-core-1.1.jar:selenium-server-standalone-2.46.0.jar:apache-log4j-extras-1.2.17.jar:log4j-1.2.17.jar:javax.json-1.0.4.jar:itextpdf-5.0.6.jar org.junit.runner.JUnitCore co.nz.powershop.testcases.PowershopFunctionalTest

