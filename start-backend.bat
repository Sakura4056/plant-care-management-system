@echo off
chcp 65001 >nul
echo ========================================
echo   植物养护管理系统 - 后端启动脚本
echo ========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)

REM 进入后端目录
cd plant-backend

REM 检查是否存在jar包
if exist "target\plant-backend-0.0.1-SNAPSHOT.jar" (
    echo [信息] 找到已编译的jar包，直接启动...
    echo.
    java -jar -Xms512m -Xmx1024m target\plant-backend-0.0.1-SNAPSHOT.jar
) else (
    echo [信息] 未找到jar包，开始编译...
    echo.
    
    REM 检查Maven
    call mvn -version >nul 2>&1
    if %errorlevel% neq 0 (
        echo [错误] 未检测到Maven，请先安装Maven 3.6+
        pause
        exit /b 1
    )
    
    REM 编译并启动
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [错误] 编译失败
        pause
        exit /b 1
    )
    
    echo.
    echo [信息] 编译成功，启动后端服务...
    echo.
    java -jar -Xms512m -Xmx1024m target\plant-backend-0.0.1-SNAPSHOT.jar
)
