/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset = true) {

    var req = new XMLHttpRequest(); // visible by closure
    
    //calling the callBack function passing the request variable on any event on the XMLHttpRequest object
    req.onreadystatechange = function() {
      cback(req)
    }; // closure
    
    //setting the request method
    req.open(method, url);
    
    if (formElement == null) {
      //if no formElement object just send the request
      req.send();
    } else {
      //if formElement object is present then send it with the request
      req.send(new FormData(formElement));
    }
    
    //resetting the formElement
    if (formElement !== null && reset === true) {
      formElement.reset();
    }
  }
