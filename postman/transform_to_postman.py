#!/usr/bin/env python3
import json
import sys

def transform_postman_collection(postman_data):
    # Ensure it's a Postman collection
    if "info" not in postman_data or "item" not in postman_data:
        print("Invalid Postman collection format")
        return postman_data

    # Add environment variables if not present
    if "variable" not in postman_data:
        postman_data["variable"] = []

    # Add required variables
    required_vars = ["base_url", "username", "password", "accessToken", "refreshToken", "bearerToken"]
    existing_vars = {var["key"] for var in postman_data["variable"]}

    for var in required_vars:
        if var not in existing_vars:
            postman_data["variable"].append({
                "key": var,
                "value": "",
                "type": "string"
            })

    # Transform items (requests)
    transform_items(postman_data["item"])

    return postman_data

def transform_items(items):
    for item in items:
        if "item" in item:  # Folder
            transform_items(item["item"])
        elif "request" in item:  # Request
            transform_request(item)

def transform_request(postman_request):
    request = postman_request["request"]
    name = postman_request.get("name", "").lower()

    # Check if it's a login request
    if "login" in name or "auth" in name or "refresh" in name:
        # Modify body to use environment variables
        if "body" in request and request["body"].get("mode") == "raw":
            raw_body = request["body"].get("raw", "")
            try:
                body_json = json.loads(raw_body)
                if "username" in body_json:
                    body_json["username"] = "{{username}}"
                if "password" in body_json:
                    body_json["password"] = "{{password}}"
                request["body"]["raw"] = json.dumps(body_json, indent=2)
            except json.JSONDecodeError:
                pass

        # Add tests to set tokens
        if "event" not in postman_request:
            postman_request["event"] = []

        postman_request["event"].append({
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test(\"Status code is 200\", function () {",
                    "    pm.response.to.have.status(200);",
                    "});",
                    "",
                    "if (pm.response.code === 200) {",
                    "    var jsonData = pm.response.json();",
                    "    if (jsonData.accessToken) {",
                    "        pm.environment.set(\"accessToken\", jsonData.accessToken);",
                    "        pm.environment.set(\"bearerToken\", jsonData.accessToken);",
                    "    }",
                    "    if (jsonData.refreshToken) {",
                    "        pm.environment.set(\"refreshToken\", jsonData.refreshToken);",
                    "    }",
                    "}"
                ],
                "type": "text/javascript"
            }
        })
    else:
        # Add Bearer auth for other requests
        if "auth" not in request:
            request["auth"] = {
                "type": "bearer",
                "bearer": [
                    {
                        "key": "token",
                        "value": "{{accessToken}}",
                        "type": "string"
                    }
                ]
            }

    # Update URLs to use base_url
    if "url" in request:
        url_obj = request["url"]
        if isinstance(url_obj, dict) and "raw" in url_obj:
            raw_url = url_obj["raw"]
            if "localhost:8080" in raw_url:
                url_obj["raw"] = raw_url.replace("http://localhost:8080", "{{base_url}}")
                # Update host and path accordingly
                if "host" in url_obj and url_obj["host"] == ["localhost:8080"]:
                    url_obj["host"] = ["{{base_url}}"]

def main():
    if len(sys.argv) != 2:
        print("Usage: python transform_postman.py <postman_collection.json>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = input_file.replace(".json", "_transformed.json")

    with open(input_file, 'r') as f:
        postman_data = json.load(f)

    transformed_data = transform_postman_collection(postman_data)

    with open(output_file, 'w') as f:
        json.dump(transformed_data, f, indent=2)

    print(f"Transformed collection saved to {output_file}")

if __name__ == "__main__":
    main()
