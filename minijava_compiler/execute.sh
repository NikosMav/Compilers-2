#! /bin/sh
mkdir -p output

myfilenames=`ls ./examples/*.java`
error="-error"
for file in $myfilenames
do
    java Main $file > "output/${file##*/}.txt"
    echo $file
done