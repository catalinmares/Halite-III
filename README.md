# PA Project 2019
# Halite III
                    
**Contributori**
* Cătălin-Constantin MAREȘ
* Ionuț-Marius BĂLĂȘESCU
* Teodor-Grigore MUTU
* Bogdan KUCSINSCHI

**Instructiuni de compilare**

Proiectul va putea fi compilat prin intermediul fisierului
Makefile din interiorul acestei arhive prin apelareaza
comenzii "make". Este important ca inainte de a rula
fisierul Makefile sa va asigurati ca aveti clasa MyBot.java
in directorul curent si celelalte clase in directorul hlt/
inclus in directorul curent.


**Detalii despre structura proiectului**

Proiectul a fost realizat pornind de la scheletul de cod
oferit de dezvoltatorii jocului Halite III pentru platforma
Java. Acesta foloseste, pe langa clasele din starter kit,
2 clase auxiliare, Dial si Strategy.
In clasa Dial se regaseste implementarea unui cadran in care
o nava se va putea misca. Implementarea din Strategy
cuprinde intreaga strategie de joc: crearea de nave, iesirea
din baza, luarea unei decizii asupra directiei de deplasare
a navei, colectarea resurselor si depozitarea acestora în
baza.

**Detalii despre abordarea algoritmica a etapei 1**

Pentru aceasta etapa, echipa noastra a decis sa porneasca cu 	un algoritm simplu de navigatie si colectare de resurse de
pe harta. Strategia abordata de noi a fost aceea de a lucra
cu 4 nave pe harta pe tot parcursul jocului. Fiecarei nave i	se atribuie odata generata un cadran in interiorul careia
aceasta sa navigheze si sa colecteze halite. Astfel, ne-am
asigurat ca navele nu vor putea niciodata sa intre in
coliziune decat in momentul in care trebuie sa intre in
baza. Aici distingem 2 cazuri: 
	* Daca o nava vrea sa intre in baza, dar baza este momentan
	  ocupata: in acest caz, nava va astepta sa se elibereze
	  baza;
	* Daca 2+ nave vor sa intre in baza si baza este libera: in
	  acest caz, toate navele vor intra concomitent in baza si
	  se vor ciocni, insa halite-ul va fi automat depozitat in
	  baza. Singura pierdere va fi astfel de maxim 4000 halite
	  pe cazul cel mai defavorabil cand toate cele 4 nave vor
	  intra goale concomitent in baza. Aceasta pierdere se 
    datoreaza costului pentru regenerarea lor.
	
Fiecare nava va naviga in propriul ei cadran si va colecta
resurse dupa urmatorul algoritm:
	* Calculeaza suma halite-ului disponibil pe linia si coloana 	  curenta pe care se afla pe harta cand tocmai a iesit din
	  baza si alege sa mearga pe directia cea mai profitabila 	     	  (cea de suma maxima)
	* Momentan, algoritmul nostru merge doar pe liniile si
	  coloanele vecine bazei.
	* In etapele urmatoare vom imbunatati algoritmul prin
	  navigarea pe linii si coloane mai indepartate.
	* Luam in considerare o eventuala schimbare a strategiei cu
	  una mai complexa în functie de rezultatele obtinute pentru
	  etapele urmatoare.

Am folosit HashMap pentru a retine starea fiecarei nave
de-a lungul jocului deoarece clasele acestora erau 
reinstantiate la inceputul fiecarei runde si nu puteam
astfel sa retin informatii in interiorul clasei Ship.

**Detalii despre abordarea algoritmica a etapei 2**

Pentru aceasta etapa, echipa noastra a decis sa implementeze 	o strategie noua bazata pe greedy pentru colectarea 	aproximativ eficienta de resurse de pe harta. Strategia 	abordata de noi a fost aceea de a lucra cu un numar de nave pe harta egal cu numarul de nave ale 	inamicilor pe tot parcursul jocului. Fiecarei nave i se 	atribuie in momentul in care se pregateste sa iasa din baza 	un target in directia caruia aceasta sa navigheze si sa 	colecteze halite. Pentru navigarea efectiva, am folosit 	metoda naiveNavigate pe care am modificat-o astfel incat sa 	intoarca prima pozitie libera cea mai apropiata de 	destinatia navei (target-ul setat la iesirea din baza).	
	
Fiecare nava isi va urmari target-ul si va colecta
resurse dupa urmatorul algoritm:
	* Cat timp nu e plina si nu a ajuns la target, daca pe 	pozitia curenta are cel putin 50 halite, sta si mineaza
	* Daca a ajuns la target, sta pana se umple sau termina
	de exploatat target-ul
	* Daca a terminat de exploatat un target si inca nu s-a 	umplut, isi seteaza urmatorul target catre care incepe sa se 	indrepte
	* Cat timp navele calatoresc catre un target, acestea retin 	intr-o stiva mutarile facute plecand din baza ca la 
	intoarcere sa scoata din stiva fiecare mutare pentru a
	reveni in baza
	* Daca la intoarcere intampina obstacole pe ruta pe care
	au calatorit, se renunta la stiva si se seteaza ca target
	baza pentru metoda naiveNavigate, deoarece drumul pe care
	au venit a fost deja exploatat si astfel pierderile de 10%
	sunt foarte mici pentru fiecare celula (pentru asta am
	folosit stiva in loc sa ne intoarcem de la inceput cu
	naiveNavigate si target baza)

Am folosit HashMap pentru a retine starea fiecarei nave
de-a lungul jocului deoarece clasele acestora erau 
reinstantiate la inceputul fiecarei runde si nu puteam
astfel sa retin informatii in interiorul clasei Ship.

Pentru determinarea target-urilor navelor, in fiecare runda
iteram prin celulele hartii si le inseram intr-o coada de
prioritate, cu prioritatea ca raport numarul de halite per
celula / distanta de la baza pana la celula in cauza.

Cand jocul este aproape de final, indiferent de pozitia
navelor pe harta, le trimitem in baza in care se vor ciocni
pentru a depozita rapid si eficient ce au acumulat pe 	ultimul lor drum.

Navele noastre momentan se misca in grup, nu sunt prea
raspandite pe harta, acest lucru fiind un dezavantaj din
cauza multor runde pierdute pentru coordonare fara 	coliziuni. 
Ne propunem pentru etapa urmatoare sa optimizam
raspandirea acestora pe harta, prin setarea target-urilor
in zone diferite ale hartii.


**Detalii despre abordarea algoritmica a etapei 3**

Pentru aceasta etapa, ne-am limitat la a imbunatati criteriul de prioritate a target-urilor la inserarea in coada de prioritate astfel: pentru a distanta target-urile si pentru a trimite mai putine nave in aceeasi zona, am folosit un factor de aplificare a raportului dintre numarul de halite de pe pozitia respectiva si distanta de la shipyard pana la acea pozitie.
