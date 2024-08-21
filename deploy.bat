@echo off

cd /d "%~dp0"

echo "======================================================================================"
echo "				Start deploy TOOLS tools-parent tools-framework tools-thirdParty"
echo "======================================================================================"
call mvn clean deploy -DskipTests --file=pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				POM SUCCESS"
echo "======================================================================================"




echo "======================================================================================"
echo "				Start deploy tools-base"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-base/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-base/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-cloud"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-cloud/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-cloud/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-mail"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-mail/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-mail/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-code"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-code/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-code/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-api"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-api/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-api/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-request"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-request/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-request/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-jackson"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-jackson/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-jackson/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-overstep"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-overstep/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-overstep/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-ds"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-ds/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-ds/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-es"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-es/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-es/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-redis"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-redis/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-redis/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-kv"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-kv/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-kv/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-mybatis"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-mybatis/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-mybatis/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-mate"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-mate/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-mate/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-logback"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-logback/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-logback/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-datalog"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-datalog/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-datalog/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-webapp"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-webapp/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-webapp/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Jar SUCCESS"
echo "======================================================================================"



echo "======================================================================================"
echo "				Start deploy tools-framework->tools-seata"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-framework/tools-seata/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-framework/tools-seata/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-framework->tools-sharding"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-framework/tools-sharding/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-framework/tools-sharding/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-framework->tools-spring"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-framework/tools-spring/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-framework/tools-spring/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-framework->tools-swagger"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-framework/tools-swagger/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-framework/tools-swagger/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Framework Jar SUCCESS"
echo "======================================================================================"



echo "======================================================================================"
echo "				Start deploy tools-thirdParty->tools-alibaba"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-thirdParty/tools-alibaba/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-thirdParty/tools-alibaba/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				Start deploy tools-thirdParty->tools-tencent"
echo "======================================================================================"
call mvn clean deploy -DskipTests -Ppg,dep --file=tools-thirdParty/tools-tencent/pom.xml -DpomFile=pom.xml
call mvn install -DskipTests -Psource --file=tools-thirdParty/tools-tencent/pom.xml -DpomFile=pom.xml
echo "======================================================================================"
echo "				ThirdParty Jar SUCCESS"
echo "======================================================================================"

echo "Finish all."
