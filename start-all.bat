@echo off
chcp 65001 >nul
echo ========================================
echo   植物养护管理系统 - 一键启动
echo ========================================
echo.

REM 检查必要环境
echo [1/3] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)
echo [OK] Java环境正常

echo.
echo [2/3] 检查Node.js环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请先安装Node.js 16+
    pause
    exit /b 1
)
echo [OK] Node.js环境正常

echo.
echo [3/3] 检查MySQL和Redis...
echo [提示] 请确保MySQL和Redis服务已启动
echo.

REM 启动后端（新窗口）
echo ========================================
echo 正在启动后端服务...
start "后端服务" cmd /k "cd plant-backend && mvn spring-boot:run"

REM 等待后端启动
echo 等待后端服务启动...
timeout /t 15 /nobreak >nul

REM 启动前端（新窗口）
echo.
echo 正在启动前端服务...
start "前端服务" cmd /k "cd plant-frontend && npm run dev"

echo.
echo ========================================
echo 启动完成！
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173
echo ========================================
pause
