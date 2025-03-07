# Base Image
FROM ubuntu:22.04

# Set non-interactive mode for apt
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    fuse \
    libfuse2 \
    ca-certificates \
    openjdk-17-jdk \
    xz-utils \
    tree \
    file \
	gpg \
    && rm -rf /var/lib/apt/lists/*

# Set Java Home
ENV JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Download and install appimagetool
RUN wget -O /usr/local/bin/appimagetool https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage && \
    chmod +x /usr/local/bin/appimagetool
	
RUN apt-get update && apt-get install -y appstream

# Set the working directory
WORKDIR /build

# Debugging and running AppImage build
CMD echo "DEBUG: Checking if appimagetool exists..." && \
    ls -lh /usr/local/bin/appimagetool && \
	echo "DEBUG: Listing /build/AppDir structure..." && \
	tree /build/AppDir || ls -R /build/AppDir && \
    echo "DEBUG: Running appimagetool..." && \
    ARCH=x86_64 /usr/local/bin/appimagetool /build/AppDir /build/DIYLC-x86_64.AppImage
