/* 

1. Name / Date: Vivekraju Vasudevaraju / 07/05/2021

2. Java version used (java -version), if not the official version for the class:

    build 11.0.10+9

3. Precise command-line compilation examples / instructions:

    > javac MiniWebServer.java

4. Precise examples / instructions to run this program:

    > java MiniWebServer

5. List of files needed for running the program.

    a. MiniWebChecklist.html
    b. MiniWebServer.java

5. Notes:
    
    None

For the MiniWebserver assignment answer these questions briefly in YOUR OWN
WORDS here in your comments:

1. How MIME-types are used to tell the browser what data is coming.
Multi purpose Internet Mail Extension abbreviated as MIME is used to describe the media type of the content served by the web. It is intended to help guide a web browser to correctly process and display the content.
There are various MIME-types like text/html, text/plain, application/pdf.

2. How you would return the contents of requested files of type HTML
Set the MIME-type in the header sent to the browser to "text/html", this indicates the browser to load an HTML file.

3. How you would return the contents of requested files of type TEXT
Set the MIME-type in the header sent to the browser to "text/plain", this indicates the browser to load an plain text file.

*/

import java.io.BufferedReader; // Import buffered reader
import java.io.File; // Import file libs
import java.io.IOException; // Import Input/Output exceptions
import java.io.InputStreamReader; // Import input stream
import java.io.PrintStream; // Input print stream
import java.net.ServerSocket; // Import server socket libs
import java.net.Socket; // Import socket libs
import java.time.LocalDateTime; // Import data time libs
import java.time.format.DateTimeFormatter; // Import date formatting libs
import java.util.HashMap; // Import hash map
import java.util.Map; // Import map
import java.util.Scanner; // Import file scanner libs

class HTTPWorker extends Thread { 
    
    Socket socket;
    HTTPWorker(Socket s) { this.socket = s; } // Constructor to define socket connection

    /**
     * Override default run method in Thead class
     */
    public void run() {
        PrintStream outputToWeb = null; 
        BufferedReader inputFromWeb = null;
        try {
            outputToWeb = new PrintStream(this.socket.getOutputStream()); // Get output stream from socket
            inputFromWeb = new BufferedReader(new InputStreamReader(this.socket.getInputStream())); // Get input stream from socket

            File file = new File("."); // "." referes to current directory path
            String directoryRoot = file.getCanonicalPath(); // Get full path name. Ex: "C:/Users/John/Project"

            // Format date time to "30/Jun/2021 20:18:51"
            DateTimeFormatter date = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss");  
            LocalDateTime currentDateTime = LocalDateTime.now();
            String formattedDate = date.format(currentDateTime).toString();

            // Get localhost address "127.0.0.1"
            String clientSocketAddress= this.socket.getLocalSocketAddress().toString().replace("/", "");

            // Capture the request type from the browser. Ex: "GET / HTTP/1.1"
            String HTTP_URL = inputFromWeb.readLine();

            // Print this in console -> 127.0.0.1 - - [30/Jun/2021 20:18:51] "GET / HTTP/1.1" 200 -
            String webServerStatus =  clientSocketAddress + " - - [" + formattedDate + "] " + HTTP_URL + " - ";
            System.out.println(webServerStatus);

            // Isolate the file path from the browser request
            String URL = HTTP_URL != null ? (HTTP_URL.split(" ").length > 0) ? HTTP_URL.split(" ")[1] : "/" : "/";
            
            // Close connection if request is null
            if (URL == null) {
                inputFromWeb.close();
                outputToWeb.close();
                this.socket.close();
                return;
            }

            String path = directoryRoot.concat(URL); // Combine URL path and current diretory of web server
            String response = ""; // Store result 
            String mimeType = "html"; // Default mime type
            String fileNames[] = path.split("\\.");
            String extension = fileNames[fileNames.length-1]; // Extract extension from file

            // If path has .fake-cgi then compute result of 2 numbers
            if (URL.contains(".fake-cgi")) {
                Map<String, String> queries = getQueryMap(path); // Map URL params as key-value pairs
                String personName = queries.get("person");
                int number1 = Integer.parseInt(queries.get("num1"));
                int number2 = Integer.parseInt(queries.get("num2"));
                int sum = addnums(number1, number2); // Sum of 2 numbers

                response +=  "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">" +
                            "<html>" +
                            "<meta content=\"text/html;charset=utf-8\" http-equiv=\"Content-Type\">" +
                            "<meta content=\"utf-8\" http-equiv=\"encoding\">" +
                            "<head><link rel=\"icon\" href=\"#\" type=\"image/x-icon\"></head>" +
                            "<title>WebAdd</title>" +
                            "<body>" +
                                "<h1>WebAdd</h2>" +
                                "<FORM method=\"GET\" action=\"http://localhost:2540/WebAdd.fake-cgi\">" +
                                    "Enter your name and two numbers. My program will return the sum:<p>" +
                                    "<INPUT TYPE=\"text\" NAME=\"person\" size=20 value=\"YourName\"><P>" +
                                    "<INPUT TYPE=\"text\" NAME=\"num1\" size=5 value=\"4\"> <br>" +
                                    "<INPUT TYPE=\"text\" NAME=\"num2\" size=5 value=\"5\"> <p>" +
                                    "<INPUT TYPE=\"submit\" VALUE=\"Submit Numbers\">" +
                                "</FORM>" +
                                "<p>Dear " + personName + ", the sum of " + number1 + " and " + number2 + " is " + sum + ".</p>" +
                            "</body>" +
                            "</html>";

            } else if (hasFileExtension(path) && !extension.equals("ico")) { // find file type
                // Set mimeType based on the file type
                mimeType = (extension.equals("html") || extension.equals("htm")) ? "html" : "plain";
                response += readFile(path); // Convert file contents to string
            } else {
                String HTMLformatted = FileAndFoldersInDirectory(path); // Get all files and folder in path
                response +=  "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">" +
                            "<html>" +
                            "<meta content=\"text/html;charset=utf-8\" http-equiv=\"Content-Type\">" +
                            "<meta content=\"utf-8\" http-equiv=\"encoding\">" +
                            "<head><link rel=\"icon\" href=\"#\" type=\"image/x-icon\"></head>" +
                            "<title>Directory listing for " + URL + "</title>" +
                            "<body>" +
                                "<h2>Directory listing for " + URL + "</h2>" +
                                "<hr>" +
                                "<ul>" + HTMLformatted + "</ul>" +
                                "<hr>" +
                            "</body>" +
                            "</html>";
            }

            sendResposeToBrowser(outputToWeb, response, mimeType); // Send response to browser to display

            inputFromWeb.close(); // Close the input stream

            this.socket.close(); // close this connection, but not the server;
        } catch (IOException x) {
            System.out.println("Error: Connetion reset. Listening again...");
            x.printStackTrace();
        }
    }

    /**
     * Map URL params into key-value pairs
     * @param query
     * @return Map<String, String>
     */
    private Map<String, String> getQueryMap(String query) {  
        String[] params = query.split("\\?")[1].split("&");  // Split on "&"
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {  // Loop
            String name = param.split("=")[0];  
            String value = param.split("=")[1];  
            map.put(name, value);  
        }  
        return map;  
    }

    /**
     * Add 2 numbers
     * @param num1
     * @param num2
     * @return int
     */
    private int addnums(int num1, int num2) {
        return num1 + num2;
    }

    /**
     * Check if file or not
     * @param ext
     * @return boolean
     */
    private boolean hasFileExtension(String ext) {
        return (ext.split("\\.").length > 1) ? true : false;
    }

    /**
     * Read file contents and convert to string
     * @param filePath
     * @return String
     */
    private String readFile(String filePath) throws IOException {
        File myObj = new File(filePath);
        Scanner myReader = new Scanner(myObj); // Declare new scanner on file
        String output = "";
        while (myReader.hasNextLine()) { // Read file as long as it encounters \n
            output += myReader.nextLine() + "\n"; // Add file to string
        }
        myReader.close(); // Close scanner
        return output;
    }

    /**
     * Get all contents from a specified directory
     * @param directoryPath
     * @return String
     */
    private String FileAndFoldersInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        String outputHTML = ""; 
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // Check if it's a file or a directory
                if (file.isFile()) {
                    outputHTML += "<li><a href=\"" + fileName + "\">" + fileName + "</a></li>\n";
                } else if (file.isDirectory()) {
                    outputHTML += "<li><a href=\"" + fileName + "/\">" + fileName + "/</a></li>\n";
                }
            }    
        }

        return outputHTML;
    }

    /**
     * Send response to browser
     * @param outputToWeb
     * @param response
     * @param mimeType
     */
    private void sendResposeToBrowser(PrintStream outputToWeb, String response, String mimeType) {
        // Start sending our reply, using the HTTP 1.1 protocol
        outputToWeb.print("HTTP/1.1 200 \r\n"); // Version & status code
        outputToWeb.println("Content-Length: " + Integer.toString(response.length()));
        outputToWeb.print("Content-Type: text/" + mimeType + ";charset=utf-8\r\n"); // The type of data
        outputToWeb.print("Connection: close\r\n"); // Will close stream
        outputToWeb.print("\r\n"); // End of headers

        outputToWeb.print(response);

        outputToWeb.close(); // Flush and close the output stream
    }
}

/**
 * Main MiniWebServer class
 */
public class MiniWebServer {

    public static void main(String a[]) throws IOException {
        int q_len = 6; /* Number of requests for OpSys to queue */
        int port = 2540;
        Socket socket;

        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println("Serving HTTP on localhost port 2540 ...");

        while (true) {
            // wait for the next client connection:
            socket = servsock.accept();
            new HTTPWorker(socket).start();
        }
    }
}
