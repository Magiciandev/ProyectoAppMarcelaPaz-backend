cd api-gateway
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ../ms-auth
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ../ms-contenidos
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ../ms-actividades-evaluaciones
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ../ms-progreso
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ../ms-admin
call .\mvnw clean -s ..\configuracion-maven\settings.xml

cd ..
