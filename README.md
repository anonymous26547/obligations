# The OWL Obligation State Manager

This project is implemented in **Java** using the **OWL API** and **Pellet** as the reasoner.  
It allows reasoning over instances of the **OCM ontology** and querying specific states of concepts at a given time.

## Features
- Reasoning over an ABox containing instances of the OCM ontology.  
- Query states of concepts such as:
  - `ENTITY`
  - `ACTION`
  - `RESOURCE`
  - `EVENT_STATE`
  - `TEMPORAL_ACTION_STATE`
  - `REGULATED_ACTION_STATE`
  - `OBLIGATION_STATE`  
- Returns the states of the queried concept for a specific `xsd:dateTime`.
## Requirements

- Java 8 
## Build & Run

To build the project:

`mvn clean install`

To run the project, provide the following inputs:

1 - ABox: RDF/Turtle file containing instances of the OCM ontology.
2 - Query as one of the following concepts:
  - `ENTITY`
  - `ACTION`
  - `RESOURCE`
  - `EVENT_STATE`
  - `TEMPORAL_ACTION_STATE`
  - `REGULATED_ACTION_STATE`
  - `OBLIGATION_STATE`  
3- DateTime – A specific value following the xsd:dateTime format.

**Example Command:**

`java -jar target/your-project-name.jar <abox-file> <query> <dateTime>`
