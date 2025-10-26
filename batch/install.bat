@echo off
pushd %~dp0
cls

echo ========================================
echo    PROJECT SETUP WITH LOCKED VERSIONS
echo ========================================
echo.

echo === 1/3 [npm install -g pnpm] ===
cmd /c npm install -g pnpm
if errorlevel 1 (
    echo ERROR: Failed to install pnpm globally
    pause
    popd
    exit /b 1
)
echo === 1/3 completed ===
echo.

echo === 2/3 [Frontend - pnpm install --frozen-lockfile] ===
cd ..\glucose-FE
if not exist "pnpm-lock.yaml" (
    echo WARNING: pnpm-lock.yaml not found. Using regular install.
    cmd /c pnpm install
) else (
    cmd /c pnpm install --frozen-lockfile
)
if errorlevel 1 (
    echo ERROR: Frontend installation failed
    pause
    popd
    exit /b 1
)
echo === 2/3 completed ===
echo.

echo === 3/3 [Backend - pip install -r requirements-lock.txt] ===
cd ..\glucose-BE
if not exist "requirements-lock.txt" (
    echo WARNING: requirements-lock.txt not found. Using requirements.txt instead.
    pip install -r requirements.txt
) else (
    pip install -r requirements-lock.txt
)
if errorlevel 1 (
    echo ERROR: Backend installation failed
    pause
    popd
    exit /b 1
)
echo === 3/3 completed ===
echo.

echo ========================================
echo    INSTALLATION COMPLETED SUCCESSFULLY
echo ========================================
echo.
echo All dependencies installed with locked versions.
echo You can now run the servers using:
echo   - Frontend: batch\web.bat
echo   - Backend:  batch\server.bat
echo.

pause
popd