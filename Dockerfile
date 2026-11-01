FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN mkdir -p uploads && find src -name "*.java" > sources.txt && javac -cp ".:lib/*" -d . @sources.txt
EXPOSE 8080
CMD ["java", "-cp", ".:lib/*", "com.campus.lostfound.WebServer"]
