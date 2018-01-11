# UserManager

Servlet-based web-application which purpose is to manage user accounts. The application uses Jetty as a web-container and provides  a user with a RESTful interface, that permits creation, retrieval, updating, and deletion of a user account. All accounts are stored on the server disk in separate files of human-readable JSON format. A record about a user account consists of fields name, sex, birthday, login, and password.

In order to build the application run 'mvn clean install' in the root folder. First build may take a while, for it will install local NodeJS and NPM and all the needed dependencies.

The web-application is running on a port 8080.
RESTful API lies at /api/accounts URL. It supports CRUD requests. For further information read the description in Swagger format at the root folder.
