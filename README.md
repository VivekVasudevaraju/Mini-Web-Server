# Mini Web Server

A lean, modular web server for rapid full-stack development.

* Supports HTTP, HTTPS and HTTP2.

Use this tool to:

* Build any type of front-end web application (static, dynamic, Single Page App etc)

### Static web site

Running `javac MiniWebServer.java` and then `java MiniWebServer` without any arguments will host the current directory as a static web site. Navigating to the server will render a directory listing or your `index.html`, if that file exists.

```sh
$ javac MiniWebServer.java
$ java MiniWebServer
Serving HTTP on localhost port 2540 ...
```

### Static files tutorial

![image](https://user-images.githubusercontent.com/45933612/155931346-1099f472-8b9f-4015-8476-394dae291e89.png)
