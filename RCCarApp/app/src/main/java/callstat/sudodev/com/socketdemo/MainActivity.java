package callstat.sudodev.com.socketdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import net.sf.xenqtt.client.AsyncClientListener;
import net.sf.xenqtt.client.AsyncMqttClient;
import net.sf.xenqtt.client.MqttClient;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.client.Subscription;
import net.sf.xenqtt.client.SyncMqttClient;
import net.sf.xenqtt.message.ConnectReturnCode;
import net.sf.xenqtt.message.QoS;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends Activity {

    TextView textResponse;
    EditText inputText;
    AsyncMqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textResponse = (TextView)findViewById(R.id.response);
        final CountDownLatch connectLatch = new CountDownLatch(1);
        final AtomicReference<ConnectReturnCode> connectReturnCode = new AtomicReference<ConnectReturnCode>();
        final List<String> catalog = Collections.synchronizedList(new ArrayList<String>());

        Log.d("Aditya","in act");

        AsyncClientListener listener = new AsyncClientListener() {

            @Override
            public void publishReceived(MqttClient client, PublishMessage message) {
                Log.d("aditya",message.getPayloadString());
                catalog.add(message.getPayloadString());
                message.ack();
            }

            @Override
            public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
                if (cause != null) {
                    Log.d("aditya","Disconnected from the broker due to an exception.", cause);
                } else {
                    Log.d("aditya","Disconnecting from the broker.");
                }

                if (reconnecting) {
                    Log.d("aditya","Attempting to reconnect to the broker.");
                }
            }

            @Override
            public void connected(MqttClient client, ConnectReturnCode returnCode) {
                connectReturnCode.set(returnCode);
                connectLatch.countDown();
            }

            @Override
            public void published(MqttClient client, PublishMessage message) {
                // We do not publish so this should never be called, in theory ;).
            }

            @Override
            public void subscribed(MqttClient client, Subscription[] requestedSubscriptions, Subscription[] grantedSubscriptions, boolean requestsGranted) {
                if (!requestsGranted) {
                    Log.d("Aditya","Unable to subscribe to the following subscriptions: " + Arrays.toString(requestedSubscriptions));
                }

                Log.d("Aditya","Granted subscriptions: " + Arrays.toString(grantedSubscriptions));
            }

            @Override
            public void unsubscribed(MqttClient client, String[] topics) {
                Log.d("Aditya","Unsubscribed from the following topics: " + Arrays.toString(topics));
            }
        };
        client = new AsyncMqttClient("tcp://192.168.1.36:1883", listener, 5);
        try {
            ConnectReturnCode returnCode = client.connect("musicProducer", false);
            if (returnCode != ConnectReturnCode.ACCEPTED) {
                System.out.println("Unable to connect to the broker. Reason: " + returnCode);
                return;
            }
            List<Subscription> subscriptions = new ArrayList<Subscription>();
            subscriptions.add(new Subscription("ack", QoS.AT_MOST_ONCE));
            client.subscribe(subscriptions);

        } catch (Exception ex) {
            System.out.println("An exception prevented the publishing of the full catalog."+ ex);
        }
    }

    public void up(View view) {
        client.publish(new PublishMessage("command", QoS.AT_MOST_ONCE, "up"));
    }

    public void down(View view) {
        client.publish(new PublishMessage("command", QoS.AT_MOST_ONCE, "down"));
    }

    public void right(View view) {
        client.publish(new PublishMessage("command", QoS.AT_MOST_ONCE, "right"));
    }

    public void left(View view) {
        client.publish(new PublishMessage("command", QoS.AT_MOST_ONCE, "left"));
    }

    public void stop(View view) {
        client.publish(new PublishMessage("command", QoS.AT_MOST_ONCE, "stop"));
    }
}
