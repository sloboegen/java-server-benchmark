set term png
set output 'reports/asynchronous/request_delta.png'
set key
set xlabel 'Request delta'
set ylabel 'time (in millis)'
set title 'archType: ASYNCHRONOUS; requestCount = 40; arraySize = 500; clientNumber = 10'
plot 'reports/asynchronous/request_delta.csv' using 1:2 with linespoints title 'timeServer', 'reports/asynchronous/request_delta.csv' using 1:3 with linespoints title 'timeTask', 'reports/asynchronous/request_delta.csv' using 1:4 with linespoints title 'timeClient'
