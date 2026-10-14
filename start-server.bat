@echo off
title MediGuard Online Server
echo ========================================================
echo  Starting MediGuard Online Server for Multi-Device Sync
echo ========================================================
cd /d "%~dp0server"
node server.js
pause
