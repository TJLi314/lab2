#!/bin/bash

# Compile the Java program
javac Main.java

# If compilation was successful, run it
if [ $? -eq 0 ]; then
    java Main
else
    echo "Compilation failed."
fi