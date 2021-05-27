// Node.js socket client script
const WebSocket = require('ws');
const socket = new WebSocket("ws://localhost:8085/ws/chat/listen/me/123?last=5", null, { headers: { Authorization: "Bearer 365d246e-dc3a-4823-95fa-5e9907e77479", userName: "Alex" }});

socket.onopen = function(e, a, b, c, d) {
  console.log("[open] Connection established");

  setInterval(() => socket.send("My name is John"), 3000);
};

socket.onmessage = function(event) {
    console.log(`[message] Data received from server: ${event.data}`);
};

socket.onclose = function(event) {
  if (event.wasClean) {
    console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
  } else {
    console.log('[close] Connection died');
  }
};

socket.onerror = function(error) {
    console.log(`[error] ${error.message}`);
};