@echo off
chcp 65001
echo.
echo [信息] 打包framework工程并发布到maven仓库。
echo.
call  mvn  install -f jzo2o-parent -DskipTests=true
cmd
