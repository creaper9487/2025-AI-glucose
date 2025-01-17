@echo off
pushd %~dp0
cls

cd ..\glucose-FE
pnpm dev --open

pause
popd