(function() { // avoid variables ending up in the global scope

	var personalMessage, classesList, roundsList, markDetails,
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
						if (classesToSHow.length == 0){  //print error message if this student isn't attending any class
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
				  anchor.setAttribute("classId", classe.classID);   //set custom html attributes for the link
				  anchor.setAttribute("className", classe.className);
				  anchor.addEventListener("click", (e) => {
					  pageOrchestrator.refresh();
					  roundsList.reset();
					  markDetails.reset();
					  roundsList.show(e.target.getAttribute("classId"), e.target.getAttribute("className"));  //calling the show function on the roundsList
					  
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


	  function RoundsList(alert, roundsTable, roundsTableBody, className) {
		  this.alert = alert;
		  this.roundsTable = roundsTable;
		  this.roundsTableBody = roundsTableBody;
		  this.className = className;

		  //this function makes the tabel hidden
		  this.reset = function() {
			  this.className.style.visibility = "hidden";
			  this.roundsTable.style.visibility = "hidden";
		  };

		  this.show = function(classId, className) {
			  var self = this;

			  makeCall("GET", "GetRoundsStudent?classId=" + classId, null, 
			  //this is the callBack function
			  function(req) {
				  if (req.readyState == XMLHttpRequest.DONE) {
					  var message = req.responseText;

					  if (req.status == 200) {  //ok request
						  var roundsToShow = JSON.parse(message);
						  if (roundsToShow.length == 0) {
							  self.reset();
							  self.alert.textContent = "there are no rounds for the class: " + className;
							  return;
						  }
						  self.className.textContent = "These are the rounds of the class: " + className;
						  self.update(roundsToShow);
					  }
					  else { //bad request
						  self.className.textContent = "";
						  self.alert.textContent = message;
					  }
				  }
			  });
		  };

		  this.update = function(roundArray) {
			var row, dateCell, linkCell, linkText, anchor;
			this.roundsTableBody.innerHTML = "";  //empties the table body

			var self = this;
			roundArray.forEach(function(round) {
				//creating row
				row = document.createElement("tr");
				//adding round date to thr row
				dateCell = document.createElement("td");
				dateCell.textContent = round.date;
				row.appendChild(dateCell);
				//adding the link to view details or to register to the round
				linkCell = document.createElement("td");
				anchor = document.createElement("a");
				linkCell.appendChild(anchor);
				if (round.studentRegistered == true) { //student already registered
					linkText = document.createTextNode("view details");
					anchor.appendChild(linkText);
					anchor.setAttribute("roundId", round.roundId);
					anchor.addEventListener("click", (e) => {
						pageOrchestrator.refresh();
						markDetails.reset();
						markDetails.show(e.target.getAttribute("roundId"));  //calling the show function of markDetails

					}, false);
					anchor.href = "#";
					row.appendChild(linkCell);
				}
				else {  //student not registered to round
					linkText = document.createTextNode("register to round");
					anchor.appendChild(linkText);
					anchor.setAttribute("roundId", round.roundId);
					anchor.addEventListener("click", (e) => {
						pageOrchestrator.refresh();
						self.registerToRound(e.target.getAttribute("roundId")); //calling the function of registeringToRound

					}, false);
					anchor.href = "#";
					row.appendChild(linkCell);
				}
				self.roundsTableBody.appendChild(row);
			});
			//making the table visible again
			this.roundsTable.style.visibility = "visible";
			this.className.style.visibility = "visible";
		  };


		  this.registerToRound = function(roundId) {
			  var self = this;

			  makeCall("POST", "RegisterToRound?roundId=" + roundId, null, 
			  //this is the callBack function
			  function(req) {
				  if (req.readyState == XMLHttpRequest.DONE) {
					  var message = req.responseText;

					  if (req.status == 200){
						  var classBean = JSON.parse(message);
						  self.reset();
						  self.show(classBean.classID, classBean.className);
					  }
					  else {
						  self.alert.textContent = message;
					  }
				  }
			  });
		  };

	  } //no ; at the end of this function because it is not a function expression



	  function MarkDetails(object){
		  this.alert = object['alert'];
		  this.markDetailsContainer = object['markDetailsContainer'];
		  this.roundDateInfo = object['roundDateInfo'];
		  this.notInsertedMessage = object['notInsertedMessage'];
		  this.nameInfo = object['nameInfo'];
		  this.surnameInfo = object['surnameInfo'];
		  this.studentNumberInfo = object['studentNumberInfo'];
		  this.degreeCourseInfo = object['degreeCourseInfo'];
		  this.mailInfo = object['mailInfo'];
		  this.markInfo = object['markInfo'];
		  this.statusInfo = object['statusInfo'];
		  this.rejectButton = object['rejectButton'];

		  this.reset = function(){
			  this.markDetailsContainer.style.visibility = "hidden";
		  };

		  
		  this.show = function(roundId) {
			  var self = this;

			  makeCall("GET", "GetMarkDetailsStudent?roundId=" + roundId, null, 
			  //callBack function
			  function(req) {
				  if (req.readyState == XMLHttpRequest.DONE) {
					  var message = req.responseText;

					  if (req.status == 200) { //ok request
						  var split = message.split("%");  //this function is used to split the string of the two json objects the server sent to us

						  var studentInfo = JSON.parse(split[0]); //parsing the first json string object
						  var roundInfo = JSON.parse(split[1]); //parsing the second json string object

						  self.update(studentInfo, roundInfo);
					  }
					  else { //bad request
						  self.alert.textContent = message;
					  }
				  }
			  });
		  };

		  this.update = function(studentInfo, roundInfo) {
			  this.roundDateInfo.textContent = roundInfo.date;

			  if (studentInfo.status == "NOT_INSERTED") { //mark still not inserted so only show the not inserted message
				this.notInsertedMessage.textContent = "The Professor still hasn't inserted the mark";
				this.nameInfo.textContent = "";
				this.surnameInfo.textContent = "";
				this.studentNumberInfo.textContent = "";
				this.degreeCourseInfo.textContent = "";
				this.mailInfo.textContent = "";
				this.markInfo.textContent = "";
				this.statusInfo.textContent = "";

				this.rejectButton.innerHTML = "";
			  }
			  else { //mark at least inserted so display the info retrieved
				this.notInsertedMessage.textContent = "";  
				this.nameInfo.textContent = "Name: " + studentInfo.name;
				this.surnameInfo.textContent = "Surname: " + studentInfo.surname;
				this.studentNumberInfo.textContent = "Student Number: " + studentInfo.studentNumber;
				this.degreeCourseInfo.textContent = "Degree Course: " + studentInfo.degreeCourse;
				this.mailInfo.textContent = "Mail: " + studentInfo.mail;
				this.markInfo.textContent = "Mark: " + studentInfo.mark;
				this.statusInfo.textContent = "Status: " + studentInfo.status;
  
				this.rejectButton.innerHTML = ""; //to delete everything in the reject button paragraph
  
				if (studentInfo.status == "INSERTED") {  //if checks pass then place the reject button
					if (studentInfo.mark != "<empty>" && studentInfo.mark != "absent" && studentInfo.mark != "failed" && studentInfo.mark != "skip next round") {
						var linkText, anchor;
						var self = this;
						anchor = document.createElement("a");
						linkText = document.createTextNode("Click here to reject the mark");
						anchor.appendChild(linkText);
						anchor.setAttribute("roundId", roundInfo.roundId);
						anchor.addEventListener("click", (e) => {
							self.RejectMark(e.target.getAttribute("roundId"));
  
						}, false);
						anchor.href = "#";
						this.rejectButton.appendChild(anchor);
					}
				}
			  }

			  this.markDetailsContainer.style.visibility = "visible";
		  };


		  this.RejectMark = function(roundId) {
			  var self = this;

			  makeCall("POST", "RejectMark?roundId=" + roundId, null ,
			  function(req) {
				  if (req.readyState == XMLHttpRequest.DONE){
					  var message = req.responseText;

					  if (req.status == 200) {
						  self.reset();
						  self.show(roundId); //just need to refresh the mark info details
					  }
					  else {
						  self.alert.textContent = message;
					  }
				  }
			  });
		  };
	  } //no ; at the end of this function because it is not a function expression



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

			  roundsList = new RoundsList(
				  alertContainer,
				  document.getElementById("roundsTable"),
				  document.getElementById("roundsTableBody"),
				  document.getElementById("className")
			  )

			  markDetails = new MarkDetails({  // many parameters, wrap them in an object with the {}
				  alert: alertContainer,
				  markDetailsContainer: document.getElementById("markDetailsContainer"),
				  roundDateInfo: document.getElementById("roundDateInfo"),
				  notInsertedMessage: document.getElementById("notInsertedMessage"),
				  nameInfo: document.getElementById("nameInfo"),
				  surnameInfo: document.getElementById("surnameInfo"),
				  studentNumberInfo: document.getElementById("studentNumberInfo"),
				  degreeCourseInfo: document.getElementById("degreeCourseInfo"),
				  mailInfo: document.getElementById("mailInfo"),
				  markInfo: document.getElementById("markInfo"),
				  statusInfo: document.getElementById("statusInfo"),
				  rejectButton: document.getElementById("rejectButton")
			  });

			  document.querySelector("a[href='Logout']").addEventListener("click", (e) => {
				  window.sessionStorage.removeItem("username");
				  window.sessionStorage.removeItem("name");
				  window.sessionStorage.removeItem("surname");
				  window.sessionStorage.removeItem("isProfessor");
			  });

			  //setting the initial state of the page
			  markDetails.reset();
			  roundsList.reset();
			  classesList.reset();
			  classesList.show();
		  };


		  this.refresh = function () { //currentClass is used for custom autoClicks if we need them
			  alertContainer.textContent = ""; //empties the error message

			  //no need to do anything else
		  };

	  }//no ; at the end of this function because it is not a function expression

})();  //IIFE