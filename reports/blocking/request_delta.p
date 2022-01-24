set term png
set output 'reports/blocking/request_delta.png'
set key
set xlabel 'Request delta'
set ylabel 'time (in millis)'
set title 'archType: BLOCKING; requestCount = 20; arraySize = 500; clientNumber = 10'
plot 'reports/blocking/request_delta.csv' using 1:2 with linespoints title 'timeServer', 'reports/blocking/request_delta.csv' using 1:3 with linespoints title 'timeTask', 'reports/blocking/request_delta.csv' using 1:4 with linespoints title 'timeClient'
