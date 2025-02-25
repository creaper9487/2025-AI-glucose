@echo off
pushd %~dp0
cls

echo === django migrate ===
cd ..\glucose-BE\glucoseBE
python manage.py migrate
echo === migrations completed ===
echo.

echo === server start ===
python manage.py runserver 0.0.0.0:8000

pause
popd