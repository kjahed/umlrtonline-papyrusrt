package ca.jahed.papyrusrt.remote;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class StandaloneRemoteExecutor {
	
	public static void main(String[] args) {
		String serverUrl = args[0];
		String modelPath = args[1];
		
		String programArgs = "";
		for(int i=2; i<args.length; i++) {
			programArgs += args[i] + " ";
		}
				
		File modelFile = new File(modelPath);
		String workspaceID;
		
		System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
		org.eclipse.jetty.util.log.Log.setLog(new Logger() {
			@Override public String getName() { return "no"; }
		    @Override public void warn(String msg, Object... args) { }
		    @Override public void warn(Throwable thrown) { }
		    @Override public void warn(String msg, Throwable thrown) { }
		    @Override public void info(String msg, Object... args) { }
		    @Override public void info(Throwable thrown) { }
		    @Override public void info(String msg, Throwable thrown) { }
		    @Override public boolean isDebugEnabled() { return false; }
		    @Override public void setDebugEnabled(boolean enabled) { }
		    @Override public void debug(String msg, Object... args) { }
		    @Override public void debug(Throwable thrown) { }
		    @Override public void debug(String msg, Throwable thrown) { }
		    @Override public Logger getLogger(String name) { return this; }
		    @Override public void ignore(Throwable ignored) { }
			@Override public void debug(String arg0, long arg1) {}
		});

		System.err.println("[UML-RT][INFO] Connecting to server...");
		
   		SslContextFactory sslContextFactory = new SslContextFactory();
    		HttpClient httpClient = new HttpClient(sslContextFactory);
    		WebSocketClient webSocketClient = new WebSocketClient(httpClient);
		
    		try {
			httpClient.start();
			webSocketClient.start();
			
			MultiPartContentProvider multiPart = new MultiPartContentProvider();
			multiPart.addFilePart("file", modelFile.getName(), new PathContentProvider(
					Paths.get(modelPath)), null);

			 
			System.err.println("[UML-RT][INFO] Uploading model...");
			ContentResponse response = httpClient.newRequest(serverUrl+"/upload")
			        .method(HttpMethod.POST)
			        .content(multiPart)
			        .send();
			
			if(response.getStatus() != 200) {
				System.err.println("[UML-RT][ERROR] Internal Server Error. Try again later.");
				return;
			}
			
			workspaceID = new String(response.getContent());
			
			System.err.println("[UML-RT][INFO] Generating code...");
			response = httpClient.newRequest(serverUrl+"/generate/"+workspaceID)
			        .method(HttpMethod.GET)
			        .send();
			
			if(response.getStatus() != 200) {
				System.err.println("[UML-RT][ERROR] Internal Server Error. Try again later.");
				return;
			}
			
			System.err.println("[UML-RT][INFO] Building code...");
			response = httpClient.newRequest(serverUrl+"/build/"+workspaceID)
			        .method(HttpMethod.GET)
			        .send();
			
			if(response.getStatus() != 200) {
				System.err.println("[UML-RT][ERROR] Internal Server Error. Try again later.");
				return;
			}
			
			String jsonStr = new String(response.getContent());
			
	        JSONParser parser = new JSONParser();
	        JSONObject jsonObj = (JSONObject) parser.parse(jsonStr);
			
	        boolean hasError = (Boolean) jsonObj.get("error");
	        String buildOutput = (String) jsonObj.get("output");
	        
	        System.err.println("[UML-RT] ****** Build Output Begin ******");
	        System.out.print(buildOutput);
	        System.err.println("[UML-RT] ****** Build Output Ends ******");
	        
	        if(hasError) {
	        		System.err.println("[UML-RT][INFO] There were build errors. Terminating.");
	        } else {
	    			System.err.println("[UML-RT][INFO] Executing code...");    			
		    		ExecutorSocket executorSocket = new ExecutorSocket();
	
		    		try {
		            ClientUpgradeRequest request = new ClientUpgradeRequest();
		            webSocketClient.connect(executorSocket, new URI(serverUrl.replace("http", "ws")+"/execute/"+workspaceID+"/"+URLEncoder.encode(programArgs, "UTF-8")), request);	
		            executorSocket.awaitClose();
	            } catch (Throwable t) {
	                t.printStackTrace();
	            } finally {
	                try {
	                		webSocketClient.stop();
	                }
	                catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
		    		
		    		System.err.println("[UML-RT][INFO] Done.");
	    		}
	        
		} catch(TimeoutException e) {
			System.err.println("[UML-RT][ERROR] Request timed out. Terminating.");
			return;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		} finally {
			try {
        			httpClient.stop();
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
}