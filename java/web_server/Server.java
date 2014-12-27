import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class WebServer {
	//to test http://localhost:portnumber/index.html
	public static void main(String[] args) throws Exception {
		//Creates 4 threads to handle new connections
		final ExecutorService clientHandler = Executors.newFixedThreadPool(4);
		//Error checking
		if (args.length != 1) {
            System.err.println("No arguement being enter:");
            System.err.println("Usage: java Server port");
            System.exit(-1);
        }
        
        //Creating port
        int port = 0;
        
        //converting user input to port
        if (args.length == 1){
			port = Integer.parseInt(args[0]);
			//System.out.println("the port is " + port);
		}
		//System.out.println("the port is " + port);
		//Creating server socket , assigning port
        final ServerSocket serverSocket = new ServerSocket(port);
        
        
        /*Creating new thread
        * @param accepting connections*/
		new Thread(new Runnable() {
		//@Override
		public void run() {
			//Keep trying to connect
			while (true) {
				try{
					Socket establishSocket = serverSocket.accept();
					System.out.println("Connected: " + establishSocket.getInetAddress().getHostAddress());
					
					/*create a new thread to handle this socket, the loop repeats
                     *and waits for another connection after that*/
					clientHandler.execute(new Response(establishSocket));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		} //end of run()
		}
		).start();
	}
	private static class Response implements Runnable{
        Socket socketToWorkOn;

        //dataoutput and input streams
        DataInputStream fromClient;
        DataOutputStream toClient;

        //constructor to assign the socket passed by executor
        public Response(Socket s){
            socketToWorkOn = s;
        }
        public Response(){
		 }

        public void run(){
            //Creating output and input streams on the current socket
            try{
                fromClient = new DataInputStream(socketToWorkOn.getInputStream());
                toClient = new DataOutputStream(socketToWorkOn.getOutputStream());

                Scanner input = new Scanner(socketToWorkOn.getInputStream());
                //Accepting client request
                String getRequest = "";
                String parseGet ="";
                while(input.hasNext()){
					getRequest = input.nextLine();
					if (getRequest.matches("GET(.*)") ){
						parseGet = getRequest;
						//System.out.println(getRequest);
						break;
					}
                        
                }
                //System.out.println("parseGet is " + parseGet);
                //String path = parseGet.split(" ")[1].replaceAll("/","");
                int start = parseGet.indexOf(" ");
                int end = parseGet.lastIndexOf(" ");
                String path = parseGet.substring(start,end);
                end = path.lastIndexOf("/");
                start = end + 1;
                String fileName = path.substring(start,path.length() );
                //System.out.println("fileName is " + fileName);
                //path = path.substring(0,end);
                //System.out.println("path is " + path);
               
   
                //searching file
                Response searchFile = new Response();
                String directoryPath = System.getProperty("user.dir");
                if (fileName.trim().equals("") || fileName.trim().equals("/")) {
					System.out.println("400 Bad Request");
					toClient.writeUTF("HTTP/1.0 400 Bad Request\n");
					System.exit(0);
				}
                if ( searchFile.search(fileName,new File(directoryPath)) == true) {
					System.out.println("200 OK");
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
					 //System.out.println("On line 111 fileName is " + fileName);
					 int fileExtensionStart = fileName.indexOf(".") + 1;
					 int fileExtensionEnd = fileName.length();
					 String fileExtension = fileName.substring(fileExtensionStart,fileExtensionEnd);
					 System.out.println("The file Extension is " + fileExtension);
					//responsing to client
					toClient.writeUTF("HTTP/1.0 200 OK\n");
					String htmlCopy = "";
					if (fileExtension.trim().equals("html") ){
						String text ="";
						
						toClient.writeUTF("Content-type: text/html\r\n");
						 while((text = reader.readLine())!=null){
							//System.out.println(text);
							htmlCopy = htmlCopy + text + "\n";
							//toClient.writeUTF("\n");
							//toClient.writeUTF(text);
 
						}
						toClient.writeUTF(htmlCopy);
						socketToWorkOn.shutdownOutput();
						socketToWorkOn.shutdownInput();
						socketToWorkOn.close();
						//System.exit(0);
					}
					else {
						System.out.println("file type" + fileExtension + "not supported");
						System.exit(0);
					}
	
					
				}
				else if ( searchFile.search(fileName,new File(directoryPath)) == false) {
					System.out.println("404 Not Found");
					toClient.writeUTF("HTTP/1.0 404 Not Found\n");
					System.exit(0);
				}
                
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        public boolean search(String name,File file){
        File[] list = file.listFiles();
        if(list!=null) {
            for (File tmp : list) {
                if (tmp.isDirectory()) {
                    search(name,tmp);
                }
                else if (name.equalsIgnoreCase(tmp.getName())) {
                    //System.out.println("parent file" + tmp.getParentFile());
                    //System.out.println("file found 119");
                    return true;
                }
            }
		}
        return false;
		}
    }
}
