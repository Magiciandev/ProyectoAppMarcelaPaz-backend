cd ms-progreso
call .\mvnw clean package -s ..\configuracion-maven\settings.xml
cd ..
docker compose up -d --build ms-progreso