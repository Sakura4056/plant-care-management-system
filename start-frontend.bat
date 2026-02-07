@echo off
chcp 65001 >nul
echo ========================================
echo   植物养护管理系统 - 前端启动脚本
echo ========================================
echo.

REM 检查Node.js环境
call node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请先安装Node.js 16+
    pause
    exit /b 1
)

REM 进入前端目录
cd plant-frontend

REM 检查是否已安装依赖
if not exist "node_modules" (
    echo [信息] 未检测到node_modules，开始安装依赖...
    echo.
    call npm install
    if %errorlevel% neq 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

echo.
echo [信息] 启动前端开发服务器...
echo.
call npm run dev
