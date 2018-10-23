@echo off
rem 设置工程路程和apk名

rem D1Launcher
set project_dir=D:\kobeMananger\workplace\source\git\myself\2\utils\app\build\outputs\apk\debug
set input_apk_name=app-debug
set output_apk_name=utils

rem 下面的不做改动

java -jar D:\SIGNAPK\signapk.jar D:\SIGNAPK\platform.x509.pem D:\SIGNAPK\platform.pk8 %project_dir%\%input_apk_name%.apk D:\SIGNAPK\out\%output_apk_name%.apk
