@echo off
cd /d "C:\Users\prads\kafka3.6.1>"
.\bin\windows\kafka-console-consumer.bat --topic library-events --from-beginning --bootstrap-server localhost:9092
pause

