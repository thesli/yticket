app = angular.module("yticket",[])


app.controller(
  "mainCtrl",
  ["$scope","$rootScope","$timeout","socket"
   (s,r,t,socket)->
     s.abc = "hello world"
     socket.on("hello",
       ->
         alert("hello world")
     )
  ]
)

app.factory("socket"
  ->
    socket = io.connect("http://192.168.1.111:3040")
    return socket
)

app.directive(
  "box",
  ->
    restrict: "A",
    scope:{

    }
    template: '<div class="box">box here</div>'
    link: (scope,elem,attr,ctrl)->

)