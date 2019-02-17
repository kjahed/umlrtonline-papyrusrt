package ca.jahed.papyrusrt.remote;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class ExecutorSocket
{
    private final CountDownLatch closeLatch;

	public ExecutorSocket() {
		this.closeLatch = new CountDownLatch(1);
	}
	
    @SuppressWarnings("unused")
    private Session session;


    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    		closeLatch.countDown();
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.print(msg);
    }
    
    public void write(String msg) {
    		try {
			session.getRemote().sendString(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void awaitClose() throws InterruptedException {
        closeLatch.await();
    }
}