set term png
set output 'reports/nonblocking/client_number.png'
set key
set xlabel 'Client number'
set ylabel 'time (in millis)'
set title 'archType: NONBLOCKING; requestCount = 20; requestDelta = 100; arraySize = 100'
plot 'reports/nonblocking/client_number.csv' using 1:2 with linespoints title 'timeServer', 'reports/nonblocking/client_number.csv' using 1:3 with linespoints title 'timeTask', 'reports/nonblocking/client_number.csv' using 1:4 with linespoints title 'timeClient'
