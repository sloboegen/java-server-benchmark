set term png
set output 'reports/asynchronous/array_size.png'
set key
set xlabel 'Array size'
set ylabel 'time (in millis)'
set title 'archType: ASYNCHRONOUS; requestCount = 40; requestDelta = 10; clientNumber = 10'
plot 'reports/asynchronous/array_size.csv' using 1:2 with linespoints title 'timeServer', 'reports/asynchronous/array_size.csv' using 1:3 with linespoints title 'timeTask', 'reports/asynchronous/array_size.csv' using 1:4 with linespoints title 'timeClient'
