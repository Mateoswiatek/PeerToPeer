# Opis

Projekt zaliczeniowy z przedmiotu Design Patterns, 5 semestr kierunku informatyka i systemy inteligentne, AGH 2025.

Zasadnicza część kodu została stworzona w jedną noc,
noc przed końcowym oddaniem projektu, po długim i wyczerpującym balu elektryka.
Z tego względu kod nadal ma wiele miejsc w których możliwe jest wprowadzenie optymalizacji,
uproszczeń i refactoringu dla poprawy czytelności. Niektóre takie miejsca zostały oznaczone
odpowiednim komentarzem TODO.


Zaprojektowanym i zaimplementowanym systemem jest sieć Pear to Pear (P2P) wraz ze zbudowaną na niej aplikacji biznesowej.
Sama sieć P2P jest uniwersalnym kodem, biblioteką, na której można nadbudować dowolną inną aplikację.
## System
### Sieć P2P
System sieci P2P umożliwia:
- Tworzenie sieci z pojedynczego node (Startowanie sieci)
- Dołączanie nowych Node do sieci
- Aktualizowanie aktywnych Node w sieci (Przed wysłaniem wiadomości do całej sieci, aktualizujemy stan sieci, lazy update)
- Wykrywanie odłączonych Node w sieci.


Node przed wysłaniem wiadomości "do całej sieci" aktualizuje swoją wiedzę na temat jej stanu, 
jak i również przed przekazaniem swojej wiedzy do innego node(np tego, który podłącza się do sieci za pośrednictwem omawianego nodea).
Jest to coś w rodzaju lazy update, aktualizujemy dopiero w momencie, gdy tego potrzebujemy.

Poniżej przedstawiono jedyny bardziej skomplikowany proces, jakim jest podłączanie nowego Nodea do sieci
- Nowo dodany node wysyła requesta do Noda znajdującego się w sieci (musi zostać on podany przy starcie nowego node).

Node będący w sieci:
- Logika zaczyna się w NetworkManagerImpl.P2PTCPListener.handleAddNewNodeToNetwork
- Aktualizuje swoją wiedzę na temat sieci (wykrywa i usuwa ze swojej listy odłączone Node)
- Dodaje do swojej sieci Node, który się podłączył
- Wysyła update sieci do wszystkich Nodów w sieci (dodając również samego siebie do listy)
- Wykonuje dodatkową akcję, która jest portem zewnętrznym modułu sieci P2P. W naszym przypadku taką dodatkową akcją jest wysłanie do nowo dodanego Node dumpa wiedzy na temat aplikacji biznesowej
  (Rozwiązania poprzednich łamanych haseł, dump chunków z aktualnie procesowanych zadań)

Komunikaty w sieci są w postaci JSONów.
Wszystkie komunikaty dziedziczą po klasie bazowej, zawierającej informację o typie wiadomości oraz o nadawcy.
```
{"type": "JoinToNetworkRequest" ,"node": {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}}
```

Podstawowe komunikaty
JoinToNetworkRequest: 
```
{"newNode": {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}}
```
UpdateNetworkMessage:
```
{"nodes": [{"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}, {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692e", "ip": "localhost", "port": 5002}]}
```

Ping:
```
{"ping":"ping"}
```

W kodzie wykorzystano dobre praktyki programistyczne między innymi zasady SOLID oraz wzorce projektowe.
Kod został w odpowiedni sposób rozdzielony, dzięki czemu projekt jest otwarty na rozwój oraz umożliwia to łatwe wykorzystanie wytworzonego
kodu go w innych projektach i systemach.

### Aplikacja biznesowa
Nad warstwą sieci P2P została prosta w zrozumieniu aplikacja łamiąca hasła na podstawie ich hashu metodą brute force.


Dla danego alfabetu i długości hasła obliczana jest liczba wszystkich możliwości.


## Użycie
1. Utwórz sieć nodeów
2. Wysyłaj requesty z hashami haseł które chcesz złamać do któregokolwiek z aktywnych nodeów w sieci, a cała sieć zacznie je rozwiązywać.
3. Po skończeniu procesowania hasła w każdym z nodeów zostanie zapisana informacja na temat zrealizowanego zadania wraz z wynikiem = hasłem.

(W założeniach początkowo miało być możliwe przekazywanie callbacku, na który zostanie wysłany POST z wynikiem działania sieci)


## Example

The run scripts are prepared in the **run.sh** file.

The task request is prepared in the **request.sh** file.

During the following Use Case, logs and files were generated:

### **Use Case Flow:**
1. Start a standalone node on port **5000**.
2. Send a task request.
3. Add a new node on port **5001**, connecting it to the existing node on port **5000**.
4. Wait a few seconds.
5. Remove the node running on port **5000** (interrupt its operation).
6. Add a new node on port **5002**, connecting it to the node running on port **5001**.
7. Nodes **5001** and **5002** should complete the task.
8. Add a new node on port **5003** and verify whether it receives information about the completed task.