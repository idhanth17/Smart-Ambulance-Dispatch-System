# Smart-Ambulance-Dispatch-System

**Smart Ambulance Dispatch System**

Emergency medical response time plays a vital role in saving lives. Traditional ambulance dispatch systems face delays due to manual coordination, unclear routing, and traffic congestion. The Smart Ambulance Dispatch System offers an automated, intelligent solution to enhance emergency response speed and hospital coordination.

This system uses a JavaFX desktop interface integrated with a Spring Boot backend to streamline ambulance assignment and tracking. When an emergency request is logged, the system automatically allocates the nearest available ambulance based on real-time location and traffic data. The allocated ambulance is tracked live, and the hospital is notified in advance to prepare for the patient’s arrival. The system records transport details securely and generates analytical reports on incidents and response performance.

**Objectives**

* Provide an intuitive operator interface for emergency dispatch.
* Automatically assign the nearest ambulance considering traffic data.
* Enable real-time ambulance tracking and hospital communication.
* Store patient transport and dispatch logs securely.
* Generate response time and incident-based analytical reports.

**Tech Stack**

* Java (JDK 17+), JavaFX (UI)
* Spring Boot (REST APIs)
* MySQL/PostgreSQL (Database)
* Google Maps API, JDBC, Spring Security, Apache POI

**Key Features**

* Emergency incident entry and logging
* Automated ambulance assignment
* Live GPS tracking & ETA updates
* Hospital readiness alerts
* Admin panel for fleet & staff management
* Data analytics & Excel report export

**Database Structure**

* *Ambulances*: ID, driver, location, status
* *Incidents*: ID, location, severity, time
* *Dispatches*: ID, ambulance, incident, hospital, dispatch & arrival times
* *Hospitals*: ID, name, location, capacity
* *Admins*: login & access control

**System Flow**
Login → Enter Incident → System allocates nearest ambulance → Live tracking and hospital notification → Arrival & status update → Report generation

This Smart Ambulance Dispatch System modernizes emergency response, reduces arrival delays, and improves hospital preparedness, ensuring faster patient care during critical moments.
