# Use an official PostgreSQL runtime as a parent image
FROM postgres:latest

# Set environment variables
ENV POSTGRES_DB=blogapp
ENV POSTGRES_USER=jovan
ENV POSTGRES_PASSWORD=jovan123

# Expose the PostgreSQL port
EXPOSE 5432

# Add a custom entrypoint if needed (Optional)
# COPY custom-entrypoint.sh /docker-entrypoint-initdb.d/custom-entrypoint.sh

# By default, the official PostgreSQL image starts the database.