### Systeemvereisten ###

  * Java JRE/JDK 1.6.0\_10 of nieuwer
  * 256MB vrije werkgeheugen
  * 10 MB beschikbare schijfruimte
  * Zip hulpprogramma om het archief uit te pakken(7-zip, WinZip, Total Commander, enz.)
### Het uitvoeren van de toepassing ###
  * Onder Windows voert u gewoon het programma de diylc.exe uit.
  * Onder **Linux / Unix** of **Mac**, opent u de terminal, verander de map naar diylc3 (cd '<'pad naar diylc3'>') en tikt u **./run.sh** in.
### Gebruikersinterface ###

Het gebruikersinterface kan in 4 belangrijke onderdelen worden ondergedeeld:

  1. **Canvas** wordt gebruikt als WYSIWYG bewerker voor het project.
  1. **Gereedschapskist** geeft en lijst van alle beschikbare componenten en staat u toe om het component die u wilt gebruiken toe te voegen.
  1. **Hoofdmenu** biedt bestands bewerkingen, zoals het openen, opslaan, exporteren en klembord operaties, enz.
  1. **Statusbalk** toont context verwante informatie, zoomcontrole, updateinformatie en geheugengebruik.

### Configureren van de toepassing ###

De applicatie kan vanuit dit menu worden geconfigureerd. Het bevat de volgende opties:

  * **Anti-Aliasing** indien aangevinkt, zullen objecten en tekst er minder geblokt uitzien, maar het kost meer tijd om dit weer te geven. Als u last heeft van verminderde prestaties, kunt u deze optie uitschakelen.

  * **Automatisch Aanmaken van Soldeereilandjes** indien aangevinkt, de toepassing zal automatisch soldeereilandjes maken wanneer een component aan de tekening wordt  toegevoegd.

  * **Automatisch Bewerken Modus** Indien aangevinkt, verschijnt het component bewerkenings dialoogvenster nadat elk component is gemaakt, op dezelfde manier versie werkt 1.x.

  * **Doorgaan met Invoegen** wanneer aangevinkt, zal de laatst geselecteerde component-soort actief blijven nadat ze geplaatst werd, zodat u snel een groot aantal componenten van hetzelfde type kunt plaatsen.

  * **Raster Exporteren** Indien aangevinkt, zullen rasterlijnen naar een bestand of printer worden geëxporteerd.

  * **Hoge Kwaliteit Rendering** Indien aangevinkt, zal de beeldkwaliteit verbeteren, maar het kan de prestatie verlagen.

  * **Uitlijnen op Raster** Wanneer aangevinkt, gebruiken “slepen & neerzetten” operaties de Controlepunten op het raster als ankerpunt, in plaats vandat ze de muiscursor beeldelement voor beeldelement volgen.

  * **Kleverig Punten** indien aangevinkt, blijven componenten aan elkaar plakken wanneer ze verplaatst worden. Houdt u de Ctrl-toets ingedrukt terwijl u sleept om tijdelijk de Kleverig Punt-modus in of uit te schakelen. Zie "Controlepunten” voor meer info over de Controlepunten.

  * **Thema** laat u een thema selecteren. Thema's worden van de thema's map in de DIYLC map gelezen. Zij omvatten achtergrondkleur en de rasterlijn kleur. U kunt uw eigen thema's toevoegen in de vorm van een XML-bestand in de thema's map. Het is het gemakkelijkst om te beginnen met een kopie van één van de bestaande bestanden.

### Het toevoegen van componenten aan de tekening ###

In deze sectie wordt uitgelegd, hoe u een bestaande component aan het project kunt voegen. Om te leren hoe u uw eigen componenten mag ontwikkelen, volgt u deze handleiding. Om een component te creëren, gaat u als volgt te werk:

  * Zoek het component in de gereedschapskist. Componenten zijn in verschillende tabbladen onderverdeeld, dus zorg dat u het juiste tabblad gebruikt.

  * Klik op de knop dat het gewenste component toont. Merk op dat de tekst in de statusbalk verklaart wat nu te doen.

  * Klik op de gewenste locatie op het Canvas om het component te creëren. Sommige componenten (Soldeereilandjes, printbanen, enz.) zullen met een enkele klik worden gemaakt, anderen (zoals weerstanden, draadbruggen, enz.) vereisen twee klikken om beide eindpunten in te stellen. Instructies in de statusbalk leiden u door het proces. Het component zal doorzichtig worden, totdat het creatieproces is voltooid.

### Verplaatsen van componenten ###

De gemakkelijkste manier om een component te verplaatsen, is door klikken & slepen naar de gewenste locatie. Wanneer de muisaanwijzer zich boven een component bevindt, zal hij in een hand aanwijzer veranderen, en de statusbalk toont de namen van de componenten die zullen worden verplaatst.

Merk op dat:

  * Als er meer dan één component geselecteerd is,  worden ze allemaal verplaatst.

  * De beweging van één (of meer) componenten zal alle componenten die vast zitten samen bewegen/uitstrekken. De statusbalk informeert u welke onderdelen het betreft. Houdt u Ctrl ingedrukt om de geselecteerde componenten los te maken, en hen individueel te verplaatsen.

### Controlepunten ###

Een component kan één of meer Controlepunten hebben. Controlepunten hebben een tweeledig doel:

  * Het veranderen van de positie of het uiterlijk van een component, door het slepen van een controle punt over het Canvas. Bijvoorbeeld, het slepen van een eindpunt van een weerstand. Om een controlepunt te slepen moet het component worden geselecteerd.  Dit maakt het makkelijker twee componenten die overlappende Controlepunten hebben los te maken, en controle te hebben over de Controlepunten die moeten worden gesleept. Dit werkt ook wanneer meerdere componenten worden geselecteerd. Indien twee of meerdere componenten vast aan elkaar zitten en u hen allemaal selecteert, kunt u hun Controlepunten op het zelfde moment allemaal tegelijk slepen.

Merk op dat:

  * Statusbalk geeft alle componenten die door slepen & neerzetten worden beïnvloed weer.

  * U kunt alleen Controlepunten slepen als ze groen worden.

### Bewerken van component eigenschappen ###

  * Dubbelklikken op een component opent de component-bewerker – een dialoogvenster dat alle bewerkbare eigenschappen van de geselecteerde component(en) geeft.

  * Aanvinken van het "standaard" selectievakje aan de rechterkant, zal die waarde de standaard instelling voor die eigenschap maken. Met andere woorden, alle Componenten die daarna worden gemaakt, zullen die waarde erven.

  * Gegevensvelden die de uitkomst van een meting vragen (grootte, weerstand, capaciteit, enz.), accepteren constanten of wiskundige uitdrukkingen. Zo kunt u bijvoorbeeld 3/32 tikken, en het zal automatisch naar decimale worden geconverteerd. Zelfs meer ingewikkelde uitdrukkingen met haakjes en vier elementaire numerieke operatoren zijn mogelijk.

### Groeperen van componenten ###

Component groepen gedragen zich op een vergelijkbare manier als groepen in Corel Draw. Het idee is om twee of meer componenten bij elkaar te houden, en hen op hetzelfde moment te verplaatsen/bewerken/verwijderen. Om componenten te groeperen, selecteert u hen en drukt u op Ctrl+G (of selecteer “Geselecteerde Items Groeperen" uit het menu). Dubbelklikken op een van de gegroepeerde componenten opent de bewerker met alle wederzijdse eigenschappen van de geselecteerde componenten, En maakt het mogelijk ze allemaal op het zelfde moment te bewerken. Merk op dat, terwijl de componenten zijn gegroepeerd, u niet hun individuele controlepunten kunt slepen.
Om de geselecteerde groep op te opheffen, selecteert u ze, en drukt u op Ctrl+U (of selecteert u "Geselecteerde Groep Opheffen" uit het menu).

Merk op dat:

  * Geneste groepen worden momenteel niet ondersteund. Als u twee groepen van componenten samen groept, zult u een grote groep maken, in plaats van een groep die twee groepen bevat.

### Het gebruiken van de statusbalk ###

Statusbalk bevat drie verschillende functies:

  * **Informatiebalk** toont context gerelateerde informatie en begeleiding via  het toepassingsprogramma.

  * **Selectie Afmetingen** toont de afmetingen van de minimale begrenzende rechthoek die alle geselecteerde componenten bevat. Het maakt gebruik van de standaard instelling meeteenheid (in. of cm.)

  * **Zoomregeling** staat u toe om in- of uit- te zoomen.

  * **Automatisch Actualiseren** kennisgeving, wanneer de lamp ingeschakeld is, zijn er updates beschikbaar. Klik erop om meer informatie te krijgen.

  * **Geheugenbalk** is het oranje icoontje aan de rechterkant. Het toont het geheugen gebruik. Om meer informatie te verkrijgen, zet u de muisaanwijzer erop. Klik erop om zoveel mogelijk geheugen vrij te maken. De kleur is rood als het geheugen verbruik hoog is.