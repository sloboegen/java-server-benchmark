set term png
set output 'reports/blocking/client_number.png'
set key
set xlabel 'Client number'
set ylabel 'time (in millis)'
set title 'archType: BLOCKING; requestCount = 40; requestDelta = 10; arraySize = 500'
plot 'reports/blocking/client_number.csv' using 1:2 with linespoints title 'timeServer', 'reports/blocking/client_number.csv' using 1:3 with linespoints title 'timeTask', 'reports/blocking/client_number.csv' using 1:4 with linespoints title 'timeClient'
