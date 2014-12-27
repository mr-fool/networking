import java.net.*;
import java.util.*;
import java.io.*;
import java.awt.Image;  
import java.awt.image.BufferedImage;
import javax.imageio.*; 

public class HTTrack {
	public static void main(String[] args) throws Exception {
		//Error checking
		if (args.length != 1) {
			System.err.println("no url has been entered");
			System.exit(-1);
		}
		
		try {
			//Converting the user input into url type
			URL user_input = new URL(args[0]);
			String url_string = args[0];
			//Preparing for parsing
			String hostName = user_input.getHost();
			String pathName = user_input.getPath();
			String content = hostName + pathName;
			String relativePath = content.substring(0, content.lastIndexOf("/")) + "/";
			//System.out.println("the relativePath is " + relativePath);
			
			String htmlSource = getHTML(url_string);
			//System.out.println("The source code is " + htmlSource);
			//all the links including 404
			ArrayList<String> testLink = new ArrayList<String>();
			testLink = getLinks(htmlSource,relativePath);
			/*for(String tmp: testLink) { 
				System.out.println("getLinks check " + tmp); 
			}*/
			//all the working links
			ArrayList<String> aliveLink = new ArrayList<String>();
			aliveLink = workingLink(testLink);
			
			//the links that should be download
			ArrayList<String> finalLink = new ArrayList<String>();
			finalLink = downloadLink(aliveLink);
			/*for(String tmp: finalLink) { 
				System.out.println("finalLink check " + tmp); 
			}*/
			
			//Downloading the files
			save(finalLink,url_string);
		}
		//catch block
		catch (IllegalArgumentException e){
			System.err.println("url is of an invalid type or null");
		}
		catch (SecurityException e) {
			System.err.println("security manager is present and permission to connect to the url is denied");
		}
		catch(MalformedURLException e) {
			System.err.println("Malformed URL Exception");
		}
		catch (Exception e) {
			System.err.println("This program is jinx");
			e.printStackTrace();
		}
	
	}
	public static String getHTML(String url_string) throws Exception {
		String sourceCodeString ="";
		try{
			//creating a connection
			URL user_input = new URL(url_string);
			String hostName = user_input.getHost();
			//System.out.println("the hostname is " + hostName);
			String pathName = user_input.getPath();
			//System.out.println("the get path is "+ pathName);
			Socket s = new Socket(InetAddress.getByName(hostName), 80);

			StringBuffer inputLine = new StringBuffer();
			String tmp =""; 
			
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println("GET "+pathName + " HTTP/1.0");
			pw.println("Host: "+hostName);
			pw.println();
			pw.flush();	
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			while((tmp = in.readLine()) != null) {
				inputLine.append(tmp);
			}
			in.close();
			//System.out.println("The source code in inputLine " + inputLine);
			sourceCodeString = inputLine.toString();
			//System.out.println("The source code in sourceCodeString " + sourceCodeString);
		}
		catch (Exception e) {
			System.out.println("getHtml get goxxed");
			e.printStackTrace();
		}
		return sourceCodeString;
	}
	public static void save (ArrayList<String> finalLink, String index) throws Exception {
		finalLink.add(index);
		BufferedImage image = null;
		
		for (String tmp: finalLink) {
			
			URL link = new URL(tmp);
			String hostName = link.getHost();
			String pathName = link.getPath();
			int start = pathName.indexOf("/");
			int stop = pathName.lastIndexOf("/") ;
			String end = pathName.substring(start,stop);
			//create directory
			boolean folder = (new File(hostName + end)).mkdirs();
			
			//file name
			int fileStart = stop + 1;
			int fileEnd = pathName.length();
			String fileName = pathName.substring(fileStart,fileEnd);
			String dir = hostName + end;
			String information = dir+"/"+fileName;
			//System.out.println("the file name is " + fileName);
			
			//open connection
			Socket socket = new Socket(InetAddress.getByName(hostName), 80);
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println("GET "+pathName + " HTTP/1.0");
			pw.println("Host: "+hostName);
			pw.println();
			pw.flush();	
			
			//handling image
			if (!fileName.contains("html") ) {
				int imageStart = fileName.indexOf(".") + 1;
				int imageStop = fileName.length();
				String fileExtension = fileName.substring(imageStart,imageStop);
				//System.out.println(fileExtension);
				image = ImageIO.read(link);
				File file = new File(information);
				ImageIO.write(image,fileExtension, file);
				continue;
			}
			
			//Prepare for write
			BufferedReader reading = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(information)));		
			
			//write 
			String line;
			while((line = reading.readLine())!=null){
				//skipping html header
				if ( line.contains("HTTP") ||  line.contains("Date:") || line.contains("Server:") ||
				line.contains("Last-Modified:") ||  line.contains("ETag:") ||
				line.contains("Accept-Ranges:") || line.contains("Content-Length:") || line.contains("Connection:") ||
				line.contains("Content-Type:") ){
					continue;
				}
				writer.write(line);
				writer.newLine();
			}
			writer.close();
		}
	}

	public static ArrayList<String> getLinks(String s, String composeLink) throws Exception {
		String sourceCodeString =""; 
		int start = 0;
		int stop = 0;
		String url = "";
		ArrayList<String> allLink = new ArrayList<String>();
		ArrayList<String> link = new ArrayList<String>();
		//Search for the href tag
		while((start = s.indexOf("href=\"", stop)) != -1) {
			
			stop = s.indexOf("\"", start +6 );
			url = s.substring(start +6, stop);
			//System.out.println("line 105 url " + url);
			if (url.contains("http") == false) { //avoid dling yahoo link
				//Creating a link
				URL user_input = new URL("http://" +composeLink +url);
				String links = user_input.toString(); 
				//System.out.println("the links are " + links);
				allLink.add(links);
			}
		}
		
		return allLink;
	}
	public static ArrayList<String> workingLink(ArrayList<String> testLink) throws Exception {
		ArrayList<String> finalLink = new ArrayList<String>();
		URL user_input = null;
		//iterating all the links and opening to determine which one is the dead link
		for(String tmp: testLink) { 
			 user_input = new URL(tmp);
			 //System.out.println("line 122 " + user_input.toString());
			 
			 //preparing to eopn a connect
			 String hostName = user_input.getHost();
			//System.out.println("the hostname is " + hostName);
			String pathName = user_input.getPath();
			//System.out.println("the get path is "+ pathName);
			Socket s = new Socket(InetAddress.getByName(hostName), 80);
			
			//start reading
			//StringBuffer inputLine = new StringBuffer();
			String temp =""; 
			
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println("GET "+pathName + " HTTP/1.0");
			pw.println("Host: "+hostName);
			pw.println();
			pw.flush();	
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			while((temp = in.readLine()) != null) {
				//System.out.println("line 148 " + temp);
				if (temp.contains("200")) {
					finalLink.add(tmp);
					break;
				}
				
			}
			in.close();
		}
		//System.out.println("line 124 " + user_input.toString());
		/*for(String check: finalLink) { 
			System.out.println("getLinks check " + check); 
		}*/
		return finalLink;
	}
	public static ArrayList<String> downloadLink(ArrayList<String> aliveLink) throws Exception {
		ArrayList<String> finalLink = new ArrayList<String>();
		URL user_input = null;
		//iterating all the links and opening to determine which one is the dead link
		for(String tmp: aliveLink) { 
			String content = "";
			user_input = new URL(tmp);
			//System.out.println("line 122 " + user_input.toString());
			 
			//preparing to eopn a connect
			String hostName = user_input.getHost();
			//System.out.println("the hostname is " + hostName);
			String pathName = user_input.getPath();
			//System.out.println("the get path is "+ pathName);
			Socket s = new Socket(InetAddress.getByName(hostName), 80);
			
			//start reading
			String temp =""; 
			
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println("GET "+pathName + " HTTP/1.0");
			pw.println("Host: "+hostName);
			pw.println();
			pw.flush();	
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			while((temp = in.readLine()) != null) {
				content = content + temp;
			}
			if (content.contains("href") == false ) {
				//System.out.println("correct link");
				finalLink.add(tmp);
			}
		}
			return finalLink;
	}//close function
}
/*Reference
 * http://docs.oracle.com/javase/tutorial/networking/urls/connecting.html
 * http://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
 * alvinalexander.com/blog/post/java/java-program-download-parse-contents-of-url
 * http://docs.oracle.com/javase/tutorial/2d/images/saveimage.html
*/
 
