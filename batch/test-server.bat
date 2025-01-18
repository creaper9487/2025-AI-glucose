@echo off
pushd %~dp0
cls

echo === del db ===
cd ..\glucose-BE\glucoseBE
del db.sqlite3
echo === del completed ===
echo.

echo === django migrate ===
python manage.py migrate
echo === migrations completed ===
echo.

echo === server start ===
python manage.py runserver 0.0.0.0:8000

pause
popd