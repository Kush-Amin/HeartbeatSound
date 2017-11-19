Pebble.addEventListener("ready", function(e) {
  console.log("Ready for interaction");
});


Pebble.addEventListener("case2", function(e) {
  console.log("Got testkey with value" + e.payload.testkey);
  var object = {
    workingOut: 0,
    sleeping: 1,
    working: 2,
    happy: 3,
    sad: 4,
    angry: 5,
    chill: 6,
  };
  Pebble.sendAppMessage(object);
});