Rumei Ma 2690683
Dennis Grotz 2360065
Vincenz Mechler 2817217

Rollen der Gruppenmitglied:
	Vincenz:
		-erstellen der Grundstruktur 
		-Implementierung der structures-package
		-Implementierung der Netlist- und Routing-Parser
		-Implementierung consistencychecker und timinganalyzer
		-gelegentlich dokumentation
	Rumei und Dennis:
		-Placementparser
		-Architectureparser
		-DesignAnalyzer
		-ErrorReporter
		-Dokumentation vervollständigt


Compilen des Programms:
	1. Den Ordner src als Arbeitsverzeichnis in Command shell festlegen
	2. javac src\designAnalyzer\DesignAnalyzer.java
	3. java designAnalyzer.DesignAnalyzer + Pfade der Inputfiles(siehe unten) + Parameter (siehe unten)

Parameter beim Aufruf des Programms:
	nach java DesignAnalyzer müssen 3 oder 4 (je nach dem, ob routing file angegeben wird) volle Dateipfade in Linux-Notation (/ statt \) angegeben werden. Die Files sollen in folgender
	Reihenfolge angegeben werden: *.net *.arch *.p (und optional) *.r 
	Darauf folgend dürfen Parameterpaare gemäß der Bezeichnung in der Aufgabenstellung wie zum Beispiel "-W 3" genutzt werden. Die Reihenfolge untereinander ist irrelevant.
	
Fehlerausgabesemantik:
	nach dem ersten ErrorReport sind alle anderen Ausgaben des Programms unverbindlich, die Ausführung wird jedoch so weit wie möglich fortgesetzt. 
	
	

	
Commithistory aus git:




commit a67a6e8b6ec9ca2a6f053c86dd885b2996fa6e79
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 23:27:12 2018 +0100

    removed debug output

commit 85c04a4b5391593af1a259d2c11538f7f6ee0b4b
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:23:31 2018 +0100

    fixed channel printing...!

commit e8b655caea8ba48c67147e77db9b3be36e5f970e
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:13:18 2018 +0100

    fixed channel printing...

commit c6fd9f3d60d9280b1a241963bb9fdc3ed74011d0
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:10:56 2018 +0100

    fixed channel printing..

commit b6b9314e3cb94906f4cd75a64095610c19cfd928
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:07:41 2018 +0100

    fixed channel printing.

commit 18bbd7892c5b271b9cc0784a4962d044a6d93ffa
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:04:22 2018 +0100

    fixed channel printing

commit 70fd4ff5fc8d32e3a2d6437de2787d6b9a06972d
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 23:01:20 2018 +0100

    sunk in despair while trying to fix critical path output...

commit 5863d471a28f07257855294e376fbddb52a3c41e
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 22:04:50 2018 +0100

    fixing minor bugs

commit 88d8247c39265235b29893e34c7dec7188daa05d
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 22:03:18 2018 +0100

    fixing timing analysis

commit 1f10427b4d31310d12f526f429a08213525f6a0e
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 21:56:46 2018 +0100

    fixing timing analysis

commit 57f1b2d85d2b14ec68cc3ce1a4c05b9797104bad
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 21:51:38 2018 +0100

    fixing timing analysis

commit 3e249cf3e5b1e12574ecd6e0057639955165f3df
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 21:40:17 2018 +0100

    fixing timing analysis

commit c9f658d5b609258f99729055493b29d558ea56c4
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 21:08:57 2018 +0100

    fixed minor bugs

commit d5e3f08a761da1f6d31ad5164230f220de1d4ff8
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 21:04:11 2018 +0100

    fixing clock net error

commit 4265a6890c88543f9770eee71e7c02be1be50d64
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:59:08 2018 +0100

    fixing clock net error

commit 270b18cedfd07c5ebd657f824f5e9e7e5256bf5a
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:50:23 2018 +0100

    fixing clock net error

commit 8073c4c5010556c68e48f2805f9b9bf8cbdde42f
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:48:42 2018 +0100

    fixing clock net error

commit b2e17e56e242a6b9f776f14a982a5bb5a89d3638
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:42:54 2018 +0100

    fixing clock net error

commit 0c985b484bbb05657a02863a32abd7c725a4e24f
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:30:06 2018 +0100

    fixing an false invalid pin error

commit e7184ffc145dda814749ab3044caaac2d5cf37ab
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:19:38 2018 +0100

    fixing an false invalid pin error

commit 33be29a1af3059b338a7922b6a5582d9df9ce6ef
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:11:26 2018 +0100

    still fixing the clocknet parsing error

commit 8ce6ffde756d052c34dc207142acd3daa8ed0d74
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:07:33 2018 +0100

    still fixing the clocknet parsing error

commit d7c69cee24ac6e1ffae3b75f327e236c6464683a
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 20:04:05 2018 +0100

    added documentation
    
    fixed routing parser trying to route clock nets

commit ff5ab6c1fcb4de6134be8f3de20b9f28b14b521f
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 19:21:09 2018 +0100

    activated consistency checker

commit ab31029a88dbce2f5dfb2725f39599d904759dc3
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 19:16:09 2018 +0100

    finished readme

commit 750144310f0ead764dda99f04ebd34a9bb802c2f
Merge: 1f828c8 b5c23c1
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 18:27:15 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE

commit b5c23c1199b015e47f85da1347a05a1d7e1ea7ec
Merge: 80c0c56 27c4905
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 18:26:35 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE
    merge...

commit 80c0c56fb9025712ae4d692d7c6834090e4f3761
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 18:26:16 2018 +0100

    implemented consistency checker and edited Net to be able to keep track of routed sinks

commit 1f828c8e768e0b8350c57ce6c6eb8fb41443ddc4
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 18:26:15 2018 +0100

    created new file README.txt

commit 27c4905d869be84cd25c6a7bf78261f92b0572ac
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 17:08:04 2018 +0100

    author in every file

commit 0c109192fda006227e843d6b273e08e0ed6d6e0c
Merge: 1fc10a6 a89fe5f
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 16:29:21 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE
    merge...

commit a89fe5f71c0e196d59fbd0ffb565acaf1a58fed9
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 16:29:02 2018 +0100

    comments and | in output

commit 1fc10a6f963404c540e02a102a37ed02504a9a00
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 16:28:15 2018 +0100

    fixed critical path output to start t origin even if combinatorial logic blocks are involved

commit 09bd6dc68f76a66deb085ab372ca164e2511895d
Merge: 86febee 3ccf6f8
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 16:00:07 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE
    merge

commit 86febee1469daefbc35f28f5176b7d2ca039bba2
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:59:59 2018 +0100

    fixed error that did not recognize end of file and tried to parse on

commit 3ccf6f8ba87363c39d8964f3c6c2b9715d50b078
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:58:40 2018 +0100

    small changes to routingparser

commit bdf4acd5fb3a5163e3416c6ab2bb8aadc616d0f6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:55:22 2018 +0100

    ErrorReporter typo

commit 4c414888d7e72e3c2871c322324782ce063fbaed
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:47:31 2018 +0100

    typo in errorreport

commit f06a43981d44e48706bae4e02cd64fab3e38dbe4
Merge: 3a5b333 b1d5195
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:40:30 2018 +0100

    tidied up failed merge

commit 3a5b333086517882bb8134f7b9aaf214a4fa90b8
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:36:59 2018 +0100

    implemented parsing of pins

commit b1d5195b845a0cd63e3f89de60dfbf2e747cf45c
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:35:34 2018 +0100

    isneighbour finished

commit 47293dfe92bd67a690b22c6816858945c0b181f5
Merge: 855a030 cdf8216
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:07:44 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE
    merge

commit cdf82160985a45ccfb62d3eeb06ec68a87ffa12a
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:07:14 2018 +0100

    ipin isneighbour

commit 855a0309ce4dc6d201329111ad2f245eaedbd0f0
Merge: d50df4c a9da7a6
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:06:41 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE
    merged

commit d50df4c22f2f7a68295c22cb8b75a0cc47ba9381
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:06:24 2018 +0100

    nothing much

commit a9da7a691b93ba77355100fa7f33c6e0733ed7f0
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 15:05:35 2018 +0100

    ipin isneighbour

commit 7ebd3937d34790531fc6d08ca5d4e63c31a040f3
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 15:03:38 2018 +0100

    Revert "ss"
    revert bad commits
    
    This reverts commit a1b29bd5e3e78bdb56624d31deca82eac90edb06.

commit a1b29bd5e3e78bdb56624d31deca82eac90edb06
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 14:47:20 2018 +0100

    ss

commit 47af51aace449c0c155baeb952c779833b26829f
Merge: a6842f9 46f0f39
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 14:46:19 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE

commit a6842f94cfe90518f92d59dd1227379a133c9db6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Nov 28 14:44:13 2018 +0100

    sss

commit 46f0f3907d6ac871cc2a2ad0cb7d709aae002b2e
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 14:43:44 2018 +0100

    added explicit data structures for IPin and OPin and implemented most inherited methods, edited annotation methods of blocks and channels to match Pins

commit eb7eff4c8680d5c7a7e0c171c2579a6cf3612529
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Nov 28 13:38:07 2018 +0100

    fixed many minor errors
    
    implemented missing methods in blocks and channels

commit cce59b6e16a4064e760a81806d403820826bf886
Merge: 4acab51 ce8ef96
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Nov 27 22:01:23 2018 +0100

    finished ErrorReporter vorerst

commit 4acab51093da53ef0ad9605036ac788637a8ee8b
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Nov 27 21:59:02 2018 +0100

    errrorReport finished, vorerst

commit ce8ef968ea20cab52ac0b16df672b6972787d9b0
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 20:34:46 2018 +0100

    whatever

commit 902436eb449d5d63a786fc9bf0bb554b6eb6a788
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 20:23:15 2018 +0100

    edited gitignore to ignore data folder

commit 787025d233c0f0eb668130b922ff51dc6ae959c9
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 20:18:42 2018 +0100

    removed two unused imports

commit f5472fd5fb8352b9f736c39ff1433b3cf167c025
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 20:14:00 2018 +0100

    implemented printing of critical path
    
    implemented isNeighbour(...) function in all subclasses of PathElement
    
    general clean up

commit 2628da7edc477e1938a1bf931a56245979a53fb9
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 17:47:01 2018 +0100

    finished RoutingParser
    
    updated access to constants of ParameterManager
    
    added and edited ErrorReporter functions

commit 9f080fb4fd6edbe87401690c6c88dd8dbac63914
Merge: 4bcfb3c 7f32040
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Nov 27 16:52:39 2018 +0100

    ...Merge branch 'master' of https://github.com/011000101101/ACE

commit 4bcfb3c97b3db72935d980563afd054162fe4e09
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Nov 27 16:50:17 2018 +0100

    finished architecture parser and command line parsing

commit 7f32040ff8b208879d9173bdee7e307592a41a89
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 14:06:33 2018 +0100

    minor changes in RoutingParser

commit 500bedd3891efe84ff8a2e812d812e10768cfb56
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 13:41:57 2018 +0100

    deaktivated unused methods in PathElement and subclasses

commit 33172cb2a1def89d59384e73f3f2589b260b44e2
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Nov 27 13:24:26 2018 +0100

    merged routingParser.java

commit 9f947d976913aaaef3d755cd4229913b657e3db7
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 01:28:02 2018 +0100

    corrected faulty path structure: moved DesignAnalyzer.java from package timingAnalyzer to package designAnalyzer.timingAnalyzer

commit 229790af47393ef4225945c73b12746af16ecf71
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Tue Nov 27 01:19:48 2018 +0100

    implemented timing analysis and slack annotation

commit 3e296f24771fabe0bbadbf7345bbbb63096ceac4
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Mon Nov 26 20:06:56 2018 +0100

    corrected errors in timing estimation

commit 89665d8ebc43173160d148e9168ac6d788de43dd
Merge: c9f00f9 2dee970
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Mon Nov 26 19:51:11 2018 +0100

    Merge branch 'master' of https://github.com/011000101101/ACE

commit c9f00f9431feeb4fc6f190b85defc292d699a002
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Mon Nov 26 19:50:34 2018 +0100

    Revert "finished implementation of timing estimation"
    
    This reverts commit 09b4382d7feccaa8a94bd058de0534e684533234.

commit 09b4382d7feccaa8a94bd058de0534e684533234
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Mon Nov 26 19:42:20 2018 +0100

    finished implementation of timing estimation

commit 2dee970e073ba6240d961e97a31e3a410f4c7b47
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Mon Nov 26 19:42:20 2018 +0100

    finished implementation of timing estimation

commit 200d2f3667cf78553e8f6c68bf0b593001b665ae
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Fri Nov 23 15:06:36 2018 +0100

    to Rumei: todo finish cases 3, 5, 7

commit 78f28645ce784f9509a26b67a9dee8eef51132a5
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Fri Nov 23 14:51:58 2018 +0100

    to Rumei: finish 3,5,7 in timinganalyzer

commit 4dae2f2b518fee9bc5f8c6cf0f67264c0837cef9
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Fri Nov 23 12:52:05 2018 +0100

    created package and class TimingAnalyzer and started implementation of timing estimization

commit 031865d64f887cd210a61ea6dd040ec3e194b7e5
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Thu Nov 22 20:14:12 2018 +0100

    worked on RoutingParser

commit 2bbaf2d94418ccdcb03d8b48c86932780ac4a0d4
Merge: 971120c fabbe04
Author: Vincenz Mechler <vm71xepe@clientmaster.rbg.informatik.tu-darmstadt.de>
Date:   Thu Nov 22 18:00:14 2018 +0100

    merged

commit 971120ce2929687f4b5f7a57620a383bfbfbb2dd
Author: Vincenz Mechler <vm71xepe@clientmaster.rbg.informatik.tu-darmstadt.de>
Date:   Thu Nov 22 17:57:24 2018 +0100

    started work at RoutingParser

commit fabbe04637227bdcf545993e92c2ed8e86634ccf
Author: Rumei <rumei.ma@gmail.com>
Date:   Thu Nov 22 17:44:09 2018 +0100

    implemented placement parser and with that several small changes to rest

commit c4abafb04d3f0912c8388367677436148823e8fe
Author: Vincenz Mechler <vm71xepe@clientmaster.rbg.informatik.tu-darmstadt.de>
Date:   Thu Nov 22 16:05:26 2018 +0100

    added parametr manager to abstract input parser

commit d25f8b39f5137ab510be2c8287181abc298d623d
Author: Vincenz Mechler <vm71xepe@clientmaster.rbg.informatik.tu-darmstadt.de>
Date:   Thu Nov 22 15:15:07 2018 +0100

    implemented path structure and critical path computation

commit f042b5c5377a9e36459e368a847c65ea6d6a5c59
Author: Vincenz Mechler <vm71xepe@clientmaster.rbg.informatik.tu-darmstadt.de>
Date:   Thu Nov 22 12:39:03 2018 +0100

    edited structures package ans subpackages, removed old pathElement stubs and added channels package and classes

commit 226599170eed5f80de3b79c0a7c02ee3e282328a
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Thu Nov 22 03:17:22 2018 +0100

    added ParameterManager
    
    added method for block placement in Structure manager and moved some code from ConsistencyChecker
    
    added method for block placement in NetlistBlock

commit 1ce5670c70d5ee8c9026d67d0f1725a6d6f93684
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 23:45:13 2018 +0100

    added parseHead() function in AbstractInputParser

commit 880b3fee11fab8693a70aed418f062b87d276f01
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 22:16:08 2018 +0100

    added ConsistencyChecker and implemented placement checking

commit 433b398c43f077bc72ca1206c554dbcd41201aaa
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 21:44:46 2018 +0100

    filled main class \(DesignAnalyzer\) with basic control flow
    
    added block management to StructureManeger
    
    added method stubs in ErrorReporter

commit bdca60196c78c1ad12301e2a4b433f28e952b4ee
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 21:07:37 2018 +0100

    moved parseNetlistFile() method of NetlistParser to AbstractInputParser and renamed to parseAll, defined abstract method parseOneBlock in AbstractInputParser

commit 0a5ef04bd90c7d46615393139d3baae8417b3736
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 20:58:07 2018 +0100

    updated package structure
    
    introduced StructureManager and moved exesting code there
    
    modified AbstractInputParser and completed NetlistParser
    
    added some method stubs to ErrorReporter

commit 29879bad460e6943197412f2544298589d824c7d
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Wed Nov 21 14:21:38 2018 +0100

    implemented NetlistParser (nearly finished)
    
    added ErrorReporter and implemented reportSyntaxError(...) and reportSyntaxMultipleChoiceError(...)
    
    modified and augmented data structures

commit f20a86d91b13da3b3d9e5759555a359f8e9460eb
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 17:52:01 2018 +0100

    added datastructure and parser class structures
    
    started implementation of AbstractInputParser
    
    filled datastructure classes with attributes (partly)

commit a83673e96eb1d0659af681682134cfa85ec06de9
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 16:21:12 2018 +0100

    renamed package

commit a2f61d4089ae7d8f995c53f4ce964f8404020580
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 16:07:04 2018 +0100

    modified gitignore

commit e02399a540170844b25f38fc5f101065f90545bd
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 16:04:20 2018 +0100

    added input parser structure

commit a6360a00e3480ae1dd2f5537f675307cfe7fed2b
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 15:53:38 2018 +0100

    created main class

commit b011a853c5a583ce3610dc22e9542e6ecf3964b4
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Nov 20 15:27:03 2018 +0100

    initial commit --project creation
