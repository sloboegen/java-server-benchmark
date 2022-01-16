set term png
set output 'reports/blocking/array_size.png'
set key
set xlabel 'Array size'
set ylabel 'time (in millis)'
set title 'archType: BLOCKING; requestCount = 20; requestDelta = 100; clientNumber = 10'
plot 'reports/blocking/array_size.csv' using 1:2 with linespoints title 'timeServer', 'reports/blocking/array_size.csv' using 1:3 with linespoints title 'timeTask', 'reports/blocking/array_size.csv' using 1:4 with linespoints title 'timeClient'
