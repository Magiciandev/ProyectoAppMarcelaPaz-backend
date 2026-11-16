cd api-gateway
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml

cd ../ms-auth
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml

cd ../ms-contenidos
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml

cd ..
