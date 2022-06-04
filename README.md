# Markoarchitektur

Das folgende Kapitel beschäftigt sich mit Markoarchitektur, Microservices, Programmiermustern und Frontendentwicklung, im Zuge der Anbindung an eine einer REST-API, anhand eines Bestellmanagements.

## Was ist Makroarchitektur

Während sich Mikroarchitektur auf den "vertikalen" Aufbau, also das innere Programmdesign (zB Technologien, Designpatterns) konzentriert, definiert die Makroarchitektur Regeln für die Kommunikation einzelner Microservices.

(https://ricofritzsche.de/mikro-und-makroarchitektur)

Fachvokabular:
 - Makroaritektur
 - Microservices
 - Monolith
 - Modulith
 - Event-Driven-Design
 - Domain-Driven-Design
 - Events
 - Domain
 - Value Objects
 - Aggregate
 - Clean-Architecture
 - Ports (Input/Output)
 - Adapters
 - Shared Kernel

## Ports and Adapters

Ist das Basismuster der **Clean-Architecture**

Clean-Architecture:
 - Unabhängigkeit der Infrastruktur (einfache Austauschbarkeit zB von Technologien)
 - Ziel ist die **Flexibilität** (immer nicht funktionale Anforderungen)
 - Zielt weiters auf die **Wiederverwendung** ab
 - Beschleunigt den Enticklungsprozess &rarr; verringert Time-to-Market (benötigte Zeit um Softwareprodukt komplett fertigzustellen)
 - fordert daher einen konkreten Lösungsweg: eine Anwendungsarchitektur<br>
&rarr; Realisierung mithilfe von **Schnittstellen**

**Ports and Adapters** liefert einen einheitlicheen Weg: Trennen von Infrastruktur und Domäne mit Adaptern, welche die Schnittstellen realisieren

Aufbau:
- Domäne: Use-Case (Interfaces, Service-Layer) und Entity
- Adapter: implementiert eine Schittstelle zB ein Repository oder Controller
- Infrastruktur: zB Datenbank, Web-Frontend

![PortsAndAdapters Aufbau](Bilder/01_PortsAndAdapters.png)
![PortsAndAdapters Beispiel](Bilder/02_PortsAndAdapters-Bsp.png)

*Grundprinzip: unabhängige Domänen*

Grundsätze (zur Erreichnung):
- Dependency Inversion (Abhängigkeit von Abstraktionen)
- Dependency Injection (Adapter Erzeugung auslagern) - die Domäne kennt nur den Use-Case

(https://de.wikipedia.org/wiki/Dependency-Inversion-Prinzip, https://de.wikipedia.org/wiki/Dependency_Injection)

Anbindung an Business-Logik funkioniert über Schnittstellen, die **Ports** (ist immer ein *Interface*) &rarr; ein Port bildet also eine Kommunikationsschnittstelle
Man unterscheidet:
- Input (Primary) Ports - Von innen implementiert, von außen verwendet zB Controller verwendet Service (Interface)
- Output (Secondary) Ports - Von außen implementiert, von innen verwendet zB Service verwendet Repository (Interface) - Datenbank ist von meiner Schnittstelle abhängig (Dependency Inversion)

## Architekturanalyse - Ausgangsprojekt erplite

Es handelt sich hierbei um ein Spring Boots Projekt, welches als **Monolith** (eine zusammenhängende Einheit) bzw. als **Modulith** (als Deployment-Monolith verstanden) umgesetzt wurde.  

(https://entwickler.de/software-architektur/microservices-oder-monolithen-beides)

Aufgeteilt in:
 - Ordermanagement
 - Stockmanagement
 - Customermanagement

Beleuchtet werden das Order- und Stockmanagement in Bezug auf den folgenden Ablauf:
*Artikel bestellen und Bestellstatus ändern (bezahlt, verpackt, verschickt)*

![Bestellvorgang Sequenzdiagramm](Bilder/03_UML.png)

Allgemeiner Aufbau:
 - MVC (Model-View-Controller) Pattern
 - Kommunikation von außen mittels REST-API über HTTP-Methoden POST und GET (REST-Controller Implementierung als Schnittstellte)

### Ordermanagement

Ordermanagement Aufbau:
 - Grundstruktur mittels Ports And Adapters Pattern
 - Ereignisgesteuert (**Event-Driven**)
   - Kommunikation zwischen den Modulen wedren durch Ereignisse/Events gesteuert - bildet die einige Schnittstelle untereinander
   - verbessert die Kopplung dahingehend, da Events von einer unabhängigen Stelle "blind" ausgeführt werden (Komponente A kennt neben der Implementierung auch nicht die Signaturen und damit die Funktionalitäten der Komponente B bzw. deren Schnittstelle)
   - Events werden über die Ports abgewickelt
   - Events (zB Controller, Repository) nutzen zentral festgelegte Commands und Queries
   - Intern umgesetzt mittels Spring Events
     - eigene Handler übernehmen Event Processing (über ApplicationListener asynchron zwischen Modulen und ApplicationEventPublisher synchron bei den Domain-eigenen Events)
 - Domänenorientiert (**Domain-Driven**)
   - Domänen verwenden um die Wiederverwendbarkeit zu gewährleisten sogenannte ValueObjects
   - **ValueObjects**: immutable/unveränderbare "Datenfelder" (keine Seiteneffekte) - umgesetzt mittels Java Records
   - **Aggregates**: fassen ValueObjects und Entitäten zusammen (definieren wie eine Unit)
 - Datenbank
   - Datenbankentität entspricht nicht der Domänenentität
   - bei Datenbankoperationen werden Daten gemappt (eigener Mapper Use Case)
 - **Shared Kernel**
   - enthält Daten die über die Schnittstelle ausgetauscht werden - diese müssen beide Aggregate kennen!
   - definiert (interne) Spring-Events, Queries, Commands (Wiederverwendbarkeit!)

Die Logik befindet sich fast komplett im Domänen und etwas im Service-Layer <br>
*Die Domäne bildet ein Businessobjekt ab*
&rarr; beispielsweise nicht das Datenbankentity, Infrastruktur != Domäne (Repositorys beziehen sich immer auf Aggregate)

(https://de.wikipedia.org/wiki/Ereignisgesteuerte_Architektur, https://de.wikipedia.org/wiki/Domain-driven_Design)

### Stockmanagement

 - kein Ports And Adapters Pattern, lediglich MVC Grundstruktur
   - auch ohne Service-Layer
   - keine Interfaces (Dependency Inversion)
 -  - Domain Events werden direkt ausgeführt (Klassenabhängigkeit)
 - Domäne entspricht der Datenbankentität
&rarr; keine Clean-Architecture und Abhängigkeiten in Bezug auf Technologien und Aufbauschichten

### REST-API und Events

Mithilfe der implementierten REST-Schnittstelle können die Funktionen genutzt werden.
Zum Testen wurde die Swagger-UI mit in das Spring Boot Projekt eingebunden, die eine Übersicht über alle Endpunkte bietet und über welche sich Abfragen absetzen lassen.

- Bestellung aufgeben
  - POST "/api/v1/orders"
  - `OrderPlacedEvent`
- Bestellung auf bezahlt (PAYMENT_VERIFIED) setzen
  - POST "/api/v1/orders/checkpayment/{orderid}"
  - `OrderPaymentValidatedSpringEvent`
- Packliste generieren
  - `StockIncomingMessageHandler` legt aufgrund des vorherigen Events eine Packliste an (im Stockmanagement)
- Packlistenitems abhaken
  - POST "stock/setPackedForPacking/{packingItemId}"
    ```java
    packingItem.setPacked(true); 
    packingItemRepository.save(packingItem);
    ```
- Bestellung auf IN_DELIVERY setzen wenn alle Packlistenitems gepackt sind
  -  `OrderIncomingMessagesAdapter` setzt aktualisiert über den das Event den Eventhandler, sofern alle Items einer Order gepackt sind, den Order-Status (Ordermanagement)
  -  `OrderPackedSpringEvent`

## Frontend - Umsetzung mittels Svelte

Dieser Ablauf soll über ein Frontend gesteuert werden. Dieses ist mithilfe von Svelte, einem "schlanken", komponentenorientierten JavaScript Fremework, umgesetzt worden.

### Vorbereitung

```bash
#zu Beginn mus nodejs installiert werden
npm init vite@latest #neues Projekt erstellen mithilfe von vite
npm install --save svelte-navigator #für Routing
npm install
npm run dev
```

*(Routing: https://www.npmjs.com/package/svelte-navigator#installation)*

Um über das Frontend Requests abschicken zu können, muss dies (mittels Cross-Site-Scripting), da die Anfrage über eine andere Adresse bzw. über einen anderen Port kommt, an den benötigten Methoden (per Annotaion) im Controller erlaubt werden. 

```java
@CrossOrigin(origins = "http://localhost:3000/")
```

### Umsetzung

**Kunde**

Kundenansicht enthält einen Warenkorb mit Produkten die gekauft werden können

![Cart](Bilder/04_Cart.png)

**Buchhaltung**

Die Buchhaltung kann manuell per Knopfdruck den Bestellstatus auf bezahlt setzen

![Accounting](Bilder/05_Accounting.PNG)

**Lager**

Im Lager werden die einzelnen Bestellpositionen angezeigt, deren Status auf verpackt gesetzt werden kann

![Warehouse](Bilder/06_Warehouse.PNG)

## Microservices

### Architekturanalyse - erweiterte Implementierung