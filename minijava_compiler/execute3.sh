#! /bin/sh
mkdir -p output3

myfilenames=`ls ./examples3/*.java`
error="-error"
for file in $myfilenames
do
    java Main $file > "output3/${file##*/}.txt"
    echo $file
done