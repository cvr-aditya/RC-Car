import paho.mqtt.client as paho
import RPi.GPIO as gpio

gpio.setmode(gpio.BOARD)


def setup():
    gpio.setup(5,gpio.OUT)
    gpio.setup(7,gpio.OUT)
    gpio.setup(11,gpio.OUT)
    gpio.setup(13,gpio.OUT)

def forward():
    gpio.output(5,0)
    gpio.output(7,1)
    gpio.output(11,1)
    gpio.output(13,0)

def backward():
    gpio.output(5,1)
    gpio.output(7,0)
    gpio.output(11,0)
    gpio.output(13,1)

def right():
    gpio.output(5,0)
    gpio.output(7,1)
    gpio.output(11,0)
    gpio.output(13,0)

def left():
    gpio.output(5,0)
    gpio.output(7,0)
    gpio.output(11,1)
    gpio.output(13,0)

def stop():
    gpio.output(5,0)
    gpio.output(7,0)
    gpio.output(11,0)
    gpio.output(13,0)

def on_connect(client, userdata, flags, rc):
    print "Connection received with code %d." % rc

def on_subscribe(client, userdata, mid, granted_qos):
    print("Subscribed: "+str(mid)+" "+str(granted_qos))

def on_message(client, userdata, msg):
    message = str(msg.payload)
    print("Message received :: "+message)
    if message == "up":
        forward()
    elif message == "down":
        backward()
    elif message == "left":
        left()
    elif message == "right":
        right()
    elif message == "stop":
        stop()
        gpio.cleanup()
        client.disconnect()
        return
    client.publish("ack", "Message from python to java", qos=1)

def on_publish(client, userdata, mid):
    print("mid: "+str(mid))


client = paho.Client()
setup()
#client.on_publish = on_publish
client.on_connect = on_connect
client.on_subscribe = on_subscribe
client.on_publish = on_publish
client.on_message = on_message
client.connect("192.168.1.36", 1883)
client.subscribe("command/#", qos=1)
client.loop_forever()
   



