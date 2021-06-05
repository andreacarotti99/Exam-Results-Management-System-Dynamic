/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("loginbutton").addEventListener('click', (e) => {

    var form = e.target.closest("form");

    if (form.checkValidity()) {

      //sending the request to the CheckLogin servlet using the makeCall function
      makeCall("POST", 'CheckLogin', form, 

      //callback paramether/function of the makeCall when the server responds back
        function(req) {
          //check if the request has been completely processed and responded by the server
          if (req.readyState == XMLHttpRequest.DONE) {

            //saving the username of the user that the server is giving to us or the error message
            var message = req.responseText;

            switch (req.status) {
              case 200://good request
                //parsing to object from json string
                var user = JSON.parse(req.responseText);
                sessionStorage.setItem('username', user.username);
                sessionStorage.setItem('name', user.name);
                sessionStorage.setItem('surname', user.surname);
                sessionStorage.setItem('isProfessor', user.isProfessor);
                
                //going to the html of the main page
                if (user.isProfessor == true){
                  window.location.href = "ProfessorHome.html";
                }
                else {
                  window.location.href = "StudentHome.html";
                }
                break;

              case 400: // bad request
                //setting the error message to the index.html page
                document.getElementById("errormessage").textContent = message;
                break;

              case 401: // unauthorized
                //setting the error message to the index.html page
                document.getElementById("errormessage").textContent = message;
                break;

              case 500: // server error
                //setting the error message to the index.html page
            	  document.getElementById("errormessage").textContent = message;
                break;
            }
          }
        }
      );  //end of makeCall function
    } 
    else {  //if form isn't valid
      //method of form
    	form.reportValidity();
    }
  });

})();