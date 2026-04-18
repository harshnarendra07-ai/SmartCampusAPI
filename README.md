# Smart Campus Sensor & Room Management API

## 1. API Overview
The **Smart Campus API** is a robust, RESTful web service built using Java and the JAX-RS (Jakarta RESTful Web Services) framework. Designed to support a university's automated building infrastructure, it manages physical `Rooms`, tracks diverse IoT `Sensors` (such as CO2, temperature, and occupancy monitors), and logs historical `SensorReadings`. 

The architecture strictly adheres to REST principles, leveraging standard HTTP methods, appropriate status codes (e.g., 201 Created, 204 No Content), sub-resource locators for nested data, and custom exception mapping (e.g., 409 Conflict, 422 Unprocessable Entity, 403 Forbidden) to ensure high resilience and meaningful error handling.

---

## 2. Build & Launch Instructions

This project is built using **Maven** and runs on a standard Java EE / Jakarta EE application server (such as Apache Tomcat, GlassFish, or Payara).

**Prerequisites:**
* Java Development Kit (JDK) 8 or higher installed.
* Apache Maven installed.
* A Java EE Application Server (e.g., GlassFish, Payara, or Tomcat) configured.

**Step-by-Step Instructions:**
1. **Clone the repository:**
   ```bash
   git clone <your-github-repo-url>
   cd CampusAPI
   ```
2. **Build the project:**
   Run the following Maven command to clean previous builds, compile the code, and package it into a `.war` file.
   ```bash
   mvn clean install
   ```
3. **Deploy the Server:**
   * **Using an IDE (NetBeans/Eclipse):** Open the project, right-click, and select **Run**. The IDE will deploy the `.war` file to your configured server automatically.
   * **Manual Deployment:** Copy the `CampusAPI-1.0-SNAPSHOT.war` file generated in the `/target` directory into your application server's `webapps` or `autodeploy` folder, then start the server.
4. **Access the API:**
   The base URL for the API is: `http://localhost:8080/CampusAPI/api/v1/`

---

## 3. Sample cURL Commands

Here are five `curl` commands demonstrating successful interactions with different parts of the API:

**1. View Discovery Metadata (GET)**
```bash
curl -X GET http://localhost:8080/CampusAPI/api/v1/ \
     -H "Accept: application/json"
```

**2. Create a new Room (POST)**
```bash
curl -X POST http://localhost:8080/CampusAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"name": "Library Study Zone", "capacity": 50}'
```

**3. Register a new Sensor (POST)**
*(Note: Replace `<room-id>` with the UUID generated from the previous request)*
```bash
curl -X POST http://localhost:8080/CampusAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"type": "CO2", "status": "ACTIVE", "roomId": "<room-id>"}'
```

**4. Filter Sensors by Type (GET)**
```bash
curl -X GET "http://localhost:8080/CampusAPI/api/v1/sensors?type=CO2" \
     -H "Accept: application/json"
```

**5. Add a Sensor Reading (POST)**
*(Note: Replace `<sensor-id>` with a valid Sensor UUID)*
```bash
curl -X POST http://localhost:8080/CampusAPI/api/v1/sensors/<sensor-id>/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 415.5}'
```


## 4. Conceptual Report

Q.1.1) Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures $(maps/lists)$ to prevent data loss or race conditions.

ANS ==> JAX-RS defaults to treating resource classes as "per-request". This means that for each incoming HTTP request, a new instance of the class (for example, RoomResource) is generated and then deleted once the response is given. To avoid data loss between many requests when no database is used, in-memory data structures (such as Maps or Lists) must be marked as static, so that they belong to the class rather than the temporary request instance.


Q.1.2) Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

ANS ==> HATEOAS integrates navigation links into API responses. Client developers gain from this method since it eliminates the need to hard-code backend URLs. Instead, the API dynamically informs the client of the actions that are currently accessible, separating the client from the server's routing structure and making the system much more responsive to future modifications.

------------------------------------------------------------------------------------------------------------------------------------------

Q.2.1) When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

ANS ==> Returning only IDs minimises the initial network bandwidth, which is useful for huge datasets. However, it forces the client to submit additional queries to obtain the exact data of each room. Returning the full room objects consumes more bandwidth per request, but it allows the client to produce the entire UI in a single round-trip, which is frequently preferable for dashboard performance.


Q.2.2) Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

ANS ==> Yes, the DELETE action is idempotent. Idempotency indicates that performing the identical request several times results in the server remaining in the exact same state. A 204 No Content error message is returned when a client deletes a room. If they issue the same DELETE request again, the server responds with a 404 Not Found. Even when the status code changes, the system's final state (the room does not exist) remains constant.

------------------------------------------------------------------------------------------------------------------------------------------

Q.3.1) We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

ANS ==> @Consumes (MediaType.The APPLICATION_JSON) annotation serves as a tight filter. If a client attempts to provide data in an invalid format (such as text/plain or XML), the JAX-RS runtime intercepts the request before it reaches the Java function. It automatically rejects the request and returns an HTTP 415 Unsupported Media Type error, shielding the application logic from parsing issues.

Q.3.2) You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

ANS ==> Query parameters are better for filtering since they serve as optional modifications to an existing collection (/sensors). If no matching sensors are found, the API simply returns an empty list [] and a 200 OK. Using URL paths (/sensors/type/CO2) indicates that "CO2" is a concrete structural item; if none exist, the API would technically have to produce a 404 Not Found error, which is semantically inappropriate for a search filter that returns no results.

------------------------------------------------------------------------------------------------------------------------------------------

Q.4.1) Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

ANS ==> The Sub-Resource Locator technique keeps a single controller class from getting large and problematic. Rather of putting the logic for /sensors/{id}/readings directly inside SensorResource, we delegate it to a separate SensorReadingResource class. This complies to the Single Responsibility Principle by separating the logic for controlling physical sensors from the logic needed to track large amounts of historical data.

------------------------------------------------------------------------------------------------------------------------------------------

Q.5.1) Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload

ANS ==> A 404 Not Found error indicates that the requested URI endpoint is incorrect. If a client sends acceptable JSON to the right endpoint such as (/sensors), but the payload contains a roomId that does not exist, throwing a 404 error is deceptive. An HTTP 422 Unprocessable Entity appropriately informs the client.

Q.5.2) From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

ANS ==> Returning a raw HTTP 500 stack trace reveals sensitive backend data, including specific framework versions (e.g., Jersey, Jackson), database drivers, and internal folder structures. Malicious actors can use this very particular version data to look for known vulnerabilities  and perform targeted attacks on those specific libraries, such as remote code execution or injection.

