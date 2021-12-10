import subprocess
import pandas as pd
import csv


def run_test(day, month, year):
    day = '{:02d}'.format(day)
    month = '{:02d}'.format(month)
    command = "java -jar target/ilp-1.0-SNAPSHOT.jar " + day + " " + month + " " + str(year) + " 9898 9876 1"
    x = subprocess.run(command, shell=True, stdout=subprocess.PIPE)
    y = x.stdout.decode('utf-8')
    result = [day, month, year] + y.split()
    print(result)
    return result


dates = pd.date_range(start="2022-01-01", end="2023-12-31").to_pydatetime().tolist()
with open("results/Greedy.csv", 'w', newline='') as resultFile:
    resultWriter = csv.writer(resultFile)
    resultWriter.writerow(["Day", "Month", "Year", "Deliveries Fulfilled", "Deliveries Available",
                           "Delivery Completion Ratio", "Deliveries Value", "Available Value",
                           "Monetary Value Ratio", "Moves Remaining"])
    for date in dates:
        resultWriter.writerow(run_test(date.day, date.month, date.year))
