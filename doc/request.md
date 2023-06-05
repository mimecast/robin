HTTP/S Cases
============
HTTP/S cases leverage an HTTP client instead of an SMTP one to make API calls and assert using the external assertions.

Details
-------
The purpose if this is to test any API endpoints your MTA might have.

This provided some basic functionalities but can leverage the external assertion implementations.

Response headers are set as magic variables.

_You will need to make your own client and implement any response body data handling._


Examples
-------

### POST/PUT JSON
Make a JSON POST/PUT request.

      request: {
        url: "https://robin.requestcatcher.com/",
        type: "POST/PUT", // Either POST or PUT.
        headers: [
          {
            name: "Cache-Control",
            value: "no-cache"
          }
        ],
        content: {
          payload: "{\"name\": \"Robin\"}",
          mimeType: "application/json"
        }
      }


### POST/PUT with parameters
Make a POST/PUT with form data.

      request: {
        url: "https://robin.requestcatcher.com/",
        type: "POST/PUT", // Either POST or PUT.
        headers: [
          {
            name: "Content-Type",
            value: "text/html"
          }
        ],
        params: [
          {
            name: "name",
            value: "Robin"
          }
        ]
      }


### POST/PUT with parameters and files
Make a POST/PUT with multipart form data (file upload).

      request: {
        url: "https://robin.requestcatcher.com/",
        type: "POST/PUT", // Either POST or PUT.
        headers: [
          {
            name: "Content-Type",
            value: "text/html"
          }
        ],
        params: [
          {
            name: "name",
            value: "Robin"
          }
        ],
        files: [
          {
            name: "logo",
            value: "src/test/resources/mime/logo.jpg"
          },
          {
            name: "photo",
            value: "src/test/resources/mime/selfie.jpg"
          }
        ]
      }

### DELETE
Make a DELETE request

      request: {
        url: "https://robin.requestcatcher.com/",
        type: "DELETE",
        headers: [
          {
            name: "Content-Type",
            value: "text/html"
          }
        ]
      }
