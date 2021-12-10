import subprocess


def run_test(day, month, year):
    day = '{:02d}'.format(day)
    month = '{:02d}'.format(month)
    command = "java -jar target/ilp-1.0-SNAPSHOT.jar " + day + " " + month + " " + str(year) + " 9898 9876 1"
    x = subprocess.run(command, shell=True, stdout=subprocess.PIPE)
    y = x.stdout.decode('utf-8')
    result = [day, month, year] + y.split()
    print(result)
    return result


for i in range(1, 13):
    run_test(i, i, 2022)
