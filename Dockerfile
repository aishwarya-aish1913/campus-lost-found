FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN javac -cp ".:lib/*" -d . $(ind src -name "*.java")
EXPOSE 8080
CMD ["java", "-cp", ".:lib/*", "com.campus.lostfound.WebServer"]
