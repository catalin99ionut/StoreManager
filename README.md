# Store Manager Project

A simple store management project with basic functionalities, like create/update/delete products and search by id/name. The products are quite simple for now, having only a unique ID, a name and a price.

## Features

- Role based operations
  - For admins:
    - Add a product
    - Update a product (name, price, or both)
    - Delete a product
  - For customers:
    - Search for a product by either ID or name

## Technologies Used

- Java
- Spring Boot
- Hibernate
- H2 Database
- JUnit / Mockito for testing

## API Endpoints

- GET /products : Retrieve all products
  - Response: 200 OK
- GET /products/id/{id} : Retrieve a product by ID
  - Response: 200 OK / 404 Not Found
- GET /products/name/{name} : Retrieve a product by name (or keyword inside its name)
  - Response: 200 OK / 404 Not Found
- POST /products : Create a new product
  - Request Body: {"name": "<product_name>", "price": <product_price>}
  - Response: 201 Created
- PATCH /products/{id} : Update an existing product
  - Request Body: {"name": "<product_name>", "price": <product_price>}
  - Response: 200 OK / 404 Not Found
- DELETE /products/{id} : Delete an existing product
  - Response: 200 OK / 404 Not Found
