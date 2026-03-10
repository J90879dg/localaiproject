@echo off
setlocal
set ROOT_DIR=%~dp0..
if "%PYTHON_BIN%"=="" set PYTHON_BIN=python
if "%HOST%"=="" set HOST=127.0.0.1
if "%PORT%"=="" set PORT=8765

cd /d "%ROOT_DIR%\assistant_core\python"
%PYTHON_BIN% tools\run_local_api_server.py --host %HOST% --port %PORT%
