set term png
set output 'reports/nonblocking/array_size.png'
set key
set xlabel 'Array size'
set ylabel 'time (in millis)'
set title 'archType: NONBLOCKING; requestCount = 20; requestDelta = 100; clientNumber = 10'
plot 'reports/nonblocking/array_size.csv' using 1:2 with linespoints title 'timeServer', 'reports/nonblocking/array_size.csv' using 1:3 with linespoints title 'timeTask', 'reports/nonblocking/array_size.csv' using 1:4 with linespoints title 'timeClient'
