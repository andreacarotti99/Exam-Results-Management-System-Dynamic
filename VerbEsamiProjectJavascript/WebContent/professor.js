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


	// When the user clicks anywhere outside of the modal, close it
	window.addEventListener("click", (e) => {
		if (e.target == document.getElementById("modalDiv")) {
			modalMultipleInsertion.reset();
		}
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


	function RegisteredStudents(alert, registeredStudentsTable, registeredStudentsTableBody, roundDateInfo, registeredStudentsButtonsParagraph) {
		this.alert = alert;
		this.registeredStudentsTable = registeredStudentsTable;
		this.registeredStudentsTableBody = registeredStudentsTableBody;
		this.roundDateInfo = roundDateInfo;
		this.registeredStudentsButtonsParagraph = registeredStudentsButtonsParagraph;

		this.reset = function() {
			this.roundDateInfo.style.visibility = "hidden";
			this.registeredStudentsTable.style.visibility = "hidden";
			this.registeredStudentsButtonsParagraph.style.visibility = "hidden";
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
			this.registeredStudentsButtonsParagraph.innerHTML = ""; //empties the buttons paragraph

			var self = this;
			studentsRegisteredToRound.forEach(function(studentInfo) {
				row = document.createElement("tr");

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.studentNumber;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.surname;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.name;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.mail;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.degreeCourse;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.mark;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
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

			var publishButton, verbalizeButton, multipleInsertionButton;

			publishButton = document.createElement("button");
			publishButton.textContent = "Publish marks";
			publishButton.addEventListener("click", (e) => {
				self.publishMarks(roundId);
			});
			this.registeredStudentsButtonsParagraph.appendChild(publishButton);

			verbalizeButton = document.createElement("button");
			verbalizeButton.textContent = "Verbalize marks";
			verbalizeButton.addEventListener("click", (e) => {
				self.verbalizeMarks(roundId);
			});
			this.registeredStudentsButtonsParagraph.appendChild(verbalizeButton);

			multipleInsertionButton = document.createElement("button");
			multipleInsertionButton.textContent = "Multiple mark insertion";
			multipleInsertionButton.addEventListener("click", (e) => {
				modalMultipleInsertion.show(roundId);
			});
			this.registeredStudentsButtonsParagraph.appendChild(multipleInsertionButton);

			this.roundDateInfo.style.visibility = "visible";
			this.registeredStudentsTable.style.visibility = "visible";
			this.registeredStudentsButtonsParagraph.style.visibility = "visible";
		};


		this.publishMarks = function(roundId) {
			var self = this;

			makeCall("POST", "PublishMarks?roundId=" + roundId, null,
			//callBack function
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE){

					if (req.status == 200){

						pageOrchestrator.refresh("roundsList");
						self.show(roundId);
					}
					else{
						var message = req.responseText;
						self.alert.textContent = message;
					}
				}
			});
		};


		this.verbalizeMarks = function(roundId) {
			var self = this;

			makeCall("POST", "VerbalizeMarks?roundId=" + roundId, null,
			//rollback function
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {

					if (req.status == 200) {

						pageOrchestrator.refresh("roundsList");
						self.show(roundId);
						verbal.show(roundId);
					}
					else {
						var message = req.responseText;
						self.alert.textContent = message;
					}
				}
			});
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

							pageOrchestrator.refresh("roundsList");
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



	function Verbal(alert, verbalDiv, verbalInfo, verbalTableBody) {
		this.alert = alert;
		this.verbalDiv = verbalDiv;
		this.verbalInfo = verbalInfo;
		this.verbalTableBody = verbalTableBody;

		this.reset = function(){
			this.verbalDiv.style.visibility = "hidden";
		};


		this.show = function(roundId) {
			var self = this;

			makeCall("GET", "GetVerbalInfo?roundId=" + roundId, null,
			//callBack function
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;

					if (req.status == 200) {
						var split = message.split("%");

						var verbalizedStudents = JSON.parse(split[0]);
						var verbal = JSON.parse(split[1]);
						var round = JSON.parse(split[2]);

						self.update(verbalizedStudents, verbal, round);
					}
					else {
						self.alert.textContent = message;
					}
				}
			});
		};


		this.update = function(verbalizedStudents, verbal, round) {
			var row, tebleCell;
			var self = this;

			this.verbalTableBody.innerHTML = ""; //empties the table rows

			this.verbalInfo.textContent = "This is the information about the verbal: " + verbal.verbalID + ", verbalized on: " + verbal.date +
			", that refers to the round on date: " + round.date + " of the class: " + round.className;

			verbalizedStudents.forEach(function(studentInfo) {
				row = document.createElement("tr");

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.studentNumber;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.surname;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.name;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.mark;
				row.appendChild(tableCell);

				self.verbalTableBody.appendChild(row);
			});

			this.verbalDiv.style.visibility = "visible";
		};

	} //no ; at the end of this function because it is not a function expression



	function ModalMultipleInsertion(alert, modalDiv, closeModalButton, modalTable, modalTableBody, submitAllMarksButton){
		this.alert = alert;
		this.modalDiv = modalDiv;
		this.closeModalButton = closeModalButton;
		this.modalTable = modalTable;
		this.modalTableBody = modalTableBody;
		this.submitAllMarksButton = submitAllMarksButton;

		this.reset = function() {
			this.modalDiv.style.display = "none";
		};


		this.show = function(roundId) {
			var self = this;

			makeCall("GET", "GetOnlyNotInsertedMarkStudentsRegisteredToRound?roundId=" + roundId, null,
			//callBack function
			function(req) {
				if (req.readyState == XMLHttpRequest.DONE) {
					var message = req.responseText;

					if (req.status == 200) {

						var studentsList = JSON.parse(message);

						if (studentsList.length == 0) {
							self.alert.textContent = "There are no students with the mark status = NOT_INSERTED";
							return;
						}

						pageOrchestrator.refresh("registeredStudents");
						self.update(studentsList, roundId);

					}
					else {
						self.alert.textContent = message;
					}
				}
			});
		};


		this.update = function(studentList, roundId) {
			var row, tableCell, select, option, hiddenInput;
			var self = this;

			this.modalTableBody.innerHTML = ""; //empties the modal table rows

			studentList.forEach(function(studentInfo) {
				row = document.createElement("tr");

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.studentNumber;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.surname;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.name;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.mail;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.degreeCourse;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.mark;
				row.appendChild(tableCell);

				tableCell = document.createElement("td");
				tableCell.className = "boxed";
				tableCell.textContent = studentInfo.status;
				row.appendChild(tableCell);

				select = document.createElement("select");
				select.setAttribute("name", "marks[]");
				select.setAttribute("type", "number");

				option = document.createElement("option");
				option.value = 0;
				option.text = "<not inserted>";
				option.selected = true;
				select.appendChild(option);

				option = document.createElement("option");
				option.value = 1;
				option.text = "absent";
				select.appendChild(option);

				option = document.createElement("option");
				option.value = 2;
				option.text = "failed";
				select.appendChild(option);

				option = document.createElement("option");
				option.value = 3;
				option.text = "skip next round";
				select.appendChild(option);

				for (var i = 18; i <= 30; i++) {
					option = document.createElement("option");
					option.value = i;
					option.text = "" + i;  //casting to string
					select.appendChild(option);
				}

				option = document.createElement("option");
				option.value = 31;
				option.text = "30 with honor";
				select.appendChild(option);

				row.appendChild(select);

				hiddenInput = document.createElement("input");
				hiddenInput.value = studentInfo.id;
				hiddenInput.type = "hidden";
				hiddenInput.name = "studentIds[]";
				row.appendChild(hiddenInput);

				self.modalTableBody.appendChild(row);
			});

			hiddenInput = document.createElement("input");
			hiddenInput.value = roundId;
			hiddenInput.type = "hidden";
			hiddenInput.name = "roundId";
			this.modalTableBody.appendChild(hiddenInput);

			this.modalDiv.style.display = "block";
		};


		this.closeModalButton.addEventListener("click", (e) => {
			this.reset();
		}, false);


		this.submitAllMarksButton.addEventListener("click", (e) => {

			var form = e.target.closest("form");

			var self = this;

			if (form.checkValidity()){

				makeCall("POST", "EditMultipleMarks", form,
				//callBack function
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;

						if (req.status == 200) {
							var roundId = parseInt(message);

							self.reset();
							pageOrchestrator.refresh("roundsList");
							registeredStudents.show(roundId);
						}
						else {
							self.reset();
							self.alert.textContent = message;
						}
					}
				});
			}
			else {
				form.reportValidity();
			}

		}, false);

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
				document.getElementById("roundDateInfo"),
				document.getElementById("registeredStudentsButtonsParagraph")
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

			verbal = new Verbal(
				alertContainer,
				document.getElementById("verbalDiv"),
				document.getElementById("verbalInfo"),
				document.getElementById("verbalTableBody")
			);

			modalMultipleInsertion = new ModalMultipleInsertion(
				alertContainer,
				document.getElementById("modalDiv"),
				document.getElementById("closeModalButton"),
				document.getElementById("modalTable"),
				document.getElementById("modalTableBody"),
				document.getElementById("submitAllMarksButton")
			);
			

			document.querySelector("a[href='Logout']").addEventListener("click", (e) => {
				window.sessionStorage.removeItem("username");
				window.sessionStorage.removeItem("name");
				window.sessionStorage.removeItem("surname");
				window.sessionStorage.removeItem("isProfessor");
			});

			//setting the initial state of the page
			modalMultipleInsertion.reset();
			verbal.reset();
			singleInsertion.reset();
			registeredStudents.reset();
			roundsList.reset();
			classesList.reset();
			classesList.show();
		};


		this.refresh = function (callingObject) { //currentClass is used for custom autoClicks if we need them
			alertContainer.textContent = ""; //empties the error message

			switch(callingObject){   //due to the waterfall style of the page we use the refresh to hidden all elements after a certain object
				case "classesList":
					roundsList.reset();
					registeredStudents.reset();
					singleInsertion.reset();
					verbal.reset();
					break;

				case "roundsList":
					registeredStudents.reset();
					singleInsertion.reset();
					verbal.reset();
					break;	
				
				case "registeredStudents":
					singleInsertion.reset();
					verbal.reset();
					break;
				default:
					//if no string is passed then don't hide anything else other that the error message
					break;
			}
		};

	}//no ; at the end of this function because it is not a function expression


})();  //IIFE