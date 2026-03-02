#!/bin/bash

# Script to generate and transform Postman collection from Spring Boot OpenAPI docs

set -e  # Exit on any error

echo "Generating OpenAPI JSON from localhost:8080..."
curl -s http://localhost:8080/v3/api-docs > temp.json

echo "Converting to Postman collection..."
npx openapi-to-postmanv2 -s temp.json -o myapp-collection.json -p -O folderStrategy=Tags

echo "Cleaning up temp file..."
rm temp.json

echo "Transforming collection for environment variables and auth..."
python3 postman/transform_to_postman.py myapp-collection.json

echo "Done! Import myapp-collection_transformed.json into Postman."
echo "Set up environment variables: base_url, username, password"
echo "Run the login request to set accessToken and refreshToken automatically."
