(function() { // avoid variables ending up in the global scope

	var personalMessage, classesList, roundsList, registeredStudents, singleInsertion, verbal, modalMultipleInsertion,
		pageOrchestrator = new PageOrchestrator();


    window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null || sessionStorage.getItem("isProfessor") != "true") {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    } // display initial content
	}, false);

      
	function PersonalMessage(name, surname, personalMessageContainer){
		this.name = name;
		this.surname = surname;

		this.show = function() {
			personalMessageContainer.textContent = this.name + " " + this.surname;
		};
	}


	function ClassesList(alert, classesTable, classesTableBody) {
		this.alert = alert;
		this.classesTable = classesTable;
		this.classesTableBody = classesTableBody;

		//this function makes the tabel hidden
		this.reset = function(){
			this.classesTable.style.visibility = "hidden";
		};

		this.show = function() {
			var self = this;//to have a reference to ClassesList function inside the makeCall
			
			makeCall("GET", "GetClassesProfessor" , null, 
			function(req) { //this is the callBack function
			  if (req.readyState == XMLHttpRequest.DONE){
				  var message = req.responseText;

				  if (req.status == 200) {  //ok request
					  var classesToSHow = JSON.parse(message);
					  if (classesToSHow.length == 0){  //print error message if this student isn't attending any class
						  self.alert.textContent = "You aren't teaching any class!";
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
					pageOrchestrator.refresh("classesList");
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

			var anchorToClick = this.classesTableBody.querySelectorAll("a")[0];  //autoclick on first element

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

			makeCall("GET", "GetRoundsProfessor?classId=" + classId, null, 
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
			linkCell.appendChild(anchor)
			linkText = document.createTextNode("view students registered");
			anchor.appendChild(linkText);
			anchor.setAttribute("roundId", round.roundId);

			anchor.addEventListener("click", (e) => {
			pageOrchestrator.refresh("roundsList");
			registeredStudents.show(e.target.getAttribute("roundId"));

			}, false);

			anchor.href = "#";
			row.appendChild(linkCell);
			self.roundsTableBody.appendChild(row);
		  });
		  //making the table visible again
		  this.roundsTable.style.visibility = "visible";
		  this.className.style.visibility = "visible";
		};

	} //no ; at the end of this function because it is not a function expression


	function RegisteredStudents(alert, registeredStudentsTable, registeredStudentsTableBody, roundDateInfo) {
		this.alert = alert;
		this.registeredStudentsTable = registeredStudentsTable;
		this.registeredStudentsTableBody = registeredStudentsTableBody;
		this.roundDateInfo = roundDateInfo;

		this.reset = function() {
			this.roundDateInfo.style.visibility = "hidden";
			this.registeredStudentsTable.style.visibility = "hidden";
		};


		this.show = function(roundId) {
			var self = this;

			makeCall("GET", "GetStudentsRegisteredToRound?roundId=" + roundId, null,
			//callBack function
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;

					if (req.status == 200) {
						var split = message.split("%"); //this function is used to split the string of the two json objects the server sent to us

						var studentsRegisteredToRound = JSON.parse(split[0]); //parsing the first json string object
						var roundInfo = JSON.parse(split[1]); //parsing the first json string object

						if (studentsRegisteredToRound.length == 0) {
							self.reset();
							self.alert.textContent = "There are no students registered to the round on date: " + roundInfo.date;
							return;
						}

						self.roundDateInfo.textContent = "These are the students registered to the round on date: " + roundInfo.date;
						self.update(studentsRegisteredToRound, roundInfo.roundId);
					}
					else { //bad request
						self.roundDateInfo.textContent = "";
						self.alert.textContent = message;
					}
				}

			});
		};


		this.update = function(studentsRegisteredToRound, roundId) {
			var row, tableCell, button;

			this.registeredStudentsTableBody.innerHTML = ""; //empties the table

			var self = this;
			studentsRegisteredToRound.forEach(function(studentInfo) {
				row = document.createElement("tr");

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.studentNumber;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.surname;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.name;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.mail;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.degreeCourse;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.mark;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.textContent = studentInfo.status;
				row.appendChild(tableCell);

				if (studentInfo.status == "NOT_INSERTED" || studentInfo.status == "INSERTED") {
					button = document.createElement("button");
					button.textContent = "modify mark";
					button.addEventListener("click", (e) => {
						pageOrchestrator.refresh("registeredStudents");
						singleInsertion.show(studentInfo, roundId);

					});
					row.appendChild(button);
				}
				self.registeredStudentsTableBody.appendChild(row);
			});

			var paragraph, publishButton, verbalizeButton, multipleInsertionButton;

			paragraph = document.createElement("p");
			publishButton = document.createElement("button");
			publishButton.textContent = "Publish marks";
			publishButton.addEventListener("click", (e) => {
				pageOrchestrator.refresh("registeredStudents");
				self.publishMarks(roundId);
			});
			paragraph.appendChild(publishButton);
			this.registeredStudentsTableBody.appendChild(paragraph);

			paragraph = document.createElement("p");
			verbalizeButton = document.createElement("button");
			verbalizeButton.textContent = "Verbalize marks";
			verbalizeButton.addEventListener("click", (e) => {
				pageOrchestrator.refresh("registeredStudents");
				self.verbalizeMarks(roundId);
			});
			paragraph.appendChild(verbalizeButton);
			this.registeredStudentsTableBody.appendChild(paragraph);

			paragraph = document.createElement("p");
			multipleInsertionButton = document.createElement("button");
			multipleInsertionButton.textContent = "Multiple mark insertion";
			multipleInsertionButton.addEventListener("click", (e) => {
				pageOrchestrator.refresh("registeredStudents");
				//TODO
			});
			paragraph.appendChild(multipleInsertionButton);
			this.registeredStudentsTableBody.appendChild(paragraph);


			this.roundDateInfo.style.visibility = "visible";
			this.registeredStudentsTable.style.visibility = "visible";
		};


		this.publishMarks = function(roundId) {

		};


		this.verbalizeMarks = function(roundId) {

		};

	} //no ; at the end of this function because it is not a function expression


	function SingleInsertion(object) {
		this.alert = object['alert'];
		this.singleInsertionDiv = object['singleInsertionDiv'];
		this.nameInfo = object['nameInfo'];
		this.surnameInfo = object['surnameInfo'];
		this.studentNumberInfo = object['studentNumberInfo'];
		this.degreeCourseInfo = object['degreeCourseInfo'];
		this.mailInfo = object['mailInfo'];
		this.markInfo = object['markInfo'];
		this.statusInfo = object['statusInfo'];
		this.newMarkButton = object['newMarkButton'];

		this.reset = function(){
			this.singleInsertionDiv.style.visibility = "hidden";
		};


		this.show = function(studentInfo, roundId) {  //no need to do any request to the db but to mantain the same style there is the
			if (studentInfo.status != "NOT_INSERTED" && studentInfo.status != "INSERTED"){  //show function that calls the update function
				this.alert.textContent = "You cant modify the mark of that student";
				return;
			}
			this.update(studentInfo, roundId);
		};


		this.update = function(studentInfo, roundId) {
			this.nameInfo.textContent = "Name: " + studentInfo.name;
			this.surnameInfo.textContent = "Surname: " + studentInfo.surname;
			this.studentNumberInfo.textContent = "Student Number: " + studentInfo.studentNumber;
			this.degreeCourseInfo.textContent = "Degree Course: " + studentInfo.degreeCourse;
			this.mailInfo.textContent = "Mail: " + studentInfo.mail;
			this.markInfo.textContent = "Mark: " + studentInfo.mark;
			this.statusInfo.textContent = "Status: " + studentInfo.status;
			
			//setting the two hiddent parameters value of the form
			var form = this.newMarkButton.closest("form");

			form.querySelector('input[name="roundId"]').setAttribute("value", roundId);
			
			form.querySelector('input[name="studentId"]').setAttribute("value", studentInfo.id);

			this.singleInsertionDiv.style.visibility = "visible";
		};

		this.newMarkButton.addEventListener("click", (e) => {

			var form = e.target.closest("form");

			var self = this;

			if (form.checkValidity()){

				makeCall("POST", "EditMark", form,
				//callBack function
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;

						if (req.status == 200) {
							var roundId = parseInt(message);

							pageOrchestrator.refresh("registeredStudents");
							registeredStudents.show(roundId);
						}
						else {
							self.alert.textContent = message;
						}
					}
				});
			}
			else {
				form.reportValidity();
			}
		});


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
			);

			registeredStudents = new RegisteredStudents(
				alertContainer,
				document.getElementById("registeredStudentsTable"),
				document.getElementById("registeredStudentsTableBody"),
				document.getElementById("roundDateInfo")
			);

			singleInsertion = new SingleInsertion({ // many parameters, wrap them in an object with the {}
				alert: alertContainer,
				singleInsertionDiv: document.getElementById("singleInsertionDiv"),
				nameInfo: document.getElementById("nameInfo"),
				surnameInfo: document.getElementById("surnameInfo"),
				studentNumberInfo: document.getElementById("studentNumberInfo"),
				degreeCourseInfo: document.getElementById("degreeCourseInfo"),
				mailInfo: document.getElementById("mailInfo"),
				markInfo: document.getElementById("markInfo"),
				statusInfo: document.getElementById("statusInfo"),
				newMarkButton: document.getElementById("newMarkButton")
			});


			

			document.querySelector("a[href='Logout']").addEventListener("click", (e) => {
				window.sessionStorage.removeItem("username");
				window.sessionStorage.removeItem("name");
				window.sessionStorage.removeItem("surname");
				window.sessionStorage.removeItem("isProfessor");
			});

			//setting the initial state of the page
			singleInsertion.reset();
			registeredStudents.reset();
			roundsList.reset();
			classesList.reset();
			classesList.show();
		};


		this.refresh = function (callingObject) { //currentClass is used for custom autoClicks if we need them
			alertContainer.textContent = ""; //empties the error message

			switch(callingObject){
				case "classesList":
					roundsList.reset();
					registeredStudents.reset();
					singleInsertion.reset();
					//TODO
					break;

				case "roundsList":
					registeredStudents.reset();
					singleInsertion.reset();
					//TODO
					break;	
				
				case "registeredStudents":
					singleInsertion.reset();
					//TODO
					break;
				default:
					break;		
			}
		};

	}//no ; at the end of this function because it is not a function expression





})();