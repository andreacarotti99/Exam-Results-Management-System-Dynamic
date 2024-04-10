# Exam Results Management System Dynamic

This project is an extension of the previous static servlet-based application that can be found [here](https://github.com/andreacarotti99/Exam-Results-Management-System-Servlets), now enhanced with JavaScript to provide a dynamic and interactive user experience. It maintains the core architecture but shifts from a static server-side rendered approach to a client-side dynamic interaction model using JavaScript and AJAX.

## System Overview

The system retains the original functionalities for managing academic exam results but introduces client-side dynamics, allowing for asynchronous data fetching and UI updates without reloading the entire page. This approach offers a more responsive and seamless experience for both faculty and students.

## Architecture and Features

### Client-Side Rendering

- **JavaScript and AJAX**: The system employs JavaScript for dynamic content rendering and AJAX for asynchronous server communication. This setup enables updating parts of the web page in response to user actions without reloading the entire page.

### Faculty and Student Portals

- **Faculty Portal**: Professors can manage course offerings, exam rounds, and student grades through interactive tables and forms that update in real-time based on user interactions.
- **Student Portal**: Students access their courses, exam schedules, and results through a dynamically updated interface, offering an intuitive and immediate feedback loop.

### Enhanced User Interaction

- **Dynamic Tables**: Tables for displaying classes, exam rounds, and student lists are dynamically populated and updated based on user actions and server responses.
- **Real-time Form Updates**: Grade entry and modification forms reflect changes immediately, with client-side validation and server updates occurring in the background.

### JavaScript Components

- **`makeCall.js`**: Manages AJAX calls, encapsulating the logic for interacting with the server asynchronously.
- **`professor.js` and `student.js`**: Contain the logic for handling user interactions, updating the UI, and communicating with the server based on the role of the user.
- **`sorting.js`**: Provides functionality for sorting table columns in the UI, enhancing data navigation and organization.

## Comparison with Previous Project

While the server-side logic and database interactions remain similar, the front-end experience is enhanced. The transition to a JavaScript-driven interface allows for a more interactive and responsive application, reducing server load.

## Conclusion

The dynamic version of the Exam Results Management System represents a modern approach to web application development, emphasizing client-side interactions and asynchronous server communication for a more efficient and user-friendly experience.
