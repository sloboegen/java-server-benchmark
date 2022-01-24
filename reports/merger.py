import os


def getTimeCol(timeType):
    if timeType == 'sTime':
        return 1
    elif timeType == 'tTime':
        return 2
    elif timeType == 'cTime':
        return 3
    else:
        raise RuntimeError('error type of time')


def getVarName(varType):
    if varType == 1:
        return 'request_delta'
    elif varType == 2:
        return 'array_size'
    elif varType == 3:
        return 'client_number'
    else:
        raise RuntimeError('error var type')


def getVarCsv(varType):
    return getVarName(varType) + '.csv'


def generate_plot_config(timeType, varType):
    varName = getVarName(varType)
    varFileCsv = getVarCsv(varType)
    csvFile = f'{timeType}_{varFileCsv}'
    with open(f'{timeType}_{varName}.p', 'w') as config:
        config.write('set term png\n')
        config.write(f'set output \'{timeType}_{varName}.png\'\n')
        config.write('set key\n')
        config.write(f'set xlabel \'{varName}\'\n')
        config.write('set ylabel \'time (in millis)\'\n')
        config.write(f'plot \'{csvFile}\' using 1:2 with linespoints title \'Blocking\', \'{csvFile}\' using 1:3 with linespoints title \'nonblocking\', \'{csvFile}\' using 1:4 with linespoints title \'asynchronous\'\n')


def merge(timeType, varType):
    varTypeCsv = getVarCsv(varType)
    with open(f'{timeType}_{varTypeCsv}', 'w') as result:
        generate_plot_config(timeType, varType)

        results = []
        init = False
        for archType in ['blocking', 'nonblocking', 'asynchronous']:
            with open(os.path.join(archType, varTypeCsv), 'r') as file:
                lines = file.readlines()[2:]
                if not init:
                    init = True
                    results.append(
                        list(map(lambda line: line.split(',')[0], lines)))

            col = getTimeCol(timeType)
            results.append(
                list(map(lambda line: line.strip().split(',')[col], lines)))

        result.write('# varP blocking nonblocking asynchronous\n')
        result.write('# ----------------\n')
        for i in range(len(results[0])):
            result.write(
                f'{results[0][i]}, {results[1][i]}, {results[2][i]}, {results[3][i]}\n')

    plot_config = timeType + '_' + getVarName(varType) + '.p'
    os.system(f'gnuplot {plot_config}')


for i in [1, 2, 3]:
    merge('sTime', i)
    merge('tTime', i)
    merge('cTime', i)
