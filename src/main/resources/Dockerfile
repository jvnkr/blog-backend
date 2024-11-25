# Use an official PostgreSQL runtime as a parent image
FROM postgres:latest

# Set environment variables
ENV POSTGRES_DB=blogapp
ENV POSTGRES_USER=blog
ENV POSTGRES_PASSWORD=blog369

# Expose the PostgreSQL port
EXPOSE 5432

# Add a custom entrypoint if needed (Optional)
# COPY custom-entrypoint.sh /docker-entrypoint-initdb.d/custom-entrypoint.sh

# By default, the official PostgreSQL image starts the database.