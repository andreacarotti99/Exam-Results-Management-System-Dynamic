(function() { // avoid variables ending up in the global scope

	var personalMessage, classesList, roundsList, markDetails, rejectButton,
		pageOrchestrator = new PageOrchestrator();


    window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null || sessionStorage.getItem("isProfessor") == "true") { //no permission to see this page
	      window.location.href = "index.html";
	    }
		 else { // display initial content
	      pageOrchestrator.start();
	      pageOrchestrator.refresh();
	    }
	  }, false);


	  function PersonalMessage(name, surname, personalMessageContainer){
		  this.name = name;
		  this.surname = surname;

		  this.show = function() {
			  personalMessageContainer.textContent = this.name + " " + this.surname;
		  }
	  }


	  function ClassesList(alert, classesTable, classesTableBody) {
		  this.alert = alert;
		  this.classesTable = classesTable;
		  this.classesTableBody = classesTableBody;

		  //this function makes the tabel hidden
		  this.reset = function(){
			  this.classesTable.style.visibility = "hidden";
		  };

		  this.show = function() {  //next will be the autoclick function if we need to autockick
			  var self = this;//to have a reference to ClassesList function inside the makeCall
			  
			  makeCall("GET", "GetClassesStudent" , null, 
			  function(req) { //this is the callBack function
				if (req.readyState == XMLHttpRequest.DONE){
					var message = req.responseText;

					if (req.status == 200) {  //ok request
						var classesToSHow = JSON.parse(message);
						if (classesToSHow.lenght == 0){  //print error message if this student isn't attending any class
							self.alert.textContent = "You aren't attending any class!";
							return;
						}
						self.update(classesToSHow);
						self.autoClick();
					}
					else {  //error status for the request
						self.alert.textContent = message;
					}
				}
			  });
		  };

		  this.update = function(classesArray) {
			  var row, nameCell, linkCell, linkText, anchor;
			  this.classesTableBody.innerHTML = "";  //empties the table body

			  //start building the table
			  var self = this;
			  classesArray.forEach(function(classe) {
				  //creating row
				  row = document.createElement("tr");
				  //adding class name to row
				  nameCell = document.createElement("td");
				  nameCell.textContent = classe.className;
				  row.appendChild(nameCell);
				  //adding the view rounds link to row
				  linkCell = document.createElement("td");
				  anchor = document.createElement("a");
				  linkCell.appendChild(anchor);
				  linkText = document.createTextNode("view rounds");
				  anchor.appendChild(linkText);
				  anchor.setAttribute("classId", classe.classID);   //set custom html attribute for the link
				  anchor.addEventListener("click", (e) => {
					//roundsList.show(e.target.getAttribute("classId"));  //calling the show function on the roundsList
				  }, false);
				  anchor.href = "#";
				  row.appendChild(linkCell);
				  //adding the row to the classesTableBody
				  self.classesTableBody.appendChild(row);
			  });

			  //setting back visible the table that was made hidden in the this.reset function
			  this.classesTable.style.visibility = "visible";
		  };


		  this.autoClick = function() {
			  var event = new Event("click");

			  var anchorToClick = this.classesTableBody.querySelectorAll("a")[0];

			  //maybe check if anchorToClick is != null
			  if (anchorToClick) {
				  anchorToClick.dispatchEvent(event);
			  }
		  };
	  }  //no ; at the end of this function because it is not a function expression


	  //complete with other elements








	  function PageOrchestrator() {
		  var alertContainer = document.getElementById("alert");

		  this.start = function() {
			  personalMessage = new PersonalMessage(
				  sessionStorage.getItem("name"),
				  sessionStorage.getItem("surname"),
				  document.getElementById("greetings")
			  );
			  personalMessage.show();

			  classesList = new ClassesList(
				  alertContainer,
				  document.getElementById("classesTable"),
				  document.getElementById("classesTableBody")
			  );
			  classesList.reset();
			  classesList.show();

			  //complete with other elements


			  document.querySelector("a[href='Logout']").addEventListener("click", (e) => {
				  window.sessionStorage.removeItem("username");
				  window.sessionStorage.removeItem("name");
				  window.sessionStorage.removeItem("surname");
				  window.sessionStorage.removeItem("isProfessor");
			  })
		  };


		  this.refresh = function () { //currentClass is used for custom autoClicks if we need them
			  alertContainer.textContent = ""; //empties the error message

			  //complete with other elements



		  };
	  }

})();