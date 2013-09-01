express = require("express")
app = new express()
app.use("/",express.static(__dirname+"/"))

io = require("socket.io").listen(3040)

io.sockets.on("connection",
  (s)->
    s.emit("hello")
)

app.listen(80)
console.log("the server now running at port 80")