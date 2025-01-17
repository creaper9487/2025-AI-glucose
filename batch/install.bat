@echo off
pushd %~dp0
cls

echo === 1/3 [npm install -g pnpm] ===
cmd /c npm install -g pnpm
echo === 1/3 completed ===
echo.

echo === 2/3 [pnpm install] ===
cd ..\glucose-FE
cmd /c pnpm install
echo === 2/3 completed ===
echo.

echo === 3/3 [pip install -r requirements.txt] ===
cd ..\glucose-BE
pip install -r requirements.txt
echo === 3/3 completed ===
echo.

echo === INSTALLATION COMPLETED ===

pause
popd