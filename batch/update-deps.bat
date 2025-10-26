@echo off
pushd %~dp0
cls

echo ========================================
echo    DEPENDENCY UPDATE UTILITY
echo ========================================
echo.
echo WARNING: This will update dependencies to latest versions
echo Make sure to test thoroughly after updating!
echo.
set /p confirm="Continue? (y/N): "
if /i not "%confirm%"=="y" (
    echo Update cancelled.
    pause
    popd
    exit /b
)

echo.
echo === 1/4 [Frontend - Update Dependencies] ===
cd ..\glucose-FE
cmd /c pnpm update
echo === Frontend update completed ===
echo.

echo === 2/4 [Frontend - Remove ^ prefixes] ===
echo Please manually remove ^ prefixes from package.json
echo Press any key after you've updated package.json...
pause
echo.

echo === 3/4 [Backend - Update Dependencies] ===
cd ..\glucose-BE
pip install --upgrade -r requirements.txt
echo === Backend update completed ===
echo.

echo === 4/4 [Backend - Generate new lock file] ===
pip freeze > requirements-lock.txt
echo === Lock file updated ===
echo.

echo ========================================
echo    UPDATE COMPLETED
echo ========================================
echo.
echo NEXT STEPS:
echo 1. Test both frontend and backend
echo 2. Commit package.json, pnpm-lock.yaml, and requirements-lock.txt
echo 3. Push changes to repository
echo.

pause
popd