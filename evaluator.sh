#!/bin/bash
latitude=55.944425
longitude=-3.188396
seed=5678
drone="stateful"

clear
for year in 2019 2020
do
    for month in $(seq -f "%02g" 1 12)
    do  
        case $month in
            02)
                if [ $year -eq 2019 ];
                then 
                    max=28
                else
                    max=29
                fi
                ;;
            04)
                max=30
                ;;
            06)
                max=30
                ;;
            09)
                max=30
                ;;
            11)
                max=30
                ;;
            12)
                if [ $year -eq 2019 ];
                then 
                    max=31
                else
                    max=30
                fi
                ;;
            *)
                max=31
                ;;
        esac
        for day in $(seq -f "%02g" 1 $max)
        do
            # echo $day $month $year 55.944425 -3.188396 5678 "stateful"
            java -jar powergrab-0.0.1-SNAPSHOT.jar $day $month $year $latitude $longitude $seed $drone
        done
    done
done