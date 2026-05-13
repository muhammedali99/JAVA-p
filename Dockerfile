FROM maven:3.9-eclipse-temurin-21

# Native libs needed for JavaFX GUI rendering via X11
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1 \
    libglib2.0-0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Pre-download Maven dependencies on first build (speeds up later runs)
COPY pom.xml* ./
RUN if [ -f pom.xml ]; then mvn dependency:resolve -q; fi

CMD ["bash"]
