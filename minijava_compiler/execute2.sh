#! /bin/sh
mkdir -p output2

myfilenames=`ls ./examples2/*.java`
error="-error"
for file in $myfilenames
do
    java Main $file > "output2/${file##*/}.txt"
    echo $file
done