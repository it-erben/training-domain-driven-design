# Lab 00: Entwicklungsumgebung einrichten

## Lernziel

Entwicklungsumgebung aufsetzen und das Starter-Projekt erfolgreich starten.

## Dauer

30 Minuten

## Voraussetzungen

- JDK 17+
- Maven 3.9+
- IDE (IntelliJ IDEA empfohlen)

## Schritt-fuer-Schritt-Anleitung

### 1. JDK 17+ installieren und pruefen

Stelle sicher, dass ein JDK in Version 17 oder hoeher installiert ist:

```bash
java -version
```

Die Ausgabe sollte eine Version >= 17 anzeigen.

### 2. Maven 3.9+ installieren und pruefen

Stelle sicher, dass Maven in Version 3.9 oder hoeher installiert ist:

```bash
mvn -version
```

Die Ausgabe sollte eine Version >= 3.9 anzeigen.

### 3. Projekt in IDE importieren

Importiere das Projekt aus dem Verzeichnis `initial-project/` in deine IDE:

- **IntelliJ IDEA**: `File` > `Open...` > Verzeichnis `initial-project/` auswaehlen
- IntelliJ erkennt die `pom.xml` automatisch und laedt die Maven-Abhaengigkeiten herunter

### 4. Projekt bauen

Baue das Projekt mit Maven:

```bash
cd initial-project
mvn clean verify
```

Der Build sollte erfolgreich durchlaufen (`BUILD SUCCESS`).

### 5. Anwendung starten

Starte die Spring-Boot-Anwendung:

```bash
mvn spring-boot:run
```

Die Anwendung startet auf Port 8080.

### 6. Health-Check aufrufen

Pruefe in einem neuen Terminal, ob die Anwendung korrekt laeuft:

```bash
curl http://localhost:8080/actuator/health
```

## Verifikation

Der Health-Endpoint gibt folgende Antwort zurueck:

```json
{"status":"UP"}
```

Wenn diese Antwort erscheint, ist die Entwicklungsumgebung korrekt eingerichtet.

## Tipps

- **IntelliJ Maven-Import**: Falls Abhaengigkeiten nicht automatisch geladen werden, klicke mit der rechten Maustaste auf die `pom.xml` und waehle `Maven` > `Reload Project`.
- **H2-Console**: Die H2-Datenbank-Konsole ist unter [http://localhost:8080/h2-console](http://localhost:8080/h2-console) erreichbar. Verwende die JDBC-URL `jdbc:h2:mem:immobiliencrm` mit dem Benutzernamen `sa` und leerem Passwort.
