(function() { // avoid variables ending up in the global scope



    window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null || sessionStorage.getItem("isProfessor") == true) {
	      window.location.href = "index.html";
	    } else {
            var greet = document.getElementById("greetings");
            greet.textContent = sessionStorage.getItem("name") + " " + sessionStorage.getItem("surname");
	      //pageOrchestrator.start(); // initialize the components
	      //pageOrchestrator.refresh();
	    } // display initial content
	  }, false);


















})();