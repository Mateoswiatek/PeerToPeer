# Projekt P2PBruteforcePassword

Projekt zaliczeniowy z przedmiotu Design Patterns, 5 semestr kierunku informatyka i systemy inteligentne, AGH 2025.

Zasadnicza część kodu została stworzona w jedną noc,
noc przed końcowym oddaniem projektu, po długim i wyczerpującym balu elektryka.
Z tego względu kod nadal ma wiele miejsc, w których możliwe jest wprowadzenie optymalizacji,
uproszczeń i refactoringu dla poprawy czytelności. Niektóre takie miejsca zostały oznaczone
odpowiednim komentarzem TODO.

Zaprojektowanym i zaimplementowanym systemem jest sieć Pear to Pear (P2P) wraz ze zbudowaną na niej aplikacji biznesowej.
Sama sieć P2P jest uniwersalnym kodem, biblioteką, na której można nadbudować dowolną inną aplikację.

Każdy z modułów generycznych (P2P, Łamacz haseł) ma własny zestaw komunikatów (obiektów Dto), za pomocą których się komunikuje. Dzięki takiemu podejściu,
moduły są niezależne oraz logika nie miesza się między modułami.
## Budowa Systemu
### Sieć P2P
Moduł `p2pnetwork` jest odpowiedzialny za samą strukturę i zarządzanie siecią P2P.

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
```json
{"type": "JoinToNetworkRequest" ,"node": {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}}
```

Podstawowe komunikaty
JoinToNetworkRequest: 
```json
{"newNode": {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}}
```
UpdateNetworkMessage:
```json
{"nodes": [{"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692d", "ip": "localhost", "port": 5001}, {"id": "6ad3b2b5-fa29-4688-8eba-19f089ef692e", "ip": "localhost", "port": 5002}]}
```

Ping:
```json
{"ping":"ping"}
```

W kodzie wykorzystano dobre praktyki programistyczne między innymi zasady SOLID oraz wzorce projektowe.
Kod został w odpowiedni sposób rozdzielony, dzięki czemu projekt jest otwarty na rozwój oraz umożliwia to łatwe wykorzystanie wytworzonego
kodu go w innych projektach i systemach.
Jedną z takich funkcji jest udostępnienie interfejsu, którego implementacja jest wykonywana w momencie wykrycia podłączenia nowego node do sieci.
Jest to szczególnie istotne, kiedy chcemy wykryć takie zdarzenia w celu przeprowadzania jakiegoś procesu biznesowego, dla przykładu 
zaktualizowanie wiedzy lub rozpoczęcie określonych operacji.

### Aplikacja biznesowa
Moduł `task` jest odpowiedzialny za zarządzanie zadaniami łamania hasła.

Nad warstwą sieci P2P została prosta w zrozumieniu aplikacja łamiąca hasła na podstawie ich hashu metodą brute force.
Dzięki podziałowi dużego zadania na mniejsze batche, system przystosowany jest do zrównoleglenia zapewniając niezbędne
api (metody) niezbędne do prawidłowej integracji z innymi instancjami / systemami.

Domyślnie każde zadanie uruchamiane jest w ramach oddzielnego wątka, dzięki czemu Controller może pracować nad wieloma zadaniami równoczesnie,
bez większego spadku wydajności (przy odpowiednio rozmiarze batchy (niwelujemy narzut związany z aktualizacjami)).

Zastosowano w tej części również kilka wymaganych wzorców projektowych. Factory, Observer, (Nietrafione Strategy)

NewTaskRequest
```json
{
  "type": "NewTaskRequest",
  "passwordHash": "216a4438875df831967fc4c6c2b15469a4c6f62dc4d28a2b5cddebddf4cfe5ad",
  "alphabet": "abcdoijklms",
  "maxLength": 7,
  "maxBatchSize": 500
}
```

#### Funkcjonalności 
Udostępnione funkcjonalności to:
- Tworzenie nowych tasków (Zarówno całkiem nowych jak i tworzenie tych, nad którymi inne instancje juz pracują)
- Aktualizowanie stanu wiedzy o konkretnym tasku (Najczęściej przy znalezieniu hasła)
- Aktualizowanie stanu wiedzy na temat batchy (W przypadku systemu rozproszonego, kiedy więcej jednostek pracuje nad tym samym zadaniem)
- Pobieranie stanu wiedzy jaki posiada dana jednostka obliczeniowa - przydatne przy kontrolowaniu postępu prac oraz zrównolegleniu - dołączenie nowej instancji.

#### Flow działania
Uproszczony proces działania:
1. Dla danego zadania = hasła do złamania, dla przekazanego alfabetu i maksymalnej długości hasła obliczana jest liczba wszystkich możliwości.
2. System wydziela zakresy kolejnych kombinacji, tworząc batche (zachowujemy tylko numery kombinacji, dzięki czemu komunikaty są lekkie)
3. Następnie dokonuje pracy nad losowym nie skończonym batchem wysyłając update oraz samemu przyjmując update na temat wykonywanych zadań.

#### Porty zewnętrzne
Poniżej zostaną elementy, dzięki którym można dostosować aplikację biznesową i połączyć ją z większym systemem.
- Repository - miejsce przechowywania informacji na temat Batchy oraz Tasków. Dzięki interfejsowi zewnętrznie decydujemy o sposobie persystencji danych.
- TaskMessageSender - sposób informowania innych instancji lub systemów o postępie prac również zależy od zewnętrznego systemu, zastosowania.
- DoneTaskProcessor - możliwe jest zdefiniowanie dodatkowych zewnętrznych aktywności, które mają się wykonać po zakończeniu konkretnego zadania. Implementacja powinna być przygotowana na wielokrotne wywołanie dla tego samego taska(Dla przykładu zapis wyniku)

### Middleware
Jest to kod odpowiedzialny za połączenie dwóch niezależnych modułów:
- aplikacji biznesowej = łamania hasła 
- Sieć P2P


Na komuterach połączonych za pomocą sieci P2P zostały uruchomione instancje aplikacji biznesowej.
Głównym celem modułu jest przekazywanie komunikatów, implementacja odpowiednich interfejsów z obydwu modułów.
Jednym z ważniejszych elementów jest wykorzystanie odpowiednio zaimplementowanego i otwartego na rozwój modelu wiadomości przekazywanych za pośrednictwem sieci P2P.
(Jesteśmy odpowiedzialni tylko za nasze stworzone komunikaty i nie musimy oglądać implemnetacji innych podstawowych komunikatów)



## Użycie
1. Utwórz sieć nodeów
2. Wysyłaj requesty z hashami haseł, które chcesz złamać do któregokolwiek z aktywnych nodeów w sieci, a cała sieć zacznie je rozwiązywać.
3. Po skończeniu procesowania hasła w każdym z nodeów zostanie zapisana informacja na temat zrealizowanego zadania wraz z wynikiem = hasłem.

(W założeniach początkowo miało być możliwe przekazywanie callbacku, na który zostanie wysłany POST z wynikiem działania sieci, jest to kierunek, w którym można rozwijać system)


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