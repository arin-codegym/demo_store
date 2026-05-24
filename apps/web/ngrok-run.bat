@echo off
title Fullstack Dev Environment

:: 1. Mo cua so moi de chay Next.js
start "Next.js Server" cmd /k "npm run dev"

:: 2. Cho 5 giay de server local khoi dong xong
timeout /t 5

:: 3. Chay ngrok ngay tai cua so nay
echo Dang ket noi localhost:3000 voi internet...
ngrok.exe start huy

pause