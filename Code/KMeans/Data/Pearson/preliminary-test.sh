#!/usr/bin/env bash

# vector-size nr-mappers nr-iterations

stm="STM.jar"
tl="TL.jar"
actor="ACTOR.jar"

function test {
  for i in {1..5}
  do
    for j in {1..100} 
    do
    if (( $j % 10 == 0 )) || (( $j == 1 ))
    then 
      java -jar $1.jar 2000000 4 $j
    fi
    done
  done
}

test stm
test tl
test actor
	
