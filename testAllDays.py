import subprocess
import shlex

print("okay")
command = "echo %JAVA_HOME%"
# command = "java -jar target/ilp-1.0-SNAPSHOT.jar 02 02 2022 9898 9876"
args = shlex.split(command)
x = subprocess.Popen(command, shell=True, stdout=True, stderr=True)
y = x.communicate()
print(y)
print(x)