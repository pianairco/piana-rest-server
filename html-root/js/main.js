/**
 * Created by SYSTEM on 7/31/2017.
 */
function sayHello() {
    $.get("hello-world", { name: $("#input-name").val() }, function(data, status){
        console.log(data);
    });
}